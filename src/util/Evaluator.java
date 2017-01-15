package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;


import org.apache.commons.math.stat.inference.TestUtils;

import syn2lin6b.Distance;

import data.Options;
import edu.stanford.nlp.mt.base.IString;
import edu.stanford.nlp.mt.base.IStrings;
import edu.stanford.nlp.mt.base.RawSequence;
import edu.stanford.nlp.mt.base.ScoredFeaturizedTranslation;
import edu.stanford.nlp.mt.base.Sequence;
import edu.stanford.nlp.mt.metrics.BLEUMetric;
import edu.stanford.nlp.mt.metrics.EvaluationMetric;
import edu.stanford.nlp.mt.metrics.IncrementalEvaluationMetric;
import edu.stanford.nlp.mt.metrics.MetricFactory;
import edu.stanford.nlp.mt.metrics.Metrics;
import edu.stanford.nlp.mt.metrics.NISTMetric;

import is2.data.Parse;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;



public class Evaluator {


	public static void   main(String[] args) {

		Options options = new Options(args);

		if (options.eval &&  options.significant1==null ) {

			System.out.println("gold "+options.goldfile);
			System.out.println("gold "+options.outfile);
			Results r = evaluate3(options.goldfile, options.outfile, true, false,options); 
		//	Results r2 = evaluate(options.goldfile, options.outfile, true, false,options);  
		
		// 	Results r = evaluate(options.goldfile, options.outfile, true, false,options); 

		} else if (options.significant1!=null && options.significant2!=null ) {
			
			System.out.println("compare1 "+options.significant1);
			System.out.println("compare2 "+options.significant2);
			System.out.println("gold     "+options.goldfile);
			
			Results r1 = evaluate(options.goldfile, options.significant1,false,false,options); 
			
			System.out.println("file 1 done ");
			
			Results r2 = evaluate(options.goldfile, options.significant2,false,false,options); 
			
			double[] s1 = new double[r1.correctHead.size()]; 
			double[] s2 = new double[r1.correctHead.size()]; 
			
			for(int k=0;k<r1.correctHead.size();k++) {
				s1[k] = r1.correctHead.get(k);
				s2[k] = r2.correctHead.get(k);
			}
			
			try {
				double p = TestUtils.pairedTTest(s1, s2);
				System.out.print("significant of "+options.sigwhat+" "+p);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
//			significant(options.significant1, options.significant2) ;


		} else if (options.significant1!=null) {
			Results r = evaluate(options.goldfile, options.outfile,true,false,options); 
//			significant(options.significant1, options.significant2) ;

		}


	}


	


	

	public static int errors(SentenceData09 s, boolean uas) {

		int errors =0;
		for (int k =1;k<s.length();k++) {

			if (s.heads[k] != s.pheads[k] && (uas || ! s.labels[k].equals(s.plabels[k]))) {
				errors++;
			}
		}
		return errors;
	}

	public static int errors(SentenceData09 s1, SentenceData09 s2, HashMap<String,Integer> r1,HashMap<String,Integer> r2) {



		int errors =0;
		for (int k =1;k<s1.length();k++) {

			if (s1.heads[k] != s1.pheads[k] || (! s1.labels[k].equals(s1.plabels[k]))) {

				if (s2.heads[k] != s2.pheads[k] || (! s2.labels[k].equals(s2.plabels[k]))) {

					// equal do nothing

				} else {

					Integer cnt = r1.get(s1.labels[k]);
					if (cnt==null) cnt=0;
					cnt++;
					r1.put(s1.labels[k],cnt);


				}

			}

			if (s2.heads[k] != s2.pheads[k] || (! s2.labels[k].equals(s2.plabels[k]))) {

				if (s1.heads[k] != s1.pheads[k] || (! s1.labels[k].equals(s1.plabels[k]))) {

					// equal do nothing

				} else {

					Integer cnt = r2.get(s2.labels[k]);
					if (cnt==null) cnt=0;
					cnt++;
					r2.put(s2.labels[k],cnt);


				}

			}
		}
		return errors;
	}


	public static final String PUNCT ="!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

	public static class Results {

		public int total;
		public int corr;
		public float las;
		public float ula;
		public float lpas;
		public float upla;
		
		ArrayList<Double> correctHead;
	}
	
	
	static class  ID {
		public ID(String id, int child) {
			this.id = id;
			this.child = child;
		}
		String id;
		int child;
		
		public String toString() {
			return id+" "+child+";";
		}
	}
	
	
	public static Results evaluate (String act_file,  String pred_file, boolean printEval, boolean sig, Options options) {

		CONLLReader09 goldReader = new CONLLReader09(act_file, -1);
		CONLLReader09 predictedReader = new CONLLReader09(pred_file, -1);

		int total = 0, corr = 0, corrL = 0, Ptotal=0, Pcorr = 0, PcorrL = 0, BPtotal=0, BPcorr = 0, BPcorrL = 0, corrLableAndPos=0, corrHeadAndPos=0;
		int corrLableAndPosP=0, corrHeadAndPosP=0,corrLableAndPosC=0;
		int numsent = 0, corrsent = 0, corrsentL = 0, Pcorrsent = 0, PcorrsentL = 0,sameProj=0;;
		
		int   corrOne = 0,dist =0;;
		
		int avgLen =0, sntAvgLen=0;
		
		int snt=0;
		
		float bleuScore=0, nistScore=0;
		
		int correct=0, avgLength =0;

		SentenceData09 goldInstance = goldReader.getNext();

		// how many sentences are fully correct
		int totalCorrectSentences = 0;
		float invEditDist = 0;

		
		SentenceData09 predInstance = predictedReader.getNext();
		HashMap<String,Integer> label = new HashMap<String,Integer>();
		HashMap<String,Integer> labelCount = new HashMap<String,Integer>();
		HashMap<String,Integer> labelCorrect = new HashMap<String,Integer>();
		HashMap<String,Integer> falsePositive = new HashMap<String,Integer>();

		// does the node have the correct head?
		ArrayList<Double> correctH = new ArrayList<Double>();

		
		int correctPosition =0, goldChildrenCount=0;
		int tokens =0;

		List<List<Sequence<IString>>> referencesList = new ArrayList<List<Sequence<IString>>>();
		List<List<Sequence<IString>>> referencesListLC = new ArrayList<List<Sequence<IString>>>();

		ArrayList<ScoredFeaturizedTranslation<IString, String>> trans = new ArrayList<ScoredFeaturizedTranslation<IString, String>>();
		ArrayList<ScoredFeaturizedTranslation<IString, String>> transLC = new ArrayList<ScoredFeaturizedTranslation<IString, String>>();

		boolean detailed = true;
		while(goldInstance != null) {

			int instanceLength = goldInstance.length();

			if (instanceLength != predInstance.length()){
				System.out.println("Lengths do not match on sentence "+numsent);
				System.out.println(goldInstance.toString());
			}
			int[] goldHeads = goldInstance.heads;
			String[] goldLabels = goldInstance.labels;
			int[] predHeads = predInstance.pheads;
			String[] predLabels = predInstance.plabels;

			boolean whole = true;
			boolean wholeL = true;

			boolean Pwhole = true;
			boolean PwholeL = true;


			int tlasS=0, totalS=0,corrLabels=0, XLabels=0;

			// NOTE: the first item is the root info added during nextInstance(), so we skip it.

	
		
		

			
			int correctPositionSnt =0;
			
			StringBuffer gold = new StringBuffer(),  pred = new StringBuffer(), goldSentence = new StringBuffer(), predSentence= new StringBuffer();
			StringBuffer goldSentenceLowerCase = new StringBuffer(), predSentenceLowerCase  = new StringBuffer();
			
			
			for (int i = 1; i < instanceLength; i++) {
				
				ArrayList<ID> gchildren = getChildren(goldInstance,i);
				
				int map = getHead(predInstance,i);
				
				ArrayList<ID> pchildren = getChildren(predInstance,map);
				
	if (detailed)
	try {			
				for (int c=0;c<gchildren.size();c++) {
					if (pchildren.size() !=gchildren.size() ) {
						DB.println("node "+i+" has different number of children ! ");
						DB.println(snt+" diff "+i+" map "+map+" gchildren "+gchildren+" pchildren "+pchildren);
						
						detailed =false;
						
						continue;
					}
					
					String goldLabel =goldInstance.labels[gchildren.get(c).child];
					if (pchildren.size()>c && pchildren.get(c).id.equals( gchildren.get(c).id))  {
					
						Integer lc = labelCorrect.get(goldLabel);
						if (lc ==null) lc=0;
						labelCorrect.put(goldLabel, lc==null?1: (lc+1));
					} else {
						
						String plabel =predInstance.labels[pchildren.get(c).child];
						Integer lc = falsePositive.get(plabel);
						falsePositive.put(goldLabel, lc==null?1:(lc+1));
					
					}
					Integer lcnt  = labelCount.get(goldLabel);
					labelCount.put(goldLabel, lcnt==null?1:(lcnt+1));
							
					goldChildrenCount++;
				}
	} catch (Exception e ) {
		
		if (detailed) {
			DB.println("error two different dependency tree annotations?");
			detailed =false;
		}
		
	}
			
				String gid = goldInstance.id[i].contains("_")?goldInstance.id[i].split("_")[1]:goldInstance.id[i];
				String pid = predInstance.id[i].contains("_")?predInstance.id[i].split("_")[1]:predInstance.id[i];
				
				gold.append((char)Integer.parseInt(gid));
				pred.append((char)Integer.parseInt(pid));
				

				if (goldSentence.length()>0) {
					goldSentence.append(' ');
					goldSentenceLowerCase.append(' ');
				}
				goldSentence.append(goldInstance.forms[i]);
				goldSentenceLowerCase.append(goldInstance.forms[i].toLowerCase());
				
				if (predSentence.length()>0) {
					predSentence.append(' ');
					predSentenceLowerCase.append(' ');
				}
				
				predSentence.append(predInstance.forms[i]);
				predSentenceLowerCase.append(predInstance.forms[i].toLowerCase());
				

				// correct position
				if (i == Integer.parseInt(pid)) {
					correctPosition ++;
					correctPositionSnt ++;
				}
				
				
			}
			boolean totalCorrect=true;
			if (!goldSentence.toString().equals(predSentence.toString())) totalCorrect=false;
			
			
			// BLUE // 
			ArrayList<String> ref = new ArrayList<String>();
			ref.add(goldSentence.toString());

			double bleusnt= BLEUMetric.computeLocalSmoothScore(predSentence.toString(), ref, 4);
	
		bleuScore +=bleusnt;
		snt++;
		sntAvgLen = +instanceLength;
	
			// NIST
			
			ArrayList<Sequence<IString>> ll =new ArrayList<Sequence<IString>>();
			
		    Sequence<IString> goldData = new RawSequence<IString>( IStrings.toIStringArray(goldSentence.toString().split(" "))); 			
			ll.add(goldData);
			referencesList.add(ll);

			ll =new ArrayList<Sequence<IString>>();

		    goldData = new RawSequence<IString>( IStrings.toIStringArray(goldSentenceLowerCase.toString().split(" "))); 			
			ll.add(goldData);
			referencesListLC.add(ll);

			
								
			Sequence<IString> translation = new RawSequence<IString>( IStrings.toIStringArray(predSentence.toString().split(" "))); //predSentence.toString()
			ScoredFeaturizedTranslation<IString, String> tran = new ScoredFeaturizedTranslation<IString, String>(    translation, null, snt);
			trans.add(tran);  
		
			translation = new RawSequence<IString>( IStrings.toIStringArray(predSentenceLowerCase.toString().split(" "))); //predSentence.toString()
			tran = new ScoredFeaturizedTranslation<IString, String>(    translation, null, snt);
			transLC.add(tran);  
			
			// edit distance
			double distsnt= Distance.LD(gold.toString(), pred.toString()); 
			dist += distsnt;

			// do not count the root node
			int sentenceLength = instanceLength-1;
			
			tokens += sentenceLength;

			if (options.sigwhat==null || options.sigwhat.equals("pos") ) {
				correctH.add((double)correctPositionSnt/(double)sentenceLength);
			} else if (options.sigwhat.equals("bleu")) {
				correctH.add((double)bleusnt);
			} else if (options.sigwhat.equals("exact")) {
				correctH.add(totalCorrect?1.0:0.0);
			} else if (options.sigwhat.equals("dist")) {
				correctH.add(distsnt);
			}


			
		//	invEditDist += 1-((double)Distance.LD(gold.toString(), pred.toString())/(double)sentenceLength);

			snt++;
			if (totalCorrect) totalCorrectSentences++;


			if(whole) corrsent++;
			if(wholeL) corrsentL++;
			if(Pwhole) Pcorrsent++;
			if(PwholeL) PcorrsentL++;
			numsent++;

			goldInstance = goldReader.getNext();
			predInstance = predictedReader.getNext();
			
			
			//correctHead.add((double) Math.round(((double)corrLabels/(instanceLength - 1))));
			//System.out.println(""+(float)Math.round(((double)tlasS/(instanceLength - 1))*100000)/1000);
		}

		EvaluationMetric<IString, String> metric = MetricFactory.metric(MetricFactory.SMOOTH_BLEU_STRING, referencesList);
		IncrementalEvaluationMetric<IString, String> incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< trans.size();i++) 
			incMetric.add(trans.get(i));
		
		
		

		
		bleuScore = (float) incMetric.score();
		
		metric = MetricFactory.metric(MetricFactory.SMOOTH_BLEU_STRING, referencesListLC);
		incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< transLC.size();i++) 
			incMetric.add(transLC.get(i));


