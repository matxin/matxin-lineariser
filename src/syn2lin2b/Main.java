package syn2lin2b;

import is2.data.Edges;
import is2.data.F2SF;
import is2.data.FV;
import is2.data.HFG;
import is2.data.Instances;
import is2.data.Long2Int;
import is2.data.Long2IntInterface;
import is2.data.MFO;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.io.HFGShallowReader;

import is2.util.DB;
import is2.util.OptionsSuper;
import is2.data.ParametersFloat;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import syn2lin2.Decoder.Path;

import edu.stanford.nlp.mt.metrics.BLEUMetric;

//import util.BLEUMetric;




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



		dis.close();

		DB.println("Reading data finnished");

		Pipe.types = new String[pipe.mf.getFeatureCounter().get(Pipe.REL)];
		for(Entry<String,Integer> e : pipe.mf.getFeatureSet().get(Pipe.REL).entrySet())  Pipe.types[e.getValue()] = e.getKey();



				Decoder decoder =  new Decoder();
				//	Decoder.NON_PROJECTIVITY_THRESHOLD =(float)options.decodeTH;

				Extractor.init(pipe.mf);

				return decoder;
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

				int root=-1;
				for(int k=0;k<is.length(n);k++) {

					if (is.heads[n][k]==-1) {

						root=k;
						//	break;
					}	
				}
				root =0;

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

						good=false;
						break;
					}
				}
				if(!good) {
					continue;
				}


				l = new Linear(is, n);

				// for each package get the contained nodes

				for(int k=0;k<l.wordChilds.length;k++) {

					int[] nds = l.wordChilds[k];

					dataNN = pipe.fillVector((F2SF)params.getFV(),  is, n, dataNN, nds);

					syn2lin2b.Decoder.Path[] order = Decoder.sortNodes(nds,k, dataNN,pipe.extractor[0],is,n,params.getFV()); // null == data

					float e = (float)pipe.errors1(order[0].path);

					if (e<=0) continue;			

					error += e;

					pred.clear();
					pipe.createVector1(is,n,order[0].path,k,pred);

					Arrays.sort(order[0].path);


					act.clear();
					pipe.createVector1(is,n,order[0].path,k, act);

					params.update(pred, act, (float) upd, e);



				}
			}

			String info = " td "+(((float)Decoder.timeDecotder)/1000000F)+" tr "+(((float)Decoder.timeRearrange)/1000000F)
					+" te "+(((float)Pipe.timeExtract)/1000000F);
			pipe.outValueErr(numInstances,Math.round(error*1000)/1000,f1/numInstances,del,last, upd,info);
			del=0;


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

		BufferedReader nbest = getReader(new File(options.testfile+".nbest-20.gz"), true);

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



		int cnt = 0;
		int del=0;
		long last = System.currentTimeMillis();

		System.out.println("\nInformation\n------------------- ");

		System.out.print("Processing Sentence: ");

		DataNN dataNN = new DataNN(100);

		Set<Entry<String,Integer>> entries = map.entrySet();
		ArrayList<Entry<String,Integer>> list = new ArrayList<Entry<String,Integer>>();
		list.addAll(entries);

		double bleu=0;

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


			// read nbest

			ArrayList<NBest> nbestList = readNBest(nbest,pipe.mf);


			short[] bestLabel=is.labels[0], bestHead=is.heads[0];
			// nbest 
			String[] forms = instance.forms;
			int root=0;

			Linear lin =null; float best=-1000;
			cnt++;

			int bcnt =0;
			for(NBest nb : nbestList) {


				if (bcnt>0) break;
				bcnt++;
				is.heads[0] = nb.heads;
				is.labels[0] = nb.labels;




				Linear l = new Linear(is, 0); 

				float score =0;
				for(int k=0;k<l.wordChilds.length;k++) {

					int[] nds = l.wordChilds[k];
					dataNN = pipe.fillVector((F2SF)params.getFV(),  is, root, dataNN,nds);

					l.wordChildsResult[k] = Decoder.sortNodes(nds,k, dataNN,pipe.extractor[0],is,root,params.getFV()); // null == data

					score += l.wordChildsResult[k][0].prob/nds.length;

					// oracle sorter
					//		for (int j=0;j<l.wordChildsResult[k].length;j++)
					//				Arrays.sort(l.wordChildsResult[k][j]);

				}			

				// for each package get the contained nodes
				
//				score = (float)(score ); 
				score = (float)(score+Math.exp(nb.prob)); 
				//			ArrayList<Integer> al = getLeft1(l,root,added,cnt);
			//	DB.println("score "+score+" l "+l);
				if (score > best) {
					lin = l;
					best =score;
					bestHead = is.heads[0];
					bestLabel = is.labels[0];
				}

			}
			
			if (lin ==null) {
				DB.println("lin is null ");
				System.exit(0);
			}


			is.heads[0] =bestHead ;
			is.labels[0]=bestLabel;

			BitSet added = new BitSet();			
			ArrayList<Integer> al = getLeft1(lin, 0, added,cnt);


			int all[] = new int[lin.wordChildsResult.length+1];
			int pos=0;
			for(int k=0;k<al.size();k++) {
				//	System.out.println();
				Integer p = al.get(k);
				//	System.out.println("order "+k);
				//	for (int w : p) System.out.print(w+" ");

				all[pos]=p;
				//					System.out.println(k+" "+p+" "+pos);
				pos++;
				//				}
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
			for(int j = 1; j < formsNoRoot.length+1; j++) {

				jx.put(all[j], j);

			}

			for(int j = 0; j < formsNoRoot.length; j++) {


				int w= all[j+1];

				formsNoRoot[j] = forms[w];
				posNoRoot[j] = instance.gpos[w];
				pposs[j] = instance.ppos[w];

				labels[j] =  instance.labels[w];

				if (jx.get(instance.heads[w])==null) ;// DB.println("error "+j);
				else heads[j] =jx.get(instance.heads[w]); //instance.heads[



				lemmas[j] = instance.lemmas[w];

				if (instance.lemmas!=null) org_lemmas[j] = instance.lemmas[w];
				if (instance.ofeats!=null)  {
					of[j] = instance.ofeats[w];
					int first= of[j].indexOf("|");
					if (of[j]!=null) {
						if (first >0 && of[j].contains("id=")) {
							of[j]=of[j].substring(first+1);
						} else if (of[j].contains("id=")) of[j]="_";
					}
				}
				if (instance.pfeats!=null)	{

					pf[j] = instance.pfeats[w];
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
		
			StringBuilder s = new StringBuilder();
			for(int k= 1;k< instance.length();k++) {
				if (s.length()>0)s.append(" ");
				s.append(instance.forms[k]);				
			}
			ArrayList<String> ref = new ArrayList<String>();
			ref.add(s.toString());
			//	System.out.println("gold:"+s.toString());
			s = new StringBuilder();
			for(int k= 0;k< instance.length()-1;k++) {
				if (s.length()>0)s.append(" ");
				s.append(i09.forms[k]);				
			}
			//	System.out.println("pred:"+s.toString());
			bleu += BLEUMetric.computeLocalSmoothScore(s.toString(), ref, 4);

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

		System.out.println("forms count "+Instances.m_count+" unkown "+Instances.m_unkown);
		System.out.println("BLUE " + (bleu/cnt));

	}





	private static ArrayList<NBest> readNBest(BufferedReader nbest, MFO mf) {


		ArrayList<NBest> n = new ArrayList<NBest>();
		for(int k=0;k<21;k++) {
			try {
				String line = nbest.readLine();
				if (line.length()<1) break;
				NBest nb = new NBest();
				String parts[] = line.split("\t");
				nb.prob = Double.parseDouble(parts[0].split(" ")[3]);
				String heads[] = parts[1].split(",");
				String labels[] = parts[2].split(",");

			

				nb.heads = new short[heads.length];
				nb.labels = new short[heads.length];

				for(int i=0;i<heads.length;i++) {
					nb.heads[i] = Short.parseShort(heads[i]);
					nb.labels[i] = (short) mf.getValueS(Pipe.REL,labels[i]);
				}


				n.add(nb);
			} catch (IOException e) {


				e.printStackTrace();
			}
		}

		//	DB.println("nbest "+n);
		return n;
	}


	private static ArrayList<Integer> getLeft1(Linear l, int x, BitSet added, int cnt) {

		int n=0;



		ArrayList<Integer> all =new ArrayList<Integer>();
		//System.out.print("r "+root+" ");
		if(added.get(x) ) {
			System.out.println(" added "+x) ;

			return all;
		}

		for(int k=0;k<l.wordChildsResult[x][n].path.length;k++) {


			if(added.get(l.wordChildsResult[x][n].path[k]) ) {
				System.out.println("xx already added "+k);
				continue;
			}

			if(l.wordChildsResult[x][n].path[k]==x) {
				all.add(x);
				added.set(x);

			} else {
				all.addAll(getLeft1(l,l.wordChildsResult[x][n].path[k],added,cnt));				
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





	public static BufferedReader getReader(File file,boolean gzipped)
			throws IOException{
		return getReader(file,gzipped,Charset.forName("UTF-8"));
	}

	public static BufferedReader getReader(File file,boolean gzipped,Charset charset) throws IOException{
		InputStream is=new FileInputStream(file);
		if(gzipped){
			is=new GZIPInputStream(is);
		}
		BufferedReader br=new BufferedReader(new
				InputStreamReader(is,charset));
		return  br;
	}

	public static BufferedWriter getWriter(File file,boolean gzipped)
			throws IOException{
		return getWriter(file,gzipped,Charset.forName("UTF-8"));
	}

	public static BufferedWriter getWriter(File file,boolean
			gzipped,Charset charset) throws IOException{
		OutputStream os=new FileOutputStream(file);
		if(gzipped){
			os=new GZIPOutputStream(os);
		}
		BufferedWriter writer=new BufferedWriter(new
				OutputStreamWriter(os,charset));
		return writer;
	}













}
