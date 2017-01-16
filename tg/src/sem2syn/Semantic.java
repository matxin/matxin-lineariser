/**
 * 
 */
package sem2syn;

import is2.data.SentenceData09;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import rt.algorithm.GraphConverter;
import rt.model.Environment;
import rt.model.Graph;
import rt.model.IO;
import rt.util.DB;

/**
 * @author Dr. Bernd Bohnet, 31.01.2010
 * 
 * 
 */
public class Semantic {

	/**
	 * @param instance1
	 */
	public static Graph convert(SentenceData09 i) {

		if (i==null) return null;

		Graph g = new Graph();
		Environment env = new Environment(g);

		HashMap<Integer,Integer> outEdge = new HashMap<Integer,Integer>();
		HashMap<Integer,Integer> inEdge = new HashMap<Integer,Integer>();
		HashSet<Integer> partens = new HashSet<Integer>();
		HashSet<Integer> done = new HashSet<Integer>();

		// create all nodes
		for(int k=0; k<i.length();k++) {
			int n =env.createNode("\""+i.lemmas[k]+"\"", Graph.NODE);
			//outEdge.put(n, 0);
			partens.add(i.heads[k]);
		}
		partens.add(0);

		// connect the root node
		for(int k=1; k<i.length();k++) {
			if (i.heads[k]==0) {
				int edge = env.createNode("A1", Graph.EDGE);
				g.createEdge(0, edge);
				g.createEdge(edge,k);
				inEdge.put(0, 1); // root is connected per default 
				inEdge.put(k, 1);
				break;
			}

		}



		if (i.arg!=null)
			for(int k=0; k<i.arg.length;k++){
				if (i.arg[k]!=null)
					for(int j=0;j<i.arg[k].length;j++) {
						int e = env.createNode(i.arg[k][j], Graph.EDGE);
						g.createEdge(i.semposition[k], e);
						g.createEdge(e,i.argposition[k][j]);

						if (i.semposition[k]!=i.argposition[k][j]) {
							outEdge.put(i.semposition[k], 1);	
							inEdge.put(i.argposition[k][j], 1);
						}
					}
			}




		for(int k=1; k<i.length();k++) {
			String label = i.labels[k];
			//	if (!inEdge.containsKey(k)) {

			// check if already connect to parent or child
			if (oneConnected(g,k,i.heads[k])) {
				done.add(k);
				continue;
			}

			if ((label.equals("NMOD")&&i.ppos[k].equals("IN")) ||(label.equals("ADV")&&i.ppos[k].equals("IN")) ) {
				int edge = env.createNode("A", Graph.EDGE);

				g.createEdge(i.heads[k], edge);
				g.createEdge(edge,k);

				outEdge.put(i.heads[k], 1);
				inEdge.put(k, 1);
				
			}
			
			// inverse direction and A1
			else if ((label.equals("NMOD")||label.equals("SUFFIX")||label.equals("AMOD")||
					label.equals("TMP")||label.equals("APPO")||label.equals("DEP")||label.equals("POSTHON")||
					label.equals("ADV")||label.equals("HMOD")||label.equals("MNR")||label.equals("GAP-LOC")||label.equals("GAP-TMP")
					||label.equals("PRD-PRP"))

					//&& inEdge.containsKey(i.heads[k])
			) {
				int edge = env.createNode("A1", Graph.EDGE);
				g.createEdge(k, edge);
				g.createEdge(edge,i.heads[k]);
				outEdge.put(k, 1);
				inEdge.put(i.heads[k], 1);
				done.add(k);			
				// inverse direction and keep name
			} else if (label.equals("TITLE")) {

				int edge = env.createNode(label, Graph.EDGE);

				g.createEdge(k, edge);
				g.createEdge(edge,i.heads[k]);
				outEdge.put(k, 1);
				inEdge.put(i.heads[k], 1);
				done.add(k);
				// same direction and keep name 
			} else if ((label.equals("P")||label.equals("NAME")||label.equals("HYPH")||label.equals("PRT")||label.equals("PRP")||
					label.equals("PRN")) ) {

				int edge = env.createNode(label, Graph.EDGE);
				g.createEdge(i.heads[k], edge);
				g.createEdge(edge,k);
				outEdge.put(i.heads[k], 1);
				inEdge.put(k,1);
				// same direction and A1
			} else if ((label.equals("COORD")||label.equals("SBJ")||label.equals("PRD")||label.equals("LOC")||label.equals("GAP-SBJ")||
					label.equals("VC"))
					//	&& ! inEdge.containsKey(k)

			) {

				int edge = env.createNode("A", Graph.EDGE);

				g.createEdge(i.heads[k], edge);
				g.createEdge(edge,k);

				outEdge.put(i.heads[k], 1);
				inEdge.put(k, 1);

				// same direction and A2
			} else if ((label.equals("CONJ")||label.equals("PMOD")||label.equals("IM")||label.equals("SUB")||label.equals("OBJ")||
					label.equals("GAP-LOC-PRD")||label.equals("LOC-PRD")||label.equals("DEP-GAP")||label.equals("VOC")||label.equals("OPRD")||label.equals("DIR")
					||label.equals("EXTR")||label.equals("LGS")||label.equals("PRD-TMP")||
					label.equals("DIR-PRD")||label.equals("EXT")) 
					//&& ! inEdge.containsKey(k)


			) {
				int edge = env.createNode("A2", Graph.EDGE);
				g.createEdge(i.heads[k], edge);
				g.createEdge(edge,k);
				inEdge.put(k, 1);
				outEdge.put(i.heads[k], 1);
			}
			//	connected.put(e.getKey(), 1);

			//			}
		}

		
		

		int notConnected =0;
		for(Entry<Integer, Integer> e :  outEdge.entrySet()){
			if (done.contains(e.getKey())) continue;
			if (!inEdge.containsKey(e.getKey()) && !(!partens.contains(e.getKey())&&outEdge.containsKey(e.getKey()))) {
				notConnected++;
				System.out.println("\n not connected "+Environment.getValue(g.getContent(e.getKey()))+" id "+e.getKey()+" "+i.labels[e.getKey()]);
			}
		}
		//System.out.println("test "+GraphConverter.str(g));
		IO.writeFile("strs/t"+(count++)+".str", GraphConverter.str(g));
		if (notConnected>0) {

		//	System.exit(0);

		}
		return g;
	}