		float bleuScoreLC = (float) incMetric.score();

		metric = MetricFactory.metric(MetricFactory.NIST_STRING, referencesList);
		incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< trans.size();i++) 
			incMetric.add(trans.get(i));
		
		nistScore = (float) incMetric.score();
		
		Results r = new Results();

		r.correctHead =correctH;
		int  mult=100000, diff=1000;

		r.total = total;
		r.corr = corr;
		r.las =(float)Math.round(((double)corrL/total)*mult)/diff;
		r.ula =(float)Math.round(((double)corr /total)*mult)/diff;
		r.lpas =(float)Math.round(((double)corrLableAndPos/total)*mult)/diff;
		r.upla =(float)Math.round(((double)corrHeadAndPos /total)*mult)/diff;

		System.out.println("Edit distance "+(float)(((double)dist)/((double)snt)));
		System.out.println("Inv Edit dist "+(float)(1-((double)dist/2F)/((double)tokens)));
		System.out.println("BLEU          "+(float)(((double)bleuScore))+" lowercase: "+bleuScoreLC+"");
		System.out.println("NIST          "+(float)(((double)nistScore)));
		System.out.println("Exact         "+(float)(((double)totalCorrectSentences*100)/((double)snt))+" %");
		System.out.println("Correct Pos.  "+(float)((((double)correctPosition*mult)/((double)tokens)))/diff+" %");
		

		if (!printEval) return r;
		
		
		System.out.println("label\ttp\tcount\trecall\t\ttp\tfp+tp\tprecision\t F-Score ");
