/**
 * 
 */
package sem2syn2;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import rt.algorithm.GraphConverter;
import rt.model.Environment;
import rt.model.Graph;
import rt.model.IO;
import rt.util.DB;
import sem2syn2.DSyntConverter.Word;

/**
 * @author Dr. Bernd Bohnet, 06.10.2010
 * 
 * 
 */
public class SemConverter {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options(args);
		
		CONLLReader09 depReader = new CONLLReader09();
		depReader.startReading(options.trainfile);
		//		CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);
		CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);


		int cnt = 0;
	//	int del=0;

		System.out.print("Processing Sentence: ");
	//	Data data=null;


		while(true) {

			SentenceData09 instance = depReader.getNext();
			if (instance==null) break;
			//cnt++;

			if (options.count<=cnt++) break;
		//	DB.println("cnt "+cnt);
			
			Graph g  = convert(instance,depWriter);
		
			/*
			if (cnt==3412) {
				DB.println(""+instance);
				System.exit(0);
			}
			*/

		}

		depWriter.finishWriting();
		DB.println("delt "+del);

		DB.println("dels "+DSyntConverter.dels+" #"+DSyntConverter.dels.size());
			
		
		DB.println("non connected count "+notConnectedCount);

	}


	static int count =0;

	static class Added {

		
		Word head,dep;
		/**
		 * @param p
		 * @param n
		 * @param label1
		 * @param label2
		 * @param name 
		 */
		public Added(int p, int n, String label1, String label2, String name) {
			parent=p;
			dependent=n;
			this.name=name;
			this.label1 = label1;
			this.label2 = label2;
		}
		int parent,dependent;
		String label1,label2,name;
		
	}

	/**
	 * @param depWriter 
	 * @param instance
	 * @return
	 * @throws IOException 
	 */
	public static Graph convert(SentenceData09 i, CONLLWriter09 depWriter) throws IOException {
		Graph g = new Graph();
		Environment env = new Environment(g);

		count++;
		

		// create all nodes
		for(int k=0; k<i.length();k++) {
			//
			String name = i.lemmas[k]; // should contain sense 
			env.createNode("\""+name+"\"", Graph.NODE);
		}
		
		ArrayList<Integer> delete = new ArrayList<Integer>();
		ArrayList<Added> added = new ArrayList<Added>();
		
		String feats[] = new String[i.length()];

		
		if (i.arg!=null) {
			for(int k=0; k<i.arg.length;k++){
				if (i.arg[k]!=null){

					for(int j=0;j<i.arg[k].length;j++) {
						
						int invert=1;  
						
						String edgeName =i.arg[k][j];

						int target = i.argposition[k][j];
						int source =-1;
						// remove prep

						if ((i.gpos[target].equals("TO") ||i.gpos[target].equals("IN"))
								&&( 
								i.arg[k][j].startsWith("A0")
								|| i.arg[k][j].equals("A1")|| i.arg[k][j].equals("A2")
								|| i.arg[k][j].equals("A3")|| i.arg[k][j].equals("A4")
								|| i.arg[k][j].equals("A5")) && 
								!(i.lemmas[target].equals("above")||i.lemmas[target].equals("about")||i.lemmas[target].equals("towards")
								  ||i.lemmas[target].equals("by")||i.lemmas[target].equals("up")||i.lemmas[target].equals("against")
								  ||i.lemmas[target].equals("among")||i.lemmas[target].equals("between")||i.lemmas[target].equals("into")
								  ||i.lemmas[target].equals("down")||i.lemmas[target].equals("until")||i.lemmas[target].equals("below")||
								  i.lemmas[target].equals("beyond")||i.lemmas[target].equals("as")||i.lemmas[target].equals("before")
								  ||i.lemmas[target].equals("atop")||i.lemmas[target].equals("fiscal")||i.lemmas[target].equals("whether")
								  ||i.lemmas[target].equals("across")||i.lemmas[target].equals("beside")||i.lemmas[target].equals("toward")
								  ||i.lemmas[target].equals("past")||i.lemmas[target].equals("around")||i.lemmas[target].equals("per")
								  ||i.lemmas[target].equals("along")||i.lemmas[target].equals("worth")||i.lemmas[target].equals("behind")
								  ||i.lemmas[target].equals("outside")||i.lemmas[target].equals("whatever")||i.lemmas[target].equals("under")
								  ||i.lemmas[target].equals("although")||i.lemmas[target].equals("after")||i.lemmas[target].equals("because")
								  ||i.lemmas[target].equals("over")||i.lemmas[target].equals("throughout")||i.lemmas[target].equals("onto")
								  ||i.lemmas[target].equals("like")||i.lemmas[target].equals("amid")||i.lemmas[target].equals("within")
								  ||i.lemmas[target].equals("though")||i.lemmas[target].equals("without")||i.lemmas[target].equals("upon")
								  ||i.lemmas[target].equals("off")||i.lemmas[target].equals("through")||i.lemmas[target].equals("near")))
						 {
						//	if (getChildren(n,i)==-1) 
								delete.add(target);
							
								
							int child = getChild(target, i);
							if (child==-1) DB.println("error while determining child of prep in sentence "+count+" prep-node "+target);
							else {
								target =child;
							}
							continue;
						}

						if (i.arg[k][j].equals("A0")||i.arg[k][j].equals("A1")||i.arg[k][j].equals("A2")||i.arg[k][j].equals("A3")||
							i.arg[k][j].equals("A4")||i.arg[k][j].equals("A5")) {
							
							edgeName=i.arg[k][j];
						
						} else if ((i.arg[k][j].equals("AM-DIR") || i.arg[k][j].equals("AM-TMP"))
								&& (i.gpos[target].equals("RB"))) {
							invert=2;
							edgeName="A1";
							source= target;
						} else if ((i.arg[k][j].equals("AM-LOC") )
								&& (i.gpos[target].equals("NNP"))) {
							invert=2;
						//	edgeName="A1";
							source= target;

						} else if ((i.arg[k][j].equals("AM-LOC")||i.arg[k][j].equals("AM-PNC")||i.arg[k][j].equals("AM-MAN")||
								i.arg[k][j].equals("AM-CAU")||i.arg[k][j].equals("AM-TMP")||i.arg[k][j].equals("AM-EXT")||
								i.arg[k][j].equals("AM-PRD")||i.arg[k][j].equals("AM-ADV")||i.arg[k][j].equals("AM-NEG")) 
								&& (i.gpos[target].equals("IN")||i.gpos[target].equals("TO"))) {
							invert=2;
						//	edgeName="A1";
							source= target;


							int child = getChild(target,i);
							int e = env.createNode("A2", Graph.EDGE);
							g.createEdge(source, e);
							g.createEdge(e,child);	


						} else if ((i.arg[k][j].equals("AM-DIS") )) {
							invert=2;
							edgeName="A2";
							source= target;
						}  else if ((i.arg[k][j].equals("AM-MOD") )) {
							invert=2;
							edgeName="A2";
							source= target;
						}  else if ((i.arg[k][j].equals("R-A1")||i.arg[k][j].equals("R-A2")||
								i.arg[k][j].equals("R-A3")||i.arg[k][j].equals("R-AM-LOC") ) 
								&& i.gpos[target].equals("WDT")) {
							
							// do nothing:
							invert=3;
							
							int a = env.createNode("thematicity", Graph.ATTRIBUTE);
							int v = env.createNode("true", Graph.VALUE);
							
							g.createEdge(i.semposition[k], a);
							g.createEdge(a, v);
							
							delete.add(target);
							
							if (!i.forms[target].equals("that") && !i.forms[target].equals("which")){
								DB.println("del!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "+i.forms[target]);
							}

							
							if (feats[i.semposition[k]] == null) feats[i.semposition[k]]="";
							feats[i.semposition[k]] += (feats[i.semposition[k]].length()>0?"|":"")+"thematicity=true";

							
							
						} else if ((i.arg[k][j].equals("C-A1") )) {
							edgeName="A1";
						} else if ((i.arg[k][j].equals("C-A2") )) {
							edgeName="A2";
						} else if ((i.arg[k][j].equals("C-A3") )) {
							edgeName="A3";
						} else if (i.arg[k][j].startsWith("C-AM-") ) {
							edgeName="A4";
						}

						// 1 normal edges, 2 invert edges, 3 do nothing
						
						// replace - in edges with _
						
						edgeName=edgeName.replace("-", "_");
						
						int e = env.createNode(edgeName, Graph.EDGE);

						if (invert==2) {
							if (source==-1) {
								System.out.println("count "+count);
								System.exit(0);
							}
							g.createEdge(source, e);
							g.createEdge(e,i.semposition[k]);	
						} else if (invert==1) {
							g.createEdge(i.semposition[k], e);
							g.createEdge(e,target);							
							if (target==-1) {
								System.out.println("count "+count);
								System.exit(0);
							}
						}

					}
				}
			}
		}
		
		
		
		// introduce attribute givennesss
		for(int n=0;n< i.length();n++) {

			if (feats[n]==null)feats[n] ="";
			
			if (i.labels[n].equals("NMOD")&&i.gpos[n].equals("DT")&&(i.forms[n].equals("a")||i.forms[n].equals("the")||i.forms[n].equals("that")||i.forms[n].equals("this"))) {
				
				if (getChild(n,i)==-1) delete.add(n);
				
				
		//		if(getChild(n,i)!=-1 ) {
		//			DB.println("has child "+getChild(n,i)+" "+	i.forms[getChild(n,i)]+" form "+i.forms[n]);
		//			System.exit(0);
		//		}
				
				
				String value = i.forms[n].equals("a")?"1":i.forms[n].equals("the")?"2":i.forms[n].equals("this")?"3":i.forms[n].equals("that")?"4":"5";
				
				int a = env.createNode("givenness", Graph.ATTRIBUTE);
				int v = env.createNode(value, Graph.VALUE);
				g.createEdge(i.heads[n], a);
				g.createEdge(a, v);
				
				int h = i.heads[n];
				if (feats[h]==null)feats[h] ="";
				
				feats[h] += (feats[h].length()>0?"|":"")+"givenness="+value;

			} else if (i.labels[n].equals("P") && 
					!(i.forms[n].equals(".")||i.forms[n].equals("''")||i.forms[n].equals("``")||i.forms[n].equals("'")||i.forms[n].equals("...")||i.forms[n].equals("-")||i.forms[n].equals("(")||i.forms[n].equals(")") 
							||i.forms[n].equals("/")||i.forms[n].equals("--")	||i.forms[n].equals("}")||i.forms[n].equals("{")||i.forms[n].equals("`")
							||i.forms[n].equals("!")||i.forms[n].equals(":")||i.forms[n].equals("?")||i.forms[n].equals(";")||i.forms[n].equals(",")) 
				//	&& !(i.forms[n].equals(",") && i.labels[n].equals("COORD"))
					) {
				if (getChild(n,i)==-1) delete.add(n);
			}
			
			// introduce 
			if (i.gpos[n].equals("NNS") || i.gpos[n].equals("NNPS")) {

				int a = env.createNode("num", Graph.ATTRIBUTE);
				int v = env.createNode("pl", Graph.VALUE);
				g.createEdge(n, a);
				g.createEdge(a, v);
				
				feats[n] += (feats[n].length()>0?"|":"")+"num=pl";

				
			} else if (i.gpos[n].equals("NN") || i.gpos[n].equals("NNP")) {

				int a = env.createNode("num", Graph.ATTRIBUTE);
				int v = env.createNode("sg", Graph.VALUE);
				g.createEdge(n, a);
				g.createEdge(a, v);

				feats[n] += (feats[n].length()>0?"|":"")+"num=sg";

			}

			if (i.gpos[n].equals("VBD")) {

				int a = env.createNode("tense", Graph.ATTRIBUTE);
				int v = env.createNode("past", Graph.VALUE);
				g.createEdge(n, a);
				g.createEdge(a, v);
				
				feats[n] += (feats[n].length()>0?"|":"")+"tense=past";

				
			}			
			if (i.gpos[n].equals("VBP")||i.gpos[n].equals("VBZ")) {

				int a = env.createNode("tense", Graph.ATTRIBUTE);
				int v = env.createNode("pres", Graph.VALUE);
				g.createEdge(n, a);
				g.createEdge(a, v);

				feats[n] += (feats[n].length()>0?"|":"")+"tense=pres";
				
			}			
			
			if (i.gpos[n].equals("VBN")) {

				int a = env.createNode("partic", Graph.ATTRIBUTE);
				int v = env.createNode("past", Graph.VALUE);
				g.createEdge(n, a);
				g.createEdge(a, v);
				
				feats[n] += (feats[n].length()>0?"|":"")+"partic=past";

				
			}			
			
			if (i.gpos[n].equals("VBG")) {

				int a = env.createNode("partic", Graph.ATTRIBUTE);
				int v = env.createNode("pres", Graph.VALUE);
				g.createEdge(n, a);
				g.createEdge(a, v);

				feats[n] += (feats[n].length()>0?"|":"")+"partic=pres";

			}			
		}


		try {
			g.buildIn();
		} catch(Exception e) {
			DB.println("ERROR important error in graph "+count);
			
			
			
			
			e.printStackTrace();
			
	//		return null;
		}
		
		int root =-1;
		for(int k=1; k<i.length();k++) {
			if (i.heads[k]==0) {
				int edge = env.createNode("ROOT", Graph.EDGE);
				g.createEdge(0, edge);
				g.createEdge(edge,k);
				root =0;
				break;
			}
		}


		
		
		ArrayList<Integer> queue = new ArrayList<Integer>();
		queue.addAll(Semantic.childs(i, root));

		
		
		while(!queue.isEmpty()) {

			int n = queue.remove(0);
			queue.addAll(Semantic.childs(i, n));
			if (delete.contains(n)) {
							
				continue;
			}
		
		//	DB.println("conntected "+Environment.getValue(g.getContent(n))+" connected to root "+Semantic.connected(g,n,root));
			
			if( !Semantic.connected(g,n,root)) {

				// parent (0), edge label (1)
				int p = i.heads[n];
				
				// do not introduce a link to the head, if there is already one
				if (Semantic.connected(g, n, p)) {
					
//					DB.println(n+" connected with "+p );
					continue;
				}
				
				String l =i.labels[n];

				// how to connect: direction, label
				Str s = getSemStr(i,n,p,l);

				if(s!=null) {

					int edge1 = env.createNode(s.label1, Graph.EDGE);
					int edge2 = env.createNode(s.label2, Graph.EDGE);
					int node = env.createNode(s.nodeName, Graph.NODE);

					int n1 = s.gov==1? p : n;
					int n2 = s.gov==1? n : p;

					Added add = new Added(p,n,s.label1,s.label2,s.nodeName);
					added.add(add);
					
					g.createEdge(node, edge1);
					g.createEdge(edge1,n1);

					g.createEdge(node, edge2);
					g.createEdge(edge2, n2 );

					//			System.out.println("str "+count );

					continue;
				}



				// how to connect: direction, label
				Edge e = getSemEdge(i,n,p,l);

				if (e==null) {
					e = new Edge (true,"A");
				//	DB.println("found no mapping for "+i.gpos[p]+" "+l+" -> "+i.gpos[n]+" forms "+i.forms[p]+" "+l+" -> "+i.forms[n] );
					notConnectedCount++;


				} 

				// remove
				if (e.label.startsWith("DEL:")) {

					delete.add(n);
					if (getChild(n,i)==-1) {
						g.buildIn();
						continue;
					}
					n= getChild(n,i);  

				}

				// create link in semantic graph
				int edge = env.createNode(e.label, Graph.EDGE);

				
				if (delete.indexOf(p)>=0) {
					
					// use parent, if possible
					
					while(delete.indexOf(p)>=0 && p!=-1) {
						p=i.heads[p];
					}
					
					
				}
				
				
				if (p==-1) {
					// all parents to root deleted
					continue;
				}
				
				if (e.direction) {
					
				//	DB.println("create edge "+Environment.getValue(g.getContent(n))+" connected to  "+Environment.getValue(g.getContent(p))+" deleted "+delete.indexOf(n));
				//	if (delete.indexOf(p)>=0 ||delete.indexOf(n)>=0) DB.println("found one !!!! p "+delete.indexOf(p)+" n  "+delete.indexOf(n) );

					g.createEdge(p, edge);
					g.createEdge(edge,n);
				} else {
				//	DB.println("create edge "+Environment.getValue(g.getContent(p))+" to  "+Environment.getValue(g.getContent(n))+" deleted "+delete.indexOf(p));
				//	if (delete.indexOf(p)>=0 ||delete.indexOf(n)>=0) DB.println("found one !!!! p "+delete.indexOf(p)+" n  "+delete.indexOf(n) );
					
					g.createEdge(n, edge);
					g.createEdge(edge,p);
				}

			}

		}

		HashMap<Integer, Integer> map= new HashMap<Integer, Integer>();

		
		for (int d : delete) {
			g.setType(d, Graph.DELETED);
			del.put(i.forms[d], "");
			
		}
		
		for(int n=1;n< i.length();n++) {

			if (delete.contains(n)) continue;
			
			int a = env.createNode("id", Graph.ATTRIBUTE);
			int v = env.createNode(""+n, Graph.VALUE);
			g.createEdge(n, a);
			g.createEdge(a, v);

			
		}

		//remove the root
	//	delete.add(0);
		
		//convert
		SentenceData09 dsynt = DSyntConverter.convertSynt(i, delete, added, feats);
		
		
		if (dsynt.length()+1!=g.countNodes(Graph.NODE)) {
			DB.println("nodes does not match dsynt "+dsynt.length()+" sem:"+g.countNodes(Graph.NODE));
			;
			DB.println("strs-org/t"+count+".str");
	//		System.exit(0);
		}


		
		
		
		Graph m = new Graph();

		
		// map the nodes first
		for(int k=0;k<g.size();k++) {
		
			
			if (g.getType(k)==Graph.NODE && delete.indexOf(k)<0) {
				
				int node = m.createNode(Graph.NODE);
				map.put(k, node);
				
			}
			
		}

		
		
		
		// map the rest
		for(int k=0;k<g.size();k++) {
		
			if ( delete.indexOf(k)<0 && g.getType(k)!=Graph.NODE   ) {
				
				int node = m.createNode(g.getType(k));
				map.put(k, node); 
			}			
		}

				
		
		// transfer the edges
		for(int k=0;k<g.size();k++) {
			
			if ( delete.indexOf(k)>=0) continue;
		
			// copy content
			
			m.setContent(map.get(k), g.getContent(k));
			m.setType(map.get(k), g.getType(k));
			
			int out[] = g.getOut(k);
			

			
			if (out==null)continue;
			
			for(int o=1;o<=out[0];o++) {
				
				if (map.get(out[o])==null && ! (delete.indexOf(out[o])>=0)) {
					DB.println("error out "+out[o]+" not deleted but null  det "+delete+" map "+map+" out[] ");
		
					DB.println("k value "+Environment.getValue(g.getContent(k)));
//					System.exit(0);
					continue;
				}
			//	if (g.getType(out[o])==Graph.EDGE && map.get(g.getOut(out[o])[1])==null) {
			//		DB.println("error out "+out[o]+" not deleted but null  det "+delete+" map "+map+" out[] "+g.getOut(out[o])[1]);
			//		System.exit(0);
					
			//	}
				if (delete.indexOf(out[o])>=0) continue;
				
				if (((g.getType(out[o]) ==Graph.EDGE&& map.get(g.getOut(out[o])[1])!=null)||
						(g.getType(out[o]) ==Graph.ATTRIBUTE&& map.get(g.getOut(out[o])[1])!=null))) 
				{
			//	if (map.get(out[o])==null) continue;
					m.createEdge(map.get(k), map.get(out[o]));
					m.createEdge(map.get(out[o]), map.get(g.getOut(out[o])[1]));
				} else {
//					DB.println("error "+out[o]+" type out[o] "+g.getType(out[o])+" "+g.getContent(i));
//					DB.println("error "+out[o]+" type        "+g.getType(g.getOut(out[o])[1]));
				}
			}
			
			//int node = m.createNode(g.getType(k));
			//	map.put(k, node);
			//}			
		}
		
		m.buildIn();
		
		
		
	//	DB.println(""+GraphConverter.str(m));
		
		
		
		if (depWriter!=null) {
			
			IO.writeFile("strs-org/t"+count+".str", GraphConverter.str(g));

			depWriter.write(dsynt);
			
			depWriter.finishWriting();
		}
		return m; //g
	}
	
	static HashMap<String,String> del = new HashMap<String,String>(); 


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

	
public static class Edge {
		
		public Edge(boolean direction, String label) {
			this.label = label;
			this.direction = direction;
		}
		String label;
		boolean direction;
	}

	public static class Str {
		
		public Str(String l1, String l2, String nodeName, int g) {
			this.label1 = l1;
			this.label2 = l2;
			this.gov = g; // is govoner node 1 or 2
			this.nodeName=nodeName;
		}
		String nodeName;
		String label1;
		String label2;
		int gov;
	}

	

	final public static HashMap<String, Edge> mappingList = new HashMap<String, Edge>();
	final public static HashMap<String, Str> mappingStr = new HashMap<String, Str>();

	static {
		mappingList.put("CONJ", new Edge(true, "A2"));
		mappingList.put("COORD*CC", new Edge(true, "A1"));
		mappingList.put("NMOD*CD", new Edge(false, "A1"));
		mappingList.put("TMP*CD", new Edge(false, "A1"));
		mappingList.put("PMOD*CD", new Edge(false, "A1"));
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
		mappingList.put("P", new Edge(true, "P"));
		mappingList.put("VC@MD", new Edge(false, "A2")); // head a mod



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
