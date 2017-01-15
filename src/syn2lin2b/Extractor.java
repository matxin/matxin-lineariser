package syn2lin2b;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import rt.model.Environment;
import rt.model.Graph;
import rt.model.IGraph;
import syn2lin2.Decoder.Path;
import gnu.trove.TIntHashSet;
import is2.data.*;


final public class Extractor {
	
	public static short s_rel,s_pos, s_word,s_type,s_dir,s_dist,seat,s_child;

	private static int s_question;

	final public MFO mf;
	final private Long2IntInterface li;

	final MFO.Data4 d0 = new MFO.Data4(), dl1 = new MFO.Data4(),  dl2 = new MFO.Data4();
	final MFO.Data4 dr = new MFO.Data4(), drrw = new MFO.Data4(),  drrww = new MFO.Data4();
	final MFO.Data4 drrrw = new MFO.Data4(); 
//	final MFO.Data4 drw = new MFO.Data4();
	final MFO.Data4 dwwp = new MFO.Data4();
	final MFO.Data4 dwwwp = new MFO.Data4();
	final MFO.Data4 dw = new MFO.Data4();
	final MFO.Data4 dwp = new MFO.Data4();

	final MFO.Data4 dlf = new MFO.Data4();

	public Extractor(MFO m, Long2IntInterface l2i) { 
		mf=m;
		li =l2i;
	}

	public static void init(MFO mf) {
		s_rel = MFO.getFeatureBits(REL);
		s_pos = MFO.getFeatureBits(POS);
		s_word = MFO.getFeatureBits(WORD);
		s_type = MFO.getFeatureBits(TYPE);
		s_dir = MFO.getFeatureBits(DIR);
	
		s_dist = MFO.getFeatureBits(DIST);
		seat = MFO.getFeatureBits(Pipe.FEAT);
		
		s_question =mf.getValue(Pipe.WORD, "?");
	}

	public void init(){
		d0.a0 = s_type;d0.a1 = s_pos;d0.a2 = s_pos;d0.a3 = s_pos;d0.a4 = s_pos;d0.a5 = s_pos;d0.a6 = s_pos;d0.a7 = s_pos;
		dl1.a0 = s_type;dl1.a1 = s_rel; dl1.a2 = s_pos;dl1.a3 = s_pos; dl1.a4 = s_pos; dl1.a5 = s_pos; dl1.a6 = s_pos; dl1.a7 = s_pos;	
		dl2.a0 = s_type;dl2.a1 = s_rel;dl2.a2 = s_word;dl2.a3 = s_pos;dl2.a4 = s_pos;dl2.a5 = s_pos;dl2.a6 = s_pos;dl2.a7 = s_pos;
		dwp.a0 = s_type; 	dwp.a1 = s_rel; 	dwp.a2 = s_word; 	dwp.a3 = s_pos; 	dwp.a4 = s_pos; dwp.a5 = s_word;
		dwwp.a0 = s_type; dwwp.a1 = s_rel; dwwp.a2 = s_word; dwwp.a3 = s_word; dwwp.a4 = s_pos; dwwp.a5 = s_pos;dwwp.a6 = s_pos;
		dwwwp.a0 = s_type; dwwwp.a1 = s_rel; dwwwp.a2 = s_word; dwwwp.a3 = s_word; dwwwp.a4 = s_word; dwwwp.a5 = s_pos;dwwwp.a6 = s_pos;
		dlf.a0 = s_type;dlf.a1 = s_rel; dlf.a2 = s_pos;dlf.a3 = s_pos; dlf.a4 = seat; dlf.a5 = seat; dlf.a6 = s_pos; dlf.a7 = s_pos;	
		dr.a0  = s_type;dr.a1 = s_rel; dr.a2 = s_rel;dr.a3 = s_pos; dr.a4 = s_pos; dr.a5 = s_pos; dr.a6 = s_pos; dr.a7 = s_pos;	
		drrw.a0  = s_type;drrw.a1 = s_rel; drrw.a2 = s_rel;drrw.a3 = s_word; drrw.a4 = s_word; drrw.a5 = s_word; drrw.a6 = s_pos; drrw.a7 = s_pos;	
		drrww.a0  = s_type;drrww.a1 = s_rel; drrww.a2 = s_rel;drrww.a3 = s_word; drrww.a4 = s_word; drrww.a5 = s_pos; drrww.a6 = s_pos; drrww.a7 = s_pos;	
		drrrw.a0  = s_type;drrrw.a1 = s_rel; drrrw.a2 = s_rel;drrrw.a3 = s_rel; drrrw.a4 = s_word; drrrw.a5 = s_pos; drrrw.a6 = s_pos; drrrw.a7 = s_pos;	

	}

