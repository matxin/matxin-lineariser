package syn2lin2b;

import is2.data.Cluster;
import is2.data.Edges;
import is2.data.F2SF;
import is2.data.FV;
import is2.data.Instances;
import is2.data.MFO;
import is2.data.Parse;
import is2.data.PipeGen;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.util.OptionsSuper;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import rt.model.Graph;

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

	
	//HashMap<Integer,Integer>  lemma2Predicate = new HashMap<Integer,Integer>() ;

	
	final public MFO mf = new MFO();

	private OptionsSuper options;
	private Integer corpusWrds;
	Cluster cl;
	public static long timeExtract;
	public static String[] types;
	
	public Pipe(OptionsSuper o) {
		options = o;
	}

	public void createInstances(String file, File featFileName, int task, Instances is)
	throws Exception {

		CONLLReader09 depReader = new CONLLReader09(file,task);

		mf.register(REL, "<root-type>");
		
		// register at least one predicate since the parsing data might not contain predicates as in 
		// the Japaness corpus but the development sets contains some

		long sl=0;
		
		
		
		
		System.out.print("Registering feature parts of sentence: ");
		int ic = 0;
		int del = 0;
		while (true) {
			SentenceData09 instance = depReader.getNext();
			if (instance == null)
				break;
			ic++;

			sl+=instance.labels.length;
			
			if (ic % 1000 == 0) {
				del = outValue(ic, del);
			}

			String[] labs1 = instance.labels;
			for (int i1 = 0; i1 < labs1.length; i1++) mf.register(REL, labs1[i1]);

			String[] w = instance.forms;
			for (int i1 = 0; i1 < w.length; i1++) mf.register(WORD, depReader.normalize(w[i1]));

			w = instance.lemmas;
			for (int i1 = 0; i1 < w.length; i1++) mf.register(WORD, depReader.normalize(w[i1]));
			

			w = instance.ppos;
			for (int i1 = 0; i1 < w.length; i1++) mf.register(POS, w[i1]);

			w = instance.gpos;
			for (int i1 = 0; i1 < w.length; i1++) mf.register(POS, w[i1]);
	
			if (instance.feats !=null) {
			String fs[][] = instance.feats;
			for (int i1 = 0; i1 < fs.length; i1++){	
				w =fs[i1];
				if (w==null) continue;
				for (int i2 = 0; i2 < w.length; i2++) mf.register(FEAT, w[i2]);
			}
			}

			
			
			
			 if ((ic-1)>options.count) break;
		}
		System.out.println("words in corpus "+(corpusWrds=mf.getFeatureCounter().get(Pipe.WORD)));
		if (options.clusterFile==null)cl = new Cluster();
		else cl=  new Cluster(options.clusterFile, mf,6);
		
		del = outValue(ic, del);

		System.out.println();
		//initFeatures();
		Extractor.initFeatures(mf);

		
		mf.calculateBits();
		Extractor.init(mf);
		for(Extractor e : extractor) e.init();


		depReader.startReading(file);

		System.gc();

		long mem1 = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		System.out.println("Memory used so far " + (mem1 / 1024) + " kb");
		int num1 = 0;
		long start1 = System.currentTimeMillis();

		is.init(ic, mf);

		String[] posx = new String[mf.getFeatureCounter().get(POS)];

		for (Entry<String, Integer> e : mf.getFeatureSet().get(POS).entrySet()) {
			posx[e.getValue()] = e.getKey();
		}

		Edges .init(posx.length);
		System.out.print("Creating Features: ");

		del = 0;
		
		//FV f =new FV();
		while (true) {
		//	if (num1 % 100 == 0)  
				
				del = outValue(num1, del);
			
			SentenceData09 instance1 = depReader.getNext(is);
	

			if (instance1 == null) break;

			int last = is.size() - 1;
			short[] pos =is.pposs[last];
			
			for (int k = 0; k < is.length(last); k++) {
				if (is.heads[last][k] < 0)	continue;
				edges.put(pos[is.heads[last][k]],pos[k], k < is.heads[last][k],is.labels[last][k]);
			}
			
			if (!options.allFeatures && num1 > options.count) break;

			num1++;

		}
		del = outValue(num1, del);

		num1 = 0;


		long end1 = System.currentTimeMillis();

		System.gc();
		long mem2 = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		System.out.print(" time " + (end1 - start1) + " mem " + (mem2 / 1024)+ " kb ");

		String[] types = new String[mf.getFeatureCounter().get(REL)];

		for (Entry<String, Integer> e : mf.getFeatureSet().get(REL).entrySet())  	types[e.getValue()] = e.getKey();
		
		//
		edges.findDefault();

		//mf.stop();
		
	//	extractor.put=false;

		System.out.print(" processed "+is.size());
	
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
	
	public double errors( Instances is, int ic, Graph d) {
		
		int nodeCount = is.length(ic);
		
		d.buildIn();
		int wrong=0;
		for(int n=0;n<nodeCount;n++) {
			
			if (is.heads[ic][n]==-1) continue;

			int in[] = d.getIn(n);
			if(n < is.heads[ic][n]) {
				
				if (d.getContent(in[1])!=1) {
					
					wrong++;
					
			//		System.out.println("left "+n+" "+is.heads[ic][n]);
				}
				
				
			} else {

				if (d.getContent(in[1])!=2) wrong++;
				
				
			}
			
		}
		 
		return wrong;
	}


	
		
	public double errors1(int[]nodes) {
		
		if (nodes.length==1) return 0;
		double errors=0;
		for (int i=0;i<nodes.length-1;i++) {
	//		System.out.println(""+nodes[i]+" "+nodes[i+1]);
			if (nodes[i]>nodes[i+1]) {
			//	System.out.println(" error "+nodes[i]+" "+nodes[i+1]);
				errors++ ;
			}
		}
		
		return errors;
	}

	
	
	

	
	public DataNN fillVector(F2SF f, Instances is, int inst, DataNN d, int nds[]) {
		
		long ts = System.nanoTime();
		
		final int length = is.length(inst);
		if (d ==null || d.len<length) d = new DataNN(length);
	
		
		for(int w1 : nds) {
			for(int w2 : nds) {
				if(w1==w2) continue;
				
				
						
					f.clear();
					extractor[0].extractFeaturesX(is, inst, w1, w2, 1,3, 3, f);
					d.abh[w1][w2]= f.getScoreF();
			
//					for(int w3 : nds) {
//						if(w1==w2||w1==w3||w2==w3) continue;
//					
//						f.clear();
//						extractor[0].extractTrigrams(is, inst, w1, w2,w3, f);
//						d.trigrams[w1][w2][w3]= f.getScoreF();
//					}
				
			}
		}
		
		
		
		timeExtract += (System.nanoTime()-ts);
	
		return d;
	}
	
		
	
	/**
	 * @param is
	 * @param nds
	 * @param pred2
	 */
	public void createVector1(Instances is, int n, int[] nodes, int head,FV f) {
	

		
		
		for(int i=0;i<nodes.length-1;i++){

			for(int j =i+1;j<nodes.length;j++) {
			
				extractor[0].extractFeaturesX(is, n, nodes[i], nodes[j],1,3,3, f);	
			}
			if(nodes.length>1&&i>0)
				extractor[0].extractTrigrams(is, n, nodes[i-1] ,nodes[i], nodes[i+1], i-1, f);	
			
		}
		extractor[0].extractGlobal(is, n, nodes, f);
		
	}

}
