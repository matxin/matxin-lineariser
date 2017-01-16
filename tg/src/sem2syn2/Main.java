package sem2syn2;

import is2.data.Edges;
import is2.data.F2SF;
import is2.data.FV;
import is2.data.HFG;
import is2.data.Instances;
import is2.data.Long2IntInterface;
import is2.data.MFO;
import is2.data.P;
import is2.data.Parse;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.io.HFGMapper;
import is2.io.HFGReader;
import is2.io.HFGShallowReader;

import is2.util.Evaluator;
import is2.util.DB;
import is2.util.OptionsSuper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


import sem2syn.DSyntConverter.Word;

import rt.algorithm.GraphConverter;
import rt.model.Environment;
import rt.model.Graph;



public class Main {

	public static int THREADS =4;

	Long2IntInterface long2int;
	OptionsSuper options;



	public static void main (String[] args) throws Exception
	{

		Runtime runtime = Runtime.getRuntime();

		THREADS = runtime.availableProcessors();


		long start = System.currentTimeMillis();
		Options options = new Options(args);

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

			P[] gs = pipe.createInstances(options.trainfile,options.dsfile,options.shallowMapping, options.deepMapping, options.formatTask,is);

			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));
			zos.putNextEntry(new ZipEntry("data")); 
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

			pipe.mf.writeData(dos);

			ParametersFloat params = new ParametersFloat(long2int.size());

			train(options, pipe,params,is, gs); 

			pipe.mf.clearData();

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


			output(options,pipe, decoder, params);
			//			DB.println("misses "+LongIntHash.misses+" good "+LongIntHash.good);

		}

		if (options.convertFile!=null) {
			convert(options);
		

		}
		
		System.out.println();

		if (options.eval) {
			System.out.println("\nEVALUATION PERFORMANCE:");
			Evaluator.evaluate(options.goldfile, options.outfile);
		}

		long end = System.currentTimeMillis();
		System.out.println("used time "+((float)((end-start)/100)/10));
	}




	/**
	 * @param options2
	 */
	private static void convert(Options options) {
		try {
			
		CONLLWriter09 syntWriter = new CONLLWriter09(options.outfile+"-ssynt", options.formatTask);
		CONLLWriter09 linWriter = new CONLLWriter09(options.outfile+"-lin", options.formatTask);
		CONLLWriter09 dlinWriter = new CONLLWriter09(options.outfile+"-dlin", options.formatTask);
		
		CONLLWriter09 dsyntWriter = new CONLLWriter09(options.outfile+"-dsynt", options.formatTask);

	//	HFGReader semReader = new HFGReader(options.testfile);
		//semReader.setSilent(true);
		HFGShallowReader dsReader = new HFGShallowReader(options.dsfile);
		HFGShallowReader dsReader2 = new HFGShallowReader(options.dsfile);
		HFGShallowReader dsReader3 = new HFGShallowReader(options.dsfile);

		DB.println("options.convertFile "+options.convertFile);
		HFGReader deepReader = new HFGReader(options.convertFile);
		dsReader.setSilent(true);
		dsReader2.setSilent(true);
		dsReader3.setSilent(true);
		
		HFGMapper mapperReader = new HFGMapper(options.shallowMapping);
		HFGMapper mapperDeepReader = new HFGMapper(options.deepMapping);




		int cnt = 0;
		int del=0;
		long last = System.currentTimeMillis();

		System.out.println("\nParsing Information ");
		System.out.println("------------------- ");


		System.out.print("Processing Sentence: ");
		Data data=null;

		System.out.println();

		while(true) {

			Instances is = new Instances();


			mapperReader.setP(new P());
			P p = mapperReader.getNext(true);
			mapperDeepReader.setP(p);
			mapperDeepReader.getNext(false);

			HFG shallowDSynt = dsReader.getNext(p);
			if (shallowDSynt==null) break;
			HFG shallowSynt = dsReader2.getNext(null);
			HFG shallowLin = dsReader3.getNext(p,true);
		
			
			HFG deepLin = deepReader.getNext(p, true);
			
			p.shallow=shallowDSynt;

			if (dsReader.noDeepMap || dsReader2.noDeepMap) {
				DB.println("no deep map skip instance ");
				continue;
			}
			

			cnt++;
			del=Pipe.outValue(cnt, del,last);



			
			// use for the training ppos
		
//			int len=forms.length;

			SentenceData09 synt = new SentenceData09();
			synt.forms = new String[shallowSynt.lemma.length-1];
			synt.lemmas= new String[shallowSynt.lemma.length-1];
			synt.init(synt.forms);


			synt.gpos = new String[synt.length()];


			synt.plabels = new String[synt.length()];
			synt.pheads= new int[synt.length()];
			synt.pfeats= new String[synt.length()];
			synt.ofeats= new String[synt.length()];


			for(int k=0;k<synt.length();k++) synt.forms[k]="_";
			for(int k=0;k<synt.length();k++) synt.gpos[k]="_";

			for(int j = 0; j < shallowSynt.lemma.length-1; j++) {
				synt.plabels[j] =  shallowSynt.label[j+1];
				synt.gpos[j] = shallowSynt.pos[j+1];
				synt.pheads[j] = shallowSynt.head[j+1];
				synt.labels[j] =shallowSynt.label[j+1];
				synt.heads[j] = shallowSynt.head[j+1];
				synt.lemmas[j] =shallowSynt.lemma[j+1];
				for(String f : shallowSynt.feats[j+1]) {
					if (f!=null)synt.pfeats[j]=synt.pfeats[j]==null?f:synt.pfeats[j]+"|"+f;
					if (f!=null)synt.ofeats[j]=synt.ofeats[j]==null?f:synt.ofeats[j]+"|"+f;
				}
			}
			
			
			
			
			SentenceData09 lin = new SentenceData09();
			lin.forms = new String[shallowLin.lemma.length-1];
			lin.lemmas= new String[shallowLin.lemma.length-1];
			lin.init(lin.forms);


			synt.gpos = new String[lin.length()];



			lin.plabels = new String[lin.length()];
			lin.pheads= new int[lin.length()];
			lin.pfeats= new String[lin.length()];
			lin.ofeats= new String[lin.length()];


			for(int k=0;k<lin.length();k++) lin.forms[k]="_";
			for(int k=0;k<lin.length();k++) lin.gpos[k]="_";

			for(int j = 0; j < shallowLin.lemma.length-1; j++) {
				lin.plabels[j] = 
					shallowLin.label[j+1];
				lin.forms[j]= shallowLin.form[j+1];
				lin.gpos[j] = shallowLin.pos[j+1];
				lin.ppos[j] = shallowLin.pos[j+1];
				lin.pheads[j] = shallowLin.head[j+1];
				lin.labels[j] =shallowLin.label[j+1];
				lin.heads[j] = shallowLin.head[j+1];
				lin.lemmas[j] =shallowLin.lemma[j+1];
				for(String f : shallowLin.feats[j+1]) {
					if (f!=null && f.startsWith("id=")) continue;
					if (f!=null)lin.pfeats[j]=lin.pfeats[j]==null?f:lin.pfeats[j]+"|"+f;
					if (f!=null)lin.ofeats[j]=lin.ofeats[j]==null?f:lin.ofeats[j]+"|"+f;
				}
			}
			
			

			SentenceData09 dsynt = new SentenceData09();
			dsynt.forms = new String[shallowDSynt.lemma.length-1];
			dsynt.lemmas= new String[shallowDSynt.lemma.length-1];
			dsynt.init(dsynt.forms);


			dsynt.plabels = new String[dsynt.length()];
			dsynt.pheads= new int[dsynt.length()];

			dsynt.pfeats= new String[dsynt.length()];
			dsynt.ofeats= new String[dsynt.length()];

			for(int k=0;k<dsynt.length();k++) dsynt.forms[k]="_";

			dsynt.gpos = new String[shallowDSynt.lemma.length-1];

			for(int k=0;k<dsynt.length();k++) dsynt.gpos[k]="_";


			for(int j = 0; j < shallowDSynt.lemma.length-1; j++) {
				dsynt.gpos[j]=shallowDSynt.pos[j+1];
				dsynt.plabels[j] = shallowDSynt.label[j+1];
				dsynt.pheads[j] = shallowDSynt.head[j+1];
				dsynt.labels[j] =shallowDSynt.label[j+1];
				dsynt.heads[j] = shallowDSynt.head[j+1];
				dsynt.lemmas[j] =shallowDSynt.lemma[j+1];
				for(String f : shallowDSynt.feats[j+1]) {
					if (f!=null)dsynt.pfeats[j]=dsynt.pfeats[j]==null?f:dsynt.pfeats[j]+"|"+f;
					if (f!=null)dsynt.ofeats[j]=dsynt.ofeats[j]==null?f:dsynt.ofeats[j]+"|"+f;
				}
		//		dsynt.pfeats[j] =shallowDSynt.feats[j+1];
			}
			dsyntWriter.write(dsynt);
			
			
			
			
			
			
			
			
			ArrayList<sem2syn.DSyntConverter.Word> snt = new ArrayList<sem2syn.DSyntConverter.Word>();

			for(int n=0; n<deepLin.lemma.length;n++) {
				Word w = new Word();
				snt.add(w);
			}

			for(int n=0; n<deepLin.lemma.length;n++) {

				if (deepLin.head[n]>=0)snt.get(n).head = snt.get(deepLin.head[n]);
			//	if (deepLin.phead[n]>=0)snt.get(n).phead = snt.get(deepLin.phead[n]);
			//	snt.get(n).plable = deepLin.plabel[n];
				snt.get(n).lable = deepLin.label[n];
				snt.get(n).feat= deepLin.feats[n];
				snt.get(n).lemma = deepLin.lemma[n];
			//	System.out.println("lemma "+deepLin.lemma[n]);
			}

			
			
			
			
			
			
			
			SentenceData09 dlin = new SentenceData09();
			dlin.forms = new String[deepLin.lemma.length-1];
			dlin.lemmas= new String[deepLin.lemma.length-1];
			dlin.init(dlin.forms);


			dlin.plabels = new String[dlin.length()];
			dlin.pheads= new int[dlin.length()];

			dlin.pfeats= new String[dlin.length()];
			dlin.ofeats= new String[dlin.length()];

			for(int k=0;k<dlin.length();k++) dlin.forms[k]="_";

			dlin.gpos = new String[deepLin.lemma.length-1];

			for(int k=0;k<dlin.length();k++) dlin.gpos[k]="_";


			for(int j = 0; j < deepLin.lemma.length-1; j++) {
			
				dlin.gpos[j]=shallowLin.pos[j+1];
				dlin.plabels[j] = shallowLin.label[j+1];
				dlin.pheads[j] = shallowLin.head[j+1];
				dlin.labels[j] =shallowLin.label[j+1];
				dlin.heads[j] = shallowLin.head[j+1];
				dlin.lemmas[j] =deepLin.lemma[j+1];
				if (dlin.lemmas[j]==null) dlin.forms[j] =shallowLin.lemma[j+1];
			
		//		dlin.pfeats[j] =shallowdlin.feats[j+1];
			}
			
			
			dlinWriter.write(dlin);
			
			
			

		

			linWriter.write(lin);
			syntWriter.write(synt);


		}
		//pipe.close();
		syntWriter.finishWriting();
		dsyntWriter.finishWriting();
		linWriter.finishWriting();
		dlinWriter.finishWriting();
		} catch(Exception e) {
			e.printStackTrace();
		}

		
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


		//		pipe.edges =new Edges();
		Edges.read(dis);
		options.decodeProjective = dis.readBoolean();

		dis.close();

		DB.println("Reading data finnished");

		pipe.types = new String[pipe.mf.getFeatureCounter().get(Pipe.REL)];
		for(Entry<String,Integer> e : pipe.mf.getFeatureSet().get(Pipe.REL).entrySet())  pipe.types[e.getValue()] = e.getKey();



		Decoder decoder =  new Decoder();
		//	Decoder.NON_PROJECTIVITY_THRESHOLD =(float)options.decodeTH;

		Extractor.init(pipe.mf);

		return decoder;
	}



	/**
	 * Do the training
	 * 
	 * @param options Command line options
	 * @param pipe  
	 * @param params
	 * @param is 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	static public void train(OptionsSuper options, Pipe pipe, ParametersFloat params, Instances is, P[] p) 
	throws IOException, InterruptedException, ClassNotFoundException {

		System.out.println("\nTraining Information \n-------------------- ");

		Decoder decoder =  new Decoder();

		int numInstances = is.size();

		int maxLenInstances =0;
		for(int i=0;i<numInstances;i++) if (maxLenInstances<is.length(i)) maxLenInstances=is.length(i);

		Data data = new Data(maxLenInstances, (short)pipe.types.length);

		int iter = 0;
		int del=0; 
		float error =0;
		float f1=0;

		FV pred = new FV();
		FV act = new FV();

		double	upd =  (double)(numInstances*options.numIters)+1;
		int dist[][][] = new int[numInstances][][];
		int distPath[][][][] = new int[numInstances][][][];

		for(; iter < options.numIters; iter++) {

			System.out.print("Iteration "+iter+": ");

			long start = System.currentTimeMillis();

			long last= System.currentTimeMillis();
			error=0;
			f1=0;

			for(int n = 0; n < numInstances; n++) {

				upd--;

				if (is.labels[n].length>options.maxLen) continue;

				String info = " td "+(((float)Decoder.timeDecotder)/1000000F)+" tr "+(((float)Decoder.timeRearrange)/1000000F)
				+" te "+(((float)Pipe.timeExtract)/1000000F);

				if((n+1) %500 == 0)  del= pipe.outValueErr(n+1,Math.round(error*1000)/1000,f1/n,del, last, upd,info);

				int len =is.length(n);

				if(dist[n]==null) {

					dist[n] = new int[len][len];
					distPath[n] = new int[len][len][];

					boolean next =false;
					for(int w1=0;w1<len;w1++) {

						try {
							dist[n][w1] = Extractor.distAll(p[n].deep.semGraph, w1, len);
							distPath[n][w1] = Extractor.distPathRel(p[n].deep.semGraph, w1, len);
						} catch(Exception e) {

							for(int k=0;k<len;k++) {
								DB.println(k+"\t"+Environment.getValue(p[n].deep.semGraph.getContent(k)));
							}
							e.printStackTrace();
							DB.println("sentence deep id  "+p[n].deep.sentId);

							DB.println(""+GraphConverter.str(p[n].deep.semGraph));
							DB.println("sentence "+n+" w "+w1);
							DB.println("len "+len+" graph size "+p[n].deep.semGraph.size()+" p[n].shallow.lemmas.length: "+p[n].shallow.lemma.length);
							DB.println(p[n].shallow.toString());
				//			System.exit(0);
							next=true;
							break;
						}

					}
					if (next) continue;
				}
				data = pipe.fillVector((F2SF)params.getFV(), decoder, is, n, data, p[n].deep.semGraph,dist[n],distPath[n]);

				Graph d=null;
				try {

					
					d = Decoder.decode(data, is.length(n),pipe.extractor[0],params);
					
				
					
				
					
					
				} catch(Exception e) {
					e.printStackTrace();
					
					DB.println("try to continue");
					continue;
					
				}
				Parse prs =getParse(d,is.length(n));
				prs = Decoder.label(prs, p[n].deep.semGraph, is, n,dist[n],pipe.extractor[0],params );
				double e= pipe.errors(is, n , d);

				//if (d.f1>0)f1+=d.f1;

				if (e>0) {
					//	Extractor.replacedEdges(d,pipe.types,gs[n]);
					//	DB.println("correct "+GraphConverter.str(d));

				
			


					// get predicted feature vector
					pred.clear();	
					pipe.createVector(d,p[n].deep.semGraph,pred,dist[n], distPath[n]);

			

					act.clear();
					pipe.createVector(is.heads[n], is.labels[n], p[n].deep.semGraph, act);
	
					params.update(act, pred, is, n, upd,e);
				
				}
				
				double labelErrors = pipe.errorsLab( is, n, prs);
				
				if (labelErrors>0) {
					pred.clear();	
					FV pd = decoder.features(prs, p[n].deep.semGraph, is, n,dist[n], pipe.extractor[0], params);

					error += labelErrors;
				
					Parse goldParse = new Parse(is.length(n));
				
					goldParse.heads = is.heads[n];
					goldParse.labels = is.labels[n];
				
					act.clear();
					FV gd = decoder.features(goldParse, p[n].deep.semGraph, is, n,dist[n], pipe.extractor[0], params);
				
					params.update(gd, pd, is, n, upd,labelErrors);
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
		
//		System.out.println(" "+Extractor.map);
//		for(int k=0;k<params.parameters.length;k++) {
//			if (params.parameters[k]!=0) System.out.println("k "+k+" "+params.parameters[k]);
//		}
		//	printSim(sim,pipe.extractor.mf);

	}                                   


	/**
	 * Do the parsing
	 * @param options
	 * @param pipe
	 * @param decoder
	 * @param params
	 * @throws IOException
	 */
	static public void output (Options options, Pipe pipe, Decoder decoder, ParametersFloat params) 
	throws Exception {

		long start = System.currentTimeMillis();

		//	CONLLReader09 depReader = new CONLLReader09(options.dstest, options.formatTask);
		//	CONLLReader09 syntReader = new CONLLReader09(options.testfile, options.formatTask);
		CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);

		CONLLWriter09 depGold = new CONLLWriter09("c:\\results\\gold", options.formatTask);

		HFGReader semReader = new HFGReader(options.testfile);
		semReader.setSilent(true);
		
		HFGShallowReader dsReader=null;
		HFGMapper mapperReader =null;
		HFGMapper mapperDeepReader=null;
		if (options.dstest!=null) {
			dsReader= new HFGShallowReader(options.dstest);
			dsReader.setSilent(true);
			mapperReader = new HFGMapper(options.shallowMappingTest);
			mapperDeepReader = new HFGMapper(options.deepMappingTest);
	}
		Extractor.initFeatures(pipe.mf);
		//Extractor.switchOff(options.features);		



		int cnt = 0;
		int del=0;
		long last = System.currentTimeMillis();

		System.out.println("\nParsing Information ");
		System.out.println("------------------- ");


		System.out.print("Processing Sentence: ");
		Data data=null;

		System.out.println("types "+pipe.types.length);
		//	for(int label=0;label< pipe.types.length;label++) {
		//		System.out.print(pipe.types[label]+" ");
		//	}
		System.out.println();
		MFO mf = new MFO();
		
		String rels[] = mf.reverse(mf.getFeatureSet().get(Pipe.REL));
		
		while(true) {

			Instances is = new Instances();
			is.init(1, pipe.mf,options.formatTask);

			//		SentenceData09 instance = pipe.nextInstance(is, depReader);
			//		SentenceData09 instanceS = syntReader.getNext(); 
			P p = new P();
			if (dsReader!=null) {
			mapperReader.setP(p);
			 mapperReader.getNext(true);
			mapperDeepReader.setP(p);
			mapperDeepReader.getNext(false);
			}
			HFG instance = semReader.getNext(); 
			
			

			if (instance==null) break;
			//	if (instance == null || p.l<=num1)	break;
		
			
			if (dsReader!=null) p.shallow= dsReader.getNext(p);;
			p.deep=instance;

			instance.semGraph.buildIn();

			Graph graph = instance.semGraph;
			
			
			for(int n =0;n<graph.size();n++) {
				if (graph.getType(n)==Graph.EDGE) {
					String label = Environment.getValue(graph.getContent(n));
					//mf.register(ARG, label);
					
					graph.setContent(n, mf.register(Pipe.ARG, label));
					
				}
		
				
				if (graph.getType(n)==Graph.ATTRIBUTE) {
					String att = Environment.getValue(graph.getContent(n));
					int[] out =graph.getOut(n);
					if (att.equals("id")) continue;
					String val = Environment.getValue(graph.getContent(out[1]));
					
				//	mf.register(FEAT, att+"="+val);
					graph.setContent(n, mf.register(Pipe.FEAT,  att+"="+val));

				}
			}
			graph.setContent(0, mf.register(Pipe.WORD, is2.io.CONLLReader09.ROOT));

			for(int k=1;k<instance.lemma.length;k++) {
				
				graph.setContent(k, mf.register(Pipe.WORD, instance.lemma[k]));
				
				
			}
			cnt++;


			
			String[] forms = instance.lemma;
			is.createInstance09(instance.lemma.length);
			// use for the training ppos
			for(int k =0;k<p.deep.lemma.length;k++) {

				is.setForm(0, k, p.deep.lemma[k]);
				is.setLemma(0, k, p.deep.lemma[k]);
		//		is.setHead(0, k, p.deep.head[k]);
		//		is.setRel(0, k, p.deep.label[k]);
			}	
			int len =is.length(0);
			int dist[][] = new int[len][len];
			int distPath[][][] = new int[len][][];
			for(int w1=0;w1<len;w1++) {
				try {
					dist[w1] = Extractor.distAll(instance.semGraph, w1, len);
					distPath[w1] = Extractor.distPathRel(instance.semGraph, w1, len);
				} catch(Exception e){

					DB.println("problem in "+cnt);
					//	DB.println(""+instance.toString());
					DB.println(""+GraphConverter.str(instance.semGraph));

					e.printStackTrace();
				}
			}

			data = pipe.fillVector((F2SF)params.getFV(), decoder, is, 0, data, instance.semGraph,dist, distPath);

			Graph d = Decoder.decode(data, is.length(0),pipe.extractor[0],params);

			Parse prs =getParse(d,is.length(0));
			
			
			prs = Decoder.label(prs,instance.semGraph, is, 0, dist, pipe.extractor[0],params );
			
			int countHead =0;
			for(int j = 1; j < forms.length-1; j++) {
				if (prs.heads[j]==0) {
					countHead++;
				//	DB.println("head is "+prs.heads[j]+" in sentence "+cnt+" word "+j);
			//		DB.println(""+GraphConverter.str(d));
				}
				
				
				
			}
			
			if (countHead>1) DB.println("error found more than one head in sentence "+cnt);

			SentenceData09 i09 = new SentenceData09();
			i09.forms = new String[len-1];
			i09.lemmas= new String[len-1];
			i09.pfeats= new String[len-1];
			i09.ofeats= new String[len-1];
			i09.init(i09.forms);


			i09.gpos = new String[len-1];

			i09.plabels = new String[i09.length()];
			i09.pheads= new int[i09.length()];


			for(int k=0;k<i09.length();k++) i09.forms[k]="-";
			for(int k=0;k<i09.length();k++) i09.gpos[k]="-";

			for(int j = 0; j < forms.length-1; j++) {

//				i09.plabels[j] = pipe.types[prs.labels[j+1]];
				i09.plabels[j] = rels[prs.labels[j+1]];
				
				i09.pheads[j] = prs.heads[j+1];
//				i09.labels[j] = pipe.types[prs.labels[j+1]];
				i09.labels[j] = rels[prs.labels[j+1]];
				
				i09.labels[j] = rels[prs.labels[j+1]];

				
				i09.heads[j] = prs.heads[j+1];
				i09.lemmas[j]=p.deep.lemma[j+1];
				for(String f : p.deep.feats[j+1]) {
					if (f!=null)i09.pfeats[j]=i09.pfeats[j]==null?f:i09.pfeats[j]+"|"+f;
					if (f!=null)i09.ofeats[j]=i09.ofeats[j]==null?f:i09.ofeats[j]+"|"+f;
				}

			}

						
			if (dsReader!=null) {
			SentenceData09 gold = new SentenceData09();
			gold.forms = new String[len-1];
			gold.lemmas= new String[len-1];
			gold.pfeats= new String[len-1];
			gold.ofeats= new String[len-1];
			gold.init(gold.forms);


			gold.plabels = new String[i09.length()];
			gold.pheads= new int[i09.length()];


			for(int k=0;k<gold.length();k++) gold.forms[k]="-";

			gold.gpos = new String[len-1];

			for(int k=0;k<gold.length();k++) gold.gpos[k]="-";


			for(int j = 0; j < forms.length-1; j++) {

				gold.plabels[j] = p.shallow.label[j+1];
				gold.pheads[j] = p.shallow.head[j+1];
				gold.labels[j] =p.shallow.label[j+1];
				gold.heads[j] = p.shallow.head[j+1];
				gold.lemmas[j] =p.shallow.lemma[j+1];
				for(String f : p.shallow.feats[j+1]) {
					if (f!=null)gold.pfeats[j]=gold.pfeats[j]==null?f:gold.pfeats[j]+"|"+f;
					if (f!=null)gold.ofeats[j]=gold.ofeats[j]==null?f:gold.ofeats[j]+"|"+f;

				}
			}

			depGold.write(gold);
			}
			depWriter.write(i09);

			del=pipe.outValue(cnt, del,last);

		}
		//pipe.close();
		depWriter.finishWriting();
		depGold.finishWriting();
		long end = System.currentTimeMillis();
		//		DB.println("errors "+error);
		System.out.println("Used time " + (end-start));
		System.out.println("forms count "+Instances.m_count+" unkown "+Instances.m_unkown);

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
