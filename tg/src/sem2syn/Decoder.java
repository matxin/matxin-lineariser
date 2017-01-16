package sem2syn;

import is2.data.FV;
import is2.data.MFO;
import is2.util.DB;

import java.util.ArrayList;
import java.util.HashSet;

import rt.model.Graph;
import rt.model.IGraph;


/**
 * @author Bernd Bohnet, 01.09.2009
 * 
 * This methods do the actual work and they build the dependency trees. 
 */
final public class Decoder   {

	public static long timeDecotder;
	public static long timeRearrange;

	/**
	 * Threshold for rearrange edges non-projective
	 */
	public static float NON_PROJECTIVITY_THRESHOLD = 1.1F;

	/**
	 * Build a dependency tree based on the data
	 * @param  
	 * @param pos part-of-speech tags
	 * @param x the data
	 * @param projective projective or non-projective
	 * @param edges the edges
	 * @return a parse tree
	 * @throws InterruptedException
	 */
	public static Graph decode(Data d, int len, Extractor e, ParametersFloat pf) throws InterruptedException {

		Graph g = new Graph();
		HashSet<Integer> nodes = new HashSet<Integer>(); 
		
		//create for all nodes in the semantic graph a node
		for(int n =0;n< len;n++) {
			g.createNode(IGraph.NODE);
			nodes.add(n);
		}


		float best =Float.NEGATIVE_INFINITY;
		int s=-1,t=-1, edge=-1;
		// get start edge
		//	if (nodes.size()>1) {
		for(int n1 : nodes) {

			for(int n2 : nodes) {

				if (n1==n2) continue;

				for(int l =1;l < d.edge[0][0].length;l++) {


					if(best<d.edge[n1][n2][l]) {
						best = d.edge[n1][n2][l];
						s=n1; t=n2; edge=l;
					}

				}		
			}	
		}

		if (s<0 || t<0) {
			DB.println("error s "+s+" or t "+t+" nodes "+nodes);
		}

		int graphEdge = g.createNode(IGraph.EDGE);
		g.setContent(graphEdge, edge);
		g.createEdge(s, graphEdge); 
		g.createEdge(graphEdge, t);

		//DB.println("edge s: "+s+" t: "+t+" l:"+edge+" graph edge "+graphEdge);


		ArrayList<Integer> toContinue = new ArrayList<Integer>();
		toContinue.add(t);
		toContinue.add(s);
		nodes.remove(t); 
		nodes.remove(s);

		int root =s;

		while(nodes.size()>0) {

			best =Float.NEGATIVE_INFINITY;
			s=-1;t=-1; edge=-1;

			boolean found =false;

			for(int n1 : toContinue) {

				for(int n2 : nodes) {

					for(int l =0;l < d.edge[0][0].length;l++) {

						FV f = new FV();

						float w = d.edge[n1][n2][l]+pf.getScore(f);



						if(best<d.edge[n1][n2][l]+w) {
							best = d.edge[n1][n2][l]+w;
							s=n1; t=n2; edge=l;
							found =true;
						}

					}		
				}	
			}	

			boolean foundNewRoot =false;

			for(int n2 : nodes) {

				for(int l =0;l < d.edge[0][0].length;l++) {

					if(best<d.edge[n2][root][l]) {
						best = d.edge[n2][root][l];
						s=n2; t=root; edge=l;
						root = n2;
						found =true;
						foundNewRoot=true;
					}
				}					
			}	

			graphEdge = g.createNode(IGraph.EDGE);
			g.setContent(graphEdge, edge);
			g.createEdge(s, graphEdge); g.createEdge(graphEdge, t);
			nodes.remove(s); nodes.remove(t);


			if (!found) break;
			//toContinue.add(s);

			if (foundNewRoot) toContinue.add(s);
			else toContinue.add(t);
			//DB.println("edge s: "+s+" t: "+t+" l:"+edge+" graph edge "+graphEdge);

		}
		//}
		g.buildIn();		


		int rootcnt =0;
		for(int n=1;n<len;n++) {
			if (g.getIn(n)==null) {
				//	int graphEdge = g.createNode(IGraph.EDGE);
				//	g.setContent(graphEdge, MFO.getValueS(Pipe.REL, "SROOT"));
				//	g.createEdge(0, graphEdge); g.createEdge(graphEdge, n);
				//			DB.println("found root "+n);

				rootcnt++;
			}
		}

		if (rootcnt>1){

			DB.println("found more root roots!!! ");

			for(int n=1;n<len;n++) {
				if (g.getIn(n)==null) {


					DB.println("found root "+n);

					rootcnt++;
				}
			}
			//	System.exit(0);

		}
		g.buildIn();	
		return g;
	}



}
