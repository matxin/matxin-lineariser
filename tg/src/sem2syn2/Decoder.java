package sem2syn2;

import is2.data.FV;
import is2.data.IFV;
import is2.data.Instances;
import is2.data.MFO;
import is2.data.Parse;
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
	
		
		for(int n1 : nodes) {

			for(int n2 : nodes) {

				if (n1==n2) continue;

				for(int l =0;l < d.edge[0][0].length;l++) {


					if(best<d.edge[n1][n2][l]) {
						best = d.edge[n1][n2][l];
						s=n1; t=n2; edge=l;
					}

				}		
			}	
		}

		if (s<0 || t<0) 
			DB.println("error s "+s+" or t "+t+" nodes "+nodes);
		

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

					for(int l =0; l < d.edge[0][0].length;l++) {

						if(best<d.edge[n1][n2][l]) {
							best = d.edge[n1][n2][l];
							s=n1; t=n2; edge=l;
							found =true;
						}
					}		
				}	
			}	

			boolean foundNewRoot =false;

			
			int possibleRoot =-1;
			
			for(int n2 : nodes) {

				for(int l =0;l < d.edge[0][0].length;l++) {

					if(best<d.edge[n2][root][l]) {
						best = d.edge[n2][root][l];
						s=n2; t=root; edge=l;
						possibleRoot = n2;
					//	root = n2;
						found =true;
						foundNewRoot=true;
					}
				}					
			}	

			graphEdge = g.createNode(IGraph.EDGE);
			g.setContent(graphEdge, edge);
			g.createEdge(s, graphEdge); g.createEdge(graphEdge, t);
			nodes.remove(s); 
			nodes.remove(t);


			if (!found) break;
			//toContinue.add(s);

			if (foundNewRoot) {
				toContinue.add(s);
				root = possibleRoot;
			}
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


			//		DB.println("found root "+n);

					rootcnt++;
				}
			}
			//	System.exit(0);

		}
		g.buildIn();	
		return g;
	}

	public static Parse label(Parse prs, Graph g, Instances is, int n, int[][] dist, Extractor extractor, ParametersFloat params) {
	
		
		long v[] = new long[50];
		IFV f = params.getFV();
		for(int d=0; d<prs.heads.length;d++) {
			
			if (prs.heads[d]==-1) continue;
			
			extractor.extractEdgeFeatures(g,  prs,d, prs.heads[d],dist, 0, v);
			
			float best =0;
			for(int l=0;l<Pipe.types.length;l++) {
				
				int lab =l<<extractor.s_type;
				
				f.clear();
				for(int k=0;k<v.length;k++) 
					if (v[k]>0) f.add(extractor.li.l2i(v[k]|lab));
			
				
				if (best< f.getScore() ) {
					best = (float)f.getScore();
					prs.labels[d] = (short)l;
				}
				
			}
		}
		return prs;
	}


	public static FV features(Parse prs, Graph g, Instances is, int n,int[][] dist,  Extractor extractor, ParametersFloat params) {
	
		
		long v[] = new long[50];
		FV f = new FV();
		
		ArrayList<Integer> deepFirst = deepFirst(prs,0, new HashSet<Integer>());
		
		for(int d : deepFirst) {
			
			if (prs.heads[d]==-1) continue;
			
			extractor.extractEdgeFeatures(g,prs, d, prs.heads[d], dist,  0, v);
					
			int lab =prs.labels[d]<<extractor.s_type;
				
			for(int k=0;k<v.length;k++) 
				if (v[k]>0) f.add(extractor.li.l2i(v[k]|lab));
		
		}
		return f;
	}


	private static ArrayList<Integer> deepFirst(Parse prs, int i, HashSet<Integer> done) {
	
		ArrayList<Integer> next = new ArrayList<Integer>();
		next.add(i);
		ArrayList<Integer> order = new ArrayList<Integer>();
		while(! next.isEmpty()) {
			
			int n = next.remove(0);
			
			
			
			if (done.contains(n)) {
				order.add(n);
				continue;
			}
			done.add(n);
			
			
			ArrayList<Integer> clds = Extractor.getChildren(prs, n);
			
			
			
			if (clds.size()==0 ) {
				order.add(n);
			} else {
					
				// do it robust add first
				for(int c : next) 
					if (!clds.contains(c) ) clds.add(c);

				clds.add(n);
				next = clds;

			}
			
			
		}
		
		return order;
	}

}
