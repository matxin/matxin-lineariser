package syn2lin7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import data.DataNN;

import is2.data.*;
import rt.algorithm.GraphConverter;
import rt.model.Environment;
import rt.model.Graph;
import rt.model.IGraph;
import rt.util.DB;

final public class Decoder   {


	public static long timeDecotder=0;
	public static long timeRearrange=0;



	/**
	 * Computes all the children of a head
	 * 
	 * @param is the instances in 09 format
	 * @param i the number of the instance in the instances storage 
	 * @param x the head
	 * @return the children of the head
	 */
	public  static ArrayList<Integer> getChilds(Instances is, int i, int x) {

		ArrayList<Integer> childs=null;
		for(int n=0;n<is.length(i);n++){
			if (is.heads[i][n]==x) {
				if (childs==null) childs = new ArrayList<Integer>();
				childs.add(n);
			}
		}
		return childs;
	}

	/**
	 * Ordered (linearized) set of nodes
	 * 
	 * @author Dr. Bernd Bohnet
	 */
	public static class Path implements Comparable<Path> {

		int[] path;
		public float prob=0;
		int headPosition=10000;

		public Path() {}


		/**
		 * Create a initial ordered set with a distinct length 
		 * @param a
		 * @param length
		 */
		public Path(int a, int length) {
			path= new int[length];
			path[0]=a;  
		}


		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Path p) {
			if (prob<p.prob) return 1;
			else if (prob==p.prob) return 0;
			return -1;
		}

		/**
		 * @param i
		 * @return
		 */
		public boolean contains(int i) {
			
			
			
			for(int w : path)
				if (w==i) return true;
			return false;
		}

		public void check(int length) {
			for(int w : path)
				if (w>=length) {
					new Exception().printStackTrace();
					System.out.println("ln "+length+" w "+w);
					System.exit(0);
				}
			
		}

		public String toString() {
			return ""+prob+" ";
		}


		/**
		 * Add a element to the ordered list 
		 * 
		 * @param i the node to be added
		 * @param head the head of the node
		 * @param data the data structure that contains the score of different alternative
		 * @param extractor the feature extractor
		 * @param is the instances (sentences) 
		 * @param n the currently processed sentence
		 * @param f the features 
		 * @return the extended path
		 */
		public Path add(int i, int head, DataNN data, Extractor extractor, Instances is, int n, F2SF f) {

			Path p = new Path();

			if(i==head) {
				headPosition=path.length;
				//	System.out.println("postion head "+headPosition);
			}

			// clone the path
			p.path = new int[path.length+1];
			for(int k=0;k<path.length;k++) p.path[k]=path[k];
			p.path[path.length]=i;
			p.prob=prob;

			// add transitive the scores of probability to order two nodes
			for(int k=0;k<path.length;k++){
				p.prob+=data.abh[p.path[k]][i];
			}

			// add the trigram scores
			if (path.length>1) {
				f.clear();
				extractor.extractTrigrams(is, n, p.path[path.length-2], p.path[path.length-1], p.path[path.length], path.length-2, f);
				p.prob+=f.getScoreF();

			}

			return p;
		}



	}

	/**
	 * linearize a set of nodes
	 * @param nodes the nodes to be linearized
	 * @param head the head
	 * @param data the data
	 * @param extractor the extractor
	 * @param is the sentences
	 * @param n the currently processed sentence
	 * @param f the features
	 * @return a sorted set of nodes
	 */
	public static int[][] sortNodes(int[] nodes, int head, DataNN data, Extractor extractor, Instances is, int n, F2SF f) {

		if (nodes.length==1) {
			int result[][] = new int[1][];
			result[0] =nodes;
			return result;
		}

		List<Path> beam = new ArrayList<Path>(); 

		// build seed

		for(int a=0;a< nodes.length;a++) 	beam.add(new Path(nodes[a],1));	

		int position=0;


		// extend the list of sorted nodes and build sets of sorted nodes
		while(position<nodes.length-1) {

			List<Path> beamN = new ArrayList<Path>(); 

			for(Path p : beam) {

				for(int b=0;b<nodes.length;b++) {

					if(p.contains(nodes[b])) continue;

					beamN.add(p.add(nodes[b],head,data,extractor, is, n,f));
				}

			}
			beam = beamN;

			Collections.sort(beam);		

			// do not consider more than 1000 lists
			if (beam.size()> 2000) beam = beam.subList(0, 2000);

			position++;
		}


		// add global information to the score of the lists
		for(Path p : beam) {
			f.clear();
			
			extractor.extractGlobal(is,n,p.path,f);
			p.prob+=f.getScore();	
		}

		// sort due to the score the lists
		Collections.sort(beam);		

				
		int result [][] = new int[beam.size()][];
		
		for(int k=0;k<beam.size();k++) {
			result[k] = beam.get(k).path;
		}

		// return the highest scoring list
		return result; //path.path;	
	}

	
	/**
	 * linearize a set of nodes
	 * @param nodes the nodes to be linearized
	 * @param head the head
	 * @param data the data
	 * @param extractor the extractor
	 * @param is the sentences
	 * @param n the currently processed sentence
	 * @param f the features
	 * @return a sorted set of nodes
	 */
	public static Path[] sortNodes2(int[] nodes, int head, DataNN data, Extractor extractor, Instances is, int n, F2SF f) {

		if (nodes.length==1) {
			Path result[] = new Path[1];
			
			result[0] =new Path();//nodes;
			result[0].path=nodes;
			return result;
		}

		List<Path> beam = new ArrayList<Path>(); 

		// build seed

		for(int a=0;a< nodes.length;a++) 	beam.add(new Path(nodes[a],1));	

		int position=0;


		// extend the list of sorted nodes and build sets of sorted nodes
		while(position<nodes.length-1) {

			List<Path> beamN = new ArrayList<Path>(); 

			for(Path p : beam) {

				for(int b=0;b<nodes.length;b++) {

					p.check(data.len);
					
					if(p.contains(nodes[b])) continue;

					beamN.add(p.add(nodes[b],head,data,extractor, is, n,f));
				}
			}
			beam = beamN;

			Collections.sort(beam);		

			// do not consider more than 1000 lists
			if (beam.size()> 2000) beam = beam.subList(0, 2000);

			position++;
		}


		// add global information to the score of the lists
		for(Path p : beam) {
			f.clear();
			
			extractor.extractGlobal(is,n,p.path,f);
			p.prob+=f.getScore();	
		}

		// sort due to the score the lists
		Collections.sort(beam);		

				
		Path result [] = new Path[beam.size()];
		
		for(int k=0;k<beam.size();k++) {
			result[k] = beam.get(k);
			
			// check beam
			for(int z=0;z<beam.get(k).path.length;z++) {
				if (beam.get(k).path[z]>=data.len) {
					new Exception().printStackTrace();
					DB.println("beam.get(k).path[z] "+beam.get(k).path[z]);
					System.exit(0);
				}
			}
			
			
		}

		
		
		
		// return the highest scoring list
		return result; //path.path;	
	}

	
	
}
