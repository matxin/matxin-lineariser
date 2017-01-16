package sem2syn2;

import is2.data.Edges;
import is2.data.F2SF;
import is2.data.FV;
import is2.data.HFG;
import is2.data.Instances;
import is2.data.MFO;
import is2.data.P;
import is2.data.Parse;
import is2.data.PipeGen;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.HFGMapper;
import is2.io.HFGReader;
import is2.io.HFGShallowReader;
import is2.util.OptionsSuper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import rt.model.Environment;
import rt.model.Graph;
import rt.model.IGraph;
import rt.util.DB;


final public class Pipe extends PipeGen {

	public Edges edges;
	public Extractor[] extractor;

	public static final String POS = "POS";
	protected static final String DIST = "DIST";
	protected static final String WORD = "WORD";
	protected static final String PRED = "PRED";
	protected static final String ARG = "ARG";
	protected static final String FEAT = "F";
	public static final String REL = "REL";
	protected static final String TYPE = "TYPE";


	HashMap<Integer,Integer>  lemma2Predicate = new HashMap<Integer,Integer>() ;

	public static String types[];


	final public MFO mf = new MFO();

	private OptionsSuper options;
	public static long timeExtract;

	public Pipe(OptionsSuper o) {
		options = o;
	}

	public P[] createInstances(String deepFile, String dsfile, String shallowMapping,String deepMapping,  int task, Instances is)
	throws Exception {


		DB.println("dsfile"+dsfile);

		HFGReader semread = new HFGReader(deepFile);
		HFGShallowReader dsReader = new HFGShallowReader(dsfile);

		//	CONLLReader09 depReader = new CONLLReader09(dsfile,task);
		//	CONLLReader09 syntReader = new CONLLReader09(file,task);

		mf.register(REL, "<root-type>");

		// register at least one predicate since the parsing data might not contain predicates as in 
		// the Japaness corpus but the development sets contains some

	//	long sl=0;

		System.out.print("Registering feature parts of sentence: ");
		int ic = 0;
		int del = 0;
		while (true) {
			
			HFG shallow = dsReader.getNext(null);
			if (shallow == null)	break;
			HFG instance = semread.getNext();


			ic++;



			//	sl+=instance.labels.length;

			//			if (ic % 1 == 0) {
			del = outValue(ic, del);
			//			}

			String[] labs1 = shallow.label;
			for (int i1 = 0; i1 < labs1.length; i1++) if (labs1[i1]!=null) mf.register(REL, labs1[i1]);

			String[] w = shallow.form;
			for (int i1 = 0; i1 < w.length; i1++) if (w[i1]!=null) mf.register(WORD, w[i1]);

			w = shallow.lemma;
			for (int i1 = 0; i1 < w.length; i1++) if (w[i1]!=null) mf.register(WORD, w[i1]);
			
			Graph graph = instance.semGraph;
			for(int n =0;n<graph.size();n++) {
				if (graph.getType(n)==Graph.EDGE) {
					String label = Environment.getValue(graph.getContent(n));
					mf.register(ARG, label);
					
			////
					graph.setContent(n, mf.register(ARG, label));
					
				}
				/*
				if (graph.getType(n)==Graph.NODE) {
					String label = Environment.getValue(graph.getContent(n));
					//mf.register(ARG, label);
					
					graph.setContent(n, mf.register(WORD, label));
					
				}
				*/
				if (graph.getType(n)==Graph.ATTRIBUTE) {
					String att = Environment.getValue(graph.getContent(n));
					int[] out =graph.getOut(n);
					if (att.equals("id")) continue;
					String val = Environment.getValue(graph.getContent(out[1]));
					
				//	mf.register(FEAT, att+"="+val);
					graph.setContent(n, mf.register(FEAT,  att+"="+val));

				}
			}

		
			

			if ((ic-1)>options.count) break;
		}

		dsReader.inputReader.close();
		semread.inputReader.close();
		//	System.exit(0);

		// Whats that ?



		del = outValue(ic, del);

		System.out.println();
		//initFeatures();
		Extractor.initFeatures(mf);


		mf.calculateBits();
		Extractor.init(mf);
		for(Extractor e : extractor) e.init();

		//		depReader.startReading(dsfile);

		long mem1 = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		System.out.println("Memory used so far " + (mem1 / 1024) + " kb");
		int num1 = 0;
		long start1 = System.currentTimeMillis();

		is.init(ic+1, mf); 

		String[] poxs = new String[mf.getFeatureCounter().get(POS)];

		//	for (Entry<String, Integer> e : mf.getFeatureSet().get(POS).entrySet()) {
		//		pos[e.getValue()] = e.getKey();
		//	}

		Edges.init(poxs.length); 
		System.out.print("Creating Features: ");

		del = 0;

		P[] p = new P[ic+1];



		semread = new HFGReader(deepFile);

		dsReader = new HFGShallowReader(dsfile);
		DB.println("shallow Mapping "+shallowMapping);
		HFGMapper mapperReader = new HFGMapper(shallowMapping);
		HFGMapper mapperDeepReader = new HFGMapper(deepMapping);

		num1=0;
		//FV f =new FV();
		while (true) {
			//	if (num1 % 100 == 0)  


			// read shallow mapping

			if ( p.length<=num1)	break;
			mapperReader.setP(new P());
			p[num1] = mapperReader.getNext(true);
			mapperDeepReader.setP(p[num1]);
			mapperDeepReader.getNext(false);

			HFG instance = semread.getNext(); 

			if (instance == null || p.length<=num1)	break;
			HFG shallow = dsReader.getNext(p[num1]);





			p[num1].deep= instance;
			p[num1].shallow= shallow;




			p[num1].deep.semGraph.buildIn();

			//		System.out.println("p "+p[num1].shallowM.size());
			is.createInstance09(shallow.lemma.length);
			Graph graph = p[num1].deep.semGraph;
			
			
			for(int n =0;n<graph.size();n++) {
				if (graph.getType(n)==Graph.EDGE) {
					String label = Environment.getValue(graph.getContent(n));
					mf.register(ARG, label);
					
			////
					graph.setContent(n, mf.register(ARG, label));
					
				}
				/*
				if (graph.getType(n)==Graph.NODE) {
					String label = Environment.getValue(graph.getContent(n));
					//mf.register(ARG, label);
					
					graph.setContent(n, mf.register(WORD, label));
					
				}
				*/
				if (graph.getType(n)==Graph.ATTRIBUTE) {
					String att = Environment.getValue(graph.getContent(n));
					int[] out =graph.getOut(n);
					if (att.equals("id")) continue;
					String val = Environment.getValue(graph.getContent(out[1]));
					
				//	mf.register(FEAT, att+"="+val);
					graph.setContent(n, mf.register(FEAT,  att+"="+val));

				}
			}

			graph.setContent(0, mf.register(WORD, is2.io.CONLLReader09.ROOT));
			for(int k=1;k<instance.lemma.length;k++) {
				
				graph.setContent(k, mf.register(WORD, instance.lemma[k]));
					
					
				}


			for(int k =0;k<p[num1].content.length;k++) {
				//		System.out.println(k+"\t"+p[num1].shallowM.get(k)+"\t"+p[num1].deepM.get(k));
			}
			for(int k =0;k<p[num1].shallow.lemma.length;k++) {

				is.setForm(num1, k, p[num1].shallow.lemma[k]);
				is.setLemma(num1, k, p[num1].shallow.lemma[k]);
				is.setHead(num1, k, p[num1].shallow.head[k]);
				is.setRel(num1, k, p[num1].shallow.label[k]);
			}				



			//			if (instance1 == null) break;
			/*
			int last = is.size() - 1;
			short[] pos =is.pposs[last];

			for (int k = 0; k < is.length(last); k++) {
				if (is.heads[last][k] < 0)	continue;
				Edges.put(pos[is.heads[last][k]],pos[k], k < is.heads[last][k],is.labels[last][k]);
			}

			if (!options.allFeatures && num1 > options.count) break;
			 */
			num1++;

		}

		System.out.println(mf.toString());

		del = outValue(num1, del);

		num1 = 0;


		long end1 = System.currentTimeMillis();

		System.gc();
		long mem2 = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		System.out.print(" time " + (end1 - start1) + " mem " + (mem2 / 1024)+ " kb ");

		types = new String[mf.getFeatureCounter().get(REL)];

		for (Entry<String, Integer> e : mf.getFeatureSet().get(REL).entrySet())  {

			types[e.getValue()] = e.getKey();
		}

		//
		Edges.findDefault();

		//		mf.stop();
		//	extractor.put=false;

		System.out.print(" processed "+is.size());
		return p;
	}