	public static final String REL = "REL";
	private static final String END = "END";
	private static final String STR = "STR";
	private static final String LA = "LA";
	private static final String RA = "RA";

	

	private static int s_str;
	private static int s_end;
	private static int s_stwrd;

	protected static final String TYPE = "TYPE";
	private static final String CHAR = "C";
	private static final String DIR = "D";
	public static final String POS = "POS";
	protected static final String DIST = "DIST";
	private static final String MID = "MID";

	private static final String _0 = "0";
	private static final String _4 = "4";
	private static final String _3 = "3";
	private static final String _2 = "2";
	private static final String _1 = "1";
	private static final String _5 = "5";
	private static final String _10 = "10";

	private static  int di0, d4,d3,d2,d1,d5,d10;


	private static final String WORD = "WORD";

	private static final String STWRD = "STWRD";
	private static final String STPOS = "STPOS";


	private static int nofeat;



	/**
	 * Initialize the features.
	 * @param maxFeatures
	 */
	static public void initFeatures(MFO mf) {

		mf.register(POS, MID);
		s_str = mf.register(POS, STR);
		s_end = mf.register(POS, END);

		mf.register(TYPE, POS);

		s_stwrd=mf.register(WORD,STWRD);
		mf.register(POS,STPOS);

		 mf.register(DIR, LA);
		 mf.register(DIR, RA);

		mf.register(TYPE, CHAR);

		mf.register(TYPE, Pipe.FEAT);
		nofeat=mf.register(Pipe.FEAT, "NOFEAT");

		

		for(int i=0;i<127;i++)
			mf.register(TYPE, "F"+i);



		di0=mf.register(DIST, _0);
		d1=mf.register(DIST, _1);
		d2=mf.register(DIST, _2);
		d3=mf.register(DIST, _3);
		d4=mf.register(DIST, _4);
		d5=mf.register(DIST, _5);
		//		d5l=mf.register(DIST, _5l);
		d10=mf.register(DIST, _10);
	}
	
	

	
	/**
	 * @param gs
	 * @param w1
	 * @param w2
	 * @param label
	 * @param inW2 
	 * @param inW1 
	 * @param f
	 * @param distPath 
	 * @param dist2 
	 */
	public void extractFeaturesX(Instances is, int i, int w1, int w2, int label, int inW1, int inW2, IFV f) {
		
		int f1 = is.glemmas[i][w1];
		int f2 = is.glemmas[i][w2];
		
		int hP = is.heads[i][w1]==-1?this.s_str:is.gpos[i][is.heads[i][w1]];

		int hF = is.heads[i][w1]==-1?this.s_stwrd:is.forms[i][is.heads[i][w1]];

		int headInRel = is.heads[i][w1]==-1?-1:is.labels[i][is.heads[i][w1]];

		
		int pos1 =is.gpos[i][w1];
		int pos2 =is.gpos[i][w2];

		int childs1=0;
		int childs2=0;
		for(int k=0;k<is.gpos[i].length;k++) {
			if(is.heads[i][k]==w1) childs1++;
			if(is.heads[i][k]==w2) childs2++;
		}
		
		long l;		

		int h1 = is.heads[i][w1]==w2?1:is.heads[i][w2]==w1?2:3;
		
		// w1 w2 head 
		// w1 head w2 
		// head w1 w2
		// head w1
		// w1 head 
		// head w2 
		// w2 head
		inW1 =h1;
		
		
	    int c1 = is.feats[i][w1]!=null&&is.feats[i][w1].length>0?is.feats[i][w1][0]:s_end;
        int c2 = is.feats[i][w2]!=null&&is.feats[i][w2].length>0?is.feats[i][w2][0]:s_end;

    dwwp.v0 = 10;     dwwp.v1 = label; dwwp.v2 = pos1; dwwp.v3 = pos2;dwwp.v4 = c1; l= mf.calc5(dwwp); f.add(li.l2i(l));    
     dwwp.v0 = 11;     dwwp.v1 = label; dwwp.v2 = pos1; dwwp.v3 = pos2;dwwp.v4 = c2;l= mf.calc5(dwwp); f.add(li.l2i(l));     
		
		// label-w1 + label-w2
		drrrw.v0 = 19; drrrw.v1 = label; drrrw.v2 = is.labels[i][w1]; drrrw.v3 = is.labels[i][w2]; 
		l= mf.calc4(drrrw); f.add(li.l2i(l)); 		

		// label-w2 + form-w1 
		drrww.v0 = 20; drrww.v1 = label; drrww.v2 = is.labels[i][w2]; drrww.v3 = f1; 
		l= mf.calc4(drrww); f.add(li.l2i(l)); 		

		// label-w2 + form-w2
		drrww.v0 = 70; drrww.v1 = label; drrww.v2 = is.labels[i][w2]; drrww.v3 = f2; 
		l= mf.calc4(drrww); f.add(li.l2i(l)); 		
		
		// label-w1 + form-w1
		drrww.v0 = 71; drrww.v1 = label; drrww.v2 = is.labels[i][w1]; drrww.v3 = f1; 
		l= mf.calc4(drrww); f.add(li.l2i(l)); 		

		
		// label-w1 + form-w2
		drrww.v0 = 72; drrww.v1 = label; drrww.v2 = is.labels[i][w1]; drrww.v3 = f2; 
		l= mf.calc4(drrww); f.add(li.l2i(l)); 		
		
		// pos-w1 + pos-w2
		dl1.v0= 21; dl1.v1=label; dl1.v2=pos1; dl1.v3=pos2; 
        l= mf.calc4(dl1); f.add(li.l2i(l));  f.add(li.l2i(dl1.calcs(s_pos,inW1,l))); 	

        // from-w1 + form-w2
		dwwp.v0 = 8; 	dwwp.v1 = label; dwwp.v2 = f1; dwwp.v3 = f2; 
		l= mf.calc4(dwwp); f.add(li.l2i(l)); 		

		
		// pos-w1 + pos-w2 + head-pos 
		dl1.v0= 22; dl1.v1=label; dl1.v2=pos1; dl1.v3=pos2; dl1.v4=hP; // dl1.v5=inW1; dl1.v6=inW2;
        l= mf.calc5(dl1); f.add(li.l2i(l));  f.add(li.l2i(dl1.calcs(s_pos,inW1,l))); 	
		
        
	

		dr.v0= 25; dr.v1=is.labels[i][w1]; dr.v2=is.labels[i][w2]; dr.v3=hP; dr.v4=inW1;
        l= mf.calc5(dr);  f.add(li.l2i(l)); //f.add(li.l2i(dr.calcs(s_pos,inW1,l))); 	

		drrw.v0= 26; drrw.v1=is.labels[i][w1]; drrw.v2=is.labels[i][w2]; drrw.v3=hF;
        l= mf.calc4(drrw); f.add(li.l2i(l)); f.add(li.l2i(drrw.calcs(s_pos,inW1,l))); 		


		drrw.v0= 27; drrw.v1=is.labels[i][w1]; drrw.v2=is.labels[i][w2]; drrw.v3=f1; drrw.v4=pos2;
        l= mf.calc5(drrw); f.add(li.l2i(l)); f.add(li.l2i(drrw.calcs(s_pos,inW1,l))); 		
        
		drrw.v0= 28; drrw.v1=is.labels[i][w1]; drrw.v2=is.labels[i][w2]; drrw.v3=f2; drrw.v4=pos1;
        l= mf.calc5(drrw); f.add(li.l2i(l)); f.add(li.l2i(drrw.calcs(s_pos,inW1,l))); 		
        
    	
        
        drrrw.v0= 30; drrrw.v1=is.labels[i][w1]; drrrw.v2=is.labels[i][w2]; drrrw.v3=headInRel; drrrw.v4=f1;drrrw.v5=pos2;
        l= mf.calc6(drrrw); f.add(li.l2i(l)); f.add(li.l2i(drrrw.calcs(s_pos,inW1,l))); 		
        
        drrrw.v0= 31; drrrw.v1=is.labels[i][w1]; drrrw.v2=is.labels[i][w2]; drrrw.v3=headInRel; drrrw.v4=f2;drrrw.v5=pos1;
        l= mf.calc6(drrrw); f.add(li.l2i(l)); f.add(li.l2i(drrrw.calcs(s_pos,inW1,l))); 		
        
        drrrw.v0= 32; drrrw.v1=is.labels[i][w1]; drrrw.v2=is.labels[i][w2]; drrrw.v3=headInRel; drrrw.v4=hF;drrrw.v5=pos1; drrrw.v6=pos2;
        l= mf.calc7(drrrw); f.add(li.l2i(l)); f.add(li.l2i(drrrw.calcs(s_pos,inW1,l))); 		
   
        drrrw.v0= 33; drrrw.v1=is.labels[i][w1]; drrrw.v2=is.labels[i][w2]; drrrw.v3=headInRel; drrrw.v4=hP;drrrw.v5=pos1; drrrw.v6=pos2;
        l= mf.calc7(drrrw); f.add(li.l2i(l)); f.add(li.l2i(drrrw.calcs(s_pos,inW1,l))); 		
   
        //drrww.v0= 34; drrww.v1=is.labels[i][w1]; drrww.v2=is.labels[i][w2]; drrww.v4=f1;drrww.v5=f2;
        drrww.v0= 34; drrww.v1=is.labels[i][w1]; drrww.v2=is.labels[i][w2];  drrww.v3 = f2;  drrww.v4=f1;drrww.v5=f2;
        l= mf.calc6(drrww); f.add(li.l2i(l)); f.add(li.l2i(drrww.calcs(s_pos,inW1,l))); 
 
        drrrw.v0= 36; drrrw.v1=is.labels[i][w1]; drrrw.v2=is.labels[i][w2]; drrrw.v3=headInRel; drrrw.v4=hP;drrrw.v5=childs1; drrrw.v6=pos2;
        l= mf.calc7(drrrw); f.add(li.l2i(l)); f.add(li.l2i(drrrw.calcs(s_pos,inW1,l))); 		
   
        drrrw.v0= 37; drrrw.v1=is.labels[i][w1]; drrrw.v2=is.labels[i][w2]; drrrw.v3=headInRel; drrrw.v4=hP;drrrw.v5=pos1; drrrw.v6=childs2;
        l= mf.calc7(drrrw); f.add(li.l2i(l)); f.add(li.l2i(drrrw.calcs(s_pos,inW1,l))); 		
        
        dwwp.v0= 39; dwwp.v1=headInRel; dwwp.v2=f1; dwwp.v3=f2; dwwp.v4=hP; dwwp.v5=pos1; dwwp.v6=pos2;
        l= mf.calc7(dwwp); f.add(li.l2i(l)); f.add(li.l2i(dwwp.calcs(s_pos,inW1,l))); 		
        
	}

	
	


