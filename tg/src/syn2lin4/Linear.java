/**
 * 
 */
package syn2lin4;

import java.util.ArrayList;
import java.util.Collections;

import syn2lin4.Decoder.Path;

import is2.data.*; 

/**
 * @author Dr. Bernd Bohnet, 21.03.2010
 * 
 * 
 */
public class Linear {

	int wordChilds[][];
	int wordChildsResult[][][];
	
	Path results[][];
	
	
	public Linear(Instances is, int n) {
		
		wordChilds = new int[is.length(n)][];
		wordChildsResult = new int[is.length(n)][][];
		results = new Path[is.length(n)][];
		
		for(int k=0;k< is.length(n);k++) {
			ArrayList<Integer> children = Decoder.getChilds(is, n, k);
			
			if (children !=null) { 
			
				Collections.reverse(children);
				wordChilds[k]=new int[children.size()+1];
				wordChildsResult[k] = new int[children.size()+1][];
				

				// add the children 
				for (int j=0;j<children.size();j++){
					wordChilds[k][j]=children.get(j);
				}
				// add the head
				wordChilds[k][children.size()]=k;
			} else {
				wordChilds[k]=new int[1];
				// add the head
				wordChilds[k][0]=k;

			}
			
			
		}
	}
	
}
