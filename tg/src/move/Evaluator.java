package move;


import is2.io.CONLLReader09;
import is2.data.*;


public class Evaluator {

	public static class Results {

		public int total;
		public int corr;
		public float las;
		public float ula;
		
	}
	/**
	 * Evaluate the results  
	 * @param act_file the gold file
	 * @param pred_file the predicted order
	 * @return information about the predicated result
	 * @throws Exception
	 */
	public static Results evaluate (String act_file,  String pred_file) throws Exception {

		CONLLReader09 goldReader = new CONLLReader09(act_file, -1);
		CONLLReader09 predictedReader = new CONLLReader09(pred_file, -1);

		int total = 0, corr = 0, corrL = 0, dist=0;
		int numsent = 0, corrsent = 0, corrsentL = 0;
		SentenceData09 goldInstance = goldReader.getNext();
		SentenceData09 predInstance = predictedReader.getNext();

		while(goldInstance != null) {

			int instanceLength = goldInstance.length();

			if (instanceLength != predInstance.length())
				System.out.println("Lengths do not match on sentence "+numsent);

			String[] goldForms = goldInstance.lemmas;
			String[] predForms = predInstance.lemmas;

			boolean whole = true; 
			boolean wholeL = true;

			
			StringBuffer gold = new StringBuffer();
			StringBuffer pred = new StringBuffer();
			
			for(int k=1;k<goldForms.length;k++) {
				gold.append((char)k);
				pred.append((char)predInstance.heads[k]);
			}
			
			// NOTE: the first item is the root info added during nextInstance(), so we skip it.

			for (int i = 1; i < instanceLength; i++) {
				if (goldForms[i].equals(predForms[i])) {
					corr++;

		//			if (goldLabels[i].equals(predLabels[i])) corrL++;
		//			else {
				//		System.out.println(numsent+" error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
	//					wholeL = false;
		//			}
				}
				else { 
			//		System.out.println(numsent+"error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
					whole = false; wholeL = false; 
				}
			}
			total += instanceLength - 1; // Subtract one to not score fake root token

			dist += Distance.LD(gold.toString(), pred.toString());
			if(whole) corrsent++;
			if(wholeL) corrsentL++;
			numsent++;

			goldInstance = goldReader.getNext();
			predInstance = predictedReader.getNext();
		}
		
		Results r = new Results();
		
		r.total = total;
		r.corr = corr;
		r.las =(float)Math.round(((double)corrL/total)*(double)100000)/1000;
		r.ula =(float)Math.round(((double)corr /total)*(double)100000)/1000;
		System.out.print("Total: " + total+" \tCorrect: " + corr+" ");
		System.out.println("LAS: " + (double)Math.round(((double)corrL/total)*(double)100000)/1000+" \tTotal: " + (double)Math.round(((double)corrsentL/numsent)*100000)/1000+
				" \tULA: " + (double)Math.round(((double)corr /total)*(double)100000)/1000+" \tTotal: " + (double)Math.round(((double)corrsent /numsent)*100000)/1000);
		System.out.println("String edit dist "+((float)dist/numsent)+" dist "+dist+" avg len "+((float)total/numsent));
		return r;
	}
	
	
	public static float round (double v){
		
		return Math.round(v*10000F)/10000F;
	}
	
}
