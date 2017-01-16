/**
 * 
 */
package syn2lin7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import syn2lin7.Decoder.Path;
import util.DB;

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
	
	
	public Linear(Instances is, int n, double upd) {
		
		wordChilds = new int[is.length(n)][];
		wordChildsResult = new int[is.length(n)][][];
		results = new Path[is.length(n)][];
		
		for(int k=0;k< is.length(n);k++) {
			ArrayList<Integer> children = Decoder.getChilds(is, n, k);
			
			if (children !=null) { 
			
				//Collections.sh
				
				Collections.shuffle(children, new Random(21436587)); // + n to be upd+(int)upd  21436580+(int)upd
				
				//Collections.reverse(children);
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
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		for(int r =0; r<results.length;r++) {
			System.out.print(r+":");
			for(int c =0; c<results[r].length;c++) {
				for(int e=0;e<results[r][c].path.length;e++) {
					System.out.print(results[r][c].path[e]+" ");
//				DB.println(" "+results[r][c].toString());
	//			s.append(r).append(":").append(c).append(":").append(results[r][c].toString());
				}
				System.out.println(" "+results[r][c].toString());
			}
		
		}
		
		
		return s.toString();
	}
	
}