	/**
	 * Creates an instance for outputParses
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	protected final SentenceData09 nextInstance(Instances is, CONLLReader09 depReader) throws Exception {

		SentenceData09 instance = depReader.getNext(is);
		if (instance == null || instance.forms == null)	return null;

		return instance;
	}



	public Data fillVector(F2SF f, Decoder decoder, Instances is, int inst, Data d, Graph g, int dist[][], int[][][] distPath) throws InterruptedException {

		long ts = System.nanoTime();

		final int length = is.length(inst);
		if (d ==null || d.len<length) d = new Data(length, types.length);

		long[] v = new long[20];


		for(int w1=0;w1<length;w1++) {
			for(int w2=0;w2<length;w2++) {
				f.clear();
				for(int k=0;k<v.length;k++) v[k]=0;
				extractor[0].extractFeatures(g, w1, w2, 0, dist, distPath, v);
				
	//			for(int label=0;label< types.length;label++) {
//					int lab=label<<extractor[0].s_type;
				
				int lab =0;
					for(int k=0;k<v.length;k++) 
						if (v[k]>0) f.add(extractor[0].li.l2i(v[k]|lab  ));
					d.edge[w1][w2][2]= f.getScoreF();
	//				d.edge[w1][w2][label]= f.getScoreF();

//				}

			}
		}



		timeExtract += (System.nanoTime()-ts);

		return d;
	}

	public double errors( Instances is, int ic, Parse p) {
		short[] act = is.heads[ic];
		int correct = 0;

		// do not count root
		for(int i = 1; i < act.length; i++)  	 {
			if (p.heads[i]==act[i] && p.labels[i]==is.labels[ic][i]) correct++;
		}

		double x = ((double)act.length-1 - correct);

		p.f1 = (double)correct / (double)(act.length-1);

		return x;
	}
	
	public double errorsLab( Instances is, int ic, Parse p) {
		short[] act = is.heads[ic];
		int correct = 0;

		// do not count root
		for(int i = 1; i < act.length; i++)  	 {
			if ( p.labels[i]==is.labels[ic][i]) correct++;
		}

		double x = ((double)act.length-1 - correct);

		p.f1 = (double)correct / (double)(act.length-1);

		return x;
	}

	public double errors( Instances is, int ic, Graph d) {

		int nodeCount = is.length(ic);

		Parse p = new Parse();
		p.heads = new short[nodeCount];
		p.labels = new short[nodeCount];



		for(int n =0;n < nodeCount;n++) {
			if (d.getIn(n)!=null) {
				p.heads[n]=(short) d.getIn(d.getIn(n)[1])[1];
				p.labels[n]=(short) d.getContent(d.getIn(n)[1]);

			} else p.heads[n] =-1;
		}

		short[] act = is.heads[ic];
		double correct = 0,wrong=0;

		// do not count root
		for(int i = 1; i < act.length; i++)  	 {
//			if (p.heads[i]==act[i] && p.labels[i]==is.labels[ic][i]) { // && p.types[i]==is.deprels[ic][i]
				if (p.heads[i]==act[i] ) { // && p.types[i]==is.deprels[ic][i]

				//if (p.types[i]==is.deprels[ic][i]) correct+=0.5;
				//else wrong+=0.5;
				//		DB.println("correct "+i+" heads "+p.heads[i]+" >>> act "+act[i]+" i "+i);
				correct+=1;
				//return 0.0;
			} else {


				//	wrong+=1;
			}
			if (p.heads[i]!=act[i]) {
				wrong+=1;
				//	if (p.labels[i]!=is.labels[ic][i]) wrong+=1;
			}
//			if (p.labels[i]!=is.labels[ic][i]) wrong+=0.5;
		}


		return wrong;
	}

	public double errorsE( Instances is, int ic, Graph d) {

		int nodeCount = is.length(ic);

		Parse p = new Parse();
		p.heads = new short[nodeCount];
		p.labels = new short[nodeCount];



		for(int n =0;n < nodeCount;n++) {
			if (d.getIn(n)!=null) {
				p.heads[n]=(short) d.getIn(d.getIn(n)[1])[1];
				p.labels[n]=(short) d.getContent(d.getIn(n)[1]);

			} else p.heads[n] =-1;
		}

		short[] act = is.heads[ic];
		double correct = 0,wrong=0;

		// do not count root
		for(int i = 1; i < act.length; i++)  	 {
			if (p.heads[i]==act[i] && p.labels[i]==is.labels[ic][i]) { // && p.types[i]==is.deprels[ic][i]

				//if (p.types[i]==is.deprels[ic][i]) correct+=0.5;
				//else wrong+=0.5;
				//		DB.println("correct "+i+" heads "+p.heads[i]+" >>> act "+act[i]+" i "+i);
				correct+=1;
				//return 0.0;
			} else {
				if (p.heads[i]!=act[i]) wrong+=0.5;
				if (p.labels[i]!=is.labels[ic][i]) wrong+=0.5;

				//	wrong+=1;
			}

		}


		return wrong;
	}

	/**
	 * @param graph 
	 * @param d
	 * @param pred2
	 */
	public void createVector(Graph p, Graph semGraph, FV f, int dist[][], int[][][] distPath) {

		p.buildIn();

		long[] v = new long[30];
		int len = p.size();
//		FV x = new FV();
		for(int n = 0;n<len;n++) {
			if(p.getType(n)!= IGraph.NODE) break;
			if (p.getIn(n)!=null) {
				if (p.getType(p.getIn(n)[1]) != IGraph.EDGE) {
					System.out.println("type  "+p.getType(p.getIn(n)[1])+" of "+p.getIn(n)[1]+" n "+n);
				}
				int i = p.getIn(p.getIn(n)[1])[1];
				int label = p.getContent(p.getIn(n)[1]);//mf.getValue(Pipe.REL,Environment.getValue
			//	int lab = label<< extractor[0].s_type;
				int lab = 0<< extractor[0].s_type;
				extractor[0].extractFeatures(semGraph, i, n, label, dist,distPath,v);
				//		f.clear();
				for(int k=0;k<v.length;k++) 
					if (v[k]>0) f.add(extractor[0].li.l2i(v[k]));
			
			}

		}


	}

	/**
	 * @param s
	 * @param ts
	 * @param graph
	 * @param act
	 */
	public void createVector(short[] heads, short[] types, Graph semGraph, FV f) {


		long[] v = new long[30];
		int dist[][] = new int[heads.length][heads.length];
		int distPath[][][] = new int[heads.length][][];
		FV x = new FV();
		for(int n  = 0;n<heads.length;n++) {
			if (heads[n]==-1) continue;
			dist[heads[n]] = Extractor.distAll(semGraph, heads[n], heads.length);
			distPath[heads[n]] = Extractor.distPathRel(semGraph, heads[n], heads.length);
			extractor[0].extractFeatures(semGraph, heads[n], n, types[n], dist,distPath, v);
			//int lab = types[n]<<extractor[0].s_type;
			
			int lab =0<<extractor[0].s_type;
			
			for(int k=0;k<v.length;k++) if (v[k]>0) f.add(extractor[0].li.l2i(v[k]|lab));
			
			
			// todo p
		//	extractor[0].extractAdd(null, heads[n],n, types[n], heads, types, f, dist, distPath, v);

		}


	}

}
