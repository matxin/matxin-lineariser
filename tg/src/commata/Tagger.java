package commata;

import is2.data.F2SF;
import is2.data.FV;
import is2.data.Instances;
import is2.data.InstancesTagger;
import is2.data.Long2IntExact;
import is2.data.Long2IntInterface;
import is2.data.PipeGen;
import is2.data.SentenceData09;
import is2.io.CONLLReader09; 
import is2.io.CONLLWriter09;
import is2.tools.IPipe;
import is2.tools.Tool; 
import is2.tools.Train;
import is2.util.DB;
import is2.data.Long2Int;
import is2.util.OptionsSuper;
import is2.data.ParametersFloat;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sem2syn.DSyntConverter.Word;


public class Tagger implements Tool, Train {

	private Pipe pipe;
	private ParametersFloat params;
	private Long2IntInterface li;
	private MFO mf;
	private OptionsSuper _options;

	/**
	 * Initialize 
	 * @param options
	 */
	public Tagger (Options options) {

		
		// load the model
		try {
			readModel(options);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	public Tagger() {	}

	/**
	 * @param modelFileName the file name of the model
	 */
	public Tagger(String modelFileName) {
		this(new Options(new String[]{"-model",modelFileName}));
	}
	
	
	
	static boolean offset = false;
	
	

	public static void main (String[] args) throws FileNotFoundException, Exception
	{

		long start = System.currentTimeMillis();
		Options options = new Options(args);


		Tagger tagger = new Tagger();

		if (options.train) {

			offset =true;
			DB.println("set offset to "+offset+" warning!!!! must be only true in training mode");
			
			//		depReader.normalizeOn=false;

			tagger.li = new Long2Int(options.hsize);
			//tagger.li = new Long2IntExact();
			tagger.pipe =  new Pipe (options, tagger.mf= new MFO());
			tagger.pipe.li = tagger.li; 
			
			//tagger.pipe.li =tagger.li;
	  	    
			InstancesTagger is = (InstancesTagger)tagger.pipe.createInstances(options.trainfile);

			tagger.params = new ParametersFloat(tagger.li.size());

			tagger.train(options, tagger.pipe,tagger.params,is);
			tagger.writeModel(options, tagger.pipe, tagger.params);

		}
		
		DB.println("set offset to "+offset+" warning!!!! must be only true in training mode");
		if (options.test) {

			tagger.readModel(options);
			
			tagger.out(options,tagger.pipe, tagger.params);
		}

		System.out.println();

		if (options.eval) {
			System.out.println("\nEVALUATION PERFORMANCE:");
			Evaluator.evaluate(options.goldfile, options.outfile,options.format);
		}
		long end = System.currentTimeMillis();
		System.out.println("used time "+((float)((end-start)/100)/10));
	}

	public void readModel(OptionsSuper options)  {

		try{
		pipe = new Pipe(options, mf =new MFO());
		_options=options;
		// load the model
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
		zis.getNextEntry();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

		pipe.mf.read(dis);
		pipe.initValues();
		pipe.initFeatures();

		params = new ParametersFloat(0);
		params.read(dis);
		li = new Long2Int(params.parameters.length);

		
		pipe.read(dis);
		this.pipe.li= li;
		options.stack = dis.readBoolean();
		System.out.println("Stacking: "+options.stack);
		dis.close(); 

		pipe.types = new String[pipe.mf.getFeatureCounter().get(Pipe.POS)];
		for(Entry<String,Integer> e : pipe.mf.getFeatureSet().get(Pipe.POS).entrySet()) 
			pipe.types[e.getValue()] = e.getKey();

		DB.println("Loading data finished. ");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Do the training
	 * @param instanceLengths
	 * @param options
	 * @param pipe
	 * @param params
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public void train(OptionsSuper options, IPipe pipe, ParametersFloat params, Instances is2) {

		InstancesTagger is = (InstancesTagger)is2;
		String wds[] = mf.reverse(this.pipe.mf.getFeatureSet().get(Pipe.WORD));
		
		this.pipe.types  = mf.reverse(this.pipe.mf.getFeatureSet().get(Pipe.FM));
		
		int pd[] = new int[this.pipe.types.length];
		for(int k=0;k<pd.length;k++) pd[k]=k;
		
		int del=0; 
		F2SF f = new F2SF(params.parameters); 
		long vs[] = new long[Pipe._MAX];
		long vsp[] = new long[Pipe._MAX];

		int types =this.pipe.types.length;

		double upd = options.numIters*is.size() +1;

		for(int i = 0; i <options.numIters ; i++) {

			long start = System.currentTimeMillis();

			int numInstances = is.size();

			long last= System.currentTimeMillis();
			FV pred = new FV(), gold = new FV();

			int correct =0,count=0;
			System.out.print("Iteration "+i+": ");
			
			for(int n = 0; n < numInstances; n++) {

				if((n+1) % 500 == 0) del= PipeGen.outValueErr(n+1, (count-correct),(float)correct/(float)count,del,last,upd);

				int length = is.length(n);
				
				upd--;
				for(int w = 0; w < length; w++) {
					is.pposs[n][w]=-1;
				}
				for(int w = 0; w < length; w++) {

					double best  = -1000;
					short bestType = -1;

					int[] lemmas; //= is.lemmas[n];
					lemmas = is.glemmas[n];
					
					int  fx = this.pipe.addFeatures(is,n,wds[is.glemmas[n][w]],w,is.glemmas[n],lemmas, vs,true);
		
					
					for(short t=0;t<types;t++) {

						long p = t<<Pipe.s_type;
						f.clear();					
						for(int k1=0;vs[k1]!=Integer.MIN_VALUE;k1++)if (vs[k1]>0) f.add(this.li.l2i(vs[k1]+p));						
									

						if (f.score > best) {
							bestType=t;							
							best =f.score;
						}
					}
					is.pposs[n][w]=(short)best;
					
			//		DB.println("")
					
					count++;
					if (bestType == mf.getValue(Pipe.FM, wds[is.forms[n][w]]) || mf.getValue(Pipe.FM, wds[is.forms[n][w]])==-1 ) {
						correct++;
						continue; 
					}
			//		DB.println("best "+bestType+" "+mf.getValue(Pipe.FM, wds[is.forms[n][w]])+" str  "+
			//				wds[is.forms[n][w]]);
					
					pred.clear();
					int pp = bestType<<Pipe.s_type;
					for (int k1=0;vs[k1]!=Integer.MIN_VALUE;k1++)  if (vs[k1]>0)	pred.add(this.li.l2i(vs[k1]+ pp));
	
					gold.clear();
					int gp = mf.getValue(Pipe.FM, wds[is.forms[n][w]])<<Pipe.s_type;
					for (int k1=0;vs[k1]!=Integer.MIN_VALUE;k1++)  if (vs[k1]>0)	gold.add(this.li.l2i(vs[k1] + gp));

					params.update(pred,gold, (float)upd, 1.0F, 0.00578125F);// 1.0F  0.0057
				}
			} 

			long end = System.currentTimeMillis();
			String info = "time "+(end-start);
			PipeGen.outValueErr(numInstances, (count-correct),(float)correct/(float)count,del,last,upd,info);
			System.out.println();
			del=0;
		}

		if (options.average) params.average(options.numIters*is.size());

	}


	/**
	 * Tag a sentence
	 * @param options
	 * @param pipe
	 * @param params
	 * @throws IOException
	 */
	public void out (OptionsSuper options, IPipe pipe, ParametersFloat params) {
		
		try {
	
			this.pipe.types  = mf.reverse(this.pipe.mf.getFeatureSet().get(Pipe.FM));

		long start = System.currentTimeMillis();

		CONLLReader09 depReader = new CONLLReader09(options.testfile, CONLLReader09.NO_NORMALIZE);
		CONLLWriter09 depWriter = new CONLLWriter09(options.outfile);

		System.out.print("Processing Sentence: ");
		pipe.initValues();

		int cnt = 0;
		int del=0;
		while(true) {

			InstancesTagger is = new InstancesTagger();
			is.init(1, mf);
			SentenceData09 instance = depReader.getNext(is);
			if (instance == null || instance.forms == null)	 break;
			
			
			is.fillChars(instance, 0, instance.lemmas,Pipe._CEND);

			cnt++;

				short[] pos = tag(is, instance,options.stack);

				ArrayList<sem2syn.DSyntConverter.Word> snt = new ArrayList<sem2syn.DSyntConverter.Word>();
				ArrayList<sem2syn.DSyntConverter.Word> snt2 = new ArrayList<sem2syn.DSyntConverter.Word>();
				for(int n=0; n<instance.lemmas.length;n++) {
					Word w = new Word();
					snt.add(w);
				}

				for(int n=0; n<instance.lemmas.length;n++) {

					if (instance.heads[n]>=0)snt.get(n).head = snt.get(instance.heads[n]);
			
					snt.get(n).plable = instance.plabels[n];
					snt.get(n).lable = instance.labels[n];
					snt.get(n).feats= instance.ofeats[n];
					snt.get(n).lemma = instance.lemmas[n];
					snt.get(n).gpos = instance.gpos[n];
					snt.get(n).ppos = instance.ppos[n];
					
					//System.out.println("lemma "+instance.lemmas[n]);
				}
				
				int loc=0;
				
				for(int k=1;k<snt.size()+1;k++) {
					snt2.add(snt.get(loc));
					if (pos[k-1]==mf.getValue(Pipe.FM, ",")) {
						Word w = new Word();
						w.ppos=",";
						w.gpos=",";
						w.lemma=",";
						w.head=snt.get(loc);
						w.phead=snt.get(loc);
						w.lable="P";
						w.plable="P";
						
						snt2.add(w);
						
					}
					loc++;
				}

				
				
				
				
				
				
				
				SentenceData09 dlin = new SentenceData09();
				dlin.forms = new String[snt2.size()-1];
				dlin.lemmas= new String[snt2.size()-1];
				dlin.init(dlin.forms);


				dlin.plabels = new String[dlin.length()];
				dlin.pheads= new int[dlin.length()];

				dlin.pfeats= new String[dlin.length()];
				dlin.ofeats= new String[dlin.length()];

				for(int k=0;k<dlin.length();k++) dlin.forms[k]="_";

				dlin.gpos = new String[dlin.length()];

				for(int k=0;k<dlin.length();k++) dlin.gpos[k]="_";


				for(int j = 0; j < snt2.size()-1; j++) {
					Word w = snt2.get(j+1);
					dlin.gpos[j]=w.gpos;
					dlin.ppos[j]=w.gpos;
					dlin.plabels[j] = w.plable;
					dlin.pheads[j] = snt.indexOf(w.head);
					dlin.labels[j] =w.lable;
					dlin.heads[j]  =snt2.indexOf(w.head);
					dlin.lemmas[j] =w.lemma;
					dlin.ofeats[j] = w.feats;
					dlin.pfeats[j] = w.feats;
				
			//		dlin.pfeats[j] =shallowdlin.feats[j+1];
				}
				
			
			
		//	SentenceData09 i09 = new SentenceData09(instance);
			dlin.createSemantic(instance);
			depWriter.write(dlin);

			if(cnt % 100 == 0) del=PipeGen.outValue(cnt, del);

		}
		del=PipeGen.outValue(cnt, del);
		depWriter.finishWriting();

	
		
		long end = System.currentTimeMillis();
		System.out.println(PipeGen.getSecondsPerInstnace(cnt,(end-start)));
		System.out.println(PipeGen.getUsedTime(end-start));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	public SentenceData09 tag(SentenceData09 instance, OptionsSuper options){
		InstancesTagger is = new InstancesTagger();
		is.init(1, pipe.mf);
		new CONLLReader09().insert(is, instance);
		is.fillChars(instance, 0, Pipe._CEND);
		tag(is, instance,options.stack);

		return instance;
	}


	static int three=0,total=0;
	
	
	private short[] tag(InstancesTagger is, SentenceData09 instance, boolean stack) {

		int length = instance.ppos.length;

		short[] pos = new short[instance.gpos.length];
	//	short pos[] =is.pposs[0];
		float sc[] =new float[instance.ppos.length];
		
		instance.ppos[0]= is2.io.CONLLReader09.ROOT_POS;
		pos[0]=(short)pipe.mf.getValue(Pipe.POS, is2.io.CONLLReader09.ROOT_POS);

		float[][] w = new float[pos.length][this.pipe.types.length];
		
		
		
		
	//	w = new float[pos.length][this.pipe.types.length];
		for(int j = 0; j < length; j++) {
			short bestType = (short)pipe.fillFeatureVectorsOne( instance.lemmas[j],params, j, is,0,pos,this.li,sc,w,offset);
			bestType = (short)pipe.adaptFV( params,j, is, 0,is.pposs[0],this.li,w, stack);
			pos[j] = bestType;
		//	instance.ppos[j]= pipe.types[bestType];
		}
		return pos;
	//	w = new float[pos.length][this.pipe.types.length];
/*
		for(int j = 0; j < length; j++) {
			short bestType = (short)pipe.fillFeatureVectorsOne( instance.lemmas[j],params, j, is,0,pos,this.li,sc,w,true);
			bestType = (short)pipe.adaptFV( params,j, is, 0,pos,this.li,w,stack);
			pos[j] = bestType;
//			instance.fillp[j]= pipe.types[bestType];
			instance.ppos[j]= pipe.types[bestType];
		//	instance.lemmas[j]= pipe.types[bestType];
		}
	*/	
	
		
		
		
	//	System.out.println("t "+total+" "+three);
	
		
//		for(int j = 1; j < length; j++) {

//			short bestType = (short)pipe.fillFeatureVectorsOne(instance.forms[j],params, j, is,0,pos,this.li,sc);
//			instance.ppos[j]= pipe.types[bestType];
//			pos[j]=bestType;			
	//	}
	}
/*
	class T implements Comparable<T> {
		float w;
		int t;
		
				public T(int i, float f) {
			this.t=i;
			this.w=f;
		}

		
		@Override
		public int compareTo(T o) {
			// TODO Auto-generated method stub
			return w>o.w?-1:w<o.w?1:0;
		}
	}
	*/
	
	/**
	 * @param w
	 * @return
	
	private ArrayList<T> getBestThree(float[][] w, int x) {
	
		ArrayList<T> tags = new ArrayList<T>();
		for(int i=0;i<w[0].length;i++) {
			tags.add(new T(i,w[x][i]));
		}
		Collections.sort(tags);
		
		return tags;
	}
	
 */
	public SentenceData09 apply(SentenceData09 snt09) {
		tag(snt09,_options);
		return snt09;
	}
	
	
	
	/* (non-Javadoc)
	 * @see is2.tools.Train#writeModel(is2.util.OptionsSuper, is2.mtag2.Pipe, is2.data.ParametersFloat)
	 */

	public void writeModel(OptionsSuper options, IPipe pipe, is2.data.ParametersFloat params) {
		try{
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));
			zos.putNextEntry(new ZipEntry("data")); 
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

			this.pipe.mf.writeData(dos);

			DB.println("number of parameters "+params.parameters.length);
			dos.flush();

			params.write(dos);
			pipe.write(dos);
			dos.writeBoolean(options.stack);

			dos.flush();
			dos.close();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