	/**
	 * @param is
	 * @param inst
	 * @param w1
	 * @param w2
	 * @param i2 
	 * @param f
	 */
	public void extractTrigrams(Instances is, int i, int w1, int w2, int w3, int i2, IFV f) {
	
		int pos1 = is.gpos[i][w1];
		int pos2 = is.gpos[i][w2];
		int pos3 = is.gpos[i][w3];
		
		
		//if(true) return;
		
		int f1 = is.glemmas[i][w1];
		int f2 = is.glemmas[i][w2];
		int f3 = is.glemmas[i][w2];
		
		int h1=0;
		if(is.heads[i][w1]==w2||is.heads[i][w3]==w2) {
			h1=2;
		} else if(is.heads[i][w2]==w1||is.heads[i][w3]==w1) {
			h1=1;
		} else if(is.heads[i][w1]==w3||is.heads[i][w2]==w3) {
			h1=3;
		} 
	
		
		long l; 
		dl1.v0 = 45; dl1.v1 = h1; dl1.v2 = pos1; dl1.v3 = pos2; dl1.v4 = pos3 ;
    	l= mf.calc5(dl1); f.add(li.l2i(l)); f.add(li.l2i(dl1.calcs(s_pos,i2,l)));		

		dwwp.v0 = 46; dwwp.v1 = h1; dwwp.v2 = f1; dwwp.v3 = f2; dwwp.v4 = pos3; 
		l= mf.calc5(dwwp); f.add(li.l2i(l)); 	f.add(li.l2i(dwwp.calcs(s_pos,i2,l)));		

		dwwp.v0 = 47; dwwp.v1 = h1; dwwp.v2 = f1; dwwp.v3 = f3; dwwp.v4 = pos2; 
		l= mf.calc5(dwwp); f.add(li.l2i(l)); 	f.add(li.l2i(dwwp.calcs(s_pos,i2,l)));		

		dwwwp.v0 = 48; dwwwp.v1 = h1; dwwwp.v2 = f1; dwwwp.v3 = f2; dwwwp.v4 = f3; 
		l= mf.calc5(dwwwp); f.add(li.l2i(l)); 	f.add(li.l2i(dwwwp.calcs(s_pos,i2,l)));		

		dwwwp.v0 = 49; dwwwp.v1 = h1; dwwwp.v2 = f1; dwwwp.v3 = f3;   
		l= mf.calc4(dwwwp); f.add(li.l2i(l)); 	f.add(li.l2i(dwwwp.calcs(s_pos,i2,l)));		

//		dwwp.v0 = 52; dwwp.v1 = h1; dwwp.v2 = f2; dwwp.v3 = f3; dwwp.v4 = pos1;  
//		l= mf.calc5(dwwp); f.add(li.l2i(l)); 	f.add(li.l2i(dwwp.calcs(s_pos,i2,l)));		

		
		drrrw.v0 = 50; drrrw.v5 =h1  ; drrrw.v1 = is.labels[i][w1]; drrrw.v2 = is.labels[i][w2]; drrrw.v3 = is.labels[i][w3]; 
		drrrw.v4= 	f1;  	drrrw.v5= 	pos2; drrrw.v6=  i2; 
		l= mf.calc6(drrrw); f.add(li.l2i(l)); f.add(li.l2i(drrrw.calcs(s_pos,i2,l)));			

		drrrw.v0 = 51; drrrw.v5 =h1  ; drrrw.v1 = is.labels[i][w1]; drrrw.v2 = is.labels[i][w2]; drrrw.v3 = is.labels[i][w3]; 
		drrrw.v4= 	f2;  	drrrw.v5= 	pos1;
		l= mf.calc6(drrrw); f.add(li.l2i(l)); f.add(li.l2i(drrrw.calcs(s_pos,i2,l)));			

	
		
	}

