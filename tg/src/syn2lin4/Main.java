package syn2lin4;

import is2.io.*;
import is2.util.*;
import is2.data.*;
import is2.data.Edges;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import data.DataNN;
import data.Long2Int;


import rt.algorithm.GraphConverter;
import rt.model.Environment;
import rt.model.Graph;
import rt.model.IGraph;



public class Main {

	public static int THREADS =4;

	Long2IntInterface long2int;
	//	Decoder decoder;
	//	ParametersFloat params;
	//	Pipe pipe;
	OptionsSuper options;



	public static void main (String[] args) throws Exception
	{


		Runtime runtime = Runtime.getRuntime();

		THREADS = runtime.availableProcessors();


		long start = System.currentTimeMillis();
		OptionsSuper options = new Options(args);

		if (options.cores<THREADS&&options.cores>0) THREADS =options.cores;

		System.out.println("Found " + runtime.availableProcessors()+" cores use "+THREADS);


		if (options.train) {
			Long2IntInterface long2int = new Long2Int(options.hsize);
			DB.println("li size "+long2int.size());

			Pipe pipe =  new Pipe (options);
			Instances is = new Instances();

			Extractor.initFeatures(pipe.mf);
			pipe.extractor = new Extractor[THREADS];
			for (int t=0;t<THREADS;t++)  pipe.extractor[t]=new Extractor(pipe.mf,long2int);

			pipe.createInstances(options.trainfile,options.trainforest,options.formatTask,is);

			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));
			zos.putNextEntry(new ZipEntry("data")); 
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

			pipe.mf.writeData(dos);

			ParametersFloat params = new ParametersFloat(long2int.size());

			train(options, pipe,params,is);

			//pipe.mf.clearData();

			DB.println("Data cleared ");

			params.write(dos);

			pipe.edges.write(dos);


			dos.writeBoolean(options.decodeProjective);

