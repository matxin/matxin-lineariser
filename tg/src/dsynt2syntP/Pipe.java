package dsynt2syntP;


import is2.data.Instances;
import is2.data.Long2IntInterface;
import is2.data.PipeGen;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.util.OptionsSuper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import rt.util.DB;



final public class Pipe extends PipeGen {

	private static final String STWRD = "STWRD",STPOS = "STPOS",END = "END",STR = "STR", ROOT="ROOT", FEAT="F",WORD="WORD", REL="REL",POS="POS", TYPE="T";

	public static final String RULE = "RULE", PS="PS";

	public static final int DEEP = 0, SURF=1;

	public static String[] types;


	final public MFO mf =new MFO();
	public Long2IntInterface li;

	final MFO.Data4 d1 = new MFO.Data4(),d2 = new MFO.Data4(),d3 = new MFO.Data4(),dw = new MFO.Data4();
	final MFO.Data4 dwp = new MFO.Data4(),dp = new MFO.Data4(),dp2 = new MFO.Data4();


	private OptionsSuper options;
	static private int _mid, _endf,_endp,_ends,_endr;

	public Pipe (Options options, Long2Int long2Int) throws IOException {
		this.options = options;

		li =long2Int;
	}

	public Pipe (OptionsSuper options) {
		this.options = options;
	}


	// the dependency edge to rule mapping
	HashMap<String,HashSet<String>> ruleMap = new HashMap<String,HashSet<String>>(); 


