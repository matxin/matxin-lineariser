package dsynt2synt;




import is2.data.FV;
import is2.data.Instances;
import is2.data.PipeGen;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.util.DB;
import is2.util.OptionsSuper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class Mapper  {

	Pipe pipe;
	ParametersFloat params;


	/**
	 * Initialize 
	 * @param options
	 */
	public Mapper (Options options) {

		// load the model
		try {
			readModel(options);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param string
	 * @throws IOException 
	 */
	public Mapper(String modelFileName) {
		this(new Options(new String[] {"-model",modelFileName}));
	}

	public Mapper() {	}

	public static void main (String[] args) throws FileNotFoundException, Exception
	{

		Options options = new Options(args);

		Mapper mapper = new Mapper();

		if (options.train) {

			Long2Int li = new Long2Int(options.hsize);
			mapper.pipe =  new Pipe (options,li);
			Instances[] is = mapper.pipe.createInstances(options.trainfile,options.synt);
			ParametersFloat params = new ParametersFloat(li.size());

			mapper.train(options, mapper.pipe,params,is);
			mapper.writeModel(options, mapper.pipe, params);
		}

		if (options.test) {

			mapper.readModel(options);
			mapper.out(options,mapper.pipe, mapper.params);
		}

		if (options.eval) {

			//		System.out.println("\nEvaluate:");
			//		Evaluator.evaluate(options.goldfile, options.outfile,options.format);
		}
	}
	public void writeModel(OptionsSuper options, Pipe pipe,ParametersFloat params)  {

		try {
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));
			zos.putNextEntry(new ZipEntry("data")); 
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

			MFO.writeData(dos);

			MFO.clearData();

			DB.println("number of parameters "+params.parameters.length);
			dos.flush();
			params.write(dos);
			pipe.write(dos);
			dos.flush();
			dos.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}


	public void readModel(OptionsSuper options) {

		try {
			pipe = new Pipe(options);
			params = new ParametersFloat(0);

			// load the model
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
			zis.getNextEntry();
			DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));
			pipe.mf.read(dis);
			pipe.initValues();
			pipe.initFeatures();

			params.read(dis);
			pipe.li = new Long2Int(params.parameters.length);
			pipe.readMap(dis);
			dis.close();

			this.pipe.types = new String[pipe.mf.getFeatureCounter().get(Pipe.RULE)];
			for(Entry<String,Integer> e :pipe.mf.getFeatureSet().get(Pipe.RULE).entrySet()) 
				this.pipe.types[e.getValue()] = e.getKey();


			DB.println("Loading data finished. ");

			DB.println("number of parameter "+params.parameters.length);		
			DB.println("number of classes   "+this.pipe.types.length);		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void train(OptionsSuper options, Pipe pipe, ParametersFloat params, Instances[] is)  {

		int i = 0;
		int del=0; 

		this.pipe.types = this.pipe.mf.reverse(this.pipe.mf.getFeatureSet().get(Pipe.RULE));
		int numInstances = is[Pipe.DEEP].size();

		float upd = (options.numIters*numInstances + 1);

		FV pred = new FV(), gold = new FV();
		for(i = 0; i < options.numIters; i++) {

			long start = System.currentTimeMillis();

			int correctNodes=0,allNodes=0;

			long last= System.currentTimeMillis();

			int correct =0,count=0;

			for(int n = 0; n < numInstances; n++) {

				upd--;

				if((n+1) % 100 == 0) del= PipeGen.outValueErr(n+1, (count-correct),(float)correct/(float)count,del,last,upd);

				//int length = is[Pipe.DEEP].length(n);

				//	long[] vs = new long[Pipe._FC]; 

				int[][] rules = Decoder.decode(is,n,this.pipe,params,upd);

				for(int rs = 0; rs<rules[0].length;rs++) {
					for(int k=0;k<rules.length;k++) {

						count++;

						if (rules[k][rs]==is[Pipe.DEEP].arg[n][k][rs])  correct++;


					}

					// traverse top down the dependency tree an map (depth first)
					//				correctNodes +=(int)acc.correct;
					//				allNodes +=acc.count;
				}
			}

			long end = System.currentTimeMillis();
			String info = " time "+(end-start);
			del= PipeGen.outValueErr(numInstances, (count-correct),(float)correct/(float)count,del,last,0,info);

			//			System.out.println();			
			System.out.println("nodes "+count+" correct ones "+correct+" acc "+((float)correct)/(float)count);

		}

		params.average(i*is[Pipe.DEEP].size());

	}


	public void out (Options options, Pipe pipe, ParametersFloat params)  {


		try {
			long start = System.currentTimeMillis();
			long last=start;

			CONLLReader09 depReader = new CONLLReader09(options.testfile, options.formatTask);

			CONLLReader09 surfReader = null;

			if (options.goldfile!=null)surfReader= new CONLLReader09(options.goldfile, options.formatTask);


			CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);

			//		depReader.normalizeOn=false;

			System.out.print("Processing Sentence: ");
			pipe.initValues();

			
			
			int cnt = 0;
			int del=0;
			int count=0, correct=0, all=0, none=0,pre=0,rec=0;
			//int correctNodes=0, count=0;

			HashMap<Integer, HashMap<Integer,Integer>> conf = new  HashMap<Integer, HashMap<Integer,Integer>>();

			while(true) {

				Instances[] is = new Instances[2];

				is[Pipe.DEEP] = new Instances();
				is[Pipe.SURF] = new Instances();

				is[Pipe.DEEP].init(1, this.pipe.mf);
				is[Pipe.SURF].init(1, this.pipe.mf);


				SentenceData09 deep = depReader.getNext();

				
				// delete the ids
				
						  
				
				
				SentenceData09 surf = null;

				if (surfReader!=null) surf = surfReader.getNext(is[Pipe.SURF]);

				if (deep == null || deep.forms == null)	 break;
				
				for(int k=0;k<deep.length();k++) {
					
					if (deep.feats[k]!=null && deep.feats[k].length>0 && deep.feats[k][0]!=null && deep.feats[k][0].startsWith("id=")) {
						
						String feats[] = new String[deep.feats[k].length-1];
						for(int j=1;j< deep.feats[k].length;j++) {
							feats[j-1]=deep.feats[k][j];
						}
						deep.feats[k]=feats;
					}
					
				}
				

				depReader.insert(is[Pipe.DEEP], deep);

				if (surfReader!=null) {
					this.pipe.deriveRules(deep, surf, new HashMap<String,Integer>());

					is[0].arg[0]=new short[is[0].length(0)][deep.arg[0].length];

					for(int ruleset=0;ruleset< deep.arg[0].length;ruleset++)
						for(int k=1;k<deep.length();k++) {
							is[0].arg[0][k][ruleset] =(short)pipe.mf.getValue(Pipe.RULE,deep.arg[k][ruleset]==null?"":deep.arg[k][ruleset]);
							if (is[0].arg[0][k][ruleset]==-1) is[0].arg[0][k][ruleset] = (short)pipe.mf.getValue(Pipe.RULE,""); 
						}

				}
				cnt++;

				int[][] rules = Decoder.decode(is, 0, pipe, params, -1);

				SentenceData09 outSnt = Decoder.apply(rules, deep, Pipe.types);


				depWriter.write(outSnt);
				//	count++;

				boolean out=false;

				int good =0;

				if (out) System.out.println("snt "+cnt+":\n");


				if(surfReader!=null)
					for(int ruleset =0;ruleset<rules[0].length;ruleset++){
						for(int k=1;k<deep.length();k++) {
							count++;

							if (out)System.out.print(deep.lemmas[k]+"\t\t"+deep.gpos[k]+"\t"+deep.heads[k]+"\t"+deep.labels[k]+"\t"+pipe.types[rules[k][ruleset]]+"\t\t\t");


							if (rules[k][ruleset]==is[Pipe.DEEP].arg[0][k][ruleset]) correct++;

							if (is[Pipe.DEEP].arg[0][k][ruleset]==pipe.mf.getValue(Pipe.RULE, "")) none++;

							if (is[Pipe.DEEP].arg[0][k][ruleset]!=pipe.mf.getValue(Pipe.RULE, "")||
									pipe.mf.getValue(Pipe.RULE, "")==rules[k][ruleset]) pre++;


							if (pipe.mf.getValue(Pipe.RULE, "")!=rules[k][ruleset]) rec++;


							if (rules[k][ruleset]!=is[Pipe.DEEP].arg[0][k][ruleset]) {

								//confusion

								HashMap<Integer,Integer> map = conf.get((int)is[Pipe.DEEP].arg[0][k][ruleset]);
								if (map==null) {
									map = new HashMap<Integer,Integer>();
									conf.put(((int)is[Pipe.DEEP].arg[0][k][ruleset]), map);

								}
								map.put(rules[k][ruleset], map.get(rules[k][ruleset])==null?1:map.get(rules[k][ruleset])+1);

								//						DB.println("conf "+rules[k][ruleset]+" "+is[Pipe.DEEP].arg[0][k][ruleset]+" "+map.get(rules[k][ruleset]));


								if (out) if (is[Pipe.DEEP].arg[0][k][ruleset]!=-1) System.out.print("correct:'"+pipe.types[is[Pipe.DEEP].arg[0][k][ruleset]]+"'"+" "+is[Pipe.DEEP].arg[0][k][ruleset]);
								else System.out.print("correct:-1");
							} else {

							}

							if (out)	System.out.println();

						}
					}
				if (out)System.out.println();

				String info1 ="nodes "+count+" correct  "+correct+" acc "+((float)correct)/(float)count+" none "+none+" to do "+(count-none)+" done "+(correct-none);
				if((cnt+1) % 100 == 0) del=PipeGen.outValueErr(cnt, count-correct, 0, del, last, 0, info1);




			}



			long end = System.currentTimeMillis();

			for(Entry<Integer,HashMap<Integer,Integer>> e :conf.entrySet()) {

				int grule = e.getKey();
				for(Entry<Integer,Integer> e2 :e.getValue().entrySet()) {
					//	if (grule==e2.getKey()) DB.println("error !!!"+grule+" "+e2.getKey());
					if (grule==-1)DB.println("error !!!"+grule+" "+e2.getKey());
					else 
						System.out.println(e2.getValue()+"\t\t"+pipe.types[grule]+" \t\t "+pipe.types[e2.getKey()]);
				}



			}
			String rul[] = pipe.mf.reverse( pipe.mf.getFeatureSet().get(Pipe.RULE));

			for(int k =0;k< rul.length;k++) {
				System.out.println(k+"\t"+rul[k]);
			}

			//
			depWriter.finishWriting();

			String info1 ="nodes "+count+" correct  "+correct+" acc "+((float)correct)/(float)count+" none "+none+" to do "+(count-none)+" done "+(correct-none);
			del=PipeGen.outValueErr(cnt, count-correct, 0, del, last, 0, info1);
			System.out.println(" correct "+((float)(correct-none))/(float)(count-none)+" rec "+rec+" pre "+pre);
		} catch(Exception e){
			e.printStackTrace();
		}
	}



}
