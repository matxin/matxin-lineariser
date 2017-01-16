package morph2;

import is2.data.*;

import java.util.ArrayList;

import rt.util.DB;
/**
 * @author Dr. Bernd Bohnet, 12.05.2010
 * 
 * The features extractions builds the structured features.  
 * 
 * The task of this class is to put in a vector the structured features. The structures features consists of 
 * parts such as characters, lemma, part-of-speech. In form of a string this would look like:
 * 
 * F1+NN+hello+'h'
 * 
 * A structured features is build out of numbers. let be F1 1, NN 3, hello 1000, and the character 'h' 7.
 * The numbers are mapped to a long number where for each element a part of the long number is reserved. 
 * The mapping of the string to the number does MFO which contains the static mapping methods. 
 * The long number is than mapped to integer number by l2i.   
 */
final public class Extractor {

	// contains the number of bits for the feature parts
	public static short s_rel,s_pos, s_word,s_type,s_dir,s_dist,s_feat,s_child,s_ops,s_char;

	// feature index mapping 
	static public Long2IntInterface li;

	static MFO mf = new MFO();

	// element to structured features mapping containers
	final D4 dcp = new D4(li), dwwp = new D4(li), dwwwp = new D4(li),dwp = new D4(li),dp = new D4(li), dwc = new D4(li);

	/**
	 * The constructor of the features extractor
	 * 
	 * @param m elements to number mappings
	 * @param l2i hash based long to integer mapping
	 */
	public Extractor(Long2IntInterface l2i) { 
		//		mf=m;
		li =l2i;
	}

	/**
	 */
	public static void initMappings() {
		s_rel = MFO.getFeatureBits(REL);
		s_pos = MFO.getFeatureBits(POS);
		s_word = MFO.getFeatureBits(WORD);
		s_type = MFO.getFeatureBits(TYPE);
		s_ops = MFO.getFeatureBits(Pipe.OPERATION);
		s_char = MFO.getFeatureBits(CHAR);
		s_feat = MFO.getFeatureBits(Pipe.FEAT);		
	}

	public void init(){
		dwp.a0 = s_type; 	dwp.a1 = s_ops;   dwp.a2 = s_word; 	dwp.a3 = s_pos; 	dwp.a4 = s_pos; dwp.a5 = s_word;
		dp.a0 = s_type; 	dp.a1 = s_ops;   dp.a2 = s_pos; 	dp.a3 = s_pos; 	dp.a4 = s_pos; dp.a5 = s_pos;
		dwwp.a0 = s_type;   dwwp.a1 = s_ops;  dwwp.a2 = s_word; dwwp.a3 = s_word; dwwp.a4 = s_pos; dwwp.a5 = s_pos;dwwp.a6 = s_pos;
		dwwwp.a0 = s_type;  dwwwp.a1 = s_ops; dwwwp.a2 = s_word; dwwwp.a3 = s_word; dwwwp.a4 = s_word; dwwwp.a5 = s_pos;dwwwp.a6 = s_pos;
		dcp.a0  = s_type; dcp.a1 = s_ops; dcp.a2 = s_char;dcp.a3 = s_char; dcp.a4 = s_char; dcp.a5 = s_char; dcp.a6 = s_pos; dcp.a7 = s_pos;	
		dwc.a0  = s_type; dwc.a1 = s_ops; dwc.a2 = s_word;dwc.a3 = s_char; dwc.a4 = s_char; dwc.a5 = s_char; dwc.a6 = s_pos; dcp.a7 = s_pos;	

	}

	public static final String REL = "REL";
	private static final String END = "END";


	protected static final String TYPE = "TYPE",CHAR = "C",DIR = "D",POS = "POS",DIST = "DIST",MID = "MID", WORD = "WORD";


	static int  _CEND;

	static int _strp;


	private static final String STPOS = "STPOS";

