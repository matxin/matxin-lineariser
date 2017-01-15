package commata;


import is2.data.Cluster;
import is2.data.F2SF;
import is2.data.Instances;
import is2.data.InstancesTagger;
import is2.data.Long2Int;
import is2.data.Long2IntInterface;
import is2.data.ParametersFloat;
import is2.data.PipeGen;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.tools.IPipe;
import is2.util.DB;
import is2.util.OptionsSuper;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;



final public class Pipe extends PipeGen implements IPipe  {


	final static int _MAX=60;

	protected static final String GPOS = "GPOS", MID = "MID",END = "END",STR = "STR",FM="FM";
	private static final String STWRD = "STWRD";
	private static final String STPOS = "STPOS";

	static final boolean   STACK = true;

	private static short s_pos,s_word,s_char;
	protected static short s_type;
	private static int _strp,_ewrd;
	static int  _CEND;

	public String[] types;

	final public MFO mf;

	final MFO.Data4 d1 = new MFO.Data4(),d2 = new MFO.Data4(),d3 = new MFO.Data4(), 
	dw = new MFO.Data4(), dwp = new MFO.Data4();

	Cluster cl;

	private OptionsSuper options;

	public Pipe (OptionsSuper options, MFO mf) throws IOException {
		this.mf =mf;
		this.options = options;
	}	
	public HashMap<Integer, int[]> _pps = new  HashMap<Integer, int[]>();

	public Long2IntInterface li;

	public  int corpusWrds = 0;

	/* (non-Javadoc)
	 * @see is2.tag5.IPipe#createInstances(java.lang.String, java.io.File, is2.data.InstancesTagger)
	 */
	public Instances createInstances(String file) {

		InstancesTagger is = new InstancesTagger();

		CONLLReader09 depReader = new CONLLReader09(CONLLReader09.NO_NORMALIZE);

		depReader.startReading(file);
		mf.register(POS,"<root-POS>");
		mf.register(WORD,"<root>");

		System.out.println("Registering feature parts ");

		HashMap<Integer, HashSet<Integer>> pps = new  HashMap<Integer, HashSet<Integer>>();

		int ic=0;
		while(true) {
			SentenceData09 instance1 = depReader.getNext();
			if (instance1== null) break;
			ic++;

			String[] w = instance1.forms;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(WORD,  w[i1]);
			for(int i1 = 0; i1 < w.length; i1++) registerChars(CHAR,  w[i1]);
			for(int i1 = 0; i1 < w.length; i1++) registerChars(CHAR,  w[i1].toLowerCase());

			if (instance1.feats!=null)
				for(int i1 = 0; i1 < w.length; i1++) {
					String[] f = instance1.feats[i1];

					if(f!=null)
						for(String x :f ){
							if (x.contains("id=")) continue;
							mf.register(FEAT, x);
						}

				}

			w = instance1.forms;
			for(int i1 = 0; i1 < w.length; i1++) if (w[i1].equals(",") || w[i1].equals("_")) mf.register(this.FM,  w[i1]);

			w = instance1.plemmas;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(WORD,  w[i1]);			
			for(int i1 = 0; i1 < w.length; i1++) registerChars(CHAR,  w[i1]);

			w = instance1.labels;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(REL,  w[i1]);			


			w = instance1.lemmas;
			for(int i1 = 0; i1 < w.length; i1++) mf.register(WORD,  w[i1]);			
			for(int i1 = 0; i1 < w.length; i1++) registerChars(CHAR,  w[i1]);

			w = instance1.gpos;
			for(int i1 = 0; i1 < w.length; i1++) {
				mf.register(POS,  w[i1]);
			}
			for(int i1 = 0; i1 < w.length; i1++) {
				HashSet<Integer> ps = pps.get(mf.getValue(POS,w[i1]));
				if (ps==null) {
					ps= new HashSet<Integer>();
					pps.put(mf.getValue(POS,w[i1]), ps);
				}
				if (i1+1<w.length) ps.add(mf.getValue(POS,w[i1+1]));
			}

		}

		for(Entry<Integer,HashSet<Integer>> e : pps.entrySet()) {
			int[] ps = new int[e.getValue().size()];
			int j=0;
			for(int k : e.getValue().toArray(new Integer[0])) {
				ps[j++] =k;
			}
			//	_pps.put(e.getKey(), ps);
			//	System.out.println("put "+e.getKey()+" "+ps.length+" pps size "+_pps.size());
		}

		System.out.println("words in corpus "+(corpusWrds=mf.getFeatureCounter().get(Pipe.WORD)));
		if (options.clusterFile==null)cl = new Cluster();
		else cl=  new Cluster(options.clusterFile, mf,6);


		initFeatures();

		mf.calculateBits();
		initValues();

		System.out.println(""+mf.toString());

		depReader.startReading(file);

		int num1 = 0;

		System.out.print("Creating Instances: ");

		is.init(ic, mf) ;
		int del=0;

		try {
			//	BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("out.dat"),"UTF8"));

			while(true) {
				if (num1 % 100 ==0) del = outValue(num1, del);
				SentenceData09 instance1 = depReader.getNext(is);
				if (instance1== null) break;

				is.fillChars(instance1, num1,instance1.lemmas,_CEND);
				for(int k=0;k<instance1.length();k++) {
					if (instance1.ppos[k].contains("\\|"))  

						is.pposs[num1][k] = (short)mf.getValue(FM, instance1.ppos[k].split("\\|")[1]);
				}

				/*
				// create features 
				for (int k=0;k<instance1.length();k++) {
					long[] vs = new long[65];

					this.addFeatures(is, num1, instance1.forms[k], k, is.forms[num1], is.plemmas[num1], vs);

					StringBuilder b = new StringBuilder();
					b.append(instance1.gpos[k]).append(' ');

					//			int l = is.gpos[num1][k]<<is2.tokenizer.Pipe.s_type;

					for(int f=0; f<vs.length;f++ ) {
						if (vs[f]==Integer.MIN_VALUE) break;
						if (vs[f]<=0 ) continue;
					//	b.append(li.l2i(vs[f])).append(':').append('1').append(' ');
						b.append(vs[f]).append(':').append('1').append(' ');
					}
					br.write(b.toString());
					if (num1<=options.count+1) br.newLine();
					//System.out.println(b.toString());	
				}
				 */


				if (num1>options.count) break;

				num1++;
			}
			//	br.flush();
			//	br.close();
		} catch (Exception  e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};

		outValue(num1, del);
		System.out.println();

		types= mf.reverse(mf.getFeatureSet().get(POS));
		return is;
	}

