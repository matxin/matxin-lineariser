package morph2;


import is2.data.*;
import is2.io.CONLLReader09;
import is2.util.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
 

final public class Pipe extends PipeGen {

	public static Extractor[] extractor;

	public static final String POS = "POS";
	protected static final String DIST = "DIST";
	protected static final String WORD = "WORD";
	protected static final String PRED = "PRED";
	protected static final String ARG = "ARG";
	protected static final String FEAT = "F";
	public static final String REL = "REL";
	protected static final String TYPE = "TYPE";
	public static final String OPERATION = "OP";
	private static final String CHAR = "C";


	final public static MFO mf = new MFO();

	private static OptionsSuper options;

	public static long timeExtract;

	public Pipe(OptionsSuper o) {
		options = o;
	}

	private static void registerChars(String type, String word) {

		for(int i=0;i<word.length();i++) mf.register(type, Character.toString(word.charAt(i)));      
	}
	
	static HashMap<String,String> lemma2form = new HashMap<String,String>();

	static public void createInstances(String file, int task, InstancesTagger is, int max) throws Exception {

		CONLLReader09 depReader = new CONLLReader09(file,false);
		//CONLLReader09.normalizeOn=false;

		mf.register(REL, "<root-type>");

		// register at least one predicate since the parsing data might not contain predicates as in 
		// the Japaness corpus but the development sets contains some

		long sl=0;

		System.out.print("Registering feature parts of sentence: ");
		int ic = 0;
		int del = 0;

		HashMap<String,Integer> ops = new HashMap<String, Integer> ();
		HashMap<String,HashMap<String,Integer>> lemma2formfreq = new HashMap<String,HashMap<String,Integer>>();
		HashMap<String,HashMap<String,Integer>> lemma2ops = new HashMap<String,HashMap<String,Integer>>();
		HashMap<String,HashMap<String,Integer>> ops2lemma = new HashMap<String,HashMap<String,Integer>>();

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

			for(int i1 = 0; i1 < w.length; i1++) registerChars(CHAR,  w[i1]);



			w = instance.plemmas;
			for (int i1 = 0; i1 < w.length; i1++) mf.register(WORD, depReader.normalize(w[i1]));

			w = instance.lemmas;
			for (int i1 = 0; i1 < w.length; i1++) mf.register(WORD, w[i1]);

			for(int i1 = 1; i1 < w.length; i1++)  {
				String op = getOperation(instance, i1);
				if (ops.get(op)==null) ops.put(op, 1);
				else ops.put(op, (ops.get(op)+1));	
				
				HashMap<String,Integer> operations4lemma  = lemma2ops.get(instance.lemmas[i1]);
				if (operations4lemma == null) 	lemma2ops.put(instance.lemmas[i1], operations4lemma= new HashMap<String,Integer>());
				operations4lemma.put(op, operations4lemma.get(op)==null?1:(operations4lemma.get(op)+1));

				HashMap<String,Integer> lemma4operations  = ops2lemma.get(op);
				if (lemma4operations == null) 	ops2lemma.put(op, lemma4operations= new HashMap<String,Integer>());
				lemma4operations.put(instance.lemmas[i1], lemma4operations.get(instance.lemmas[i1])==null?1:(lemma4operations.get(instance.lemmas[i1])+1));
			}

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
			w = instance.ofeats;
			for (int i1 = 0; i1 < w.length; i1++) if (w[i1]!=null) mf.register(FEAT, w[i1]);
			w = instance.pfeats;
			for (int i1 = 0; i1 < w.length; i1++) if (w[i1]!=null) mf.register(FEAT, w[i1]);


			
			
			
			for (int j = 0; j < w.length; j++) {
				
				HashMap<String,Integer> form2freq = lemma2formfreq.get(instance.lemmas[j]);
				if (form2freq ==null   ) lemma2formfreq.put(instance.lemmas[j], form2freq = new HashMap<String,Integer>());
				Integer freq =form2freq.get(instance.forms[j]);
				if (freq==null) form2freq.put(instance.forms[j], 1);
			}

			
			
			
			
			//depReader.insert(is, instance);

			if ((ic-1)>max) break;
		}