/*
		for(Entry<String, Integer> e : labelCount.entrySet()) {
		
			int tp = labelCorrect.get(e.getKey())==null?0:labelCorrect.get(e.getKey()).intValue();
			Integer count = labelCount.get(e.getKey());
			int fp = falsePositive.get(e.getKey())==null?0:falsePositive.get(e.getKey()).intValue();
			System.out.println(e.getKey()+"\t"+tp+"\t"+count+"\t"+roundPercent((float)tp/count)+"\t\t"+tp+"\t"+(fp+tp)+
					"\t"+roundPercent((float)tp/(fp+tp))+"\t\t"+roundPercent((((float)tp/count))+(float)tp/(fp+tp))/2F); //+totalD
		}
	*/
		
	//	DB.println("edit dist "+	 Distance.LD("dernd", "bernd".toString()));
		
		
		
		return r;
	}


	public static Results evaluate2 (String act_file,  String pred_file, boolean printEval, boolean sig, Options options) {

		System.out.println("act "+act_file+" pred "+pred_file);
		
		CONLLReader09 goldReader = new CONLLReader09(act_file, -1);
		CONLLReader09 predictedReader = new CONLLReader09(pred_file, -1);

		int total = 0, corr = 0, corrL = 0, Ptotal=0, Pcorr = 0, PcorrL = 0, BPtotal=0, BPcorr = 0, BPcorrL = 0, corrLableAndPos=0, corrHeadAndPos=0;
		int corrLableAndPosP=0, corrHeadAndPosP=0,corrLableAndPosC=0;
		int numsent = 0, corrsent = 0, corrsentL = 0, Pcorrsent = 0, PcorrsentL = 0,sameProj=0;;
		
		int   corrOne = 0,dist =0;;
		
		int snt=0;
		int sntAvgLen =0;
		
		float avgLen=0;
		float bleuScore=0, nistScore=0;
		
		int correct=0, avgLength =0;

		SentenceData09 goldInstance = goldReader.getNext();

		// how many sentences are fully correct
		int totalCorrectSentences = 0;
		float invEditDist = 0;

		
		SentenceData09 predInstance = predictedReader.getNext();
		HashMap<String,Integer> label = new HashMap<String,Integer>();
		HashMap<String,Integer> labelCount = new HashMap<String,Integer>();
		HashMap<String,Integer> labelCorrect = new HashMap<String,Integer>();
		HashMap<String,Integer> falsePositive = new HashMap<String,Integer>();

		// does the node have the correct head?
		ArrayList<Double> correctH = new ArrayList<Double>();

		
		int correctPosition =0, goldChildrenCount=0;
		int tokens =0;

		List<List<Sequence<IString>>> referencesList = new ArrayList<List<Sequence<IString>>>();
		List<List<Sequence<IString>>> referencesListLC = new ArrayList<List<Sequence<IString>>>();

		ArrayList<ScoredFeaturizedTranslation<IString, String>> trans = new ArrayList<ScoredFeaturizedTranslation<IString, String>>();
		ArrayList<ScoredFeaturizedTranslation<IString, String>> transLC = new ArrayList<ScoredFeaturizedTranslation<IString, String>>();

		boolean detailed = true;
		while(goldInstance != null) {

			int instanceLength = goldInstance.length();
			int instancePredLength = predInstance.length();

			
			int[] goldHeads = goldInstance.heads;
			String[] goldLabels = goldInstance.labels;
			int[] predHeads = predInstance.pheads;
			String[] predLabels = predInstance.plabels;

			boolean whole = true;
			boolean wholeL = true;

			boolean Pwhole = true;
			boolean PwholeL = true;


			int tlasS=0, totalS=0,corrLabels=0, XLabels=0;

			// NOTE: the first item is the root info added during nextInstance(), so we skip it.
		
			
		

			
			int correctPositionSnt =0;
			
			StringBuffer gold = new StringBuffer(),  pred = new StringBuffer(), goldSentence = new StringBuffer(), predSentence= new StringBuffer();
			StringBuffer goldSentenceLowerCase = new StringBuffer(), predSentenceLowerCase  = new StringBuffer();
			
			for (int i = 1; i < instanceLength; i++) {
				
				
			//	if (goldInstance.gpos[i].equals("SYM")) continue;
				
				if (goldInstance.lemmas[i].startsWith("_")) continue;
				goldSentence.append(goldInstance.lemmas[i]);
				goldSentenceLowerCase.append(goldInstance.lemmas[i].toLowerCase());
				if (goldSentence.length()>0) {
					goldSentence.append(' ');
					goldSentenceLowerCase.append(' ');
				}
			}
			
			for (int i = 1; i < instancePredLength; i++) {
				predSentence.append(predInstance.lemmas[i]);
				predSentenceLowerCase.append(predInstance.lemmas[i].toLowerCase());
				if (predSentence.length()>0) {
					predSentence.append(' ');
					predSentenceLowerCase.append(' ');
				}
			}
			
			
			boolean totalCorrect=true;
			if (!goldSentence.toString().equals(predSentence.toString())) totalCorrect=false;
			
			
			// BLUE // 
			ArrayList<String> ref = new ArrayList<String>();
			
			ref.add(goldSentence.toString());

			double bleusnt= BLEUMetric.computeLocalSmoothScore(predSentence.toString(), ref, 4);
			if	(instanceLength<=999)	{
				bleuScore +=bleusnt;
				sntAvgLen++;
				avgLen += instanceLength;
			
		//	bleuScore +=bleusnt;
			// NIST
	//		System.out.println("gold "+goldSentence.toString());
	//		System.out.println("pred "+predSentence.toString()+" "+bleusnt);
			ArrayList<Sequence<IString>> ll =new ArrayList<Sequence<IString>>();
			
		    Sequence<IString> goldData = new RawSequence<IString>( IStrings.toIStringArray(goldSentence.toString().split(" "))); 			
			ll.add(goldData);
			referencesList.add(ll);

			ll =new ArrayList<Sequence<IString>>();

		    goldData = new RawSequence<IString>( IStrings.toIStringArray(goldSentenceLowerCase.toString().split(" "))); 			
			ll.add(goldData);
			referencesListLC.add(ll);

			
								
			Sequence<IString> translation = new RawSequence<IString>( IStrings.toIStringArray(predSentence.toString().split(" "))); //predSentence.toString()
			ScoredFeaturizedTranslation<IString, String> tran = new ScoredFeaturizedTranslation<IString, String>(    translation, null, snt);
			trans.add(tran);  
		
			translation = new RawSequence<IString>( IStrings.toIStringArray(predSentenceLowerCase.toString().split(" "))); //predSentence.toString()
			tran = new ScoredFeaturizedTranslation<IString, String>(    translation, null, snt);
			transLC.add(tran);  
			
			// edit distance
			double distsnt= Distance.LD(gold.toString(), pred.toString()); 
			dist += distsnt;

			// do not count the root node
			int sentenceLength = instanceLength-1;
			
			tokens += sentenceLength;
		
			if (options.sigwhat==null || options.sigwhat.equals("pos") ) {
				correctH.add((double)correctPositionSnt/(double)sentenceLength);
			} else if (options.sigwhat.equals("bleu")) {
				correctH.add((double)bleusnt);
			} else if (options.sigwhat.equals("exact")) {
				correctH.add(totalCorrect?1.0:0.0);
			} else if (options.sigwhat.equals("dist")) {
				correctH.add(distsnt);
			}


			
		//	invEditDist += 1-((double)Distance.LD(gold.toString(), pred.toString())/(double)sentenceLength);

			snt++;
			if (totalCorrect) totalCorrectSentences++;
			}

			if(whole) corrsent++;
			if(wholeL) corrsentL++;
			if(Pwhole) Pcorrsent++;
			if(PwholeL) PcorrsentL++;
			numsent++;

			goldInstance = goldReader.getNext();
			predInstance = predictedReader.getNext();
			
			
			//correctHead.add((double) Math.round(((double)corrLabels/(instanceLength - 1))));
			//System.out.println(""+(float)Math.round(((double)tlasS/(instanceLength - 1))*100000)/1000);
		}

		EvaluationMetric<IString, String> metric = MetricFactory.metric(MetricFactory.SMOOTH_BLEU_STRING, referencesList);
		IncrementalEvaluationMetric<IString, String> incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< trans.size();i++) 
			incMetric.add(trans.get(i));
		
		
		

		
		bleuScore = (float) incMetric.score();
		
		metric = MetricFactory.metric(MetricFactory.SMOOTH_BLEU_STRING, referencesListLC);
		incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< transLC.size();i++) 
			incMetric.add(transLC.get(i));


		float bleuScoreLC = (float) incMetric.score();

		metric = MetricFactory.metric(MetricFactory.NIST_STRING, referencesList);
		incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< trans.size();i++) 
			incMetric.add(trans.get(i));
		
		nistScore = (float) incMetric.score();
		
		Results r = new Results();

		r.correctHead =correctH;
		int  mult=100000, diff=1000;

		r.total = total;
		r.corr = corr;
		r.las =(float)Math.round(((double)corrL/total)*mult)/diff;
		r.ula =(float)Math.round(((double)corr /total)*mult)/diff;
		r.lpas =(float)Math.round(((double)corrLableAndPos/total)*mult)/diff;
		r.upla =(float)Math.round(((double)corrHeadAndPos /total)*mult)/diff;

		System.out.println("Edit distance "+(float)(((double)dist)/((double)snt)));
		System.out.println("Inv Edit dist "+(float)(1-((double)dist/2F)/((double)tokens)));
		System.out.println("avg leng      "+(float)((((double)avgLen)/((double)sntAvgLen)))+" "+avgLen+" "+sntAvgLen);
		System.out.println("BLEU          "+(float)(((double)bleuScore))+" lowercase: "+bleuScoreLC+"");
		System.out.println("NIST          "+(float)(((double)nistScore)));
		System.out.println("Exact         "+(float)(((double)totalCorrectSentences*100)/((double)snt))+" %");
		System.out.println("Correct Pos.  "+(float)((((double)correctPosition*mult)/((double)tokens)))/diff+" %");
	

		if (!printEval) return r;
		
		
		System.out.println("label\ttp\tcount\trecall\t\ttp\tfp+tp\tprecision\t F-Score ");