	private void registerChars(String type, String word) {
		for(int i=0;i<word.length();i++) 	mf.register(type, Character.toString(word.charAt(i)));      
	}


	/* (non-Javadoc)
	 * @see is2.tag5.IPipe#initValues()
	 */
	public void initValues() {
		s_pos = mf.getFeatureBits(POS);
		s_word =  mf.getFeatureBits(WORD);
		s_type =  mf.getFeatureBits(TYPE);
		s_char =  mf.getFeatureBits(CHAR);

		d1.a0 = s_type; d1.a1 = s_pos; d1.a2= s_word;d1.a3= s_word;
		d2.a0 = s_type; d2.a1 = s_pos; d2.a2= s_pos; d2.a3= s_pos; d2.a4= s_pos; d2.a5= s_pos; d2.a6= s_pos;
		d3.a0 = s_type; d3.a1 = s_pos; d3.a2=  s_char; d3.a3= s_char; d3.a4= s_char; d3.a5=  s_char; d3.a6=  s_char; d3.a7= s_char;d3.a8= s_char;
		dw.a0 = s_type; dw.a1 = s_pos;dw.a2=  s_word; dw.a3= s_word; dw.a4= s_word; dw.a5=  s_word; dw.a6=  s_word; dw.a7= s_word;
		dwp.a0 = s_type; dwp.a1 = s_pos;dwp.a2= s_word ; dwp.a3= s_pos; dwp.a4= s_word; 

	}

	/* (non-Javadoc)
	 * @see is2.tag5.IPipe#initFeatures()
	 */
	public void initFeatures() {
		// 62
		for(int t=0;t<67;t++)  mf.register(TYPE, "F"+t);

		_strp = mf.register(POS, STR);
		_ewrd =mf.register(WORD, END);
		_CEND = mf.register(CHAR, END);


	}