	/*
	 *  the feature type index
	 *  
	 *  Why is this not in a array? Because it is to the max maximized for speed.
	 */
	private static int _f1,_f2,_f3,_f4,_f5,_f6,_f7,_f8,_f9,_f10,_f11,_f12,_f13,_f14,_f15,_f16,_f17,_f18,_f19,_f20;
	private static int _f21,_f22,_f23,_f24,_f25,_f26,_f27,_f28,_f29,_f30,_f31,_f32,_f33,_f34,_f35,_f36,_f37,_f38,_f39,_f40,_f41;
	private static int _f42,_f43,_f44,_f45,_f46,_f47,_f48,_f49,_f50,_f51,_f52,_f53,_f54,_f55,_f56,_f57,_f58,_f59,_f60;
	private static int _f61,_f62,_f63,_f64,_f65,_f66,_f67,_f68,_f69,_f70,_f71,_f72,_f73,_f74,_f75,_f76,_f77,_f78,_f79,_f80;
	private static int nofeat;


	/**
	 * Initialize the features types.
	 */
	static public void initFeatures() {

		// This method is designed for speed and therefore its not just a loop which it could be!

		_strp =mf.register(POS, MID);mf.register(TYPE, POS);mf.register(POS,STPOS);mf.register(TYPE, CHAR); mf.register(TYPE, Pipe.FEAT);

		nofeat= mf.register(Pipe.FEAT, "NOFEAT");
		_CEND = mf.register(CHAR, END);

		_f1 = mf.register(TYPE, "F1");  _f2 = mf.register(TYPE, "F2");  _f3 = mf.register(TYPE, "F3");  _f4 = mf.register(TYPE, "F4");
		_f5 = mf.register(TYPE, "F5");  _f6 = mf.register(TYPE, "F6");  _f7 = mf.register(TYPE, "F7");  _f8 = mf.register(TYPE, "F8");
		_f9 = mf.register(TYPE, "F9");  _f10 = mf.register(TYPE, "F10");_f11 = mf.register(TYPE, "F11");_f12 = mf.register(TYPE, "F12");
		_f13 = mf.register(TYPE, "F13");_f14 = mf.register(TYPE, "F14");_f15 = mf.register(TYPE, "F15");_f16 = mf.register(TYPE, "F16");
		_f17 = mf.register(TYPE, "F17");_f18 = mf.register(TYPE, "F18");_f19 = mf.register(TYPE, "F19");_f20 = mf.register(TYPE, "F20");
		_f21 = mf.register(TYPE, "F21");_f22 = mf.register(TYPE, "F22");_f23 = mf.register(TYPE, "F23");_f24 = mf.register(TYPE, "F24");
		_f25 = mf.register(TYPE, "F25");_f26 = mf.register(TYPE, "F26");_f27 = mf.register(TYPE, "F27");_f28 = mf.register(TYPE, "F28");
		_f29 = mf.register(TYPE, "F29");_f30 = mf.register(TYPE, "F30");_f31 = mf.register(TYPE, "F31");_f32 = mf.register(TYPE, "F32");
		_f33 = mf.register(TYPE, "F33");_f34 = mf.register(TYPE, "F34");_f35 = mf.register(TYPE, "F35");_f36 = mf.register(TYPE, "F36");
		_f37 = mf.register(TYPE, "F37");_f38 = mf.register(TYPE, "F38");_f39 = mf.register(TYPE, "F39");_f40 = mf.register(TYPE, "F40");
		_f41 = mf.register(TYPE, "F41");_f42 = mf.register(TYPE, "F42");_f43 = mf.register(TYPE, "F43");_f44 = mf.register(TYPE, "F44");
		_f45 = mf.register(TYPE, "F45");_f46 = mf.register(TYPE, "F46");_f47 = mf.register(TYPE, "F47");_f48 = mf.register(TYPE, "F48");
		_f49 = mf.register(TYPE, "F49");_f50 = mf.register(TYPE, "F50");_f51 = mf.register(TYPE, "F51");_f52 = mf.register(TYPE, "F52");
		_f53 = mf.register(TYPE, "F53");_f54 = mf.register(TYPE, "F54");_f55 = mf.register(TYPE, "F55");_f56 = mf.register(TYPE, "F56");
		_f57 = mf.register(TYPE, "F57");_f58 = mf.register(TYPE, "F58");_f59 = mf.register(TYPE, "F59");_f60 = mf.register(TYPE, "F60");
		_f61 = mf.register(TYPE, "F61");_f62 = mf.register(TYPE, "F62");_f63 = mf.register(TYPE, "F63");_f64 = mf.register(TYPE, "F64");
		_f65 = mf.register(TYPE, "F65");_f66 = mf.register(TYPE, "F66");_f67 = mf.register(TYPE, "F67");_f68 = mf.register(TYPE, "F68");
		_f69 = mf.register(TYPE, "F69");_f70 = mf.register(TYPE, "F70");_f71 = mf.register(TYPE, "F71");_f72 = mf.register(TYPE, "F72");
		_f73 = mf.register(TYPE, "F73");_f74 = mf.register(TYPE, "F74");_f75 = mf.register(TYPE, "F75");_f76 = mf.register(TYPE, "F76");
		_f77 = mf.register(TYPE, "F77");_f78 = mf.register(TYPE, "F78");_f79 = mf.register(TYPE, "F79");

	}





