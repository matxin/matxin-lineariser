package sem2syn2;

import is2.data.DataF;
import is2.data.Edges;
import is2.data.FV;
import is2.data.IFV;
import is2.data.Long2IntInterface;
import is2.data.MFO;
import is2.data.Parse;
import is2.util.DB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import rt.model.Environment;
import rt.model.Graph;
import rt.model.IGraph;

final public class Extractor {
	
	public static short s_rel,s_pos, s_word,s_type,s_dir,s_dist,s_feat,s_child,s_arg;

	final public MFO mf;
	final public Long2IntInterface li;

	final MFO.Data4 d0 = new MFO.Data4();
	final MFO.Data4 dl1 = new MFO.Data4();
	final MFO.Data4 dl2 = new MFO.Data4();
	final MFO.Data4 dr = new MFO.Data4();
	final MFO.Data4 dwr = new MFO.Data4();
	final MFO.Data4 dwwp = new MFO.Data4();

	final MFO.Data4 dw = new MFO.Data4();
	final MFO.Data4 dwp = new MFO.Data4();

	final MFO.Data4 dlf = new MFO.Data4();

	public Extractor(MFO m, Long2IntInterface l2i) { 
		mf=m;
		li =l2i;
	}

	public static void init(MFO mf) {
		s_rel = mf.getFeatureBits(REL);
		s_pos = mf.getFeatureBits(POS);
		s_word = mf.getFeatureBits(WORD);
		s_type = mf.getFeatureBits(TYPE);
		s_dir = mf.getFeatureBits(DIR);
		la = mf.getValue(DIR, LA);
		ra = mf.getValue(DIR, RA);
		s_dist = mf.getFeatureBits(DIST);
		s_feat = mf.getFeatureBits(Pipe.FEAT);
		s_arg = mf.getFeatureBits(Pipe.ARG);
	}

	public void init(){
		d0.a0 = s_type;d0.a1 = s_pos;d0.a2 = s_pos;d0.a3 = s_pos;d0.a4 = s_pos;d0.a5 = s_pos;d0.a6 = s_pos;d0.a7 = s_pos;
		dl1.a0 = s_type;dl1.a1 = s_rel; dl1.a2 = s_arg;dl1.a3 = s_arg; dl1.a4 = s_arg; dl1.a5 = s_arg; dl1.a6 = s_arg; dl1.a7 = s_arg;	
		dl2.a0 = s_type;dl2.a1 = s_rel;dl2.a2 = s_word;dl2.a3 = s_pos;dl2.a4 = s_pos;dl2.a5 = s_pos;dl2.a6 = s_pos;dl2.a7 = s_pos;
		dwp.a0 = s_type; 	dwp.a1 = s_rel; 	dwp.a2 = s_word; 	dwp.a3 = s_arg; 	dwp.a4 = s_arg; dwp.a5 = s_arg;
		dwwp.a0 = s_type; dwwp.a1 = s_rel; dwwp.a2 = s_word; dwwp.a3 = s_word; dwwp.a4 = s_arg; dwwp.a5 = s_arg;
		dlf.a0 = s_type;dlf.a1 = s_rel; dlf.a2 = s_pos;dlf.a3 = s_pos; dlf.a4 = s_feat; dlf.a5 = s_feat; dlf.a6 = s_pos; dlf.a7 = s_pos;	

	}

	public void extractFeatures(short[] pposs, int from, int to, IFV f)
	{

		int dir= (from < to)? ra:la;
		d0.v0= _f39; d0.v1=pposs[from]; d0.v2=pposs[to]; //d0.stop=4;
		int end= (from >= to ? from : to);
		int start = (from >= to ? to : from) + 1;

		for(int i = start ; i <end ; i++) {
			d0.v3=pposs[i];
			long l= mf.calc4(d0);
			f.add(li.l2i(l=d0.calcs(s_dir,dir,l)));
		}

	}


	


	private void extractFeat(IFV f, int dir, short[] featsP, short[] featsD) {
		long l;
		if (featsP!=null && featsD!=null) {
			for(short i1=0;i1<featsP.length;i1++) {
				for(short i2=0;i2<featsD.length;i2++) {							
				    dlf.v4=featsP[i1]; dlf.v5=featsD[i2];
					l= mf.calc6(dlf); l=dlf.calcs(s_dir,dir,l); f.add(li.l2i(l));
				}
			} 
		} else if (featsP==null && featsD!=null) {

			for(short i2=0;i2<featsD.length;i2++) {							
				dlf.v4=nofeat; dlf.v5=featsD[i2];
				l= mf.calc6(dlf); l=dlf.calcs(s_dir,dir,l); f.add(li.l2i(l));

			}		
		} else if (featsP!=null && featsD==null) {

			for(short i1=0;i1<featsP.length;i1++) {							
				dlf.v4=featsP[i1]; dlf.v5=nofeat;
				l= mf.calc6(dlf); l=dlf.calcs(s_dir,dir,l); f.add(li.l2i(l));

			}		
		}
	}


	


	


	
	public static final String REL = "REL";
	private static final String END = "END";
	private static final String STR = "STR";
	private static final String LA = "LA";
	private static final String RA = "RA";

	private static int ra;
	private static int la;
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