	final public int addFeatures(InstancesTagger is, int ic, String fs,int i, int[] forms, int[] lemmas, long[] vs, boolean offset) {

		int next =i+1;
		if (offset) {
			if (i>0) i=i-1;
			next = i+2;
		}
		
		short[] pos =is.gpos[ic];

		forms=lemmas;

		int c0= is.chars[ic][i][0], c1=is.chars[ic][i][1], c2=is.chars[ic][i][2], c3=is.chars[ic][i][3], c4=is.chars[ic][i][4],c5=is.chars[ic][i][5];
		int e0 =is.chars[ic][i][6], e1 =is.chars[ic][i][7],e2 =is.chars[ic][i][8],e3 =is.chars[ic][i][9],e4 =is.chars[ic][i][10];

		int f=1,n=0;
		short upper =0, number = 1;
		for(int k1=0;k1<fs.length();k1++){
			char c = fs.charAt(k1);
			if (Character.isUpperCase(c)) {
				if (k1==0) upper=1;
				else {
					// first char + another
					if (upper==1) upper=3;
					// another uppercase in the word
					else if (upper==0) upper=2;
				}
			}

			// first 
			if (Character.isDigit(c) && k1==0) number =2 ;
			else if (Character.isDigit(c) && number==1) number = 3;
		}

		int  form2 = forms[i]<corpusWrds?forms[i]:-1;

		int len = forms.length;		

		d1.v0 = f++; d1.v2=form2; vs[n++]=mf.calc3(d1);

		d1.v0 = f++; d1.v2=is.formlc[ic][i]; vs[n++]=mf.calc3(d1);

		d3.v2=c0; d3.v3=c1; d3.v4=c2; d3.v5=c3; d3.v6=c4;
		d3.v0=f++; vs[n++]=mf.calc3(d3);
		d3.v0=f++; vs[n++]=mf.calc4(d3); 
		d3.v0=f++; vs[n++]=mf.calc5(d3); 
	//	d3.v0=f++; vs[n++]=mf.calc6(d3); 
	//	d3.v0=f++; vs[n++]=mf.calc7(d3);





		f+=3;
		//		DB.println("fz "+f+" "+n);

		d3.v2=e0; d3.v3=e1; d3.v4=e2; d3.v5=e3; d3.v6=e4;

			d3.v0 =f++; vs[n++]=mf.calc7(d3); 
		//	d3.v0 =f++; d3.v7=upper; vs[n++]=mf.calc8(d3); 
		//	d3.v0 =f++; vs[n++]=mf.calc6(d3);  
		//	d3.v0 =f++; d3.v6=upper; vs[n++]=mf.calc7(d3); 
		//	d3.v0 =f++; vs[n++]=mf.calc5(d3);   
		//	d3.v0 =f++; d3.v5=upper; vs[n++]=mf.calc6(d3); 
		//	d3.v0 =f++; vs[n++]=mf.calc4(d3);   
		////	d3.v0 =f++; d3.v4=upper; vs[n++]=mf.calc5(d3); 
		//	d3.v0 =f++; vs[n++]=mf.calc3(d3);	
		//	d3.v0 =f++; d3.v3=upper; vs[n++]=mf.calc4(d3); 

		d3.v2=e0; d3.v3=e1; d3.v4=e2;



		// sign three-grams
	//	d3.v0=f++;d3.v2=c1; d3.v3=c2; d3.v4=c3;  vs[n++]=mf.calc5(d3); 
	//	d3.v0=f++;d3.v2=c2; d3.v3=c3; d3.v4=c4;  vs[n++]=mf.calc5(d3); 
	//	d3.v0=f++;d3.v2=c3; d3.v3=c4; d3.v4=c5;  vs[n++]=mf.calc5(d3); 

		// sign quad-grams
		d3.v0=f++;d3.v2=c1; d3.v3=c2; d3.v4=c3; d3.v5=c4; vs[n++]=mf.calc6(d3); 
		d3.v0=f++;d3.v2=c2; d3.v3=c3; d3.v4=c4; d3.v5=c5; vs[n++]=mf.calc6(d3); 

		//	if (i+1<len && forms[i+1]<this.corpusWrds) {dw.v0=f; dw.v2=i+1<len?forms[i+1]:_ewrd;dw.v3= form2;vs[n++]=mf.calc4(dw);}
		f++;

		int th = is.pheads[ic][i];

		d2.v0=f++; d2.v2=is.chars[ic][i][11];vs[n++]=mf.calc3(d2);





		// the training set might contain id=x|.. as first one while the test set might contain none
	//	if (is.feats[ic][i]!=null && is.feats[ic][i].length>0) {
	//		dw.v0=f; dw.v2= is.feats[ic][i][0]; vs[n++]=mf.calc3(dw);

	//	}
	//	if (is.feats[ic][i]!=null && is.feats[ic][i].length>1) {
	//		dw.v0=f; dw.v2= is.feats[ic][i][1]; vs[n++]=mf.calc3(dw);

	//	}
	//	if (is.feats[ic][i]!=null && is.feats[ic][i].length>2) {
	//		dw.v0=f; dw.v2= is.feats[ic][i][2]; vs[n++]=mf.calc3(dw);

//		}
		f++;

		d3.v0=f++; d3.v2=is.labels[ic][i]; vs[n++]=mf.calc3(d3);


		dw.v0 = f++; dw.v2 =i>=1? lemmas[i-1]:_strp; vs[n++]=mf.calc3(dw);
		dw.v0 = f++; dw.v2 =i>1? lemmas[i-2]:_strp; vs[n++]=mf.calc3(dw);
		dw.v0 = f++; dw.v2 =i>2? lemmas[i-3]:_strp; vs[n++]=mf.calc3(dw);
		
		dw.v0 = f++; dw.v2 =next<len-2? lemmas[next]:_strp; vs[n++]=mf.calc3(dw);
		dw.v0 = f++; dw.v2 =next<len-3? lemmas[next+1]:_strp; vs[n++]=mf.calc3(dw);
		dw.v0 = f++; dw.v2 =next<len-3? lemmas[next+2]:_strp; vs[n++]=mf.calc3(dw);
		
		
		dw.v0 = f++; dw.v2 =i>=1? pos[i-1]:_strp; vs[n++]=mf.calc3(dw);
		dw.v0 = f++; dw.v2 =i>1?  pos[i-2]:_strp; vs[n++]=mf.calc3(dw);
	

		dw.v0 = f++; dw.v2 =i>=1? pos[i-1]:_strp; dw.v3 =i>1? pos[i-2]:_strp; vs[n++]=mf.calc4(dw);
		dw.v0 = f++; dw.v2 =i>=1? pos[i-1]:_strp; dw.v3 =i<len-2? pos[next]:-1; vs[n++]=mf.calc4(dw);
	

		dw.v0 = f++; dw.v2  =next<len-2? pos[next]:_strp; vs[n++]=mf.calc3(dw);
		dw.v0 = f++; dw.v2  =next<len-3? pos[next+1]:_strp; vs[n++]=mf.calc3(dw);
		

		
		dw.v0 = f++; dw.v2 =i>=1? lemmas[i-1]:_strp; dw.v3 =i<len-2? lemmas[next]:-1; vs[n++]=mf.calc4(dw);
		dw.v0 = f++; dw.v2 =i>=1? lemmas[i-1]:_strp; dw.v3 =i>1? lemmas[i-2]:_strp; vs[n++]=mf.calc4(dw);
		dw.v0 = f++; dw.v2 =i<len-2? lemmas[next]:-1; dw.v3 =i<len-3? lemmas[next+1]:-1; vs[n++]=mf.calc4(dw);
	
	
		int tm1 = i>0?pos[i-1]:_strp;
		int tm2 = i>1?pos[i-2]:_strp;

		int tc =pos[i];
		
		int tp1 = i<is.pposs[ic].length-2?pos[next]:_strp;
		int tp2 = i<is.pposs[ic].length-3?pos[next+1]:_strp;

		d2.v1 = 0;

   
		d2.v0 = f++; d2.v2=tm2;d2.v3=tm1;d2.v4=tc; vs[n++]=mf.calc5(d2);
		d2.v0 = f++; d2.v2=tc;d2.v3=tp1; d2.v4=tp2;vs[n++]=mf.calc5(d2);
		d2.v0 = f++; d2.v2=tm1;d2.v3=tc; d2.v4=tp1;vs[n++]=mf.calc5(d2);
		
		int[] children = getChildren(i, is,ic,offset?i+1:-1);

		d2.v2=children.length>0?pos[children[0]]:_strp; 
		d2.v3=children.length>1?pos[children[1]]:_strp; 
		d2.v4=children.length>2?pos[children[2]]:_strp; 

		vs[n++]=mf.calc3(d2);
		vs[n++]=mf.calc4(d2);
		vs[n++]=mf.calc5(d2);

		if (th>=0) {

			d3.v0=f+1; d3.v2 =is.chars[ic][th][0];vs[n++]=mf.calc3(d3);
			d3.v0 =f+2; d3.v4 =is.chars[ic][th][6];vs[n++]=mf.calc5(d3);

	

			dw.v0=f+3; dw.v2= lemmas[th]; vs[n++]=mf.calc3(dw);
			dw.v0=f+4; dw.v2= lemmas[th]; dw.v3 = lemmas[i];vs[n++]=mf.calc4(dw);
			dw.v0=f+4; dw.v2= lemmas[th]; dw.v3 = next<len? lemmas[next]:-1;vs[n++]=mf.calc4(dw);


			//		d2.v0 = f++;  		
			
				children = getChildren(th, is,ic,offset?i+1:-1);

						d2.v0 = f++;  
						d2.v2=children.length>0?pos[children[0]]:_strp; 
						d2.v3=children.length>1?pos[children[1]]:_strp; 
						d2.v4=children.length>2?pos[children[2]]:_strp; 

						if (children.length>2 && children[2]==i+1) DB.println("cotain ");
						
					vs[n++]=mf.calc3(d2);
					vs[n++]=mf.calc4(d2);
					vs[n++]=mf.calc5(d2);

			d2.v0=f++; d2.v2=is.labels[ic][th]; vs[n++]=mf.calc3(d2);
			d2.v0=f++; d2.v2=is.labels[ic][th]; d2.v3=d2.v2=is.labels[ic][i];  vs[n++]=mf.calc4(d2);

		}

		vs[n] = Integer.MIN_VALUE;
		return f;
	}


