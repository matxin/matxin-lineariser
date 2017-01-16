package tag7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map.Entry;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;


public class Evaluator {

	public static void evaluate (String act_file,  String pred_file, String format) throws Exception {

		CONLLReader09 goldReader = new CONLLReader09(act_file);//DependencyReader.createDependencyReader();
	//	boolean labeled = goldReader.startReading(act_file);

		CONLLReader09 predictedReader = new CONLLReader09();
		 predictedReader.startReading(pred_file);

		Hashtable<String,Integer> errors = new Hashtable<String,Integer>();
		Hashtable<String,StringBuffer> words = new Hashtable<String,StringBuffer>();

		int total = 0, corr = 0, corrL = 0, corrT=0;
		int numsent = 0, corrsent = 0, corrsentL = 0;
		SentenceData09 goldInstance = goldReader.getNext();
		SentenceData09 predInstance = predictedReader.getNext();

		while(goldInstance != null) {

			int instanceLength = goldInstance.length();

			if (instanceLength != predInstance.length()) {
				System.out.println("Error: Lengths do not match on sentence "+numsent);
				break;
			}
			String gold[] = goldInstance.gpos;
			String pred[] = predInstance.ppos;

			boolean whole = true;
			boolean wholeL = true;

			// NOTE: the first item is the root info added during nextInstance(), so we skip it.

			for (int i = 1; i < instanceLength; i++) {
				if (gold[i].equals(pred[i])) corrT++;
				else {
					String key = "gold: '"+gold[i]+"' pred: '"+pred[i]+"'";
					Integer cnt = errors.get(key);
					StringBuffer errWrd = words.get(key);
					if (cnt==null) {
						errors.put(key,1);
						words.put(key, new StringBuffer().append(goldInstance.forms[i]));
					}
					else {
						errors.put(key,cnt+1);		
						errWrd.append(" "+goldInstance.forms[i]);
					}
				}
				
				
			}
			total += instanceLength - 1; // Subtract one to not score fake root token

			if(whole) corrsent++;
			if(wholeL) corrsentL++;
			numsent++;

			goldInstance = goldReader.getNext();
			predInstance = predictedReader.getNext();
		}

		 

	
					
				//	System.out.println("error gold:"+goldPos[i]+" pred:"+predPos[i]+" "+goldInstance.forms[i]+" snt "+numsent+" i:"+i);
		ArrayList<Entry<String, Integer>> opsl = new ArrayList<Entry<String, Integer>>();
		for(Entry<String, Integer> e : errors.entrySet()) {
			opsl.add(e);
		}
		
		Collections.sort(opsl, new Comparator<Entry<String, Integer>>(){

			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				
				return o1.getValue()==o2.getValue()?0:o1.getValue()>o2.getValue()?-1:1;
			}
			
			
		});
			
		
		int cnt=0;
		System.out.println("top most errors:");
		for(Entry<String, Integer> e : opsl) {
			cnt++;
		//	if(e.getValue()>10) System.out.println(e.getKey()+"  "+e.getValue()+" context: "+words.get(e.getKey()));
		}


		
		
		System.out.println("Tokens: " + total+" Correct: " + corrT+" "+(float)corrT/total);
	}

	public static void main (String[] args) throws Exception {
		String format = "CONLL";
		if (args.length > 2)
			format = args[2];

		evaluate(args[0], args[1], format);
	}

}