/*
		for(Entry<String, Integer> e : labelCount.entrySet()) {
		
			int tp = labelCorrect.get(e.getKey())==null?0:labelCorrect.get(e.getKey()).intValue();
			Integer count = labelCount.get(e.getKey());
			int fp = falsePositive.get(e.getKey())==null?0:falsePositive.get(e.getKey()).intValue();
			System.out.println(e.getKey()+"\t"+tp+"\t"+count+"\t"+roundPercent((float)tp/count)+"\t\t"+tp+"\t"+(fp+tp)+
					"\t"+roundPercent((float)tp/(fp+tp))+"\t\t"+roundPercent((((float)tp/count))+(float)tp/(fp+tp))/2F); //+totalD
		}
	*/
		
	//	DB.println("edit dist "+	 Distance.LD("dernd", "bernd".toString()));
		
		
		
		return r;
	}

	

	public static Results evaluate3 (String act_file,  String pred_file, boolean printEval, boolean sig, Options options) {

		System.out.println("act "+act_file+" pred "+pred_file);
		
		CONLLReader09 goldReader = new CONLLReader09(act_file, -1);
		CONLLReader09 predictedReader = new CONLLReader09(pred_file, -1);

		int total = 0, corr = 0, corrL = 0, Ptotal=0, Pcorr = 0, PcorrL = 0, BPtotal=0, BPcorr = 0, BPcorrL = 0, corrLableAndPos=0, corrHeadAndPos=0;
		int corrLableAndPosP=0, corrHeadAndPosP=0,corrLableAndPosC=0;
		int numsent = 0, corrsent = 0, corrsentL = 0, Pcorrsent = 0, PcorrsentL = 0,sameProj=0;;
		
		int   corrOne = 0,dist =0;;
		
		int snt=0;
		int sntAvgLen =0;
		
		float avgLen=0;
		float bleuScore=0, nistScore=0;
		
		int correct=0, avgLength =0;

		SentenceData09 goldInstance = goldReader.getNext();

		// how many sentences are fully correct
		int totalCorrectSentences = 0;
		float invEditDist = 0;

		
		SentenceData09 predInstance = predictedReader.getNext();
		HashMap<String,Integer> label = new HashMap<String,Integer>();
		HashMap<String,Integer> labelCount = new HashMap<String,Integer>();
		HashMap<String,Integer> labelCorrect = new HashMap<String,Integer>();
		HashMap<String,Integer> falsePositive = new HashMap<String,Integer>();

		// does the node have the correct head?
		ArrayList<Double> correctH = new ArrayList<Double>();

		
		int correctPosition =0, goldChildrenCount=0;
		int tokens =0;

		List<List<Sequence<IString>>> referencesList = new ArrayList<List<Sequence<IString>>>();
		List<List<Sequence<IString>>> referencesListLC = new ArrayList<List<Sequence<IString>>>();

		ArrayList<ScoredFeaturizedTranslation<IString, String>> trans = new ArrayList<ScoredFeaturizedTranslation<IString, String>>();
		ArrayList<ScoredFeaturizedTranslation<IString, String>> transLC = new ArrayList<ScoredFeaturizedTranslation<IString, String>>();

		boolean detailed = true;
		while(goldInstance != null) {

			int instanceLength = goldInstance.length();
			int instancePredLength = predInstance.length();

			
			int[] goldHeads = goldInstance.heads;
			String[] goldLabels = goldInstance.labels;
			int[] predHeads = predInstance.pheads;
			String[] predLabels = predInstance.plabels;

			boolean whole = true;
			boolean wholeL = true;

			boolean Pwhole = true;
			boolean PwholeL = true;


			int tlasS=0, totalS=0,corrLabels=0, XLabels=0;

			// NOTE: the first item is the root info added during nextInstance(), so we skip it.
		
			
		

			
			int correctPositionSnt =0;
			
			StringBuffer gold = new StringBuffer(),  pred = new StringBuffer(), goldSentence = new StringBuffer(), 
					predSentence= new StringBuffer();
			StringBuffer goldSentenceLowerCase = new StringBuffer(), predSentenceLowerCase  = new StringBuffer();
			
			for (int i = 1; i < instanceLength; i++) {
				
				
			//	if (goldInstance.gpos[i].equals("SYM")) continue;
				if ("...--,!:?''´´``_".contains(goldInstance.forms[i])) continue;
				if ("_".contains(goldInstance.lemmas[i])) continue;
				
				if ("-lrb-".startsWith(goldInstance.forms[i])) continue;
//				if (goldInstance.forms[i].startsWith("_")) continue;
				
			//	System.out.print(i+":"+goldInstance.forms[i]+" ");
			//	System.out.println();
				goldSentence.append(goldInstance.lemmas[i]);
				goldSentenceLowerCase.append(goldInstance.lemmas[i].toLowerCase());
				if (goldSentence.length()>0) {
					goldSentence.append(' ');
					goldSentenceLowerCase.append(' ');
				} 
			} 
			
			for (int i = 1; i < instancePredLength; i++) {

				if ("...--,!:?''´´``".contains(predInstance.forms[i])) continue;
				if ("-lrb-".startsWith(predInstance.forms[i])) continue;

				
				predSentence.append(predInstance.lemmas[i]);
				predSentenceLowerCase.append(predInstance.lemmas[i].toLowerCase());
				if (predSentence.length()>0) {
					predSentence.append(' ');
					predSentenceLowerCase.append(' ');
				}
			}
			
			
			boolean totalCorrect=true;
			if (!goldSentence.toString().equals(predSentence.toString())) totalCorrect=false;
			System.out.println(predSentence);
			System.out.println(goldSentence);
			System.out.println();
			// BLUE // 
			ArrayList<String> ref = new ArrayList<String>();
			
			ref.add(goldSentence.toString());

			double bleusnt= BLEUMetric.computeLocalSmoothScore(predSentence.toString(), ref, 4);
			if	(instanceLength<=999)	{
				bleuScore +=bleusnt;
				sntAvgLen++;
				avgLen += instanceLength;
			
		//	bleuScore +=bleusnt;
			// NIST
	//		System.out.println("gold "+goldSentence.toString());
	//		System.out.println("pred "+predSentence.toString()+" "+bleusnt);
			ArrayList<Sequence<IString>> ll =new ArrayList<Sequence<IString>>();
			
		    Sequence<IString> goldData = new RawSequence<IString>( IStrings.toIStringArray(goldSentence.toString().split(" "))); 			
			ll.add(goldData);
			referencesList.add(ll);

			ll =new ArrayList<Sequence<IString>>();

		    goldData = new RawSequence<IString>( IStrings.toIStringArray(goldSentenceLowerCase.toString().split(" "))); 			
			ll.add(goldData);
			referencesListLC.add(ll);

			
								
			Sequence<IString> translation = new RawSequence<IString>( IStrings.toIStringArray(predSentence.toString().split(" "))); //predSentence.toString()
			ScoredFeaturizedTranslation<IString, String> tran = new ScoredFeaturizedTranslation<IString, String>(    translation, null, snt);
			trans.add(tran);  
		
			translation = new RawSequence<IString>( IStrings.toIStringArray(predSentenceLowerCase.toString().split(" "))); //predSentence.toString()
			tran = new ScoredFeaturizedTranslation<IString, String>(    translation, null, snt);
			transLC.add(tran);  
			
			// edit distance
			double distsnt= Distance.LD(gold.toString(), pred.toString()); 
			dist += distsnt;

			// do not count the root node
			int sentenceLength = instanceLength-1;
			
			tokens += sentenceLength;
		
			if (options.sigwhat==null || options.sigwhat.equals("pos") ) {
				correctH.add((double)correctPositionSnt/(double)sentenceLength);
			} else if (options.sigwhat.equals("bleu")) {
				correctH.add((double)bleusnt);
			} else if (options.sigwhat.equals("exact")) {
				correctH.add(totalCorrect?1.0:0.0);
			} else if (options.sigwhat.equals("dist")) {
				correctH.add(distsnt);
			}


			
		//	invEditDist += 1-((double)Distance.LD(gold.toString(), pred.toString())/(double)sentenceLength);

			snt++;
			if (totalCorrect) totalCorrectSentences++;
			}

			if(whole) corrsent++;
			if(wholeL) corrsentL++;
			if(Pwhole) Pcorrsent++;
			if(PwholeL) PcorrsentL++;
			numsent++;

			goldInstance = goldReader.getNext();
			predInstance = predictedReader.getNext();
			
			
			//correctHead.add((double) Math.round(((double)corrLabels/(instanceLength - 1))));
			//System.out.println(""+(float)Math.round(((double)tlasS/(instanceLength - 1))*100000)/1000);
		}

		EvaluationMetric<IString, String> metric = MetricFactory.metric(MetricFactory.SMOOTH_BLEU_STRING, referencesList);
		IncrementalEvaluationMetric<IString, String> incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< trans.size();i++) 
			incMetric.add(trans.get(i));
		
		
		

		
		bleuScore = (float) incMetric.score();
		
		metric = MetricFactory.metric(MetricFactory.SMOOTH_BLEU_STRING, referencesListLC);
		incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< transLC.size();i++) 
			incMetric.add(transLC.get(i));


		float bleuScoreLC = (float) incMetric.score();

		metric = MetricFactory.metric(MetricFactory.NIST_STRING, referencesList);
		incMetric = metric.getIncrementalMetric();
		
		for(int i =0;i< trans.size();i++) 
			incMetric.add(trans.get(i));
		
		nistScore = (float) incMetric.score();
		
		Results r = new Results();

		r.correctHead =correctH;
		int  mult=100000, diff=1000;

		r.total = total;
		r.corr = corr;
		r.las =(float)Math.round(((double)corrL/total)*mult)/diff;
		r.ula =(float)Math.round(((double)corr /total)*mult)/diff;
		r.lpas =(float)Math.round(((double)corrLableAndPos/total)*mult)/diff;
		r.upla =(float)Math.round(((double)corrHeadAndPos /total)*mult)/diff;

		System.out.println("Edit distance "+(float)(((double)dist)/((double)snt)));
		System.out.println("Inv Edit dist "+(float)(1-((double)dist/2F)/((double)tokens)));
		System.out.println("avg leng      "+(float)((((double)avgLen)/((double)sntAvgLen)))+" "+avgLen+" "+sntAvgLen);
		System.out.println("BLEU          "+(float)(((double)bleuScore))+" lowercase: "+bleuScoreLC+"");
		System.out.println("NIST          "+(float)(((double)nistScore)));
		System.out.println("Exact         "+(float)(((double)totalCorrectSentences*100)/((double)snt))+" %");
		System.out.println("Correct Pos.  "+(float)((((double)correctPosition*mult)/((double)tokens)))/diff+" %");
	

		if (!printEval) return r;
		
		
		System.out.println("label\ttp\tcount\trecall\t\ttp\tfp+tp\tprecision\t F-Score ");