	private static final String _F1 = "F1",_F2 = "F2",_F3 = "F3",_F4 = "F4",_F5 = "F5",_F6= "F6",_F7= "F7",_F8= "F8",_F9="F9",_F10 = "F10";
	private static final String _F11="F11",_F12="F12",_F13= "F13",_F14="F14",_F15="F15",_F16="F16",_F17="F17",_F18="F18",_F19="F19",_F20="F20";
	private static final String _F21="F21",_F22="F22",_F23= "F23",_F24="F24",_F25="F25",_F26="F26",_F27="F27",_F28="F28",_F29="F29",_F30="F30";
	private static final String _F31="F31",_F32="F32",_F33= "F33",_F34="F34",_F35="F35",_F36="F36",_F37="F37",_F38="F38",_F39="F39",_F40="F40";
	private static final String _F41="F41",_F42="F42",_F43= "F43",_F44="F44",_F45="F45",_F46="F46",_F47="F47",_F48="F48",_F49="F49",_F50="F50";
	private static final String _F51="F51",_F52="F52",_F53= "F53",_F54="F54",_F55="F55",_F56="F56",_F57="F57",_F58="F58",_F59="F59",_F60="F60";
	private static final String _F61="F61",_F62="F62",_F63= "F63",_F64="F64",_F65="F65",_F66="F66",_F67="F67",_F68="F68",_F69="F69",_F70="F70";
	private static final String _F71="F71",_F72="F72",_F73= "F73",_F74="F74",_F75="F75",_F76="F76",_F77="F77",_F78="F78",_F79="F79";

	private static final String _F1l = "F1l",_F2l = "F2l",_F3l = "F3l",_F4l = "F4l",_F5l = "F5l",_F6l= "F6l",_F7l= "F7l",_F8l= "F8l",_F9l="F9l",_F10l = "F10l";
	private static final String _F11l="F11l",_F12l="F12l",_F13l= "F13l", _F24l="F24l",_F25l="F25l",_F26l="F26l",_F27l="F27l",_F28l="F28l",_F29l="F29l",
	_F33l= "F33l",_F34l="F34l",_F35l="F35l",_F36l="F36l",_F37l="F37l",_F38l="F38l";


	private static int _f1,_f2,_f3,_f4,_f5,_f6,_f7,_f8,_f9,_f10,_f11,_f12,_f13,_f14,_f15,_f16,_f17,_f18,_f19,_f20;
	private static int _f21,_f22,_f23,_f24,_f25,_f26,_f27,_f28,_f29,_f30,_f31,_f32,_f33,_f34,_f35,_f36,_f37,_f38,_f39,_f40,_f41;
	private static int _f42,_f43,_f44,_f45,_f46,_f47,_f48,_f49,_f50,_f51,_f52,_f53,_f54,_f55,_f56,_f57,_f58,_f59,_f60;
	private static int _f61,_f62,_f63,_f64,_f65,_f66,_f67,_f68,_f69,_f70,_f71,_f72,_f73,_f74,_f75,_f76,_f77,_f78,_f79,_f80;
	private static int nofeat;

	private static int _f1l,_f2l,_f3l,_f4l,_f5l,_f6l,_f7l,_f8l,_f9l,_f10l,_f11l,_f12l,_f13l,
	_f24l,_f25l,_f26l,_f27l,_f28l,_f29l,_f33l,_f34l,_f35l,_f36l,_f37l,_f38l;

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

		la = mf.register(DIR, LA);
		ra = mf.register(DIR, RA);

		mf.register(TYPE, CHAR);

		mf.register(TYPE, Pipe.FEAT);
		nofeat=mf.register(Pipe.FEAT, "NOFEAT");

for(int k=0;k<50;k++) {
	mf.register(TYPE, "F"+k);
}
		


		di0=mf.register(DIST, _0);
		d1=mf.register(DIST, _1);
		d2=mf.register(DIST, _2);
		d3=mf.register(DIST, _3);
		d4=mf.register(DIST, _4);
		d5=mf.register(DIST, _5);
		//		d5l=mf.register(DIST, _5l);
		d10=mf.register(DIST, _10);
	}
	
	

	public void extractFeatures1(Graph g, int w1, int w2, int label, IFV f, int[][] dist, int[][][] distPath, long[] v) {
		// in the graph w1 and w2 are nodes
				
		
		int pWord = g.getContent(w1) ;
		int dWord =g.getContent(w2); //g.getContent(w2);
	
		
		int d = dist[w1][w2];
		int dir = d<0?1:2;
		d = Math.abs(d);
		
		long l;
		int n=0, p=1;
		
		dl1.v0 = p++; dl1.v1 = label; dl1.v2 = d; dl1.v3=dir; l=v[n++]= l=mf.calc4(dl1); f.add(li.l2i(l)); 		
		
		
		
		dwp.v0 = p++; dwp.v1 = label; dwp.v2 = pWord; dwp.v3 = d; dwp.v4 = dir; v[n++]= l=mf.calc5(dwp); f.add(li.l2i(l)); 
		dwp.v0 = p++; dwp.v1 = label; dwp.v2 = dWord; dwp.v3 = d; dwp.v4 = dir; 	v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l)); 		
		dwwp.v0 = p++; dwwp.v1 = label; dwwp.v2 = dWord; dwwp.v3 = d; dwwp.v4 = dir; 	v[n++]=l= mf.calc5(dwwp); f.add(li.l2i(l)); 		