	/**
	 * Extract the global features
	 * @param is the sentences 
	 * @param i the id of a sentneces in the sentneces list
	 * @param p a path 
	 * @param f the features or score
	 */
	public void extractGlobal(Instances is, int i, int[] path, IFV f) {
		
				
		
		int head =-1;
		// get head
		for(int w1 : path) {
			for(int w2 : path) {
				if (is.heads[i][w1] ==w2) {
					head=w2;
				}
			}
		}
		
		// nodes before head
		int nodesBefore=0;
		for(int w1 : path) {
			if(w1==head) break;
			nodesBefore++;
		}
	
		
		// part of speech of the head 
		
		int posH = is.gpos[i][head]==-1?s_str:is.gpos[i][head];
		
		
		// part of speech of the head of the head
		int posHH = is.heads[i][head]==-1?s_str:is.heads[i][is.heads[i][head]];
		int fHH = is.heads[i][head]==-1?this.s_stwrd:is.glemmas[i][is.heads[i][head]];
		
		// lemma of the head
		int fH = is.glemmas[i][head];
		int relToHeadOfHead =  is.labels[i][head];
		
		// does the sentence have a question mark
		long l;
		int questionMark=0;
		for(int k=0;k<is.length(i);k++) {
			if (is.glemmas[i][k]==s_question) {
				questionMark=1;
				break;
			}
		}
		
		
		
		if(path.length>=2) {
			drrrw.v0=60; drrrw.v1=is.labels[i][path[0]]; drrrw.v2=is.labels[i][path[path.length-1]];drrrw.v3=is.labels[i][path[path.length-2]];
			drrrw.v4=posH; drrrw.v5=is.gpos[i][path[0]]; drrrw.v6=is.gpos[i][path[path.length-1]]; drrrw.v7=posHH;//is.gpos[i][path[path.length-2]]; 
	        l= mf.calc8(drrrw); f.add(li.l2i(l));  	
	       
		}
		
		if(path.length>3) {
			drrrw.v0=61; drrrw.v1=is.labels[i][path[0]]; drrrw.v2=is.labels[i][path[1]];drrrw.v3=is.labels[i][path[2]];
			drrrw.v4=posH; drrrw.v5=is.gpos[i][path[0]]; drrrw.v6=is.gpos[i][path[path.length-1]];  
	        l= mf.calc7(drrrw); f.add(li.l2i(l));  	f.add(li.l2i(drrrw.calcs(s_pos,questionMark,l)));

	        drrrw.v0=62; drrrw.v1=is.labels[i][path[0]]; drrrw.v2=is.labels[i][path[1]];drrrw.v3=is.labels[i][path[2]];
			drrrw.v4=fH; drrrw.v5=is.gpos[i][path[0]]; drrrw.v6=is.gpos[i][path[path.length-1]]; drrrw.v7=posHH;  
	        l= mf.calc8(drrrw); f.add(li.l2i(l));  	f.add(li.l2i(drrrw.calcs(s_pos,questionMark,l)));
	           
	   
		}

		if(path.length>4) {
			int len = path.length;
			dl1.v0 =63;dl1.v1 = relToHeadOfHead; dl1.v2 = nodesBefore;
			dl1.v3 = is.gpos[i][path[0]]; dl1.v4 = is.gpos[i][path[1]]; dl1.v5 = is.gpos[i][path[2]]; dl1.v6 = is.gpos[i][path[3]];
			dl1.v7 = is.gpos[i][path[len-1]]; 
			l= mf.calc8(dl1); f.add(li.l2i(l)); 

			dl1.v0 =64;dl1.v1 = relToHeadOfHead; dl1.v2 = nodesBefore;
			dl1.v3 = is.gpos[i][path[0]]; dl1.v4 = is.gpos[i][path[len-4]];; dl1.v5 = is.gpos[i][path[len-3]]; dl1.v6 = is.gpos[i][path[len-2]];
			dl1.v7 =  is.gpos[i][path[len-1]]; 
			l= mf.calc8(dl1); f.add(li.l2i(l)); 
		}		
		
		drrw.v0=65; drrw.v1=is.labels[i][path[0]]; drrw.v2=is.labels[i][path[path.length-1]];drrw.v3=fH;
		drrw.v4=is.glemmas[i][path[path.length-1]]; drrw.v5=is.glemmas[i][path[path.length-2]]; drrw.v6=questionMark;//is.gpos[i][path[path.length-2]]; 
		l= mf.calc7(drrw); f.add(li.l2i(l));  	


	
	}
}