	/**
	 * Extracts and builds the complex features.
	 * 
	 * @param is the input data of the sentences in form of numbers.
	 * @param i the index of the selected sentence 
	 * @param op the operation to come from the lemma to the form
	 * @param w the running number of word of the sentence
	 * @param lemma the lemma or what does it look like
	 * @param f the feature vector
	 * @param vs 
	 */
	public void extractFeatures(InstancesTagger is, int i, int w, int opi, String lemma, IFV f, long[] vs) {

		int len = is.length(i);
		
		

		int pos =		(int) (dwp.v5=   (w==1?1:w==2?3:w==3?3:3));   

		int n=1, c=0;
		dwp.v0 = n++; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.cz3(); f.add(li.l2i(vs[c++]=dwp.getVal()));
		dwp.v0 = n++; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=pos;dwp.cz4(); f.add(li.l2i(vs[c++]=dwp.getVal()));
		dwp.v0 = n++; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gfeats[i][w];  dwp.cz4(); f.add(li.l2i(vs[c++]=dwp.getVal()));		

		dwp.v0 = n++; dwp.v1 = opi; dwp.v3=is.gfeats[i][w]; dwp.cz3(); f.add(li.l2i(vs[c++]=dwp.getVal()));		


		dwp.v0 = n++; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gfeats[i][w]; dwp.v4=is.gpos[i][w]; dwp.cz5(); f.add(li.l2i(vs[c++]=dwp.getVal()));		
		dwp.v0 = n++; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gfeats[i][w]; dwp.v4=is.gpos[i][w]; dwp.v5=pos;  dwp.cz6(); f.add(li.l2i(vs[c++]=dwp.getVal()));		

		
		dwwp.v0 = n++; dwwp.v1 = opi; dwwp.v2=is.gfeats[i][w]; dwwp.v3=w>0?is.gfeats[i][w-1]:s_word-1;  dwwp.cz4(); f.add(li.l2i(vs[c++]=dwwp.getVal()));	
		dwwp.v0 = n++; dwwp.v1 = opi; dwwp.v2=is.gfeats[i][w]; dwwp.v3=len>w+1?is.gfeats[i][w+1]:s_word-1;  dwwp.cz4(); f.add(li.l2i(vs[c++]=dwwp.getVal()));	
		
		dwwp.v0 = n++; dwwp.v1 = opi; dwwp.v2=is.gfeats[i][w];dwwp.v3=is.gpos[i][w]; dwwp.cz4(); f.add(li.l2i(vs[c++]=dwwp.getVal()));	
		dwwp.v0 = n++; dwwp.v1 = opi; dwwp.v2=is.gfeats[i][w];dwwp.v3=is.gpos[i][w]; dwwp.v4=pos; dwwp.cz5(); f.add(li.l2i(vs[c++]=dwwp.getVal()));	

		dwwwp.v0 = n++; dwwwp.v1 = opi; dwwwp.v2=is.gfeats[i][w]; dwwwp.v3=len>w+1?is.gfeats[i][w+1]:s_word-1; dwwwp.v4  =is.glemmas[i][w];  		dwwwp.cz5(); f.add(li.l2i(vs[c++]=dwwwp.getVal()));	

		
		dwwp.v1 = opi;
		if (w>0) {

			dwwp.v0 = n;    dwwp.v2 = is.gpos[i][w]; dwwp.v3=is.gpos[i][w-1]; dwwp.v4=pos; dwwp.cz5(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		
			dwwp.v0 = n+1;   dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.gpos[i][w-1]; dwwp.v4=pos; dwwp.cz5(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		
			dwwp.v0 = n+2;  dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.glemmas[i][w-1]; dwwp.v4=pos; dwwp.cz5(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		

			dwwp.v0 = n+3;   dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.gfeats[i][w-1]; dwwp.cz4(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		
			dwwp.v0 = n+4;   dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.gfeats[i][w]; dwwp.v4=is.gpos[i][w-1]; dwwp.v5=pos; dwwp.cz6(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		

		}
		n+=5;
		
		if (w>1) {	
			dwwp.v0 = n;  dwwp.v2 = is.gpos[i][w]; dwwp.v3=is.gpos[i][w-1]; dwwp.v4=is.gpos[i][w-2]; dwwp.v5=pos; dwwp.cz6(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		
			dwwp.v0 = n+1;  dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.gfeats[i][w-1]; dwwp.v4=is.gfeats[i][w-2]; dwwp.cz5(); f.add(li.l2i(vs[c++]=dwwp.getVal()));	
			dwwp.v0 = n+2;  dwwp.v2=is.gfeats[i][w]; dwwp.v3=is.gfeats[i][w-1]; dwwp.v4=is.gfeats[i][w-2]; dwwp.cz5(); f.add(li.l2i(vs[c++]=dwwp.getVal()));	
		}
		n+=3;
		if (len>w+1) {

			dwwp.v0 = n; dwwp.v1 = opi; dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.glemmas[i][w+1]; dwwp.v4=is.gfeats[i][w]; dwwp.cz5(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		
			dwwp.v0 = n+1; dwwp.v1 = opi; dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.glemmas[i][w+1]; dwwp.v4=pos; dwwp.cz5(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		
		}
		n+=2;



		if (is.feats[i][w]!=null)
			for(int j=0;j<is.feats[i][w].length;j++) {
				dwwp.v0 = n; dwwp.v1 = opi; dwwp.v2=is.feats[i][w][j];dwwp.v3=is.gpos[i][w];dwwp.cz4(); f.add(li.l2i(vs[c++]=dwwp.getVal()));	

			}
		n++;


		int e0 =is.chars[i][w][6], e1 =is.chars[i][w][7],e2 =is.chars[i][w][8],e3 =is.chars[i][w][9],e4 =is.chars[i][w][10];


		dcp.v0 = n++; dcp.v1 = opi; dcp.v2=e0; dcp.v3=is.gfeats[i][w]; dcp.cz4(); f.add(li.l2i(vs[c++]=dcp.getVal()));	 
		dcp.cz3(); f.add(li.l2i(vs[c++]=dcp.getVal()));	

		if (lemma.length()>1) {
			dcp.v0 = n; dcp.v2=e0; dcp.v3=e1; dcp.v4 =is.gfeats[i][w]; dcp.cz5(); f.add(li.l2i(vs[c++]=dcp.getVal()));	
		}
		n+=1;

		if (lemma.length()>2) {
			dcp.v0 = n;  dcp.v2=e0; dcp.v3=e1; dcp.v4  =e2;dcp.v5 =is.gfeats[i][w]; dcp.cz6(); f.add(li.l2i(vs[c++]=dcp.getVal()));	
		}
		n+=1;

		if (lemma.length()>3) {
			dcp.v0 = n;  dcp.v2=e0; dcp.v3=e1; dcp.v4  =e2; dcp.v5 =e3; dcp.v6 =is.gfeats[i][w]; dcp.cz7(); f.add(li.l2i(vs[c++]=dcp.getVal()));	
		}
		n+=1;

		if (lemma.length()>4) {
			dcp.v0 = n;  dcp.v2=e0; dcp.v3=e1; dcp.v4  =e2; dcp.v5 =e3; dcp.v6 =e4; dcp.v7 =is.gfeats[i][w]; dcp.cz8(); f.add(li.l2i(vs[c++]=dcp.getVal()));	
		}
		n+=1;

		int c0= is.chars[i][w][0], c1=is.chars[i][w][1], c2=is.chars[i][w][2], c3=is.chars[i][w][3], c4=is.chars[i][w][4];

		dcp.v0 = n++; dcp.v1 = opi; dcp.v2=c0; dcp.v3=is.gfeats[i][w]; dcp.cz3(); f.add(li.l2i(vs[c++]=dcp.getVal()));	

		if (lemma.length()>1) {
			dcp.v0 = n;  dcp.v2=c0; dcp.v3=c1; dcp.v4 =is.gfeats[i][w]; 			dcp.cz4(); f.add(li.l2i(vs[c++]=dcp.getVal()));	
		}
		n+=1;

		if (lemma.length()>2) {
			dcp.v0 = n;  dcp.v2=c0; dcp.v3=c1; dcp.v4  =c2;dcp.v5 =is.gfeats[i][w]; dcp.cz5(); f.add(li.l2i(vs[c++]=dcp.getVal()));	
		}
		n+=1;

		if (lemma.length()>3) {
			dcp.v0 = n;  dcp.v2=c0; dcp.v3=c1; dcp.v4  =c2; dcp.v5 =c3; dcp.v6 =is.gfeats[i][w]; dcp.cz6(); f.add(li.l2i(vs[c++]=dcp.getVal()));	
		}
		n+=1;

		if (lemma.length()>4) {
			dcp.v0 = n; dcp.v2=c0; dcp.v3=c1; dcp.v4  =c2; dcp.v5 =c3; dcp.v6 =c4; dcp.v7 =is.gfeats[i][w]; dcp.cz7(); f.add(li.l2i(vs[c++]=dcp.getVal()));	
		}
		n+=1;

		dwp.v0 = n++; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gfeats[i][w]; dwp.v4=is.gpos[i][w]; dwp.v5=is.labels[i][w];  dwp.v6=pos;    
		dwp.cz7(); f.add(li.l2i(vs[c++]=dwp.getVal()));		

	
		n++;

		int[] children = getChildren2(w, is,i);

		if (children.length>0  ) {
			int[] childrenChild = getChildren2(children[0], is,i);



			// get the coordinations
			int cc = (childrenChild.length>0)?is.gpos[i][childrenChild[0]]:_strp;
			cc = (childrenChild.length>1)?is.gpos[i][childrenChild[1]]:_strp;


			dwwp.v0 = n; dwwp.v1 = opi; dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.glemmas[i][children[0]]; dwwp.v4=cc;;//;children[0]<w?1:2; 
			dwwp.v5=is.labels[i][children[0]]; dwwp.v6=is.gfeats[i][w]; dwwp.v7=is.gfeats[i][children[0]]; dwwp.cz8(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		

			dwp.v0 = n+1; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gpos[i][children[0]]; dwp.v4=cc;//children[0]<w?1:2; 
			dwp.v5=is.labels[i][children[0]]; dwp.v6=is.gfeats[i][w]; dwp.v7=is.gfeats[i][children[0]]; dwp.cz8(); f.add(li.l2i(vs[c++]=dwp.getVal()));	

			dwp.v0 = n+2; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gpos[i][children[0]]; dwp.v4=is.gfeats[i][children[0]];
			dwp.v5=is.labels[i][children[0]]; dwp.v6=is.gfeats[i][w]; dwp.v7=children[0]<w?1:2; ; dwp.cz8(); f.add(li.l2i(vs[c++]=dwp.getVal()));	

			dwp.v0 = n+3; dwp.v1 = opi; dwp.v2 = is.glemmas[i][children[0]]; dwp.v3=is.gpos[i][w]; dwp.v4=cc;//children[0]<w?1:2; 
			dwp.v5=is.labels[i][children[0]]; dwp.v6=is.gfeats[i][w]; dwp.v7=is.gfeats[i][children[0]]; dwp.cz8(); f.add(li.l2i(vs[c++]=dwp.getVal()));		
			
		}
		n+=5;

		// backup agreement
		for(int e=0;e<children.length;e++) {
			dwp.v0 = n; dwp.v1 = opi; dwp.v2 = is.glemmas[i][children[e]]; dwp.v3=is.gpos[i][w]; //dwp.v4=cc;//children[0]<w?1:2; 
			dwp.v4=is.labels[i][children[e]]; dwp.v6=is.gfeats[i][w]!=-1?is.gfeats[i][w]:63;  dwp.cz7(); f.add(li.l2i(vs[c++]=dwp.getVal()));		

			dwp.v0 = n+1; dwp.v1 = opi; dwp.v2 = is.gpos[i][children[e]]; dwp.v3=is.gpos[i][w]; //dwp.v4=cc;//children[0]<w?1:2; 
			dwp.v4=is.labels[i][children[e]]; dwp.v6=is.gfeats[i][w]!=-1?is.gfeats[i][w]:63; dwp.v7=is.gfeats[i][children[e]]!=-1?is.gfeats[i][children[e]]:63;  
			dwp.cz8(); f.add(li.l2i(vs[c++]=dwp.getVal()));		
			
			dwp.v0 = n+2; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gpos[i][w]; dwp.v4=is.labels[i][e];  dwp.v5=pos;dwp.v6=is.gpos[i][e];    
			dwp.cz7(); f.add(li.l2i(vs[c++]=dwp.getVal()));	

		}
		n+=3;
		if (children.length>1  ) {
			int[] childrenChild = getChildren2(children[1], is,i);

			int cc = (childrenChild.length>0)?is.gpos[i][childrenChild[0]]:_strp;

			dwwp.v0 = n; dwwp.v1 = opi; dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.glemmas[i][children[1]]; dwwp.v4=cc;//children[1]<w?1:2; 
			dwwp.v5=is.labels[i][children[1]]; dwwp.v6=is.gfeats[i][w]; dwwp.v6=is.gfeats[i][children[1]]; dwwp.cz8(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		

			dwp.v0 = n+1; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gpos[i][children[1]]; dwp.v4=cc;//children[1]<w?1:2; 
			dwp.v5=is.labels[i][children[1]]; dwp.v6=is.gfeats[i][w]; dwp.v7=is.gfeats[i][children[1]]; dwp.v8=children[1]<w?1:2; dwp.cz9(); f.add(li.l2i(vs[c++]=dwp.getVal()));		

			dwp.v0 = n+2; dwp.v1 = opi; dwp.v2 = is.glemmas[i][children[1]]; dwp.v3=is.gpos[i][w]; dwp.v4=children[1]<w?1:2; 
			dwp.v5=is.labels[i][children[1]]; dwp.v6=is.gfeats[i][w]; dwp.v7=is.gfeats[i][children[1]]; dwp.cz8(); f.add(li.l2i(vs[c++]=dwp.getVal()));		


		}
		n+=3;
		if (children.length>2  ) {
			int[] childrenChild = getChildren2(children[2], is,i);

			int cc = (childrenChild.length>0)?is.gpos[i][childrenChild[0]]:_strp;

			dwwp.v0 = n; dwwp.v1 = opi; dwwp.v2 = is.glemmas[i][w]; dwwp.v3=is.glemmas[i][children[2]]; dwwp.v4=cc;//children[2]<w?1:2; 
			dwwp.v5=is.labels[i][children[2]]; dwwp.v6=is.gfeats[i][w]; dwwp.v6=is.gfeats[i][children[2]]; dwwp.cz8(); f.add(li.l2i(vs[c++]=dwwp.getVal()));		

			dwp.v0 = n+1; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w]; dwp.v3=is.gpos[i][children[2]]; dwp.v4=cc;//children[2]<w?1:2; 
			dwp.v5=is.labels[i][children[2]]; dwp.v6=is.gfeats[i][w]; dwp.v7=is.gfeats[i][children[2]]; dwp.cz8(); f.add(li.l2i(vs[c++]=dwp.getVal()));		


		}
		n+=2;


		int th = is.pheads[i][w];

		// removed due to an error
	//	dwp.v0 = n++; dwp.v1 = opi; dwp.v2 = is.glemmas[i][w] ; dwp.v3=is.gpos[i][w]; dwp.v4=is.chars[i][th][0];dwp.cz5(); f.add(li.l2i(vs[c++]=dwp.getVal()));		

		if (is.length(i)>w+1) {
			dwc.v0 = n; dwc.v1 = opi; dwc.v2 = is.glemmas[i][w] ; dwc.v3=is.gpos[i][w]; dwc.v4=is.chars[i][w+1][0]; dwc.cz5(); f.add(li.l2i(vs[c++]=dwc.getVal()));		
			dwc.v0 = n+1; dwc.v1 = opi; dwc.v2 = is.glemmas[i][w] ; dwc.v3=is.gpos[i][w]; dwc.v4=is.chars[i][w+1][0]; dwc.v5=is.chars[i][w+1][1]; dwc.cz6(); f.add(li.l2i(vs[c++]=dwc.getVal()));
		}
		n+=1;
		vs[n] = Integer.MIN_VALUE;



		n++;

	}

	public static int[] getChildren2(int h, Instances is, int n) {

		int cnt=0;

		// count siblings
		for(int i=0;i<is.length(n);i++) if (is.pheads[n][i]==h) cnt++;
		int[] children = new int[cnt];
		cnt=0;
		for(int i=0;i<is.length(n);i++) 
			if (is.pheads[n][i]==h) children[cnt++]=i;



		return children;
	}

	

	public DataNN fillVector(String[] operations, int k, F2SF f, InstancesTagger is, int inst, String lemma, DataNN d) {

		long ts = System.nanoTime();

		final int length = operations.length;
		if (d ==null || d.len<length) d = new DataNN(length);


		long[] vs = new long[200];
		extractFeatures(is, inst,  k, 0,lemma, f,vs); 
		for(int i = 0;i<length;i++ ) {

			f.clear();
			int opi =mf.getValueS(Pipe.OPERATION, operations[i])*Extractor.s_type;
			for(int j=0;j<vs.length;j++) {
				if (vs[j]>0) f.add(li.l2i(vs[j] +opi));	
			}
			d.abh[i][0]= f.score;

		}
		//	timeExtract += (System.nanoTime()-ts);

		return d;
	}

	/**
	 * @param is
	 * @param n
	 * @param k
	 * @param gold
	 * @param lemma 
	 * @param pred2
	 */
	public void createVector(InstancesTagger is, int i, int k, String gold, String lemma, FV f) {

		f.clear();
		long vs[] = new long[200];
		int opi =mf.getValueS(Pipe.OPERATION, gold);
		extractFeatures(is, i, k, opi,lemma, f,vs); 

	}

}