		for(Entry<String,HashMap<String,Integer>> e  : lemma2formfreq.entrySet()) {
			
			if (e.getValue().size()==1 &&  e.getValue().get(e.getKey())==null && countUpperCase((String)e.getValue().keySet().toArray()[0])>1) {
				//DB.println("found unique mapping for lemma "+e.getKey()+" with values "+e.getValue());
				lemma2form.put(e.getKey(),(String)e.getValue().keySet().toArray()[0]);
				
							
			}
			
		}
		
		DB.println("found direct mappings "+lemma2form.size());
		
		System.out.print("remove seldom operations: ");
		HashSet<String> seldom = new HashSet<String>();
		for(Entry<String,HashMap<String,Integer>> e  : ops2lemma.entrySet()) {
			
			if (e.getValue().size()==1&& ((Integer)e.getValue().values().toArray()[0])<2) {
				
				System.out.print(e.getKey()+":"+e.getValue()+" ");
				//DB.println("found unique operation op "+e.getKey()+" for lemma "+e.getValue());
				seldom.add(e.getKey());
				
				//lemma2form.put(e.getKey(),(String)e.getValue().keySet().toArray()[0]);
						
			}
			
		}
		System.out.println();
		
		int count=0, totalOps=0;
		for(Entry<String, Integer> e : ops.entrySet()) {
			totalOps++;
			if (e.getValue()>1) 
			if (!seldom.contains(e.getKey())) {
				mf.register(OPERATION, e.getKey());
				count++;
			}
		}
		DB.println("Found operations "+totalOps+" used operations "+count);



		del = outValue(ic, del);
		System.out.println();

		mf.calculateBits();

		System.out.println(mf.toString());


		DB.println("init instances "+ic+"    icnt   "+max);
		is.init(ic, new MFO());

		System.out.println();


		depReader.startReading(file);		

		int num1=0;
		while (true) {
			if (num1 % 100 == 0)  {
				del = outValue(num1, del);
//				DB.println("break "+icount+"   "+num1+" "+(num1 >= icount));
						}

			SentenceData09 instance = depReader.getNext(is);

			if (instance == null)
				break;
			is.fillChars(instance, num1,instance.lemmas,Extractor._CEND);
			if ( max<num1) {
//				DB.println("break "+icount+"   "+num1+" "+(num1 >= icount));
				
				break;
			}

			num1++;

		}

		del = outValue(num1, del);

		System.out.print(" processed "+is.size());

	}





	/**
	 * Count the number of upper caes in a string 
	 * @param value
	 * @return
	 */
	private static int countUpperCase(String s) {
		
		int upperCaseCount=0;
		for(char c :s.toCharArray() ) {
			if (Character.isUpperCase(c)) upperCaseCount++;
		}
		return upperCaseCount;
	}

	public static String getOperation(SentenceData09 instance1, int i1) {

		// inverse to lemmatizer
		String t = new StringBuffer(instance1.forms[i1]).reverse().toString().toLowerCase();
		String s = new StringBuffer(instance1.lemmas[i1]).reverse().toString().toLowerCase();

		StringBuffer po = new StringBuffer();
		String op;
		if (!s.equals(t)) {

			int[][] d =StringEdit.LD(s, t);
			StringEdit.searchPath(s,t,d, po, false);
			op = po.toString();
		} else op ="0"; // do nothing
		return op;
	}




	/**
	 * Write the lemma that are not mapped by operations
	 * @param dos
	 */
	static  void writeMap(DataOutputStream dos) {

		try {
			dos.writeInt(lemma2form.size());
			for(Entry<String, String> e : lemma2form.entrySet()) {
				dos.writeUTF(e.getKey());
				dos.writeUTF(e.getValue());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}



	/**
	 * Read the form-lemma mapping not read by operations
	 * @param dis
	 */
	static public void readMap(DataInputStream dis) {
		try {
			int size = dis.readInt();
			for(int i =0; i<size;i++) {
				lemma2form.put(dis.readUTF(), dis.readUTF());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}




}