	/**
	 * Create the training instances
	 */
	public Instances[] createInstances(String deepFile, String surfaceFile) {

		CONLLReader09  deepReader = new CONLLReader09(deepFile);
		CONLLReader09  surfaceReader = new CONLLReader09(surfaceFile);


		mf.register(FEAT, CONLLReader09.NO_TYPE);
		mf.register(FEAT, "");
		mf.register(PS, ROOT);

		Instances[] is = new Instances[2];

		for(int k=0;k<is.length;k++) {
			is[k] = new Instances();
		}
		
		
		
		System.out.println("Registering feature parts ");

		HashMap<String, Integer> rules = new HashMap<String, Integer>();
		

		int ic=0;
			// collect the aligned trees
		while (true) {
			SentenceData09 deep=null;
			SentenceData09 surf=null;

			try {
				deep = deepReader.getNext();
				surf = surfaceReader.getNext();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (deep == null || surf==null) break;
		
			System.out.println("len deep "+deep.length());
			System.out.println("len synt "+surf.length());
			
			String[] w = deep.lemmas;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(WORD,  w[i1]);

			w = surf.lemmas;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(WORD,  w[i1]);

			
			for(int i1 = 0; i1 < w.length; i1++) {
				mf.register(WORD,  w[i1].toLowerCase());
			}

			String[] labs1 = deep.labels;
			for (int i1 = 0; i1 < labs1.length; i1++) mf.register(REL, labs1[i1]);

			labs1 = surf.labels;
			for (int i1 = 0; i1 < labs1.length; i1++) mf.register(REL, labs1[i1]);

			w = deep.lemmas;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(WORD,  w[i1]);			

			w = surf.ppos;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(POS,  w[i1]);

			w = deep.gpos;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(POS,  w[i1]);

			
			 
			for(int i1 = 0; i1 < w.length; i1++) {
				String fts[] =deep.feats[i1];
				//if ()
				if (fts==null) continue;
				for(String f : fts) {
					if (f.startsWith("id=")) continue;
					mf.register(FEAT, f);
				}
			}

			
			w = deep.ofeats;
		
				
			if (ic>options.count+1) break;
			ic++;
			
			
			deriveRules(deep,surf,rules);
		
		}

	//	System.out.println("hello !!!!!!!!!!!!! rules "+rules);
		
		HashSet<String> seldom = new HashSet<String>();
		for(Entry<String,Integer>  r : rules.entrySet()){
		//	if (r.getValue()<16)seldom.add(r.getKey());
			
			// remove all punctuation rules with in:
			
			// if (r.getKey().startsWith("in:")) seldom.add(r.getKey());
		}
		
		for(String r : seldom){
			rules.remove(r);
		}	
		for(String r : rules.keySet()){
			mf.register(RULE, r);
		}
		mf.register(RULE, "");
		
		
		DB.println("rules "+rules);
		DB.println("found # rules "+rules.size());


		// register all rules
		for (Entry<String, HashSet<String>> ruleset : ruleMap.entrySet()) {

			for(String rule : ruleset.getValue()) {
				mf.register(this.RULE, rule);
			}
		}

			initFeatures();

		mf.calculateBits();
		initValues();

		System.out.println(""+mf.toString());

		deepReader.startReading(deepFile);
		surfaceReader.startReading(surfaceFile);


		int num1 = 0;
		long start1 = System.currentTimeMillis();

		System.out.print("Creating Features: ");
		is[0].init(ic, mf) ;
		is[1].init(ic, mf) ;
		int del=0;

		
		while(true) {
			try {
			if (num1 % 100 ==0) {del = outValue(num1, del);}
			SentenceData09 deep = deepReader.getNext();
			SentenceData09 sure = surfaceReader.getNext(is[1]);
			
			

			if (deep== null|| sure==null) break;
			deriveRules(deep,sure,rules);

			for(int k=0;k<deep.length();k++) {
				
				if (deep.feats[k]!=null && deep.feats[k].length>0 && deep.feats[k][0]!=null && deep.feats[k][0].startsWith("id=")) {
					
					String feats[] = new String[deep.feats[k].length-1];
					for(int j=1;j< deep.feats[k].length;j++) {
						feats[j-1]=deep.feats[k][j];
					}
					deep.feats[k]=feats;
				}
				
			}
			
			deepReader.insert(is[0], deep);
			
			int i= is[0].size()-1; 
			
//			is[0].predicat[i] = new int[is[0].length(i)];
			is[0].arg[i] = new short[is[0].length(i)][2];

			// attache rules 
			for(int k=1;k<deep.length();k++) {
		
				is[0].arg[i][k][0] = (short)mf.getValue(RULE,deep.arg[k][0]==null?"":deep.arg[k][0]);
				is[0].arg[i][k][1] = (short)mf.getValue(RULE,deep.arg[k][1]==null?"":deep.arg[k][1]);
				// care for seldom rules and do not updates when not possible
				if (is[0].arg[i][k][0]==-1) is[0].arg[i][k][0] =(short)mf.getValue(RULE,"");
				if (is[0].arg[i][k][1]==-1) is[0].arg[i][k][1] =(short)mf.getValue(RULE,"");
							
	//			DB.println("rule:"+deep.arg[k][0]+"\t"+deep.arg[k][1]+"\t0:"+is[0].arg[i][k][0]+"\t1:"+is[0].arg[i][k][1]);
	//			if (deep.arg[k][0]!=null && mf.getValue(RULE,deep.arg[k][0]==null?"":deep.arg[k][0])==-1){
	//				System.exit(0);
					
	//			}
				
			}
			
			
			
			if (num1>options.count) break;

			num1++;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		long end1 = System.currentTimeMillis();
		System.gc();
		long mem2 = Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory();
		System.out.print("  time "+(end1-start1)+" mem "+(mem2/1024)+" kb");

		types = new String[mf.getFeatureCounter().get(RULE)];

		for(Entry<String,Integer> e : mf.getFeatureSet().get(RULE).entrySet()) {
			types[e.getValue()] = e.getKey();
		}


		System.out.println("Num Features: " + types.length);
		return is;//.toNativeArray();

	}

	

	/**
	 * @param deep
	 * @param sure
	 */
	public void deriveRules(SentenceData09 deep, SentenceData09 sure, HashMap<String,Integer> rules) {
		
		
		

		// deep to surface mapping
		int d2s[] = new int[deep.length()];

		d2s[0]=0;

		//deep.fillp = new String[deep.length()];
		deep.arg = new String[deep.length()][2];

		// build a id node mapping
		for(int k=0;k<deep.length();k++) {
			
			d2s[k]=-1;
			String hs =deep.ofeats[k]; 
			if (hs ==null) continue;
			String fs[] = hs.split("\\|");
			for(String f : fs) {
				
				if (f.startsWith("id=")||f.startsWith("id0=")) {
					String[] id = f.split("=");
					if (id[1].contains("_"))  {
											String[] id_ = id[1].split("_");
											id[1]=id_[0];
					}
					try {
					d2s[k] = Integer.parseInt(id[1]);
					} catch (Exception e ) {
						e.printStackTrace();
						
					}
				}
			}
		
		}
		int s2d[] = new int[sure.length()];
		
		for(int k=0;k<s2d.length;k++) {
			s2d[k]=-1;
		}
		for(int k=0;k<d2s.length;k++) {
			if (d2s[k]==-1)continue;
			if (s2d.length<=d2s[k]) continue;
			s2d[d2s[k]]=k; 
		}
		
		

		
		for(int k=1;k<sure.length();k++) {
			
			
			// changed form pfeats to ofeats
			String hs =sure.ofeats[k]; 
			if (hs ==null) continue;
			String fs[] = hs.split("\\|");
			s2d[k] =-1;
			for(String f : fs) {
	//			if (f.startsWith("id=")||f.startsWith("id0=")) {
	//				String[] id = f.split("=");
	//				if (id[1].contains("_"))  {
	//					String[] id_ = id[1].split("_");
	//					id[1]=id_[0];
	//				}
				if (f.startsWith("id=")||f.startsWith("id0=")) {
					String[] id = f.split("=");
					try {
						
						if (id[1].contains("_")) {
							String xx[] = id[1].split("_");
							xx[0] = xx[0].replace("\"", "");
							s2d[k] = Integer.parseInt(xx[0]);
						} else  s2d[k] = Integer.parseInt(id[1]);
					} catch(Exception e) {
					//	e.printStackTrace();
					}
				}
			}
		}
		
		
		
		
		// delete lower hanging mappings with same number: will:85 -> be:85 -> 0    ::: Oper1:85 -> 0

		for(int k=1;k<sure.length();k++) {
			if (s2d[k] !=-1) {
				
				ArrayList<Integer> children = this.getChilds(k, sure);
				for(int c : children) {
					if (s2d[c]== s2d[k]) s2d[c] =-1;
				}
				
			}
		}
		
	
		//map root to root
		
	//	BitSet covered;
	/*
		int sf2dp[] = new int[sure.length()];	
		for(int k=1;k<sure.length();k++) {
			sf2dp[k]=-1;
			for(int j=0;j<dp2sf.length;j++) {

				if (dp2sf[j]==k) {
					sf2dp[k]=j;
					break;
				}
				
			}
			
	//		DB.println("sf2dp insert rules "+k+"\t"+sf2dp[k]+"\t"+sure.forms[k]+"\t\t"+sure.heads[k]);
		}
*/
	
		for(int k=1;k<deep.length();k++) {
			
			ArrayList<Integer> clds = getChilds(k, deep);

			// introduce only single child edges or leafs
			
		
			// map the edges
			int s1 = d2s[k];
			if (s1==-1){
			//	System.out.print("err:-1 ");
				continue;
			}
			
			for (int c : clds ) {
				
				
			
				int s2 = this.getNode(d2s[c], s2d);
				
				try {
				ArrayList<Integer> path = getPath(s1,s2,sure);
				
			
	
				if (path.size()>2 && path.size()<=3) {
		//			System.out.println("deep edge "+k+" -> "+c+" :: "+d2s[k]+" -> "+d2s[c]+" surface "+s1+" -> "+s2+" path "+path);
		//			System.out.println("deep "+deep.lemmas[k]+" "+deep.lemmas[c]);
					for(int p:path) {
	//					System.out.print(sure.lemmas[p]+" ");
					}
					
					System.out.println();
					StringBuilder sb = new StringBuilder();
					
					
					String what =sure.lemmas[k]; 
					if (what.equals("will")) what ="be1"; 
					
//					ArrayList<Integer> children = this.getChilds(k, sure);
//					for(int j : children) {
//						if (s2d[j]==-1) what = sure.lemmas[j];
//						
					
//					}
					
					
				//	sb.append("ie:").append(sure.labels[s1]).append(':').append(what).append(':').append(sure.labels[s2]);
					
					int d = c;
//					deep.arg[d][0]=sb.toString();
					deep.arg[d][0]= deep.arg[d][0]==null? sb.toString():(deep.arg[d][0]+";"+sb.toString());
		//			System.out.println("found rule "+sb);
				}
				
				} catch(Exception e ) {
					e.printStackTrace();
					
				}
 
			}
		
			// relable edge
			if (!deep.labels[k].equals(sure.labels[s1])) {
				StringBuilder sb = new StringBuilder();
				sb.append("l:"+sure.labels[s1]);
				deep.arg[k][1]=deep.arg[k][1]==null?sb.toString():(deep.arg[k][1]+";"+sb.toString());
			//	System.out.println("l:"+sure.labels[path.get(0)]);
			} 
			//else
		//		System.out.println("dl "+deep.labels[k]+" "+sure.labels[s1]); 
			
			if (s1>=0 && !deep.lemmas[k].equals(sure.lemmas[s1])) {
//				DB.println("lemma diff "+deep.lemmas[k]+" "+sure.lemmas[s1]);
	
				// ignore this rule for now
				StringBuilder sb = new StringBuilder();
				sb.append("r:"+sure.lemmas[s1]);
				deep.arg[k][0]= deep.arg[k][0]==null? sb.toString():(deep.arg[k][0]+";"+sb.toString());
			}
		
			/*
			if (s1>=0) {
				ArrayList<Integer> sclds = getChilds(s1, sure);
			
				if (clds.size()==0 && sclds.size()>0) {
					
					// insert childs 
					
					int c = sclds.get(0);
					
					ArrayList<Integer> scc = getChilds(c, sure);
					
					// does child has also children => error
					if (scc.size()>0) {
						
						DB.println("child has again childs !! => no an insert rule");
						continue;
					}
					
					for(int x : sclds) {
						DB.println("children :"+sure.forms[x]);
					}
					
					int deepChild = k;
					
					if (deepChild==-1) {
				//		DB.println("Warning deepChild==-1 !!! "+sure.org_lemmas[c]);
						continue;
					}	

					StringBuilder sb = new StringBuilder();
					sb.append("in:").append(sure.labels[c]).append(':').append(sure.lemmas[c]);
			
					deep.fillp[deepChild]=sb.toString();
					
					deep.arg[deepChild][1]= deep.arg[deepChild][1]==null?sb.toString():(deep.arg[deepChild][1]+";"+sb.toString());
			//		System.out.println("found insert rule "+sb.toString());
					
					
				}
				
				//if (sclds.size()>1) continue;
				
				// 
				
				
			}
			*/
			
		/*
		if (sf2dp[k]==-1) {
					
				
				
				ArrayList<Integer> clds = getChilds(k, sure);

				// introduce only single child edges or leafs
				if (clds.size()>1) continue;
				
				else if (clds.size()==1) {
				
					// insert a edge
				
					int c = clds.get(0);
								
					int deepChild = sf2dp[c];
					
					if (deepChild==-1) {
				//		DB.println("Warning deepChild==-1 !!! "+sure.org_lemmas[c]);
						continue;
					}	

					StringBuilder sb = new StringBuilder();
					sb.append("ie:").append(sure.labels[k]).append(':').append(sure.lemmas[k]).append(':').append(sure.labels[c]);
					
					deep.fillp[deepChild]=sb.toString();
					
					if (sure.lemmas[k].equals("move")) {
						DB.println("back "+sure.lemmas[k]);
						
						DB.println("deep "+deep.toString());
						
						DB.println("surf "+sure.toString());
							
						new Exception().printStackTrace();
						System.exit(0);
					}
					deep.arg[deepChild][0]=sb.toString();
					
					
				} else {  // clds.size() == 0
					
					// add a node as leaf
					
					StringBuilder sb = new StringBuilder();
					sb.append("in:").append(sure.labels[k]).append(':').append(sure.lemmas[k]);
					
					int d = sf2dp[sure.heads[k]];
				
					if (d==-1) {
			//			DB.println("Waring d = -1: reason try to attach node to deleted node "+k+"\t"+sure.forms[k]+" head "+sure.heads[k]);
						continue;
			
						
					}
					if (deep.arg[d][1]!=null) {
				//		DB.println("found "+deep.fillp[d]);
						if (!deep.fillp[d].contains(sb.toString()))
						//deep.fillp[d]+=";"+sb.toString();
						if (!deep.arg[d][1].contains(sb.toString()))
							deep.arg[d][1]+=";"+sb.toString();
					}
					else {
						//deep.fillp[d]=sb.toString();
						deep.arg[d][1]=sb.toString();
					}
					
				}
			}
			*/
		}
		
		for(int k=0;k<deep.length();k++) {
		//	DB.println(""+k+"\t"+deep.org_lemmas[k]+"\t\t"+deep.heads[k]+"\t"+deep.fillp[k]);
			
			if (deep.arg[k][0]!=null) {
				Integer ruleCnt = rules.get(deep.arg[k][0]);
				rules.put(deep.arg[k][0], ruleCnt==null?1:(ruleCnt+1));
			}

			if (deep.arg[k][1]!=null) {
				
				Integer ruleCnt = rules.get(deep.arg[k][1]);
				rules.put(deep.arg[k][1], ruleCnt==null?1:(ruleCnt+1));
				
			}

			
			if (deep.fillp[k]!=null) { 
				
				//rules.add(deep.fillp[k]);
			}
		}
		
		
				
		
	}



	
	

	



	private ArrayList<Integer> getPath(int s1, int s2, SentenceData09 sure) {

		ArrayList<Integer> p = new ArrayList<Integer>();
		
		int h = s2;;
		p.add(h);
		while(true) {
			
			h = sure.heads[h];
			p.add(h);
			if (h==s1) break;
			if (p.size()>3) {
				System.out.println("path not found "+p);
				p.clear();
				break;
			}
		}
		
		
		return p;
	}

	private int getNode(int i, int[] d2f) {
		
		for(int k=0;k<d2f.length;k++) {
			if (d2f[k]==i) return k;
		}
		
		return -1;
	}

	/**
	 * @param r
	 * @param d
	 * @return
	 */
	public ArrayList<Integer> getChilds(int h, SentenceData09 d) {

		ArrayList<Integer> cs = new ArrayList<Integer>();
		for(int i=0;i<d.length();i++) {

			if (d.heads[i]==h) cs.add(i);

		}
		return cs;
	}

	
	


	public void initValues() {
		s_rules = mf.getFeatureBits(RULE);
		s_word = mf.getFeatureBits(WORD);
		s_type = mf.getFeatureBits(TYPE);
		s_pos =mf.getFeatureBits(POS);
		s_ps =mf.getFeatureBits(PS);
		s_rel=mf.getFeatureBits(REL);
		//	dl1.a[0] = s_type; dl1.a[1] = s_pos;
		//	for (int k = 2; k < 7; k++) dl1.a[k] = s_pos;

		d1.a0 = s_type; d1.a1 = s_rules; d1.a2= s_word; d1.a3= s_word;d1.a4= s_pos;d1.a5= s_pos;d1.a6= 3;
		d2.a0 = s_type; d2.a1 = s_rules; d2.a2= s_rules; d2.a3= s_rules; d2.a4= s_rules; d2.a5= s_rules; d2.a6= s_rules;
		d3.a0 = s_type; d3.a1 = s_rules; d3.a2=  s_char; d3.a3= s_char; d3.a4= s_char; d3.a5=  s_char; d3.a6=  s_char; d3.a7= s_char;
		dp.a0 = s_type; dp.a1 = s_rules; dp.a2=  s_pos; dp.a3= s_pos; dp.a4= s_pos; dp.a5= s_pos; dp.a5=  s_pos; dp.a6=  s_pos;// dp.a7= s_char;
		dp2.a0 = s_type; dp2.a1 = s_rules; dp2.a2=  s_pos; dp2.a3= s_rel; dp2.a4= s_rel; dp2.a5= s_rel; dp2.a5=  s_rel; dp2.a6=  s_rel; dp2.a7= s_rel;
		dw.a0 = s_type; dw.a1 = s_rules;dw.a2=  s_word; dw.a3= s_word; dw.a4= s_word; dw.a5=  s_word; dw.a6=  s_word; dw.a7= s_word;
		dwp.a0 = s_type; dwp.a1 = s_rules;dwp.a2= s_word ; dwp.a3= s_pos; dwp.a4= s_pos; dwp.a5= s_pos; dwp.a6= s_pos; dwp.a7= s_pos; 

	}

	public static short s_rules,s_word,s_type,s_dir,s_dist,s_char,s_pos,s_ps, s_rel;



	/**
	 * Initialize the features types.
	 */
	public void initFeatures() {

		for(int t=0;t<62;t++) {
			mf.register(TYPE,"F"+t);			
		}


		//		_mid = mf.register(POS, MID);
//		mf.register(POS, STR);
	//	_strrel = mf.register(REL, STR);
		_endp= mf.register(POS, END);
		_endf= mf.register(FEAT, END);

		_endr= mf.register(FEAT, END);

		_ends=mf.register(WORD, STR);
		//	_ewrd = mf.register(WORD, END);


		//	_CEND = mf.register(CHAR, END);




		// optional features
		mf.register(WORD,STWRD);
		mf.register(POS,STPOS);


	}


	final public void addCF(Instances is, int ic, int node, int rs, long[] vs) {

		int f=1,n=0;
		int head = is.heads[ic][node];

		int hh = head==-1?-1:is.heads[ic][head];

		short[] labs = is.labels[ic];
		
		int nP = is.gpos[ic][node];		
		
		short[] pos = is.gpos[ic];
		
		int hP = head==-1?_endp:is.gpos[ic][head];
		
		int hhP = hh==-1?_endp:is.gpos[ic][head];
		
		
		int nL = is.glemmas[ic][node];
		int hL = head==-1?_ends:is.glemmas[ic][head];

		int hR = head==-1?_endr:is.labels[ic][node];
		int nR = is.labels[ic][node];
				
		

		dp.v0 = f++; dp.v2=rs; dp.v3=nP;            vs[n++]=mf.calc4(dp);
		dp.v0 = f++; dp.v2=rs; dp.v3=hP;            vs[n++]=mf.calc4(dp);
		dp.v0 = f++; dp.v2=rs; dp.v3=hhP;           vs[n++]=mf.calc4(dp);

		dp.v0 = f++; dp.v2=rs;dp.v3=nP; dp.v4=hP;   vs[n++]=mf.calc5(dp);	
	    dp.v0 = f++; dp.v2=rs;dp.v3=nP; dp.v4=hP; dp.v5=nR;  ;vs[n++]=mf.calc6(dp);
	    dp2.v0 = f++; dp2.v2=rs;dp2.v3=nP; dp2.v4=hP; dp2.v5=nR; dp2.v6=hR;  ;vs[n++]=mf.calc7(dp);
	    		
		
	    dw.v0 = f++; dw.v2=rs;dw.v3=nL;            vs[n++]=mf.calc4(dw); 
	    dw.v0 = f++; dw.v2=rs;dw.v3=hL;            vs[n++]=mf.calc4(dw); 
	    dw.v0 = f++; dw.v2=rs;dw.v3=nL;dw.v4=hL;   vs[n++]=mf.calc5(dw); 

	    dw.v0 = f++; dw.v2=rs;dw.v3=nL;dw.v4=nP;   vs[n++]=mf.calc5(dw); 
	    dw.v0 = f++; dw.v2=rs;dw.v3=nL;dw.v4=hP;dw.v5=nR;   vs[n++]=mf.calc6(dw); 
	    dw.v0 = f++; dw.v2=rs;dw.v3=nP;dw.v4=hL;dw.v5=nR;   vs[n++]=mf.calc6(dw); 
	    
	    
		
		int[] children = getChildren(node, is,ic);
		
		dp.v0 = f++; dp.v2=rs; dp.v3=pos[node]; 

		// bag of child pos
		
		int bag[] = getBag(children, pos);
		
		if (bag[0]!=1000) dp.v4=bag[0]; 
		if (bag[1]!=1000) dp.v5=bag[1]; 	
		if (bag[2]!=1000) dp.v6=bag[2]; 	


		if (bag[0]!=1000 && bag[1]==1000) vs[n++]=mf.calc5(dp);
		else if (bag[2]==1000) vs[n++]=mf.calc6(dp);				
		else vs[n++]=mf.calc7(dp);

		
		// get relative pronouns
		
		for(int c : children) {
			dp.v0 = f; dp.v2=rs; dp.v3=pos[node]; dp.v4=pos[c];
			int[] gcs = getChildren(c, is,ic);
			
			bag = getBag(gcs, pos);
			
			if (bag[0]!=1000) dp.v5=bag[0]; 
			if (bag[1]!=1000) dp.v6=bag[1]; 	
			if (bag[2]!=1000) dp.v7=bag[2]; 	


			if (bag[0]!=1000 && bag[1]==1000)  vs[n++]=mf.calc6(dp);
			else if (bag[2]==1000) vs[n++]=mf.calc7(dp);				
			else vs[n++]=mf.calc8(dp);
			
		}
		f++;
	   
		bag = getBag(children, pos);

		dp2.v0 = f++; dp2.v2=rs; dp2.v3=labs[node]; 

		if (bag[0]!=1000) dp.v4=bag[0]; 
		if (bag[1]!=1000) dp.v5=bag[1]; 	
		if (bag[2]!=1000) dp.v6=bag[2]; 	


		//add gc
		
		if (bag[0]!=1000) dp2.v4=bag[0]; 
		if (bag[1]!=1000) dp2.v5=bag[1]; 	
		if (bag[2]!=1000) dp2.v6=bag[2]; 	


		if (bag[0]!=1000 && bag[1]==1000)      vs[n++]=mf.calc5(dp2);
		else if (bag[2]==1000) vs[n++]=mf.calc6(dp2);				
		else vs[n++]=mf.calc7(dp2);

		int cnt=0;
		for(int c : children) {
			dp2.v0 = f; dp2.v2=rs; dp2.v3=pos[node]; dp2.v4=labs[c];
			int[] gcs = getChildren(c, is,ic);
			
			bag = getBag(gcs, labs);
			
			if (bag[0]!=1000) dp2.v5=bag[0]; 
			if (bag[1]!=1000) dp2.v6=bag[1]; 	
			if (bag[2]!=1000) dp2.v7=bag[2]; 	


			if (bag[0]!=1000 && bag[1]==1000)  vs[n++]=mf.calc6(dp2);
			else if (bag[2]==1000) vs[n++]=mf.calc7(dp2);				
			else vs[n++]=mf.calc8(dp2);
		
			dwp.v0 =f+1; dwp.v2 =rs; dwp.v4=pos[node]; dwp.v5=pos[c];
			

			
			for(int g :gcs) {
				dwp.v3=is.glemmas[ic][g];
				if (cnt++>18) break;	
					
				vs[n++]=mf.calc6(dwp);
				
			
			}
			
			
		}
						

		f+=2;
	
		
		if (head!=-1) {
			
			//add the siblings
			
			dp2.v0 = f; dp2.v2=rs; dp2.v3=labs[head]; //dp2.v4=labs[node]; 

			children = getChildren(head, is,ic);
			
			bag = getBag(children, labs);

			if (bag[0]!=1000) dp2.v4=bag[0]; 
			if (bag[1]!=1000) dp2.v5=bag[1]; 	
			if (bag[2]!=1000) dp2.v6=bag[2]; 	


			if (bag[0]!=1000 && bag[1]==1000)      vs[n++]=mf.calc5(dp2);
			else if (bag[2]==1000) vs[n++]=mf.calc6(dp2);				
			else vs[n++]=mf.calc7(dp2);

			cnt=0;
			for(int c : children) {
				dp2.v0 = f+1; dp2.v2=rs+(node==c?1:2)*4; dp2.v3=pos[head]; dp2.v4=labs[c];
				int[] gcs = getChildren(c, is,ic);
				
				bag = getBag(gcs, labs);
				
				if (bag[0]!=1000) dp2.v5=bag[0]; 
				if (bag[1]!=1000) dp2.v6=bag[1]; 	
				if (bag[2]!=1000) dp2.v7=bag[2]; 	


				if (bag[0]!=1000 && bag[1]==1000)  vs[n++]=mf.calc6(dp2);
				else if (bag[2]==1000) vs[n++]=mf.calc7(dp2);				
				else vs[n++]=mf.calc8(dp2);
				
				dwp.v0 =f+1; dwp.v2 =rs+(node==c?1:2)*4; dwp.v4=pos[head]; dwp.v5=pos[c];
				
				for(int g :gcs) {
					dwp.v3=is.glemmas[ic][g];
					if (cnt++>12) break;
						vs[n++]=mf.calc6(dwp);
				}
				
			}
			
		} 
		
		f+=2;
			    
		vs[n] = Long.MIN_VALUE;
	}




	
	/**
	 * @param children
	 * @param pos2
	 * @return
	 */
	final public static int[] getBag(int[] children, short[] pos) {

		int bag[] = new int[3]; bag[0]=1000;bag[1]=1000;bag[2]=1000;
		int position=0;
		for(int c : children) {	
			for(int b : bag) {
				if (b==pos[c]) break;
				if (b==1000) {
					bag[position++]=pos[c];
					break;
				}
			}
			if(position>=bag.length) break;
		}
				
		Arrays.sort(bag);

		return bag;
	}

	/**
	 * @param h
	 * @param is
	 * @param n
	 * @return
	 */
	public static int[] getChildren(int h, Instances is, int n) {

		int cnt=0;

		// count siblings
		for(int i=0;i<is.length(n);i++) if (is.heads[n][i]==h) cnt++;
		int[] children = new int[cnt];
		cnt=0;
		for(int i=0;i<is.length(n);i++) 
			if (is.heads[n][i]==h) children[cnt++]=i;



		return children;
	}










	static class Rule implements Comparable<Rule> {
		int rule;
		float score;
		public int compareTo(Rule o) {
			return o.score>score?1:o.score==score?0:-1;
		}
	}

	



	public static int _FC =130 ;


	/**
	 * Write the lemma that are not mapped by operations
	 * @param dos
	 */
	public void writeMap(DataOutputStream dos) {



		//static HashMap<String,HashSet<String>> ruleMap
		try {

			//static HashMap<String,HashSet<String>>
			dos.writeInt(this.ruleMap.size());
			for(Entry<String,HashSet<String>> e :  ruleMap.entrySet()) {
				dos.writeUTF(e.getKey());
				HashSet<String> hs = e.getValue();
				dos.writeInt(hs.size());
				for(String s : hs ){
					dos.writeUTF(s);
				}
				//dos.writeInt(e.getValue());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}



	/**
	 * Read the rule mapping
	 * @param dis
	 */
	public void readMap(DataInputStream dis) {
		try {
			int size = dis.readInt();
			//			dos.writeInt(this.ruleMap.size());

			for(int i =0; i<size;i++) {

				String key = dis.readUTF();
				HashSet<String> hs = new HashSet<String>();
				int hashSize = dis.readInt();
				this.ruleMap.put(key, hs);
				for(int k =0; k<hashSize;k++) {
					hs.add(dis.readUTF());
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see is2.tools.IPipe#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) {
		writeMap(dos);
	}

	/**
	 * Get all leafs
	 * @param ds
	 * @return
	 */
	public int[] getLeafs(SentenceData09 ds) {

		// count the leafs
		int leafCount=0;
		for(int i=0;i<ds.length();i++) 	if (!hasChild(i,ds)) leafCount++;
		
		int leafs[] = new int[leafCount];
		leafCount=0;
		for(int i=0;i<ds.length();i++) 	{
			if (!hasChild(i,ds)) leafs[leafCount++]=i;	
		}
		
		return leafs;
	}

	/**
	 * Has n at least one child?
	 * @param n
	 * @param ds
	 * @return
	 */
	private boolean hasChild(int n, SentenceData09 ds) {

		for(int i=ds.length()-1;i>=0;i--) {
			if (ds.heads[i]==n) return true;
		}
		return false;
	}

	
		
	


}
