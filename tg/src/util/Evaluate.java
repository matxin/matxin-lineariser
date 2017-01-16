/**
 * 
 */
package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import rt.model.Environment;
import rt.model.Graph;
import rt.model.IGraph;

/**
 * @author Dr. Bernd Bohnet, 13.05.2011
 * 
 * 
 */
public class Evaluate {

	public static class  Attr {
		
		float fp=0;
		float tp=0;
		public float notfound;
		
		
		public String toString() {
			return "fp "+fp+" tp "+tp+"\t attribute not found "+notfound+
			"\t recall "+((tp+fp)/(notfound+fp+tp))+
			"\t precision "+((tp)/(fp+tp))+"\n";
		}
		//int fn=0;
		
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
		HashMap<String,Attr> attr = new HashMap<String,Attr>();
		
		int count =1;
		while(true) {

			HashMap<Integer,Integer> id2node = new HashMap<Integer,Integer>();
			

			String gname = args[0]+"t"+count+".str";
			String sname = args[1]+"t"+count+".str";
			
			File f = new File(gname);			
			if (!f.exists()) break;
			
			
			Graph gg = loadGraph(gname);
			
			for(int n =0;n<gg.size();n++) {
				if (gg.getType(n)==IGraph.NODE) {
					
					// get id 
					int o[] = gg.getOut(n);
					if(o!=null)
						for(int j =1;j<=o[0];j++) {
							if (gg.getType(o[j])==IGraph.ATTRIBUTE) {
								if (gg.getContent(o[j])==Environment.content("id")) {
									int valueNode = gg.getOut(o[j], 0);
									id2node.put( gg.getContent(valueNode),n);									
								}
								
								
							}
						}
					
					
				}
			}
			
	//	DB.println(""+id2node);	
		Graph sg = loadGraph(sname);
			
		for(int n =0;n<sg.size();n++) {
			if (sg.getType(n)==IGraph.NODE) {
				
				// get id 
				int o[] = sg.getOut(n);
				if(o!=null)
					for(int j =1;j<=o[0];j++) {
						if (sg.getType(o[j])==IGraph.ATTRIBUTE) {
							if (sg.getContent(o[j])==Environment.content("id")) {

								int valueNode = sg.getOut(o[j], 0);
								// get corresponding node in gold 
	//							DB.println("corr "+id2node.get(sg.getContent(valueNode)));

								Integer gn = id2node.get(sg.getContent(valueNode));
								if (gn==null) {
									DB.println(gname+": no correspondence node in gold "+sg.getContent(valueNode));
									continue;
								}
								int[] gout = gg.getOut(gn);

								if (gout==null) {
									DB.println(gname+": no attributes for node "+gn);
								}

								if (gout!=null)
								for(int a1 =1;a1<=o[0];a1++) {
									if (sg.getType(o[a1])!=IGraph.ATTRIBUTE) continue;
									if (sg.getContent(o[a1])==Environment.content("id")) continue;
									boolean found =false;
									
									for(int a2 =1;a2<=gout[0];a2++) {
										if (gg.getType(gout[a2])!=IGraph.ATTRIBUTE) continue;

										// same attribute name
										if (gg.getValue(gout[a2])==sg.getValue(o[a1])) {
											found =true;
											Attr ax = attr.get(gg.getValue(gout[a2]));
											if (ax==null) {
												ax = new Attr ();
												attr.put(gg.getValue(gout[a2]), ax);
											}
											// same attribute value 
											
											int valuePred = sg.getContent(sg.getOut(o[a1], 0));
											int valueGold = gg.getContent(gg.getOut(gout[a2], 0));
											if (valuePred ==valueGold ) ax.tp++;
											else ax.fp++;
											
											break;
											
										}
										
									
									}									
									if (!found)  {
										Attr ax = attr.get(sg.getValue(o[a1]));
										if (ax==null) {
											ax = new Attr ();
											attr.put(sg.getValue(o[a1]), ax);
										}
										ax.notfound++;
	//									DB.println("attribute not found ! ");
									}
								}
								
								
								
							
							}
							
							
						}
					}
				
				
			}
		}
			
			
			count ++;
		}

		DB.println(""+attr);

	
	}

	
	public static Graph loadGraph(String name) throws IOException {
		BufferedReader bos = new BufferedReader(new FileReader(name));
		StringBuilder s = new StringBuilder();
		
		while(true) {
			String l = bos.readLine();
			if (l==null) break;
			s.append(l);
			
		}
		//ArrayList<Graph> gs = rt.parser.graph.Parser.parse(gname); 
		ArrayList<Graph> ps = rt.parser.graph.Parser.parse(new StringReader(s.toString()));
	
		return ps.get(0);
	}
	
}