	/**
	 * Are the the nodes s, t connected in a distance of 1
	 * @param g
	 * @param s
	 * @param t
	 * @return
	 */
	private static boolean oneConnected(Graph g, int s, int t) {

		int out[] =g.getOut(s);
		if (out!=null)
			for(int k=1;k<=out[0];k++){
				if (g.getOut(out[k])[1]==t) return true;
			}
		g.buildIn();
		int in[] =g.getIn(s);
		if (in!=null)
			for(int k=1;k<=in[0];k++){
				if (g.getIn(in[k])[1]==t) return true;
			}

		return false;
	}

	static int count =0;
	
	
	public static Graph convertTMethod(SentenceData09 i) {

		if (i==null) return null;

		Graph g = new Graph();
		Environment env = new Environment(g);

//		HashMap<Integer,Integer> outEdge = new HashMap<Integer,Integer>();
//		HashMap<Integer,Integer> inEdge = new HashMap<Integer,Integer>();
//		HashSet<Integer> partens = new HashSet<Integer>();

		// create all nodes
		for(int k=0; k<i.length();k++) {
			env.createNode("\""+i.lemmas[k]+"\"", Graph.NODE);
//			partens.add(i.heads[k]);
		}

		
		int root =-1;
		
		// connect the root node
		for(int k=1; k<i.length();k++) {
			if (i.heads[k]==0) {
				int edge = env.createNode("A1", Graph.EDGE);
				g.createEdge(0, edge);
				g.createEdge(edge,k);
				root = i.heads[k];
				break;
			}
		}

				
		
		if (i.arg!=null)
			for(int k=0; k<i.arg.length;k++){
				if (i.arg[k]!=null)
					for(int j=0;j<i.arg[k].length;j++) {
						int e = env.createNode(i.arg[k][j], Graph.EDGE);
						g.createEdge(i.semposition[k], e);
						g.createEdge(e,i.argposition[k][j]);

					}
			}

		g.buildIn();
		//count++;
		IO.writeFile("strs-org/t"+count+".str", GraphConverter.str(g));
		// warning this is the option for a base line!!!!
		//if(true) return g;
		
		ArrayList<Integer> queue = new ArrayList<Integer>();
		queue.addAll(childs(i, root));
		
		while(!queue.isEmpty()) {
			
			int n = queue.remove(0);
			queue.addAll(childs(i, n));
			if( !connected(g,n,root)) {
				
				
				// parent (0), edge label (1)
				int p = i.heads[n];
				String l =i.labels[n];
				
				// how to connect: direction, label
				Edge e = getSemEdge(i,n,p,l);
				
				// create link in semantic graph
				
				int edge = env.createNode(e.label, Graph.EDGE);
				
				if (e.direction) {
					g.createEdge(p, edge);
					g.createEdge(edge,n);
				} else {
					g.createEdge(n, edge);
					g.createEdge(edge,p);
				}
				g.buildIn();
				
			}
			
		}

	
		//System.out.println("test "+GraphConverter.str(g));
		IO.writeFile("strs/t"+(count)+".str", GraphConverter.str(g));
		count++;
	///	DB.println("not connected  "+notConnectedCount+"  "+GraphConverter.str(g));
	//	if (notConnectedCount>0) DB.println("not connected  "+notConnectedCount);
		
		return g;
	}