//		if (true ) return ;
		
		ArrayList<Integer> inEdgeW1 = getInEdges(g,w1);
	
		if (inEdgeW1!=null && inEdgeW1.size()>0) {
			
			Collections.sort(inEdgeW1);
			dwp.v0 = p; dwp.v1 = label; dwp.v2 = inEdgeW1.get(0); dwp.v3 = d; dwp.v4 = dir; v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l));
			
			if (inEdgeW1.size()>1) {
				int k=0;
				for(int edge : inEdgeW1) {
					if(k>0 && k<3) {
						dwp.v0 = p+1; 	dwp.v1 = label; dwp.v2 = edge; dwp.v3 = d; dwp.v4 = dir; 	
						v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l));
					}
					k++;
					
							
				}		
			}
			dwp.v0 = p+2; 	dwp.v1 = label;  dwp.v2 = pWord;  dwp.v3 = inEdgeW1.get(0); dwp.v4 = d; dwp.v5 = dir; 	
			v[n++]=l= mf.calc6(dwp); f.add(li.l2i(l));

			dwp.v0 = p+3; 	dwp.v1 = label;  dwp.v2 = dWord;  dwp.v3 = inEdgeW1.get(0); dwp.v4 = d; dwp.v5 = dir; 	
			v[n++]=l= mf.calc6(dwp); f.add(li.l2i(l));
		
		}
		p+=4;
	