	final public void addFeatPos(InstancesTagger is, int ic, int i, short pos[], int[] forms, int f, long[] vs, boolean stack ) {
		int n=0;
		if(!stack) {
			vs[n] = Integer.MIN_VALUE;
			return;
		}


		if (true) return ;

		int tm1 = i>0?pos[i-1]:_strp;
		int tm2 = i>1?pos[i-2]:_strp;

		int tc =pos[i];

		/*
		int tp1 = i<is.pposs[ic].length-1?pos[i+1]:_strp;
		int tp2 = i<is.pposs[ic].length-2?pos[i+2]:_strp;

		d2.v1 = 0;

		d2.v0 = f++; d2.v2=tc; vs[n++]=mf.calc3(d2);
		d2.v0 = f++; d2.v2=tp1;vs[n++]=mf.calc3(d2);
		d2.v0 = f++; d2.v2=tm1;vs[n++]=mf.calc3(d2);

		d2.v0 = f++; d2.v2=tc;d2.v3=tm1;vs[n++]=mf.calc4(d2);
		d2.v0 = f++; d2.v2=tc;d2.v3=tp1;vs[n++]=mf.calc4(d2);

		d2.v0 = f++; d2.v2=tm2;d2.v3=tm1;d2.v4=tc; vs[n++]=mf.calc5(d2);
		d2.v0 = f++; d2.v2=tc;d2.v3=tp1; d2.v4=tp2;vs[n++]=mf.calc5(d2);
		d2.v0 = f++; d2.v2=tm1;d2.v3=tc; d2.v4=tp1;vs[n++]=mf.calc5(d2);
		 */	

		int th = is.pheads[ic][i];
		int thp=th>=0?pos[th]:_strp;

		d2.v0 = f++; d2.v2=tc;  d2.v3=thp;vs[n++]=mf.calc4(d2);
		d2.v0 = f++; d2.v2=thp; vs[n++]=mf.calc3(d2);

		dw.v0 = f++; dw.v2=tc;  dw.v3=th>=0?forms[th]:-1;     vs[n++]=mf.calc4(dw);
		//	dw.v0 = f++; dw.v2=th>=0?forms[th]:-1;                vs[n++]=mf.calc3(dw);

/*
		d2.v0 = f++;  
		int[] children = getChildren(i, is,ic);

		d2.v2=children.length>0?pos[children[0]]:_strp; 
		d2.v3=children.length>1?pos[children[1]]:_strp; 
		d2.v4=children.length>2?pos[children[2]]:_strp; 

		vs[n++]=mf.calc3(d2);
		vs[n++]=mf.calc4(d2);
		vs[n++]=mf.calc5(d2);

		d2.v0 = f++;  		
		children = getChildren(th, is,ic);

		d2.v0 = f++;  
		d2.v2=children.length>0?pos[children[0]]:_strp; 
		d2.v3=children.length>1?pos[children[1]]:_strp; 
		d2.v4=children.length>2?pos[children[2]]:_strp; 

		vs[n++]=mf.calc3(d2);
		vs[n++]=mf.calc4(d2);
		vs[n++]=mf.calc5(d2);


		int thm1 = th>0?pos[th-1]:_strp;

		int thp1 = th<is.pposs[ic].length-1?pos[th+1]:_strp;

		d2.v0 = f++; d2.v2=tc; d2.v3=thp; vs[n++]=mf.calc4(d2);
		d2.v0 = f++;  d2.v3=thp1;vs[n++]=mf.calc4(d2);
		d2.v0 = f++;  d2.v3=thm1;vs[n++]=mf.calc4(d2);

		d2.v0 = f++;  d2.v3=thp;d2.v4=thm1;vs[n++]=mf.calc5(d2);
		d2.v0 = f++;  d2.v3=thp;d2.v4=thp1;vs[n++]=mf.calc5(d2);
*/
		//		d2.v0 = f++; d2.v2=thp; vs[n++]=mf.calc3(d2);
		//		d2.v0 = f++; d2.v2=thp1;vs[n++]=mf.calc3(d2);
		//		d2.v0 = f++; d2.v2=thm1;vs[n++]=mf.calc3(d2);

		//		d2.v0 = f++; d2.v2=thp;d2.v3=thm1;vs[n++]=mf.calc4(d2);
		//		d2.v0 = f++; d2.v2=thp;d2.v3=thp1;vs[n++]=mf.calc4(d2);


		vs[n] = Integer.MIN_VALUE;

	}