	final public static HashMap<String, Edge> mappingList = new HashMap<String, Edge>();
	
	static int notConnectedCount=0;
	
	static {
		mappingList.put("CONJ", new Edge(true, "A2"));
		mappingList.put("P", new Edge(true, "P"));
		mappingList.put("AMOD", new Edge(false, "A1"));
		mappingList.put("PMOD", new Edge(false, "A2"));
		mappingList.put("NMOD*JJ", new Edge(false, "A1"));
		mappingList.put("COORD*CC", new Edge(true, "A1"));
		mappingList.put("COORD", new Edge(true, "A"));
		mappingList.put("NAME", new Edge(true, "NAME"));
		mappingList.put("TITLE", new Edge(true, "TITLE"));
		mappingList.put("LOC", new Edge(false, "AM-LOC"));
		mappingList.put("APPO", new Edge(false, "A1"));
		mappingList.put("DEP", new Edge(false, "A1"));
		mappingList.put("POSTHON", new Edge(false, "A1"));
		mappingList.put("SUFFIX", new Edge(false, "A1"));
		mappingList.put("NMOD*DT", new Edge(false, "A1"));
		mappingList.put("NMOD*NN", new Edge(false, "A1"));
		mappingList.put("NMOD*CD", new Edge(false, "A1"));
		mappingList.put("NMOD", new Edge(false, "A1"));
		mappingList.put("TMP", new Edge(true, "AM-TMP"));
		mappingList.put("PRN", new Edge(true, "A"));
		mappingList.put("HMOD", new Edge(false, "A"));
		mappingList.put("HYPH", new Edge(true, "A"));
		mappingList.put("SBJ", new Edge(true, "A0"));
		mappingList.put("IM", new Edge(true, "A2"));
		mappingList.put("OPRD", new Edge(true, "AX"));
		mappingList.put("PRD", new Edge(true, "A1"));
		mappingList.put("ADV", new Edge(true, "A"));
		mappingList.put("VC", new Edge(true, "A1"));
		mappingList.put("SUB", new Edge(true, "A1"));
		mappingList.put("PRT", new Edge(true, "A1"));
		mappingList.put("PRP", new Edge(false, "A1"));
		mappingList.put("LOC-PRD", new Edge(true, "AM-LOC"));
		mappingList.put("PRD-PRP", new Edge(false, "A1"));
		mappingList.put("EXTR*TO", new Edge(false, "A1"));
		mappingList.put("EXTR-GAP", new Edge(false, "A2"));
		mappingList.put("EXTR", new Edge(false, "A1"));
		mappingList.put("GAP-PRD", new Edge(false, "A2"));
		mappingList.put("DIR", new Edge(false, "A1"));
		mappingList.put("OBJ", new Edge(true, "A3"));

		
		// spa incomplete
		mappingList.put("cc", new Edge(true, "A2"));
		mappingList.put("f", new Edge(true, "P"));
		mappingList.put("AMOD", new Edge(false, "A1"));
		mappingList.put("sp", new Edge(false, "A2"));
		mappingList.put("NMOD*JJ", new Edge(false, "A1"));
		mappingList.put("COORD*CC", new Edge(true, "A1"));
		mappingList.put("COORD", new Edge(true, "A"));
		mappingList.put("NAME", new Edge(true, "NAME"));
		mappingList.put("TITLE", new Edge(true, "TITLE"));
		mappingList.put("LOC", new Edge(false, "AM-LOC"));
		mappingList.put("APPO", new Edge(false, "A1"));
		mappingList.put("DEP", new Edge(false, "A1"));
		mappingList.put("POSTHON", new Edge(false, "A1"));
		mappingList.put("SUFFIX", new Edge(false, "A1"));
		mappingList.put("spec*d", new Edge(false, "A1"));
		mappingList.put("NMOD*NN", new Edge(false, "A1"));
		mappingList.put("spec", new Edge(false, "A1"));
		mappingList.put("sn", new Edge(false, "A1"));
		mappingList.put("TMP", new Edge(true, "AM-TMP"));
		mappingList.put("PRN", new Edge(true, "A"));
		mappingList.put("HMOD", new Edge(false, "A"));
		mappingList.put("HYPH", new Edge(true, "A"));
		mappingList.put("suj", new Edge(true, "A0"));
		mappingList.put("IM", new Edge(true, "A2"));
		mappingList.put("OPRD", new Edge(true, "AX"));
		mappingList.put("PRD", new Edge(true, "A1"));
		mappingList.put("ADV", new Edge(true, "A"));
		mappingList.put("VC", new Edge(true, "A1"));
		mappingList.put("SUB", new Edge(true, "A1"));
		mappingList.put("PRT", new Edge(true, "A1"));
		mappingList.put("PRP", new Edge(false, "A1"));
		mappingList.put("LOC-PRD", new Edge(true, "AM-LOC"));
		mappingList.put("PRD-PRP", new Edge(false, "A1"));
		mappingList.put("EXTR*TO", new Edge(false, "A1"));
		mappingList.put("EXTR-GAP", new Edge(false, "A2"));
		mappingList.put("EXTR", new Edge(false, "A1"));
		mappingList.put("GAP-PRD", new Edge(false, "A2"));
		mappingList.put("DIR", new Edge(false, "A1"));
		mappingList.put("OBJ", new Edge(true, "A3"));

		// chn incomplete
		mappingList.put("CJTN", new Edge(false, "A1"));
		mappingList.put("CJT", new Edge(true, "A2"));
		mappingList.put("AUX", new Edge(true, "A1"));
		mappingList.put("COMP", new Edge(false, "A2"));
		mappingList.put("DMOD", new Edge(false, "A1"));
		mappingList.put("COMP", new Edge(false, "A1"));
		mappingList.put("UNK", new Edge(true, "P"));
		mappingList.put("RELC", new Edge(false, "A1"));
		mappingList.put("cCJTN", new Edge(true, "P"));
		mappingList.put("APP", new Edge(false, "A1"));

		
		mappingList.put("NMOD*JJ", new Edge(false, "A1"));
		mappingList.put("COORD*CC", new Edge(true, "A1"));
		mappingList.put("COORD", new Edge(true, "A"));
		mappingList.put("NAME", new Edge(true, "NAME"));
		mappingList.put("TITLE", new Edge(true, "TITLE"));
		mappingList.put("LOC", new Edge(false, "AM-LOC"));
		mappingList.put("APPO", new Edge(false, "A1"));
		mappingList.put("DEP", new Edge(false, "A1"));
		mappingList.put("POSTHON", new Edge(false, "A1"));
		mappingList.put("SUFFIX", new Edge(false, "A1"));
		mappingList.put("NMOD*NN", new Edge(false, "A1"));
		mappingList.put("spec", new Edge(false, "A1"));
		mappingList.put("sn", new Edge(false, "A1"));
		mappingList.put("TMP", new Edge(true, "AM-TMP"));
		mappingList.put("PRN", new Edge(true, "A"));
		mappingList.put("HMOD", new Edge(false, "A"));
		mappingList.put("HYPH", new Edge(true, "A"));
		mappingList.put("suj", new Edge(true, "A0"));
		mappingList.put("IM", new Edge(true, "A2"));
		mappingList.put("OPRD", new Edge(true, "AX"));
		mappingList.put("PRD", new Edge(true, "A1"));
		mappingList.put("ADV", new Edge(true, "A"));
		mappingList.put("SUB", new Edge(true, "A1"));
		mappingList.put("PRT", new Edge(true, "A1"));
		mappingList.put("PRP", new Edge(false, "A1"));
		mappingList.put("LOC-PRD", new Edge(true, "AM-LOC"));
		mappingList.put("PRD-PRP", new Edge(false, "A1"));
		mappingList.put("EXTR*TO", new Edge(false, "A1"));
		mappingList.put("EXTR-GAP", new Edge(false, "A2"));
		mappingList.put("EXTR", new Edge(false, "A1"));
		mappingList.put("GAP-PRD", new Edge(false, "A2"));
		mappingList.put("DIR", new Edge(false, "A1"));
		mappingList.put("OBJ", new Edge(true, "A3"));

	
	}
	