//		if (true) return;
		
		ArrayList<Integer> inEdge = getInEdges(g,w2);	
		if (inEdge!=null && inEdge.size()>0) {
			Collections.sort(inEdge);
			dwp.v0 = p; 	dwp.v1 = label; dwp.v2 = inEdge.get(0); dwp.v3 = d; dwp.v4 = dir;  v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l));
			if (inEdge.size()>1) {
				int k=0;
				for(int edge : inEdge) {
					if(k>0 && k<3) {
						dwp.v0 = p+1; 	dwp.v1 = label; dwp.v2 = edge; dwp.v3 = d; dwp.v4 = dir;  v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l));
					}
					k++;
					
							
				}		
			}
			dwp.v0 = p+2; 	dwp.v1 = label;  dwp.v2 = dWord;  dwp.v3 = inEdge.get(0); dwp.v4 = d; dwp.v5 = dir; 	
			v[n++]=l= mf.calc6(dwp); f.add(li.l2i(l));

			dwp.v0 = p+3; 	dwp.v1 = label;  dwp.v2 = pWord;  dwp.v3 = inEdge.get(0); dwp.v4 = d; dwp.v5 = dir; 	
			v[n++]=l= mf.calc6(dwp); f.add(li.l2i(l));

			}
		p+=4;
		if (distPath[w1][w2].length==1) {
//			dwp.v0 = p; 	dwp.v1 = label;  dwp.v2 = 1;  dwp.v3 = distPath[w1][w2][0]; dwp.v4 = d; dwp.v5 = dir; 
	//		v[n++]=l= mf.calc6(dwp); f.add(li.l2i(l));
			
		} else 	if (distPath[w1][w2].length==2 ) {
	//		dwp.v0 = p; 	dwp.v1 = label;  dwp.v2 = 1;  dwp.v3 = distPath[w2][w1][0]; dwp.v4 = d; dwp.v5 = dir; 
	//		v[n++]=l= mf.calc6(dwp); f.add(li.l2i(l));
		}
		p++;
	
	}

	public static HashMap<String,Integer> map = new HashMap<String,Integer>(); 
	
	public void extractFeatures(Graph g, int w1, int w2, int label, int[][] dist, int[][][] distPath, long[] v) {
		// in the graph w1 and w2 are nodes
				
		
		int pWord = g.getContent(w1) ;
		int dWord =g.getContent(w2); //g.getContent(w2);
	
		
		int d = dist[w1][w2];
		int dir = d<0?1:2;
		d = Math.abs(d);
		
		int n=0, p=1;
		
		int[] inEdge1 = getInEdgesI(g,w1);
		int[] inEdge2 = getInEdgesI(g,w2);

		// get the edge label
		int edge=-1;
		
		if (dir == 1) {
			int in[] = g.getIn(w1);
			if (in!=null)
			for(int k=1;k<=in[0];k++) {
				int in2[] = g.getIn(in[k]);
				if (in2[1]== w2) edge = g.getContent(in[k]);
			}
		} else {
			int in[] = g.getIn(w2);
			if (in!=null)
			for(int k=1;k<=in[0];k++) {
				int in2[] = g.getIn(in[k]);
				if (in2[1]== w1) edge = g.getContent(in[k]);
			}
			
		}
	//	System.out.println( " d "+d+" dir "+dir);
		long l =0;
		if (d==0) return;
	//	dl1.v0 = p++; dl1.v1 = label; dl1.v2 = d; v[n++]= mf.calc4(dl1);// f.add(li.l2i(l)); 		
	//	dl1.v0 = p++; dl1.v1 = label; dl1.v2 = dir; v[n++]= mf.calc4(dl1);// f.add(li.l2i(l)); 		
label=0;
		dl1.v0 = 1; dl1.v1 = 0; dl1.v2 = d; dl1.v3=dir; l=v[n++]= mf.calc4(dl1);// f.add(li.l2i(l)); 		
	//	map.put("dt "+d+" di  "+dir, (int)	li.l2i(l) );
//		dl1.v0 = 2; dl1.v1 = 0; dl1.v2 = d; dl1.v3=dir; dl1.v4=edge; l=v[n++]= mf.calc5(dl1);// f.add(li.l2i(l)); 		
	
		
		dwp.v1 = label;
		
//		dwp.v0 = p++;  dwp.v2 = pWord; dwp.v3 = d; dwp.v4 = dir; v[n++]= mf.calc5(dwp); //f.add(li.l2i(l)); 
//		dwp.v0 = p++;  dwp.v2 = dWord; dwp.v3 = d; dwp.v4 = dir; v[n++]= mf.calc5(dwp);// f.add(li.l2i(l)); 
		
//		dwp.v0 = p++;  dwp.v2 = pWord; dwp.v3 = d; dwp.v4 = dir; dwp.v5 = inEdge1==null?0: inEdge1.length; v[n++]= mf.calc5(dwp); //f.add(li.l2i(l)); 
//		dwp.v0 = p++;  dwp.v2 = dWord; dwp.v3 = d; dwp.v4 = dir; dwp.v5 = inEdge2==null?0:inEdge2.length; v[n++]= mf.calc5(dwp);// f.add(li.l2i(l)); 		

//		if (distPath[w1][w2].length>0 ) {
//			dwp.v0 = p++;  dwp.v2 = pWord; dwp.v3 = d; dwp.v4 = dir; dwp.v5 = distPath[w1][w2][0]; v[n++]= mf.calc5(dwp); //f.add(li.l2i(l)); 
//			dwp.v0 = p++;  dwp.v2 = dWord; dwp.v3 = d; dwp.v4 = dir; dwp.v5 =distPath[w1][w2][0]; v[n++]= mf.calc5(dwp);// f.add(li.l2i(l)); 		
//		}
		
//		dwwp.v0 = p++;  dwwp.v2 = dWord; dwwp.v3 = pWord; dwwp.v4 = d; dwwp.v5 = dir; v[n++]= mf.calc6(dwwp);// f.add(li.l2i(l)); 		
//		dwp.v0 = p++;  dwp.v2 = pWord; dwp.v3 = d; dwp.v4 = dir; v[n++]= mf.calc5(dwp); //f.add(li.l2i(l)); 
//		dwp.v0 = p++;  dwp.v2 = dWord; dwp.v3 = d; dwp.v4 = dir; v[n++]= mf.calc5(dwp);// f.add(li.l2i(l)); 		

//		if (true ) return ;
		
	
	
//		if (inEdgeW1!=null && inEdgeW1.length>0) {
			
	//		Collections.sort(inEdgeW1);
			int hash = 0;
			if (inEdge1!=null && inEdge1.length>0) {
				Arrays.sort(inEdge1);

				Arrays.hashCode(inEdge1) ;
			}

//			dwp.v0 = p+1;  dwp.v2 = hash; dwp.v3 = d; dwp.v4 = dir; v[n++]= mf.calc5(dwp); //f.add(li.l2i(l));

			
//			dwp.v0 = p+1;  dwp.v2 = inEdgeW1[0]; dwp.v3 = d; dwp.v4 = dir; v[n++]= mf.calc5(dwp); //f.add(li.l2i(l));
		
			/*
			int k=0;
			for(int edge : inEdgeW1) {
				if(k>0 && k<3) {
					dwp.v0 = p+2;  dwp.v2 = edge; dwp.v3 = d; dwp.v4 = dir; 	
					v[n++]= mf.calc5(dwp); //f.add(li.l2i(l));
				}
				k++;
			}
			dwp.v0 = p+3;  dwp.v2 = pWord;  dwp.v3 = inEdgeW1[0]; dwp.v4 = d; dwp.v5 = dir; 	
			v[n++]= mf.calc6(dwp); //f.add(li.l2i(l));

			dwp.v0 = p+4;   dwp.v2 = dWord;  dwp.v3 = inEdgeW1[0]; dwp.v4 = d; dwp.v5 = dir; 	
			v[n++]= mf.calc6(dwp);// f.add(li.l2i(l));
		*/
//		}
		p+=5;
	
//		if (true) return;
		
//	
	//		dwp.v0 = p+1;  dwp.v2 = hash; dwp.v3 = d; dwp.v4 = dir; v[n++]= mf.calc5(dwp); //f.add(li.l2i(l));

			/*
			dwp.v0 = p; 	dwp.v2 = inEdge[0]; dwp.v3 = d; dwp.v4 = dir;  v[n++]= mf.calc5(dwp); //f.add(li.l2i(l));
			int k=0;
			for(int edge : inEdge) {
				if(k>0 && k<3) {
					dwp.v0 = p+1; 	 dwp.v2 = edge; dwp.v3 = d; dwp.v4 = dir;  v[n++]= mf.calc5(dwp);// f.add(li.l2i(l));
				}
				k++;
			}
			dwp.v0 = p+2; 	  dwp.v2 = dWord;  dwp.v3 = inEdge[0]; dwp.v4 = d; dwp.v5 = dir; 	
			v[n++]= mf.calc6(dwp); //f.add(li.l2i(l));

			dwp.v0 = p+3; 	 dwp.v2 = pWord;  dwp.v3 = inEdge[0]; dwp.v4 = d; dwp.v5 = dir; 	
			v[n++]= mf.calc6(dwp); // f.add(li.l2i(l));
*/
		//	}
		p+=4;
		if (distPath[w1][w2].length==1) {
	//		dwp.v0 = p;   dwp.v2 = 1;  dwp.v3 = distPath[w1][w2][0]; dwp.v4 = d; dwp.v5 = dir; 
	//		v[n++]= mf.calc6(dwp);
		} else 	if (distPath[w1][w2].length==2 ) {
	//		dwp.v0 = p; 	dwp.v1 = label;  dwp.v2 = 1;  dwp.v3 = distPath[w2][w1][0]; dwp.v4 = d; dwp.v5 = dir; 
	//		v[n++]=l= mf.calc6(dwp); f.add(li.l2i(l));
		}
		p++;
	
	}

	
	
	// w1 gets extended
	public void extractAdd(Graph g, int w1, int w2, int label, Graph t, IFV f, int[][] dist, int[][][] distPath, long[] v) {
		
	//	if (true) return;

		
//		int prntF =  mf.getValue(WORD,Environment.getValue(  g.getContent(w1) ));
	//	int childF = mf.getValue(WORD, Environment.getValue(t.getContent(w2)));; //g.getContent(w2);
		
	//	int d = dist[w1][w2];
	//	int dir = d<0?1:2;
	//	d = Math.abs(d);
    t.buildIn();		
		
		long l;
		int n=0, p=1;
//		dl1.v0= p++; dl1.v1=label; dl1.v2=d; dl1.v3=dir;
//		l=v[n++]= l=mf.calc4(dl1); f.add(li.l2i(l)); 		
		
//		dwp.v0 = p++; 	dwp.v1 = label; dwp.v2 = prntF; dwp.v3 = d; dwp.v4 = dir; 	
//		v[n++]= l=mf.calc5(dwp); f.add(li.l2i(l)); 

//		dwp.v0 = p++; 	dwp.v1 = label; dwp.v2 = childF; dwp.v3 = d; dwp.v4 = dir; 	
	//	v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l)); 		
	//	
	//	dwwp.v0 = _f4; 	dwwp.v1 = label; dwwp.v2 = childF; dwwp.v3 = prntF; dwwp.v4 = d; dwwp.v5 = dir; 	
	//	l= mf.calc6(dwwp); f.add(li.l2i(l)); 		

		ArrayList<Integer> inEdge = getInEdgesS(t,w1);
	
		if (inEdge!=null && inEdge.size()>0) {
			dwp.v0 = _f5; 	dwp.v1 = label; dwp.v2 = 0; dwp.v3 = inEdge.get(0);  	
	
			l= mf.calc4(dwp); f.add(li.l2i(l));
		}
		
		
	/*	
		ArrayList<Integer> inEdge = getInEdges(g,w2);	
		if (inEdge!=null && inEdge.size()>0) {
			Collections.sort(inEdge);
			dwp.v0 = p; 	dwp.v1 = label; dwp.v2 = inEdge.get(0); dwp.v3 = d; dwp.v4 = dir; 	
			v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l));
		}
		p++;
		}
*/
	}
	public void extractAdd(Graph g, int w1, int w2, int label, short[] heads, short[] types, IFV f, int[][] dist, int[][][] distPath, long[] v) {
		
		
		
//		int prntF =  mf.getValue(WORD,Environment.getValue(  g.getContent(w1) ));
	//	int childF = mf.getValue(WORD, Environment.getValue(t.getContent(w2)));; //g.getContent(w2);
		
	//	int d = dist[w1][w2];
	//	int dir = d<0?1:2;
	//	d = Math.abs(d);
//t.buildIn();		
		
		long l;
		int n=0, p=1;
//		dl1.v0= p++; dl1.v1=label; dl1.v2=d; dl1.v3=dir;
//		l=v[n++]= l=mf.calc4(dl1); f.add(li.l2i(l)); 		
		
//		dwp.v0 = p++; 	dwp.v1 = label; dwp.v2 = prntF; dwp.v3 = d; dwp.v4 = dir; 	
//		v[n++]= l=mf.calc5(dwp); f.add(li.l2i(l)); 

//		dwp.v0 = p++; 	dwp.v1 = label; dwp.v2 = childF; dwp.v3 = d; dwp.v4 = dir; 	
	//	v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l)); 		
	//	
	//	dwwp.v0 = _f4; 	dwwp.v1 = label; dwwp.v2 = childF; dwwp.v3 = prntF; dwwp.v4 = d; dwwp.v5 = dir; 	
	//	l= mf.calc6(dwwp); f.add(li.l2i(l)); 		

		ArrayList<Integer> inEdge =new ArrayList();
		if (heads[w1]>0) inEdge.add((int)types[w1]);
		
	
		if (inEdge!=null && inEdge.size()>0) {
			dwp.v0 = _f8; 	dwp.v1 = label; dwp.v2 = 0; dwp.v3 = inEdge.get(0);  	
	
			l= mf.calc4(dwp); f.add(li.l2i(l));
		}
		
		
	/*	
		ArrayList<Integer> inEdge = getInEdges(g,w2);	
		if (inEdge!=null && inEdge.size()>0) {
			Collections.sort(inEdge);
			dwp.v0 = p; 	dwp.v1 = label; dwp.v2 = inEdge.get(0); dwp.v3 = d; dwp.v4 = dir; 	
			v[n++]=l= mf.calc5(dwp); f.add(li.l2i(l));
		}
		p++;
		}
*/
	}
	/**
	 * @param g
	 * @param w1
	 * @return
	 */
	private ArrayList<Integer> getInEdges(Graph g, int n) {

		
		int[] in = g.getIn(n);
		if (in ==null) 	return null;
		ArrayList<Integer> edges= new ArrayList<Integer>(); 
		for(int i=1;i<=in[0];i++) {	
		//	System.out.print(" "+Environment.getValue(g.getContent(in[i])));
			edges.add(g.getContent(in[i]));
		//	edges.add(mf.getValue(Pipe.ARG,Environment.getValue(g.getContent(in[i]))));
		}
		return edges;
	
	}
	
	private int[] getInEdgesI(Graph g, int n) {

		
		int[] in = g.getIn(n);	
		
		if (in ==null) 	return null;
		int[] c = new int[in[0]];
		ArrayList<Integer> edges= new ArrayList<Integer>(); 
		for(int i=1;i<=in[0];i++) 
			c[i-1]  = g.getContent(in[i]);			
	
		return c;
	
	}

	private ArrayList<Integer> getInEdgesS(Graph g, int n) {

		
				
		int[] in = g.getIn(n);
		if (in ==null) 	return null;
		ArrayList<Integer> edges= new ArrayList<Integer>(); 
		for(int i=1;i<=in[0];i++) {	
		//	System.out.print(" "+Environment.getValue(g.getContent(in[i])));
			edges.add(g.getContent(in[i])); //mf.getValue(Pipe.REL,Environment.getValue(
		}
		return edges;
	
	}
	/**
	 * The idea of the algorithm is Dijkstra distance.
	 * 
	 * @param g
	 * @param w1
	 * @param w2
	 * @return
	 */
	public static  int dist(Graph g, int w1, int w2) {
		
		
		if (w1==w2) return 0;
		BitSet nodes = new BitSet();
		nodes.set(w1);
		
		int currentDist =1;
	
//		TIntHashSet nextNodesDistX = new TIntHashSet();
		HashSet<Integer> nextNodesDistX = new HashSet<Integer>();

		Integer[] nn = new Integer[1];//nodesWithDistX.toArray();
		nn[0]=w1;
		while(true) {
			for (Integer n : nn) {
				if (n==null) break;
				int[] out = g.getOut(n);
				if (out !=null)
					for(int o=1;o<=out[0];o++) {
						int x = g.getOut(out[o])[1];
						if (x==w2) return currentDist;
						if (!nodes.get(x)) {
							nodes.set(x);
							nextNodesDistX.add(x);
						}
					}


				int[] in = g.getIn(n);
				if (in !=null)
					for(int i=1;i<=in[0];i++) {
						
						int x = g.getIn(in[i])[1];
						if (x==w2) return currentDist;
						if (!nodes.get(x)) {
							nodes.set(x);
							nextNodesDistX.add(x);
						}
					}

			}
			currentDist++;
			//nodesWithDistX.clear();
			if (nextNodesDistX.size()==0) break;
			nn =nextNodesDistX.toArray(nn);
			
			//nodesWithDistX.addAll(nextNodesDistX.toArray());
			nextNodesDistX.clear();
		}	
		
		return -1;
	}

	/**
	 * The idea of the algorithm is Dijkstra distance.
	 * 
	 * @param g
	 * @param w1
	 * @param w2
	 * @return
	 */
	static public int[] distAll(Graph g, int w1, int len) {
		

		BitSet nodes = new BitSet();
		nodes.set(w1);

		
		int [] dists =new int[len];
		for(int i=0;i<len;i++) dists[i]=-31;
		
		dists[w1]=0;
		
		int currentDist =1;
	
		HashSet<Integer> nextNodesDistX = new HashSet<Integer>();
		HashSet<Integer> distNodesX = new HashSet<Integer>();

		//Integer[] nn = new Integer[1];//nodesWithDistX.toArray();
		distNodesX.add(w1);
		dists[w1]=0;
		while(true) {
			for (int n : distNodesX) {
				int[] out = g.getOut(n);
				if (out !=null)
					for(int o=1;o<=out[0];o++) {
				
						int x = g.getOut(out[o])[1];
						
						if (!nodes.get(x)&&g.getType(x)==Graph.NODE) {
							nodes.set(x);
						try {
							dists[x]=currentDist;
							nextNodesDistX.add(x);
						} catch(Exception e) {
								
							
							//DB.println("\nproblem with node "+Environment.getValue(g.getContent(x))+" node type "+g.getType(x)+" dists.len "+dists.length+" x "+x);
							
		DB.println("\nproblem with node '"+Environment.getValue(g.getContent(x))+"' node type "+
				g.getType(x)+" dists.len "+dists.length+" x "+x+" len "+len);
		DB.println("graph size "+g.size());
									e.printStackTrace();
									//System.exit(0);
								
					}
					}
					}

				int[] in = g.getIn(n);
				if (in !=null)
					for(int i=1;i<=in[0];i++) {
		
						
						if (g.getIn(in[i])==null) {
							// error
							DB.println("error no connecting in "+in[i]+" "+i+" of :"+Environment.getValue(n)+" node "+n);
						}
						int x = g.getIn(in[i])[1];
						if (!nodes.get(x)&&g.getType(x)==Graph.NODE) {
			
							try {
							nodes.set(x);
							dists[x]=-currentDist;
							nextNodesDistX.add(x);
							} catch(Exception e) {
								DB.println("problem with node "+Environment.getValue(g.getContent(x))+" node type "+g.getType(x)+" "+x);
								//e.printStackTrace();
								
							}
						}
					}

			}
			currentDist++;
			if (nextNodesDistX.size()==0) break;

			distNodesX.clear();
			distNodesX.addAll(nextNodesDistX);
			
			nextNodesDistX.clear();
		}	
//		for(int i=0;i<len;i++) DB.println("dist "+w1+" +"+i+" d "+dists[i]+" "+dist(g,w1,i));
//		if (w1==5)System.exit(0);
		return dists;
	}


	/**
	 * The idea of the algorithm is Dijkstra distance.
	 * 
	 * @param g
	 * @param w1
	 * @param w2
	 * @return
	 */
	static public int[][] distPathRel(Graph g, int w1, int len) {
		

		BitSet nodes = new BitSet();
		nodes.set(w1);
		
		int dists[][] =new int[len][];
		
		int currentDist =0;
	
		HashSet<Integer> nextNodesDistX = new HashSet<Integer>();
		HashSet<Integer> distNodesX = new HashSet<Integer>();

		//Integer[] nn = new Integer[1];//nodesWithDistX.toArray();
		distNodesX.add(w1);
		dists[w1] = new int[0];
		while(true) {
			for (int n : distNodesX) {
				int[] out = g.getOut(n);
				if (out !=null)
					for(int o=1;o<=out[0];o++) {
						int x = g.getOut(out[o])[1];
						if (!nodes.get(x)&&g.getType(x)==Graph.NODE) {
							nodes.set(x);
							if (dists[x]==null) {
								
								dists[x] = dists[n];
							}
							
							int[] pathOld = dists[x];
							dists[x] = new int[pathOld.length+1];
							System.arraycopy(pathOld, 0, dists[x], 0, pathOld.length);
							
							dists[x][currentDist]=g.getContent(out[o]);
							nextNodesDistX.add(x);
						}
					}


				int[] in = g.getIn(n);
				if (in !=null)
					for(int i=1;i<=in[0];i++) {
						
						int x = g.getIn(in[i])[1];
						if (!nodes.get(x)&&g.getType(x)==Graph.NODE) {
							nodes.set(x);
							if (dists[x]==null) {
								
								dists[x] = dists[n];
							}
							int[] pathOld = dists[x];
							dists[x] = new int[pathOld.length+1];
							System.arraycopy(pathOld, 0, dists[x], 0, pathOld.length);
							
							dists[x][currentDist]=g.getContent(in[i]);
							nextNodesDistX.add(x);
						}
					}

			}
			currentDist++;
			if (nextNodesDistX.size()==0) break;

			distNodesX.clear();
			distNodesX.addAll(nextNodesDistX);
			
			nextNodesDistX.clear();
		}	
//		for(int i=0;i<len;i++) DB.println("dist "+w1+" +"+i+" d "+dists[i]+" "+dist(g,w1,i));
//		if (w1==5)System.exit(0);
		return dists;
	}

	
	
	/**
	 * @param d
	 * @param types
	 * @param sem 
	 */
	public static void replacedEdges(Graph d, String[] types, Graph sem) {
	
		for(int n=0;n<d.size();n++){
			if(d.getType(n)==IGraph.EDGE){
				d.setContent(n, Environment.content(types[d.getContent(n)]));
			} else if (d.getType(n)==IGraph.NODE){
				d.setContent(n, sem.getContent(n));				
			}
		}
		
		
	}

	public void extractEdgeFeatures(Graph g, Parse prs,  int d, short h, int[][] dist, int i, long[] v) {
	
		int dL =g.getContent(d);
		int hL =g.getContent(h);
		
		int dt = dist[d][h];
		int dir = dt<0?1:2;
		dt = Math.abs(dt);
		
		int edge=0;
		if (dir == 1) {
			int in[] = g.getIn(d);
			if (in!=null)
			for(int k=1;k<=in[0];k++) {
				int in2[] = g.getIn(in[k]);
				if (in2[1]== d) edge = g.getContent(in[k]);
			}
		} else {
			int in[] = g.getIn(h);
			if (in!=null)
			for(int k=1;k<=in[0];k++) {
				int in2[] = g.getIn(in[k]);
				if (in2[1]== d) edge = g.getContent(in[k]);
			}
			
		}
		
		int p=3, n=0;
		dl1.v1 = 0; dwp.v1 = 0;
		
		int hh = prs.heads[h];
		
		int hhL = hh==-1?-1:g.getContent(hh);

		
		
		dwp.v0 = p++;  dwp.v2 = hL;  v[n++]= mf.calc3(dwp);
		dwp.v0 = p++;  dwp.v2 = hL; dwp.v3 = dt;  v[n++]= mf.calc4(dwp);
		dwp.v0 = p++;  dwp.v2 = hL; dwp.v3 = dt;dwp.v4 = dir;  v[n++]= mf.calc5(dwp);

		dwp.v0 = p++;  dwp.v2 = dL;  v[n++]= mf.calc3(dwp); 
		dwp.v0 = p++;  dwp.v2 = dL; dwp.v3 = dt;  v[n++]= mf.calc4(dwp);
		dwp.v0 = p++;  dwp.v2 = dL; dwp.v3 = dt;dwp.v4 = dir;  v[n++]= mf.calc5(dwp);
		
		dwwp.v0 = p++;  dwwp.v2 = hL; dwwp.v3 = dL;v[n++]= mf.calc4(dwwp);
		dwp.v0 = p++;  dwp.v2 = dL; dwp.v3 = edge; dwp.v4 = dir; v[n++]= mf.calc5(dwp);
		dwp.v0 = p++;  dwp.v2 = hhL;  dwp.v3 =dt; dwp.v4 = dir; v[n++]= mf.calc5(dwp);

		
		ArrayList<Integer> outEdge1 = getOutEdgesI(g,d);
		ArrayList<Integer> outEdge2 = getOutEdgesI(g,h);
		if (outEdge1 !=null && outEdge1.size()>0) {
	//		dwp.v0 = p;  dwp.v2 = outEdge1.get(0); dwp.v3 = dt; dwp.v4 = dir; v[n++]= mf.calc5(dwp);
			
		}
p++;
	if (outEdge2 !=null && outEdge2.size()>0) {
//		dwp.v0 = p;  dwp.v2 = outEdge2.get(0); dwp.v3 = dt; dwp.v4 = dir; v[n++]= mf.calc5(dwp);
	}
p++;
		
		int[] inEdge1 = getInEdgesI(g,d);
		int[] inEdge2 = getInEdgesI(g,h);
				
		if (inEdge1 !=null && inEdge1.length>0) {
			Arrays.sort(inEdge1);

			dwp.v0 = p;  dwp.v2 = inEdge1[0]; dwp.v3 = dt; dwp.v4 = dir; v[n++]= mf.calc5(dwp);
			dwp.v0 = p+1;dwp.v2 = dL;  dwp.v3 = inEdge1[0]; dwp.v4 = dt; dwp.v5 = dir; 	v[n++]= mf.calc6(dwp);
			dwp.v0 = p+2;dwp.v2 = hL;  dwp.v3 = inEdge1[0]; dwp.v4 = dt; dwp.v5 = dir; 	v[n++]= mf.calc6(dwp);
		}
		p+=3;
		
		if (inEdge2 !=null && inEdge2.length>0) {
			Arrays.sort(inEdge2);

			dwp.v0 = p;  dwp.v2 = inEdge2[0]; dwp.v3 = dt; dwp.v4 = dir; v[n++]= mf.calc5(dwp);
			
			dwp.v0 = p+1; 	  dwp.v2 = hL;  dwp.v3 = inEdge2[0]; dwp.v4 = dt; dwp.v5 = dir; 	v[n++]= mf.calc6(dwp);
			dwp.v0 = p+2; 	  dwp.v2 = dL;  dwp.v3 = inEdge2[0]; dwp.v4 = dt; dwp.v5 = dir; 	v[n++]= mf.calc6(dwp);
		}	
		p+=3;
		
		ArrayList<Integer> cld = getChildren(prs,d);
	
		StringBuilder sb = new StringBuilder(cld.size());
		HashSet<Integer> rels = new HashSet<Integer>();
		for(int c : cld) 
			rels.add((int)prs.labels[c]);
	
		int[] relss = new int[rels.size()]; 
			
				
		int k=0;			
		for(int r : rels) { 
			relss[k]= r ;k++;
		}
		
		Arrays.sort(relss);
		for(int c : relss) 	
			sb.append((char)c);
				
		int c = mf.register("X", sb.toString());
//		System.out.println("c "+c );
//		dwwp.v0 = p++;  dwwp.v2 = c; dwwp.v3 = dt;v[n++]= mf.calc4(dwwp);	
	}

	private ArrayList<Integer> getOutEdgesI(Graph g, int d) {
		
		int[] out = g.getOut(d);	
		
		if (out ==null) 	return null;
		//int[] c = new int[in[0]];
		ArrayList<Integer> edges= new ArrayList<Integer>(); 
		for(int i=1;i<=out[0];i++) 
			if (g.getType(out[i]) == Graph.ATTRIBUTE) edges.add(g.getContent(g.getOut(out[i])[1]));			
	
		return edges;
	
	}

	public static ArrayList<Integer> getChildren(Parse prs, int d) {
		
		ArrayList<Integer> clds = new ArrayList<Integer>();
		for(int c=0;c<prs.heads.length;c++) 
			if (d== prs.heads[c]) clds.add(c);
		
		return clds;
		
	}

}
