/**
 * 
 */
package sem2syn2;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import rt.util.DB;
import sem2syn2.SemConverter.Added;
import sem2syn2.Semantic.Edge;
import sem2syn2.Semantic.Str;

/**
 * @author Dr. Bernd Bohnet, 06.10.2010
 * 
 * 
 */
public class DSyntConverter {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Options options = new Options(args);
		
		CONLLReader09 depReader = new CONLLReader09(options.trainfile);
		CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);
		
		
		
		int cnt = 0; 
		int del=0;
	
		System.out.print("Processing Sentence: ");
		Data data=null;


		while(true) {

		//	Instances09 is = new Instances09();
		//	is.init(1, pipe.mf,options.formatTask);

	//		SentenceData09 instance = pipe.nextInstance(is, depReader);
			SentenceData09 instance = depReader.getNext();
			if (instance==null) break;
			cnt++;

//			Graph semGraph  = Semantic.convert(instance);
//			Graph semGraph  = Semantic.convertTMethod(instance);
			
		//	SentenceData09 outInst = convertSynt(instance,new);
			
			System.out.println("cnt "+cnt);
		//	Graph g  = convert(instance);
			
		}
		
		System.out.println("non connected "+notConnectedCount);

	}

	public static class Word {
		public String plable;
		public String form, gpos,ppos,lemma,lable,feats ;
		public String feat[];
		public sem2syn2.DSyntConverter.Word head;
		public Word phead;
		
		public Word(SentenceData09 i, int k, String ft ) {
			form = i.forms[k];
			lemma = i.lemmas[k];
			gpos = i.gpos[k];
			ppos = i.ppos[k];
			lable =i.labels[k];
			if (ft!=null)feats ="id="+k+(ft.length()>0?"|"+ft:"");
		}
		
		/**
		 * @param a
		 */
		public Word(Added a) {
			head=a.head;
			
	//		DB.println("lable1 "+a.label1+" label2 "+a.label2);
			
			lable=a.label1;
			a.dep.head =this;
			a.dep.lable=a.label2;
			gpos="POS";
			ppos="POS";
			form=a.name;
			lemma=a.name;
			
		}

		/**
		 * 
		 */
		public Word() {
			// TODO Auto-generated constructor stub
		}

		public String toString() {
			return ""+lable+" "+lemma+" "+gpos+" ";
		}
		
	}
	
	
	/**
	 * @param instance
	 * @param delete 
	 * @param added 
	 * @param feats 
	 * @return
	 */
	public static SentenceData09 convertSynt(SentenceData09 instance, 
			ArrayList<Integer> delete, ArrayList<Added> added, String[] feats) {
		
		ArrayList<Word> snt = new ArrayList<Word>();
		for(int k=0; k< instance.length();k++) {
			snt.add(new Word(instance,k, feats[k]));
		}

		for(int k=0; k< instance.length();k++) {
			Word w = snt.get(k);
			int h =instance.heads[k];
			if (h>=0) {
				Word hw = snt.get(h);
				w.head=hw;
			}
		}

		for(int k=0; k< added.size();k++) {
			Added add =added.get(k);
			
			add.head = snt.get(add.parent);
			add.dep = snt.get(add.dependent);
		}

		
		
		ArrayList<Word> del = new ArrayList<Word>();
		
		for(int k=0; k< snt.size();k++) {
			
			Word w = snt.get(k);
//			if (w.ppos.equals("TO")||w.form.equals(",")) {
			if (delete.contains(k)) { //elem)w.ppos.equals("TO")||w.form.equals(",")
					ArrayList<Word> cs = getChilds(snt, k);
					del.add(w);

				// reattache childs

					
					
					Word head =w.head;
				if (delete.contains(snt.indexOf(head))) {
					//DB.println("head is deleted as well use head of head");
					
					head = head.head;
					if (delete.contains(snt.indexOf(head))) {
						
						DB.println("Warning: head  of head is deleted - no recovery possible");
						
					}					
		//			continue;
				}
			
				for (Word c : cs) {
					c.head=head;
				}
				
				dels.add(w.form);
			
			}
		}

		// check if any of the head words was deleted
		for(int k=1; k< snt.size();k++) {
			if (snt.indexOf(snt.get(k).head)==-1) {
				DB.println("error! head was deleted "+snt.get(k)+" head "+snt.get(k).head);
		//		System.exit(0);
			}
		
		}

		SentenceData09 out = new SentenceData09();

		snt.removeAll(del);
		for(int k=0;k<snt.size();k++) {
//			System.out.println(k+"\t"+snt.get(k).form+"\t"+snt.get(k).ppos+"\t"+snt.indexOf(snt.get(k).head)+"\t"+snt.get(k).lable);
		}
		
		for(Added a : added) {
			Word w =new Word(a);
			
			snt.add(w);
		}
		
//		if (instance.length()!=snt.size()) {
//			DB.println(instance.length()+"\tn"+snt.size()+"\n");
//		}
		
		// rmove root
		int nodes = snt.size()-1;
		
		out.forms = new String[nodes];
		out.lemmas = new String[nodes];
		out.pfeats = new String[nodes];
		out.gpos = new String[nodes];
		out.ppos = null;
		out.heads = new int[nodes];
		out.labels= new String[nodes];
		out.labels = new String[nodes];
		
		
		// ignore the root node !!!
		for(int k=0;k<nodes;k++) {
			Word w =snt.get(k+1);
			
			
			out.forms[k] = w.form;
			
			
			out.lemmas[k]=w.lemma;
			out.lemmas[k]=w.lemma;
			out.heads[k]=snt.indexOf(w.head);
			out.gpos[k]=w.gpos;
			out.labels[k]=w.lable;
		//	out.pfeats[k] = new String[1];
			out.pfeats[k]= w.feats;
//			System.out.println(""+w.feats);
//			System.out.println(k+"\t"+snt.get(k).form+"\t"+snt.get(k).ppos+"\t"+snt.indexOf(snt.get(k).head)+"\t"+snt.get(k).lable);
		}

		
		// the root was removed but keep print MATE read friendly 
//		out.heads[0]=0;
//		out.labels[0]="sroot";
		
		return out;
	}


	static HashSet<String> dels = new HashSet<String>();
	
	/**
	 * @param snt
	 * @param k
	 * @return
	 */
	private static ArrayList<Word> getChilds(ArrayList<Word> snt, int h) {
	 
		ArrayList<Word> childs = new ArrayList<Word>();
		
		for(int k=0; k< snt.size(); k++) {
			
			if (snt.get(k).head == snt.get(h)) {
				childs.add(snt.get(k));
			}
			
		}
		
		
		return childs;
	}


	static int count =0;



	/**
	 * @param target
	 * @param i
	 * @return
	 */
	private static int getChild(int target, SentenceData09 i) {
		
		for(int k=1; k<i.length();k++) {
			if (i.heads[k]==target) return k;
		}
		
		
		return -1;
	}
	
	public static int notConnectedCount=0;

	public static Edge getSemEdge(SentenceData09 i, int n, int p, String l) {
		
		StringBuffer key = new StringBuffer();
		key.append(l);
	
	//	Edge e = null;
		Edge e = mappingList.get(key.toString());
	
		
		
		key.append('*').append(i.gpos[n]);
	
		// take the more specific
		Edge e1 = mappingList.get(key.toString());
		if (e1!=null) return e1;
	
		key = new StringBuffer();
		key.append(l).append('@').append(i.gpos[p]);

		// find head 
		Edge e2 = mappingList.get(key.toString());
		if (e2!=null) return e2;
		
		return e;	
	}
	
	public static Str getSemStr(SentenceData09 i, int n, int p, String l) {
		
		StringBuffer key = new StringBuffer();
		key.append(l);
	
		Str e = mappingStr.get(key.toString());
		key.append('*').append(i.gpos[n]);

		// use the more specific one
		Str e1 = mappingStr.get(key.toString());
		if (e1!=null) e=e1;
			
		
		return e;	
	}

	
	final public static HashMap<String, Edge> mappingList = new HashMap<String, Edge>();
	final public static HashMap<String, Str> mappingStr = new HashMap<String, Str>();

	static {
		mappingList.put("CONJ", new Edge(true, "A2"));
		mappingList.put("COORD*CC", new Edge(true, "A1"));
		mappingList.put("NMOD*CD", new Edge(false, "A1"));
		mappingList.put("TMP*CD", new Edge(false, "A1"));
		mappingList.put("PMOD*CD", new Edge(false, "A1"));
//		mappingList.put("HMOD*CD", new Edge(false, "1"));
		mappingList.put("ADV*IN", new Edge(false, "A1"));
		mappingList.put("ADV*TO", new Edge(false, "A1"));
		mappingList.put("ADV*NN", new Edge(false, "A1"));
		mappingList.put("ADV*RB", new Edge(false, "A1"));
		mappingList.put("ADV*WRB", new Edge(false, "A1"));
		mappingList.put("AMOD", new Edge(false, "A1"));
		mappingList.put("APPO*JJ", new Edge(false, "A1"));
		mappingList.put("APPO*JJR", new Edge(false, "A1"));
		mappingList.put("APPO*VBD", new Edge(false, "A1"));
		mappingList.put("APPO*VBN", new Edge(false, "A1"));
		mappingList.put("APPO*VBG", new Edge(false, "A1"));
		mappingList.put("BNF", new Edge(false, "A1"));
		mappingList.put("DIR*RP", new Edge(false, "A1"));
		mappingList.put("LOC", new Edge(false, "A1"));
		mappingList.put("MNR", new Edge(false, "A1"));
		mappingList.put("PRN", new Edge(false, "A1"));
		mappingList.put("PUT", new Edge(false, "A1"));
		mappingList.put("LOC-OPRD", new Edge(true, "A3"));
		mappingList.put("LOC-PRD", new Edge(true, "A2"));
		mappingList.put("LOC-TMP", new Edge(false, "A1"));
		mappingList.put("MNR*RP", new Edge(false, "PHRAS"));
		mappingList.put("MNR-PRD", new Edge(false, "A2"));
		mappingList.put("MNR-TMP", new Edge(false, "A1"));
		mappingList.put("NAME", new Edge(true, "PHRAS"));
		mappingList.put("HYPH", new Edge(true, "PHRAS"));
		mappingList.put("OBJ", new Edge(true, "A2"));
		mappingList.put("OPRD", new Edge(true, "A2"));
		mappingList.put("PMOD", new Edge(true, "A2"));
		mappingList.put("POSTHON", new Edge(true, "PHRAS"));
		mappingList.put("PRD", new Edge(true, "A2"));
		mappingList.put("PRD-PRP", new Edge(true, "A2"));
		mappingList.put("PRD-TMP", new Edge(true, "A2"));
		mappingList.put("PRP*IN", new Edge(false, "A1"));
		mappingList.put("PRP*TO", new Edge(false, "A1"));
		mappingList.put("PRP*WRB", new Edge(false, "A1"));
		mappingList.put("PRP*RB", new Edge(false, "A1"));
		mappingList.put("SBJ", new Edge(true, "A1"));
		mappingList.put("SUB", new Edge(true, "A1"));
		mappingList.put("SUFFIX", new Edge(false, "A2"));
		mappingList.put("ADV-GAP", new Edge(true, "A2_gap"));
		mappingList.put("AMOD-GAP", new Edge(true, "A2_gap"));
		mappingList.put("AMOD-GAP", new Edge(true, "A2_gap"));
		mappingList.put("DIR-GAP", new Edge(true, "A2_gap"));
		mappingList.put("DTV-GAP", new Edge(true, "A2_gap"));
		mappingList.put("EXT-GAP", new Edge(true, "A2_gap"));
		mappingList.put("EXT-GAP", new Edge(true, "A2_gap"));
		mappingList.put("EXT-GAP", new Edge(true, "A2_gap"));
		mappingList.put("EXTR-GAP", new Edge(true, "A2_gap"));
		mappingList.put("GAP-LGS", new Edge(true, "A2_gap"));
		mappingList.put("GAP-LOC", new Edge(true, "A2_gap"));
		mappingList.put("GAP-LOC-PRD", new Edge(true, "A2_gap"));
		mappingList.put("GAP-MNR", new Edge(true, "A2_gap"));
		mappingList.put("GAP-NMOD", new Edge(true, "A2_gap"));
		mappingList.put("GAP-OBJ", new Edge(true, "A2_gap"));
		mappingList.put("GAP-OPRD", new Edge(true, "A2_gap"));							
		mappingList.put("GAP-PMOD", new Edge(true, "A2_gap"));
		mappingList.put("GAP-PRD", new Edge(true, "A2_gap"));
		mappingList.put("GAP-PRP", new Edge(true, "A2_gap"));
		mappingList.put("GAP-PUT", new Edge(true, "A2_gap"));
		mappingList.put("GAP-SBJ", new Edge(true, "A2_gap"));
		mappingList.put("GAP-SUB", new Edge(true, "A2_gap"));
		mappingList.put("GAP-TMP", new Edge(true, "A2_gap"));
		mappingList.put("GAP-VC", new Edge(true, "A2_gap"));
		mappingList.put("ROOT", new Edge(true, "A1"));
		mappingList.put("DEP", new Edge(false, "A1"));
//		mappingList.put("DEP*CC", new Edge(false, "1"));
		mappingList.put("NMOD*DT", new Edge(false, "A1"));
		mappingList.put("NMOD*IN", new Edge(false, "A2"));
		mappingList.put("NMOD*JJ", new Edge(false, "A1"));
		mappingList.put("NMOD*JJR", new Edge(false, "A1"));
		mappingList.put("NMOD*JJS", new Edge(false, "A1"));
		mappingList.put("NMOD*NNP", new Edge(false, "A1"));
		mappingList.put("NMOD*NNPS", new Edge(false, "A1"));
		mappingList.put("NMOD*NN", new Edge(false, "A2"));
		mappingList.put("NMOD*NNS", new Edge(false, "A2"));
		mappingList.put("NMOD*RBR", new Edge(false, "A2"));
		mappingList.put("NMOD*RBS", new Edge(false, "A2"));
		mappingList.put("NMOD*RB", new Edge(false, "A1"));
		mappingList.put("NMOD*VBD", new Edge(false, "A1"));
		mappingList.put("NMOD*PRP$", new Edge(true, "A1"));
		mappingList.put("NMOD*TO", new Edge(true, "DEL:2"));
		mappingList.put("NMOD*VBD", new Edge(false, "A1"));
		mappingList.put("VC@MD", new Edge(false, "A2")); // head a mod

		
	

		
		
		
		;
		mappingList.put("HMOD", new Edge(true, "PHRAS"));
		mappingList.put("IM", new Edge(true, "PHRAS"));
		mappingList.put("TITLE", new Edge(false, "A1"));
		mappingList.put("PRT", new Edge(true, "PHRAS"));
		mappingList.put("PRT", new Edge(true, "PHRAS"));
		mappingList.put("NMOD*VBG", new Edge(true, "PHRAS"));


		mappingStr.put("TMP", new Str("A1","A2","TIME",1 ));
		mappingStr.put("VOC", new Str("A1","A2","ADDRESSEE",1 ));
		mappingStr.put("PRN", new Str("A1","A2","ADJUNCTION",1 ));
		mappingStr.put("LOC*IN", new Str("A1","A2","LOCATION",1 ));
		mappingStr.put("LOC*TO", new Str("A1","A2","LOCATION",1 ));
		mappingStr.put("LOC*WRB", new Str("A1","A2","LOCATION",1 ));
		mappingStr.put("LOC*RB", new Str("A1","A2","LOCATION",1 ));
		mappingStr.put("LOC*NN", new Str("A1","A2","LOCATION",1 ));
		mappingStr.put("DIR*IN", new Str("A1","A2","DIRECTION",1 ));
		mappingStr.put("DIR*IN", new Str("A1","A2","DIRECTION",1 ));
		mappingStr.put("DIR*WRB", new Str("A1","A2","DIRECTION",1 ));
		mappingStr.put("DIR*RB", new Str("A1","A2","DIRECTION",1 ));
		mappingStr.put("DIR*NN", new Str("A1","A2","DIRECTION",1 ));
		mappingStr.put("DIR*NN", new Str("A1","A2","DIRECTION",1 ));
		mappingStr.put("COORD*NN", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*JJ", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*NNS", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*NNP", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*CD", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*MD", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*VBD", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*VBZ", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*VBP", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*VBN", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*VB", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*IN", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*RB", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*$", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*HYPH", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*JJR", new Str("A1","A2","and",1 ));
		mappingStr.put("COORD*VBG", new Str("A1","A2","and",1 ));
		
		mappingStr.put("COORD*:", new Str("A1","A2","and",1 ));
		mappingStr.put("APPO*NN", new Str("A1","A2","ELABORATION",1 ));
		mappingStr.put("ADV*VB", new Str("A1","A2","N",1 ));
		mappingStr.put("ADV*VBD", new Str("A1","A2","N",1 ));
		mappingStr.put("ADV*VBZ", new Str("A1","A2","N",1 ));
		mappingStr.put("ADV*JJ", new Str("A1","A2","N",1 ));
		mappingStr.put("AM-DIR", new Str("A1","A2","N",1 ));
		mappingStr.put("APPO*NNS", new Str("A1","A2","ELABORATION",1 ));
		mappingStr.put("APPO*NNP", new Str("A1","A2","ELABORATION",1 ));
		mappingStr.put("APPO*NNPS", new Str("A1","A2","ELABORATION",1 ));
		mappingStr.put("ADV*VBG", new Str("A1","A2","N",1 ));
		mappingStr.put("ADV*VBN", new Str("A1","A2","N",1 ));
		



	}

}