/*
		for(Entry<String, Integer> e : labelCount.entrySet()) {
		
			int tp = labelCorrect.get(e.getKey())==null?0:labelCorrect.get(e.getKey()).intValue();
			Integer count = labelCount.get(e.getKey());
			int fp = falsePositive.get(e.getKey())==null?0:falsePositive.get(e.getKey()).intValue();
			System.out.println(e.getKey()+"\t"+tp+"\t"+count+"\t"+roundPercent((float)tp/count)+"\t\t"+tp+"\t"+(fp+tp)+
					"\t"+roundPercent((float)tp/(fp+tp))+"\t\t"+roundPercent((((float)tp/count))+(float)tp/(fp+tp))/2F); //+totalD
		}
	*/
		
	//	DB.println("edit dist "+	 Distance.LD("dernd", "bernd".toString()));
		
		
		
		return r;
	}


	private static int getHead(SentenceData09 instance, int p) {
		
		for(int i=0;i<instance.length();i++) {
			
			int id = 	Integer.parseInt(instance.id[i].contains("_")?instance.id[i].split("_")[1]:instance.id[i]);

			if (id == p) return i;
		}
		
		return -1;
		
	}







	/*
	 * Get the children of this instance
	 * @param head 
	 * @return children of the head 
	 */
	public static ArrayList<ID> getChildren(SentenceData09 instance, int head) {

		ArrayList<ID> children = new ArrayList<ID>();
		for(int i=0;i<instance.length();i++) {
			
			if (instance.heads[i]==head) {
				String id = instance.id[i];
				if (id.contains("_")) {
					id = id.split("_")[1];
				}
				children.add(new ID(id,i));
			}
		}
		return children;
	}





	public static float round (double v){

		return Math.round(v*10000F)/10000F;
	}

	public static float roundPercent (double v){

		return Math.round(v*10000F)/100F;
	}




}