	private int[] getChildren(int h, Instances is, int n, int j) {

		int cnt=0;

		// count siblings
		for(int i=0;i<is.length(n);i++) if (is.pheads[n][i]==h) cnt++;
		int[] children = new int[cnt];
		cnt=0;
		for(int i=0;i<is.length(n);i++) 
			if (is.pheads[n][i]==h && j!=i) children[cnt++]=i;



		return children;
	}


	public int fillFeatureVectorsOne(String fs,	ParametersFloat params, int w1, InstancesTagger is, int n, short[] dd,
			Long2IntInterface li, float[] score, float[][] w, boolean offset) {

		float best  = -1000;
		int bestType = -1;

		F2SF f = new F2SF(params.parameters);

		long vs[] = new long[_MAX];
		int lemmas[];
		//		if (options.noLemmas) lemmas = new int[is.length(n)];
		lemmas = is.glemmas[n];

		addFeatures(is,n,fs,w1,is.glemmas[n],lemmas, vs,offset);


		for(int t=0;t<types.length;t++) {

			int p = t<<s_type;

			f.clear();
			for(int k=0;vs[k]!=Integer.MIN_VALUE;k++) if(vs[k]>0) f.add(li.l2i(vs[k]+p));
			w[w1][t]=f.score;
			if (f.score > best) {
				bestType=t;
				score[w1]= best =f.score;
			}
		}	
		return bestType;

	}