	/**
	 * @param i
	 * @param n
	 * @param p
	 * @param l
	 */
	public static Edge getSemEdge(SentenceData09 i, int n, int p, String l) {
	
		StringBuffer key = new StringBuffer();
		key.append(l);
	
	//	Edge e = null;
		Edge e = mappingList.get(key.toString());
		key.append('*').append(i.gpos[n]);
		
		// take the more specific
		Edge e1 = mappingList.get(key.toString());
		if (e1!=null) e=e1;
		
		
		if (e==null) {
			e = new Edge (true,"A");
			DB.println("found no mapping for "+i.gpos[p]+" "+l+" -> "+i.gpos[n]+" forms "+i.forms[p]+" "+l+" -> "+i.forms[n] );
			notConnectedCount++;
		} 
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

	
	/**
	 * @param g 
	 * @param n
	 * @param root
	 * @return
	 */
	static boolean connected(Graph g, int n, int root) {
	
		return Extractor.dist(g, root, n)>=0;
	}

	/**
	 * @param i 
	 * @param root
	 * @return
	 */
	public static Collection<? extends Integer> childs(SentenceData09 i, int n) {
		
		ArrayList<Integer> childs = new ArrayList<Integer>();
		for(int k=0;k<i.length();k++) {
			if (i.heads[k]==n) childs.add(k);
		}
		
		return childs;
	}

	
	
}