			//	pipe.extractor.write(dos);
			pipe.cl.write(dos);
			dos.writeUTF(""+Main.class.toString());
			dos.flush();
			dos.close();
			DB.println("Writting data finished ");

		}

		if (options.test) {

			Pipe pipe = new Pipe(options);
			ParametersFloat params = new ParametersFloat(0);  // total should be zero and the parameters are later read

			// load the model

			Decoder decoder = readAll(options, pipe, params);


			test(options,pipe, decoder, params);
			//			DB.println("misses "+LongIntHash.misses+" good "+LongIntHash.good);

		}

		System.out.println();

		if (options.eval) {
			System.out.println("\nEVALUATION PERFORMANCE:");
			Evaluator.evaluate(options.goldfile, options.outfile);
		}

		long end = System.currentTimeMillis();
		System.out.println("used time "+((float)((end-start)/100)/10));
	}


	public  static Decoder readAll(OptionsSuper options, Pipe pipe,ParametersFloat params) throws IOException {


		DB.println("Reading data started");

		// prepare zipped reader
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
		zis.getNextEntry();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

		pipe.mf.read(dis);

		params.read(dis);
		Long2IntInterface long2int = new Long2Int(params.size());
		DB.println("parsing -- li size "+long2int.size());


		pipe.extractor = new Extractor[THREADS];

		for (int t=0;t<THREADS;t++) pipe.extractor[t]=new Extractor(pipe.mf,long2int);

		Extractor.initFeatures(pipe.mf);
		Extractor.init(pipe.mf);
		for (int t=0;t<THREADS;t++) {
			pipe.extractor[t].init();
		}


		Edges.read(dis);

		options.decodeProjective = dis.readBoolean();

		pipe.cl =new Cluster(dis);

		dis.close();

		DB.println("Reading data finnished");

		Pipe.types = new String[pipe.mf.getFeatureCounter().get(Pipe.REL)];
		for(Entry<String,Integer> e : pipe.mf.getFeatureSet().get(Pipe.REL).entrySet())  Pipe.types[e.getValue()] = e.getKey();



		Decoder decoder =  new Decoder();
		//	Decoder.NON_PROJECTIVITY_THRESHOLD =(float)options.decodeTH;

		Extractor.init(pipe.mf);

		return decoder;
	}


	static private int[] getChildren( Instances is, int n,int h) {

		int cnt=0;

		// count siblings
		for(int i=0;i<is.length(n);i++) if (is.pheads[n][i]==h) cnt++;
		int[] children = new int[cnt];
		cnt=0;
		for(int i=0;i<is.length(n);i++) 
			if (is.pheads[n][i]==h) children[cnt++]=i;



		return children;
	}

	/**
	 * Do the training
	 * @param instanceLengths
	 * @param options
	 * @param pipe
	 * @param params
	 * @param is 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	static public void train(OptionsSuper options, Pipe pipe, ParametersFloat params, Instances is) 
	throws IOException, InterruptedException, ClassNotFoundException {

		System.out.println("\nTraining Information ");
		System.out.println("-------------------- ");

		int numInstances = is.size();


		int maxLenInstances =0;
		for(int i=0;i<numInstances;i++) if (maxLenInstances<is.length(i)) maxLenInstances=is.length(i);


		DataNN dataNN = new DataNN(maxLenInstances);


		int iter = 0;
		int del=0; 
		float error =0;
		float f1=0;

		FV pred = new FV();
		FV act = new FV();

		double	upd =  (double)(numInstances*options.numIters)+1;


		String px[] =pipe.mf.reverse(pipe.mf.getFeatureSet().get(Pipe.POS));	
		String wds[] =pipe.mf.reverse(pipe.mf.getFeatureSet().get(Pipe.WORD));	
		String rel[] =pipe.mf.reverse(pipe.mf.getFeatureSet().get(Pipe.REL));	


		for(int i=0;i<is.size();i++) {

			is.predicat[i] = new int[is.length(i)];

			for(int h=0;h<is.length(i);h++) {

				int children[] =getChildren(is,i,h);


				// collect equal children 

				HashMap<String,ArrayList<Integer>>  equalChildren = new HashMap<String,ArrayList<Integer>>();

				for(int c : children) {
					String signature = wds[is.glemmas[i][c]]+" "+px[is.gpos[i][c]]+" "+rel[is.labels[i][c]]+"\t"+is.gfeats[i][c]+"\t"+is.predicat[i][c];
					ArrayList<Integer> cldsWithSignature =equalChildren.get(signature);
					if (cldsWithSignature==null)equalChildren.put(signature, (cldsWithSignature = new ArrayList<Integer>() ));

					cldsWithSignature.add(c);
				}

				for(Entry<String,ArrayList<Integer>>  e :equalChildren.entrySet()) {
					//				if (e.getValue().size()>1) System.out.println("sig "+e.getKey()+"\t\t"+e.getValue());

					int cnt=0;
					for(int j : e.getValue()) {
						is.predicat[i][j] =cnt++;
					}
				}

			}


		}




		for(; iter < options.numIters; iter++) {

			System.out.print("Iteration "+iter+": ");

			long start = System.currentTimeMillis();

			long last= System.currentTimeMillis();
			error=0;
			f1=0;
			// upd=0;
			for(int n = 0; n < numInstances; n++) {

				upd--;

				if (is.labels[n].length>options.maxLen) continue;

				String info = " td "+(((float)Decoder.timeDecotder)/1000000F)+" tr "+(((float)Decoder.timeRearrange)/1000000F)
				+" te "+(((float)Pipe.timeExtract)/1000000F);

				if((n+1) %500 == 0) 
					del= pipe.outValueErr(n+1,Math.round(error*1000)/1000,f1/n,del, last, upd,info);






				Linear l = new Linear(is, n);

				//				boolean found=false;
				int root=-1;
				for(int k=0;k<is.length(n);k++) {

					if (is.heads[n][k]==-1) {

						//						found =true;
						root=k;
						//	break;
					}	
				}


				// check for non-projective trees

				for(int j=0;j<l.wordChilds.length;j++){
					Arrays.sort(l.wordChilds[j]);
				}


				ArrayList<Integer> al = getLeftC(l,root,new BitSet(),1);
				int all[] = new int[l.wordChilds.length];
				int pos=0;
				for(int k=0;k<al.size();k++) {
					Integer p = al.get(k);
					all[pos]=p;
					pos++;
				}

				boolean good =true;
				for(int j=0;j<all.length-1;j++) {

					if(all[j]>all[j+1]) {
						//					System.out.print(" x: "+all[j]+" "+all[j+1]);
						good=false;
						break;
					}
				}
				if(!good)  continue;



				l = new Linear(is, n);

				// for each package get the contained nodes

				for(int k=0;k<l.wordChilds.length;k++) {

					int[] nds = l.wordChilds[k];

					dataNN = pipe.fillVector((F2SF)params.getFV(),  is, n, dataNN, nds);

					int order[][] = Decoder.sortNodes(nds,k, dataNN,pipe.extractor[0],is,n,params.getFV()); // null == data

					double e = pipe.errors1(order[0]);

					if (e<=0) continue;			

					error += e;

					pred.clear();
					pipe.createVector1(is,n,order[0],k,pred);

					Arrays.sort(order[0]);


					act.clear();
					pipe.createVector1(is,n,order[0],k, act);

					params.update(act, pred, is, n, upd,e);					
				}
			}

			String info = " td "+(((float)Decoder.timeDecotder)/1000000F)+" tr "+(((float)Decoder.timeRearrange)/1000000F)
			+" te "+(((float)Pipe.timeExtract)/1000000F);
			pipe.outValueErr(numInstances,Math.round(error*1000)/1000,f1/numInstances,del,last, upd,info);
			del=0;
			//System.out.println();
			//DB.println("Decoder time decode "+(((float)Decoder.timeDecotder)/1000000F)+" rearrange "+(((float)Decoder.timeRearrange)/1000000F)
			//		+" extract "+(((float)Pipe.timeExtract)/1000000F));

			Decoder.timeDecotder=0;Decoder.timeRearrange=0; Pipe.timeExtract=0;
			long end = System.currentTimeMillis();

			System.out.println(" time:"+(end-start));			

		}
		params.average(iter*is.size());

		//System.exit(0);
	}                                   


	static int changed =0;



	static HashMap<String,Integer> map = new HashMap<String,Integer>();

	/**
	 * Do the parsing
	 * @param options
	 * @param pipe
	 * @param decoder
	 * @param params
	 * @throws IOException
	 */
	static public void test (OptionsSuper options, Pipe pipe, Decoder decoder, ParametersFloat params) 
	throws Exception {

		long start = System.currentTimeMillis();

		CONLLReader09 depReader = null;
		HFGShallowReader shallowReader = null;

		CONLLWriter09 	 depWriter = new CONLLWriter09(options.outfile, options.formatTask);

		if (options.formatTask==11) {

			DB.println("Using HFG format ");
			shallowReader = new HFGShallowReader(options.testfile); 

		} else {

			DB.println("Using CoNLL format ");
			depReader = new CONLLReader09(options.testfile, options.formatTask);

		}




		Extractor.initFeatures(pipe.mf);

		String px[] =pipe.mf.reverse(pipe.mf.getFeatureSet().get(Pipe.POS));	
		String wds[] =pipe.mf.reverse(pipe.mf.getFeatureSet().get(Pipe.WORD));	
		String rel[] =pipe.mf.reverse(pipe.mf.getFeatureSet().get(Pipe.REL));	




		int cnt = 0;
		int del=0;
		long last = System.currentTimeMillis();

		System.out.println("\nInformation\n------------------- ");

		System.out.print("Processing Sentence: ");

		DataNN dataNN = new DataNN(100);



		float oracle =0;


		while(true) {

			Instances is = new Instances();
			is.init(1, pipe.mf,options.formatTask);


			SentenceData09 instance=null;
			if (depReader!=null) {
				instance	= depReader.getNext(is);
				if (instance==null) break;
			} else {
				HFG hfg = shallowReader.getNext(null);

				if (hfg==null) break;

				instance = new SentenceData09();
				String[] forms = new String[hfg.lemma.length];
				instance.init(forms);
				instance.lemmas = new String[instance.length()];
				instance.plabels = new String[instance.length()];
				instance.pheads = new int[instance.length()];
				instance.pfeats=new String[instance.length()];
				instance.ofeats=new String[instance.length()];
				for(int j=0;j< forms.length;j++) {
					instance.forms[j]="_";

					instance.gpos[j]=hfg.pos[j];
					instance.ppos[j]=hfg.pos[j];
					instance.lemmas[j]=hfg.lemma[j];
					instance.plabels[j] =hfg.label[j];
					instance.pheads[j] = hfg.head[j];
					instance.labels[j] =hfg.label[j];
					instance.heads[j] = hfg.head[j];
					instance.plemmas[j] =hfg.lemma[j];

					//		System.out.println(j+"\t"+instance.lemmas[j]+"\t"+instance.heads[j]);


					if (hfg.feats[j]!=null)
						for(String f :hfg.feats[j]) {
							if (f!=null)instance.pfeats[j]=instance.pfeats[j]==null?f:instance.pfeats[j]+"|"+f;
							if (f!=null)instance.ofeats[j]=instance.ofeats[j]==null?f:instance.ofeats[j]+"|"+f;

						}

				}
				//				DB.println("head "+instance.heads[instance.forms.length-1]);

				new CONLLReader09().insert(is, instance);
			}


			// make indistinguishable words distinguishable

			for(int i=0;i<is.size();i++) {

				is.predicat[i] = new int[is.length(i)];

				for(int h=0;h<is.length(i);h++) {

					int children[] =getChildren(is,i,h);


					// collect equal children 

					HashMap<String,ArrayList<Integer>>  equalChildren = new HashMap<String,ArrayList<Integer>>();

					for(int c : children) {
						String signature = instance.lemmas[c]+" "+instance.gpos[c]+
						" "+instance.labels[c]+
						"\t"+instance.feats[c]+
						"\t"+is.predicat[i][c];
						ArrayList<Integer> cldsWithSignature =equalChildren.get(signature);
						if (cldsWithSignature==null)equalChildren.put(signature, (cldsWithSignature = new ArrayList<Integer>() ));

						cldsWithSignature.add(c);
					}

					for(Entry<String,ArrayList<Integer>>  e :equalChildren.entrySet()) {

						int cntD=0;
						for(int j : e.getValue()) {
							is.predicat[i][j] =cntD++;
						}
					}

				}


			}




			cnt++;

			Linear l = new Linear(is, 0); 


			for(int k=0;k<l.wordChilds.length;k++) {

				int[] nds = l.wordChilds[k];
				dataNN = pipe.fillVector((F2SF)params.getFV(),  is, 0, dataNN,nds);

				l.results[k] = Decoder.sortNodes2(nds,k, dataNN,pipe.extractor[0],is,0,params.getFV()); // null == data

			}			

			// for each package get the contained nodes

			String[] forms = instance.forms;
			// get root
			int root=-1;
			boolean found =false;
			

			for(int k=0;k<is.length(0);k++) {

				if (is.heads[0][k]==0) {
					if(found ==true){
						//						System.out.println("root already found "+k);
						//						break;
						//			queue.add(0);
					}
					found =true;
					root=k;
					//	break;
				}	
			}

			/// the organization of the results is 
			/*

			 w1 order1 order2
			 w2 order1 order2
			 w3 ...

			 The "orders" are orderings of the dependent of head w. 

			 */





			BitSet added = new BitSet();			
			//	ArrayList<Integer> al = getLeft1(l,root,added,cnt);

			//	printbest(l,cnt);


			List<Order> orders = getLeftN(l,root,50,8);
			
		
			for(int o=0;o<orders.size();o++) {
				Order x = orders.get(o);
				if (x.isGold()) 				{
					oracle++;;

					break;
				}

			}
			
			if(orders.size()>3) 			System.out.println(""+orders.subList(0, 3));
			else System.out.print(""+orders);
			System.out.println(" "+((float)oracle)/cnt);
			//getLeftN(l,root,added,cnt);

			ArrayList<Integer> al = getLeft2(l,root,added,cnt);

			int all[] = new int[l.wordChildsResult.length+1];
			int pos=0;
			for(int k=0;k<al.size();k++) {

				Integer p = al.get(k);

				all[pos]=p;
				pos++;
			}


			//		if (cnt==7) System.exit(0);

			String[] formsNoRoot = new String[forms.length-1];
			String[] posNoRoot = new String[formsNoRoot.length];
			String[] lemmas = new String[formsNoRoot.length];

			String[] org_lemmas = new String[formsNoRoot.length];

			String[] of = new String[formsNoRoot.length];
			String[] pf = new String[formsNoRoot.length];

			String[] pposs = new String[formsNoRoot.length];
			String[] labels = new String[formsNoRoot.length];
			String[] fillp = new String[formsNoRoot.length];

			int[] heads = new int[formsNoRoot.length];


			HashMap<Integer,Integer> jx = new HashMap<Integer,Integer>();
			for(int j = 1; j < formsNoRoot.length; j++) {

				jx.put(all[j], j);

			}

			for(int j = 0; j < formsNoRoot.length; j++) {

				formsNoRoot[j] = forms[all[j]];
				posNoRoot[j] = instance.gpos[all[j]];
				pposs[j] = instance.ppos[all[j]];

				labels[j] =  instance.labels[all[j]];

				if (jx.get(instance.heads[all[j]])==null) ;// DB.println("error "+j);
				else heads[j] =jx.get(instance.heads[all[j]])+1; //instance.heads[


				lemmas[j] = instance.lemmas[all[j]];

				if (instance.lemmas!=null) org_lemmas[j] = instance.lemmas[all[j]];
				if (instance.ofeats!=null)  {
					of[j] = instance.ofeats[all[j]];
					int first= of[j].indexOf("|");
					if (of[j]!=null) {
						if (first >0 && of[j].contains("id=")) {
							of[j]=of[j].substring(first+1);
						} else if (of[j].contains("id=")) of[j]="_";
					}
				}
				if (instance.pfeats!=null)	{

					pf[j] = instance.pfeats[all[j]];
					if (pf[j]!=null) {
						int first= pf[j].indexOf("|"); 
						if (first >0 && pf[j].contains("id=")) {
							pf[j]=pf[j].substring(first+1);
						} else if (pf[j].contains("id=")) pf[j]="_"; 
					}
				}

			}
			//	DB.println("head "+instance.heads[instance.forms.length-1]+"  "+all[instance.forms.length-1]);
			SentenceData09 i09 = new SentenceData09(formsNoRoot, lemmas, org_lemmas,posNoRoot, pposs, labels, heads,fillp,of, pf);
			i09.pheads=heads;
			/*			
			i09.sem = instance.sem;
			i09.semposition = instance.semposition;

			if (instance.semposition!=null)
				for (int k= 0;k< instance.semposition.length;k++) {
					i09.semposition[k]=instance.semposition[k]-1;
				}


			i09.arg = instance.arg;


			i09.argposition = instance.argposition;

			if (i09.argposition!=null)
				for (int p= 0;p< instance.argposition.length;p++) {
					if (i09.argposition[p]!=null)
						for(int a=0;a<instance.argposition[p].length;a++)
							i09.argposition[p][a]=instance.argposition[p][a]-1;
				}

			 */
			depWriter.write(i09);
			//	depWriter.finishWriting();
			//	System.exit(0);

			del=pipe.outValue(cnt, del,last);

		}
		//pipe.close();
		depWriter.finishWriting();
		long end = System.currentTimeMillis();
		//		DB.println("errors "+error);
		System.out.println("Used time " + (end-start));
		System.out.println("forms count "+Instances.m_count+" unkown "+Instances.m_unkown+" oracle "+(oracle/(float)cnt));

	}





	/**
	 * @param l
	 * @param cnt
	 */
	private static void printbest(Linear l, int cnt) {

		System.out.println();

		for(int k=0;k<l.results.length;k++) {

			System.out.print(k+"\t");

			for(int p=0;p<l.results[k].length&&p<16;p++) {
				System.out.print(l.results[k][p].prob+"\t");
			}
			System.out.println();

		}
		int n=0;


		/*	
		ArrayList<Integer> all =new ArrayList<Integer>();
		//System.out.print("r "+root+" ");
		if(added.get(root) ) {
			System.out.println(" added "+root) ;

			return all;
		}
		for(int k=0;k<l.results[root][n].path.length;k++) {


			if(added.get(l.results[root][n].path[k]) ) {
				System.out.println("xx already added "+k);
				continue;
			}

			if(l.results[root][n].path[k]==root) {
				all.add(root);
				added.set(root);

			} else {
				all.addAll(getLeft2(l,l.results[root][n].path[k],added,cnt));				
			}



		}
		 */
	}


	final private static List<Order> getLeftN(Linear l, int root, int cnt,int pm ) {


		List<Order> allOrders =new ArrayList<Order>();

		for(int p =0;p<(pm>=1?pm:1) && p<l.results[root].length;p++) { 

			List<Order> orders =new ArrayList<Order>();
			
			Order o = new Order();
			orders.add(o);

			for(int k=0;k<l.results[root][p].path.length;k++) {

				if(l.results[root][p].path[k]==root) {
					
					for(Order oo : orders)  oo.nodes.append((char)root);

				} else {

					
					if (l.results[root][p].path.length<=1) {

						for(Order o1 : orders) o1.nodes.append((char)l.results[root][p].path[k]);

					} else {

						List<Order> ordersR = getLeftN(l, l.results[root][p].path[k], cnt,pm-1);
						
					
						/*
						Order gold = null;
						for(Order o1 : orders) {
							if(o1.isPartGold()) {
								gold =o1;
							
								break;
							}
						}	

						if (gold!=null) {
						
							orders.clear();
							orders.add(gold);
						}
						gold=null;
						for(Order o1 : ordersR) {
							if(o1.isPartGold()) {
								gold =o1;
							
								break;
							}
						}	

						if (gold!=null) {
						
							ordersR.clear();
							ordersR.add(gold);
						}
						
						*/
						
						
						
						Order[] olist = new Order[(ordersR.size()>(cnt)?(cnt):ordersR.size())*(orders.size()>(cnt)?(cnt):orders.size())];
						int i=0;
						for(Order o1 : orders) {
							if (i>=olist.length) break;
						
							for(int m=0;m<cnt&&m<ordersR.size();m++){
								
								olist[i++]  = o1.add(ordersR.get(m),l.results[root][p].prob);
							}
						}

						Arrays.sort(olist);
						orders.clear();
						
						for(int j=0;j<cnt&&j<olist.length;j++)  orders.add(olist[j]) ;
					}
				}
			}
			allOrders.addAll(orders);
		}
		Collections.sort(allOrders);

		return allOrders;
	}


	private static ArrayList<Integer> getLeft2(Linear l, int root, BitSet added, int cnt) {

		int n=0;



		ArrayList<Integer> all =new ArrayList<Integer>();
		//System.out.print("r "+root+" ");
		if(added.get(root) ) {
			System.out.println(" added "+root) ;

			return all;
		}
		for(int k=0;k<l.results[root][n].path.length;k++) {


			if(added.get(l.results[root][n].path[k]) ) {
				System.out.println("xx already added "+k);
				continue;
			}

			if(l.results[root][n].path[k]==root) {
				all.add(root);
				added.set(root);

			} else {
				all.addAll(getLeft2(l,l.results[root][n].path[k],added,cnt));				
			}



		}
		//	System.out.println(" ");


		return all;
	}



	private static ArrayList<Integer> getLeft1(Linear l, int root, BitSet added, int cnt) {

		int n=0;



		ArrayList<Integer> all =new ArrayList<Integer>();
		//System.out.print("r "+root+" ");
		if(added.get(root) ) {
			System.out.println(" added "+root) ;

			return all;
		}
		for(int k=0;k<l.wordChildsResult[root][n].length;k++) {


			if(added.get(l.wordChildsResult[root][n][k]) ) {
				System.out.println("xx already added "+k);
				continue;
			}

			if(l.wordChildsResult[root][n][k]==root) {
				all.add(root);
				added.set(root);

			} else {
				all.addAll(getLeft1(l,l.wordChildsResult[root][n][k],added,cnt));				
			}



		}
		//	System.out.println(" ");


		return all;
	}


	private static ArrayList<Integer> getLeftC(Linear l, int x, BitSet added, int cnt) {

		ArrayList<Integer> all =new ArrayList<Integer>();
		//System.out.print("r "+root+" ");
		if(added.get(x) ) {
			System.out.println(" added "+x) ;

			return all;
		}
		for(int k=0;k<l.wordChilds[x].length;k++) {


			if(added.get(l.wordChilds[x][k]) ) {
				System.out.println("xx already added "+k);
				continue;
			}

			if(l.wordChilds[x][k]==x) {
				all.add(x);
				added.set(x);

			} else {
				all.addAll(getLeftC(l,l.wordChilds[x][k],added,cnt));				
			}



		}
		//	System.out.println(" ");


		return all;
	}

	/**
	 * @param nodes
	 * @param next
	 * @param nodes2
	 */
	private static int[] combine(int[] nodes, int next, int[] nodes2) {

		int len =1;
		if (nodes!=null) len+=nodes.length;
		if(nodes2!=null) len+=nodes2.length;
		int[] cbn = new int[len];

		int ct=0;
		if(nodes!=null){

			for(int i=0;i<nodes.length;i++) {
				cbn[ct]=nodes[i];
				ct++;
			}

		}
		cbn[ct]=next;
		ct++;
		if(nodes2!=null){

			for(int i=0;i<nodes2.length;i++) {
				cbn[ct]=nodes2[i];
				ct++;
			}	
		}
		return cbn;

	}





	/**
	 * @param c
	 * @param all
	 * @return
	 */
	private static int[] combine(int[] all, int[] c) {
		if (c==null) return all;
		if (all==null) return c;
		int cbn[] =new int[c.length+all.length];

		for(int i=0;i<all.length;i++) {
			cbn[i]=all[i];
		}
		for(int i=all.length;i<all.length+c.length;i++) {
			cbn[i]=c[i-all.length];
		}

		return cbn;
	}


	/**
	 * @param n
	 * @param d
	 * @param order
	 * @return
	 */
	private static ArrayList<Integer> orderer(int n, Graph d, ArrayList<Integer> order) {

		int out[] = d.getOut(n);

		if (out==null || out[0]==0) return new  ArrayList<Integer>();

		for(int o=1;o<=out[o];o++) {

			int x = d.getOut(out[o])[1];

			if (d.getContent(x)==Environment.content("L")) {

				//	int order[] = Decoder.sortNodes(nds, dataNN); // null == data

			}
		}
		return order;

	}




	/**
	 * @param d
	 * @param length
	 * @return
	 */
	private static Parse getParse(Graph d, int length) {

		int nodeCount = length;

		Parse p = new Parse();
		p.heads = new short[nodeCount];
		p.labels = new short[nodeCount];

		d.buildIn();


		for(int n =0;n < nodeCount;n++) {
			if (d.getIn(n)!=null) {
				p.heads[n]=(short) d.getIn(d.getIn(n)[1])[1];
				p.labels[n]=(short) d.getContent(d.getIn(n)[1]);

			} else p.heads[n] =-1;
		}
		return p;
	}




}