	public int adaptFV(ParametersFloat params,	int word, InstancesTagger is, int n, short[] pos, Long2IntInterface li,float[][] w, boolean stack) {

		float best  = -1000;
		int bestType = -1;

		F2SF f = new F2SF(params.parameters);

		long vs[] = new long[_MAX];


		addFeatPos(is,n,word,pos,is.glemmas[n],60, vs,stack);// 59

		for(int t=0;t<types.length;t++) {

			int p = t<<s_type;

			f.clear();
			for(int k=0;vs[k]!=Integer.MIN_VALUE;k++) if(vs[k]>0) f.add(li.l2i(vs[k]+p));
			w[word][t]+=f.score;
			//	DB.println("before "+(w[word][t]-f.score)+" after "+w[word][t] );
			if (w[word][t] > best) {
				bestType=t;
				best =w[word][t];
			}
		}	
		return bestType;

	}


	/* (non-Javadoc)
	 * @see is2.tag5.IPipe#write(java.io.DataOutputStream)
	 */

	public void write(DataOutputStream dos){
		try {
			this.cl.write(dos);
			dos.writeInt(this.corpusWrds);
			dos.writeInt(_pps.size());

			for(Entry<Integer,int[]> e : _pps.entrySet()) {
				dos.writeInt(e.getValue().length);
				for(int k : e.getValue()) dos.writeInt(k);
				dos.writeInt(e.getKey());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void read(DataInputStream dis){
		try {
			this.cl =new Cluster(dis);
			this.corpusWrds = dis.readInt();

			int pc = dis.readInt();
			for(int j=0;j<pc;j++) {
				int ps[] = new int [dis.readInt()];
				for(int k=0;k<ps.length;k++) 	ps[k]=dis.readInt();
				_pps.put(dis.readInt(), ps);
			}
			//	System.out.println("_pps "+ps.length);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
