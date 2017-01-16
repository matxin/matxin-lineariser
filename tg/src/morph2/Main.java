package morph2;

import is2.data.*;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;

import is2.util.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;



public class Main {

	Long2IntInterface long2int;
	OptionsSuper options;


	/**
	 * The main method
	 * @param args the command line arguments 
	 * @throws Exception 
	 */
	public static void main (String[] args) throws Exception 
	{

		long start = System.currentTimeMillis();
		OptionsSuper options = new Options(args);


		if (options.train) {

			Long2IntInterface long2int = new Long2Int(options.hsize);

			//Pipe pipe =  new Pipe (options);
			InstancesTagger is = new InstancesTagger();

			Extractor extractor = new Extractor(long2int);
			Extractor.initFeatures();
			DB.println("icon "+options.count);
			Pipe.createInstances(options.trainfile,options.formatTask,is,options.count);

			Extractor.initMappings();
			extractor.init();

			ParametersFloat params = new ParametersFloat(long2int.size());

			train(options,params,is,extractor);


			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));
			zos.putNextEntry(new ZipEntry("data")); 
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

			Pipe.mf.writeData(dos);

			MFO.clearData();

			DB.println("Data cleared ");

			params.write(dos);

			Pipe.writeMap(dos);
			//for() 

			dos.writeUTF(""+Main.class.toString());
			dos.flush();
			dos.close();
			DB.println("Writting data finished ");

		}

		if (options.test) {

			Pipe pipe = new Pipe(options);
			ParametersFloat params = new ParametersFloat(0);  // total should be zero and the parameters are later read

			// load the model

			Extractor extractor = readAll(options, pipe, params);


			outputParses(options,pipe, params,extractor);
		}

		System.out.println();

		if (options.eval) {
			System.out.println("\nEVALUATION PERFORMANCE:");
			Evaluator.evaluate(options.goldfile, options.outfile);
		}

		long end = System.currentTimeMillis();
		System.out.println("used time "+((float)((end-start)/100)/10));
	}


	public  static Extractor  readAll(OptionsSuper options, Pipe pipe,ParametersFloat params) throws IOException {


		DB.println("Reading data started");

		// prepare zipped reader
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
		zis.getNextEntry();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

		new MFO().read(dis);

		params.read(dis);
		Long2IntInterface long2int = new Long2Int(params.size());
		DB.println("li size "+long2int.size());


		Extractor extractor = new Extractor(long2int);

		//		for (int t=0;t<pipe.extractor.length;t++) pipe.extractor[t]=new Extractor(pipe.mf,long2int);

		Extractor.initFeatures();
		Extractor.initMappings();
		extractor.init();
		Pipe.readMap(dis);

		dis.close();

		DB.println("Reading data finnished");

		//		Pipe.types = new String[pipe.mf.getFeatureCounter().get(Pipe.REL)];
		//		for(Entry<String,Integer> e : pipe.mf.getFeatureSet().get(Pipe.REL).entrySet())  Pipe.types[e.getValue()] = e.getKey();

		Extractor.initMappings();

		return extractor;
	}



	/**
	 * Do the training
	 * @param instanceLengths
	 * @param options
	 * @param pipe
	 * @param params
	 * @param is 
	 * @param extractor 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	static public void train(OptionsSuper options, ParametersFloat params, InstancesTagger is, Extractor extractor) 
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


		String words[] =MFO.reverse(MFO.getFeatureSet().get(Pipe.WORD));	

		String operations[] =MFO.reverse(MFO.getFeatureSet().get(Pipe.OPERATION));	

		int LC = operations.length+1, UC = LC+1;
		int wrongUC=0,correctUC=0, allUC=0;

		for(; iter < options.numIters; iter++) {

			System.out.print("Iteration "+iter+": ");

			long start = System.currentTimeMillis();

			long last= System.currentTimeMillis();
			error=0;
			f1=0;

			//	DB.println("num instances "+ numInstances);


			for(int n = 0; n < numInstances; n++) {

				upd--;

				if (is.labels[n].length>options.maxLen) continue;

				String info = " te "+(((float)Pipe.timeExtract)/1000000F)+" wrong/correc/#uc UC "+wrongUC+"/"+correctUC+"/"+allUC+"   ";

				if((n+1) %500 == 0)  del= Pipe.outValueErr(n+1,Math.round(error*1000)/1000,f1/n,del, last, upd,info);

				int uc= MFO.getValueS(Pipe.WORD, "_");

				for(int k=1;k<is.length(n);k++) {



					String lemma = words[is.glemmas[n][k]];
					if (uc==is.glemmas[n][k]) lemma = words[is.glemmas[n][k]];

					// DB.println("lemma"+k+" is zero "+lemma);


					if (Pipe.lemma2form.get(lemma)!=null) 	continue;
					


					dataNN = extractor.fillVector(operations,  k, (F2SF)params.getFV(),  is, n, lemma, dataNN);

					int predictedOp=1;
					double best=-10000;

					for(int o =0;o<operations.length;o++) {

						if (dataNN.abh[o][0]> best) {
							best = dataNN.abh[o][0];
							predictedOp=o;
						}
					}

					String pform = StringEdit.change(lemma.toLowerCase(), operations[predictedOp]);

					String s = new StringBuffer(lemma).reverse().toString().toLowerCase();
					//
					String form = words[is.forms[n][k]];
					String t = new StringBuffer(form).reverse().toString().toLowerCase();

					StringBuffer goldOperations = new StringBuffer();


					if (!s.equals(t)) {

						int[][] d =StringEdit.LD(s, t);
						StringEdit.searchPath(s,t,d, goldOperations, false);

					} else   goldOperations.append("0"); // do nothing

					String go = goldOperations.toString();
					int gold =-1;

					//	DB.println("number of gold"+operations.length);
					for(int j=0;j< operations.length;j++) {
						if(operations[j].equals(go)) {
							gold =j;
							break;
						}
					}

					double e = form.toLowerCase()  .equals(pform)?0:1;

					if (e>0) {			

						error += e;

						// create the predicted feature vector
						pred.clear();
						extractor.createVector(is,n,k, operations[predictedOp], lemma, pred);

						// create the gold feature vector					
						act.clear();
						extractor.createVector(is,n,k, go, lemma,  act);
						params.update(act, pred, is, n, upd,e);					

					}






					// upper case
					//ptions.upper
					if (true) {

						//	extractor.createVector(is,n,k, operations[predictedOp], lemma, pred);
						FV ucf = new FV();

						long vs[] = new long[200];
						extractor.extractFeatures(is, n, k, 0,lemma, ucf,vs); 
						ucf.clear();
						for(int l=vs.length-1;l>=0;l--) if (vs[l]>0) ucf.add(extractor.li.l2i(vs[l]+(LC*Extractor.s_type)));

						float ucscore = params.getScore(ucf);

						ucf.clear();
						for(int l=vs.length-1;l>=0;l--) if (vs[l]>0) ucf.add(extractor.li.l2i(vs[l]+(UC*Extractor.s_type)));
						float lcscore = params.getScore(ucf);

						//	extractor.createVector(is,n,k, LC, lemma,  ucf);
						int correctOP =-1, selectedOP =-1;	
						if (words[is.forms[n][k]].length()>0 &&
								Character.isUpperCase(words[is.forms[n][k]].charAt(0)) &&
								ucscore > lcscore) {

							correctOP = UC;
							selectedOP =LC;
						}  else if (words[is.forms[n][k]].length()>0 
								&&Character.isLowerCase(words[is.forms[n][k]].charAt(0)) &&
								ucscore <= lcscore) {


							correctOP = LC;
							selectedOP =UC;
						}

						if (correctOP!=-1 && words[is.forms[n][k]].length()>0) {

							wrongUC++;
							FV f = new FV();
							for(int l=vs.length-1;l>=0;l--) if (vs[l]>0) f.add(extractor.li.l2i(vs[l]+(selectedOP*Extractor.s_type)));

							FV g = new FV();
							for(int l=vs.length-1;l>=0;l--) if (vs[l]>0) g.add(extractor.li.l2i(vs[l]+(correctOP*Extractor.s_type)));


							//							FV dist = g.getDistVector(f);		
							params.update(g, f, is, n, upd,1.0f);					

							//							dist.update(params., params.total, params.update(dist,loss), upd,false); 

						} else {
							correctUC++;
						}

						if (Character.isUpperCase(words[is.forms[n][k]].charAt(0))) allUC++;

					}

				}

			}



			String info = " te "+(((float)Pipe.timeExtract)/1000000F)+" wrong/correc/all UC "+wrongUC+"/"+correctUC+"/"+allUC+"   ";
			Pipe.outValueErr(numInstances,Math.round(error*1000)/1000,f1/numInstances,del,last, upd,info);
			del=0;
			wrongUC=0;correctUC=0;allUC=0;
			Pipe.timeExtract=0;
			long end = System.currentTimeMillis();

			System.out.println(" time:"+(end-start));			

		}
		params.average(iter*is.size());
	}                                   


	static int changed =0;

	/**


	/**
	 * Do the parsing
	 * @param options
	 * @param pipe
	 * @param decoder
	 * @param params
	 * @throws IOException
	 */
	static public void outputParses (OptionsSuper options, Pipe pipe, ParametersFloat params, Extractor extractor) 
	throws Exception {

		long start = System.currentTimeMillis();

		CONLLReader09 depReader = new CONLLReader09(options.testfile, false);
		//	CONLLReader09.normalizeOn=false;

		CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);
		BufferedWriter tf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(options.outfile+".xml"),"UTF8"));

		Extractor.initFeatures();

		int cnt = 0;
		int del=0;
		long last = System.currentTimeMillis();

		System.out.println("\nParsing Information ");
		System.out.println("------------------- ");


		System.out.print("Processing Sentence: ");

		DataNN dataNN = new DataNN(100);

		String operations[] =MFO.reverse(MFO.getFeatureSet().get(Pipe.OPERATION));	
		int LC = operations.length+1, UC = LC+1;


		//	tf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		//	tf.write("<!DOCTYPE mteval SYSTEM \"ftp://jaguar.ncsl.nist.gov/mt/resources/mteval-xml-v1.5.dtd\">\n");
		tf.write("<mteval>");
		tf.write("<tstset setid=\"sample_set\" srclang=\"en\"  SysID=\"BU-1.1\">\n"); 	
		tf.write("<doc sysid=\"tst\" docid=\"1\" genre=\"nw\">\n");


		while(true) {

			InstancesTagger is = new InstancesTagger();
			is.init(1, new MFO());

			SentenceData09 instance = depReader.getNext(is);
			if (instance==null || instance.forms == null) break;

			
			is.fillChars(instance, 0,instance.lemmas,   Extractor._CEND);
			cnt++;
			int n=0;

			String[] formsNoRoot = new String[instance.forms.length-1];



			for(int k=1;k<is.length(n);k++) {




				String lemma = instance.lemmas[k];//;words[is.glemmas[n][k]];
				if (lemma.equals("_")) {


					//		System.out.println("lemma "+lemma+" "+instance.lemmas[k]);

					lemma =instance.lemmas[k];

				}
				String pform;
				//		System.out.println("lemmas "+lemma);
				if (pipe.lemma2form.get(lemma)==null) {


					dataNN = extractor.fillVector(operations,  k, (F2SF)params.getFV(),  is, n, lemma,dataNN);

					int predictedOp=1;
					double best=-1000;

					for(int o =0;o<operations.length;o++) 
					{
						if (dataNN.abh[o][0]> best) {
							best = dataNN.abh[o][0];
							predictedOp=o;
						}
					}

					pform = StringEdit.change(lemma, operations[predictedOp]);


					formsNoRoot[k-1]=pform;




					//	extractor.createVector(is,n,k, operations[predictedOp], lemma, pred);
					FV ucf = new FV();

					long vs[] = new long[200];
					extractor.extractFeatures(is, n, k, UC,lemma, ucf,vs); 

					float ucscore = params.getScore(ucf);

					ucf.clear();
					//long vs[] = new long[80];
					extractor.extractFeatures(is, n, k, LC,lemma, ucf,vs); 

					float lcscore = params.getScore(ucf);


					//	extractor.createVector(is,n,k, LC, lemma,  ucf);
					if (ucscore > lcscore && pform.length()>0) {

						if (pform.length()>1) formsNoRoot[k-1]=Character.toUpperCase(pform.charAt(0))+pform.substring(1);
						else formsNoRoot[k-1]= ""+Character.toUpperCase(pform.charAt(0));

					}




				} else  {

					pform=pipe.lemma2form.get(lemma);
					//	System.out.println("direct "+lemma +" "+pform);

					formsNoRoot[k-1]=pform;
				}




			}


			// first word in sentence upper case
			if (formsNoRoot[0].length()>0)
				formsNoRoot[0] =	(""+(formsNoRoot[0].charAt(0))).toUpperCase()+formsNoRoot[0].substring(1);

			String[] posNoRoot = new String[formsNoRoot.length];
			String[] lemmas = new String[formsNoRoot.length];

			String[] org_lemmas = new String[formsNoRoot.length];

			String[] of = new String[formsNoRoot.length];
			String[] pf = new String[formsNoRoot.length];

			String[] pposs = new String[formsNoRoot.length];
			String[] labels = new String[formsNoRoot.length];
			String[] fillp = new String[formsNoRoot.length];

			int[] heads = new int[formsNoRoot.length];

			tf.write("<seg id=\""+cnt+"\">"); 
			tf.write("<best>");

			for(int j = 0; j < formsNoRoot.length; j++) {


				//	formsNoRoot[j] = forms[j+1];
				posNoRoot[j] = instance.gpos[j+1];

				tf.write(formsNoRoot[j]);
				//	if (j<formsNoRoot.length-1)		
				tf.write(" ");


				pposs[j] = instance.ppos[j+1];

				labels[j] = instance.labels[j+1];
				heads[j] = instance.heads[j+1];
				lemmas[j] = instance.lemmas[j+1];

				if (instance.lemmas!=null) org_lemmas[j] = instance.lemmas[j+1];
				if (instance.ofeats!=null)  of[j] = instance.ofeats[j+1];
				if (instance.pfeats!=null)	pf[j] = instance.pfeats[j+1];

				if (instance.fillp!=null) fillp[j] = instance.fillp[j+1];

				//		formsNoRoot[j] = forms[j+1];

				if (instance.lemmas!=null) org_lemmas[j] = instance.lemmas[j+1];
				//	if (instance.ofeats!=null)  of[j] = instance.ofeats[all[j]];
				//	if (instance.pfeats!=null)	pf[j] = instance.pfeats[all[j]];
				//	if (instance.fillp!=null) fillp[j] = instance.fillp[j+1];
			}
			tf.write("</best>"); 
			tf.write("</seg>"); tf.newLine();


			SentenceData09 i09 = new SentenceData09(formsNoRoot, lemmas, org_lemmas,posNoRoot, pposs, labels, heads,fillp,of, pf);
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


			depWriter.write(i09);

			del=pipe.outValue(cnt, del,last);

		}
		tf.write("</doc></tstset></mteval>"); tf.newLine();
		tf.flush();
		tf.close();

		depWriter.finishWriting();
		long end = System.currentTimeMillis();
		//		DB.println("errors "+error);
		System.out.println("Used time " + (end-start));
		System.out.println("forms count "+Instances.m_count+" unkown "+Instances.m_unkown);

	}






}
