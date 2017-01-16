package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;



import mtf.MTC;
import mtf.converter.model.Attribute;
import mtf.converter.model.Node;
import mtf.converter.model.Relation;
import mtf.converter.model.Structure;
import mtf.red.Preferences;

import mtf.red.Text;

public class Converter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// 25-dsynt-ssynt_new_15-con-sem2011_11_01_16-20.str
		// 35-dmorph-smorph_15-con-sem2011_11_01_16-20.str
		
		if (args.length<2) {
//			System.out.println("Conversion of mate structures into conll09: java -cp tg.jar util.Convert ssynt smorph ");
			System.out.println("Example:java -cp c:\\workspace\\tg\\classes\\;c:\\workspace\\mtt\\dist\\mate-20120604.jar util.Converter "+
					" c:/Users/bohnetbd/Desktop/PESCaDO/Agg_2011_11_01_16-20/30-ssynt-dmorph_new_15-con-sem2011_11_01_16-20.str  "+
					"c:\\Users\bohnetbd\\Desktop\\PESCaDO\\Agg_2011_11_01_16-20\\40-smorph-sentence_15-con-sem2011_11_01_16-20.str");
		}
		
		try {
			BufferedReader  ssynt  = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"),32768);
			BufferedReader  smorph  = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]),"UTF-8"),32768);
		
			BufferedWriter	out =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]),"UTF8"));
			StringBuffer ssyntStr = new StringBuffer();
			
			String line=null;
			
			while((line = ssynt.readLine()) !=null) {
				ssyntStr.append(line);
				ssyntStr.append("\n");
			}
			
			
			StringBuffer dmStr = new StringBuffer();
			
		
			
			while((line = smorph.readLine()) !=null) {
				dmStr.append(line);
				dmStr.append("\n");
			}
			Preferences prefs = new Preferences();
			prefs.load();
			
			Vector strs =  MTC.parseStructures(ssyntStr.toString()); 
			
			Structure str = (Structure)strs.get(0);
			
		
			 strs =  MTC.parseStructures(dmStr.toString()); 
			 
			Structure dmstr = (Structure)strs.get(0);
			
			
			
			HashMap<String, Node> idnodemap  = new HashMap<String, Node>();
			for(int n =0;n<str.getNodeCount(); n++ ){
				if (str.getNode(n).getName().equals("S")) {
					
				//	System.out.println("found node set "+str.getNode(n).getAllSubNodeSet());
					for(Node x :str.getNode(n).getAllSubNodeSet()) {
						String id =x.getAttribute("id").getValue();
						idnodemap.put(id, x);
					}
					
				}
			}
			
		
			HashMap<Node,Integer> node2head = new HashMap<Node,Integer>();
			ArrayList<StringBuffer> snt = new ArrayList<StringBuffer>();
			ArrayList<Node> orderedNodes = new ArrayList<Node>();
			int cnt = 1;
			
			ArrayList<Structure> ordered = dmstr.linear();
			Structure stro = ordered.get(0);
			
			for (int k =0;k<stro.getNodes().size();k++) {
			//	System.out.print(lin.get(k)+" ");
				if (stro.getNodes().get(k) instanceof Node) {
					
					Node n =(Node)(stro.getNodes().get(k));
					System.out.println("node "+n);
					if (n==null) {
						System.out.print("node null "+stro.getNodes().get(k).toString());
						continue;
					}
					Attribute a = n.getAttribute("id");
					if (n.getName().equals("\".\"")) {
						
						
						for(int k1=0;k1<orderedNodes.size();k1++) {
							StringBuffer outLine = snt.get(k1);
							Node sn = orderedNodes.get(k1);
							
							int head =0;
							String relName = "root";
							if (sn.getInRelations()!=null) {
								Relation r = sn.getInRelations().get(0);
								relName = r.getName();
								if (node2head.get(r.getSource())==null) {
									DB.println("error head id not found of "+r.getSource());
								} else head = node2head.get(r.getSource());
								
							}
							
							outLine.append("\t").append(head).append("\t").append(head).append("\t").append(relName).append("\t").append(relName);
							outLine.append("\t").append("_");
							outLine.append("\t").append("_");
							out.write(outLine.toString());
							out.newLine();
						//	System.out.println(outLine.toString());
						}
						out.newLine();
						
						
						snt.clear();
						orderedNodes.clear();
						node2head.clear();
						
						
						
						cnt =1;
					}
					if (a==null) {
						DB.println("xxx attribute id null xxx>"+n+"<xxx");
						
						System.out.println("");
						
						continue;
					}
					String id = n.getAttribute("id").getValue();
					
					Node sn = idnodemap.get(id);
					if (sn == null) {
						DB.println("warning ssynt node null with id "+id);
						continue;
					}

					 
					
				//	if (n.getName().equals("\"\"")) continue;
					
					StringBuffer outLine = new StringBuffer ();
					
					String form = n.getName().length()>2?Text.rmQuotes(n.getName()):n.getName();
					// id form lemma lemma
					outLine.append(cnt).append("\t").append(form).append("\t").append(Text.rmQuotes(sn.getName())).
					append("\t").append(Text.rmQuotes(sn.getName())).append("\t");
					
					Attribute spos = sn.getAttribute("spos");
					
					String pos="none";
					if (spos !=null) pos =spos.getValue();
					
					outLine.append(pos).append("\t").append(pos).append("\t");
					StringBuffer attrs = new StringBuffer();
					for(Attribute x : sn.getAttributes()) {
						if (x.getName().contains("slex")||x.getName().contains("dlex")||x.getName().contains("id")
								||x.getName().contains("agree")||x.getName().contains("class")||x.getName().contains("weight")
								||x.getName().contains("actant")||x.getName().contains("spos")||x.getName().contains("dpos")) continue;
						if (attrs.length()>1) attrs.append("|");
							
						attrs.append(x.getName()).append("=").append(x.getValue());
					
					}
					if (attrs.length()==0)attrs.append("_");
					outLine.append(attrs.toString()).append("\t").append(attrs.toString()).append("");
					
				//	System.out.println(outLine.toString());
					
					node2head.put(sn, cnt);
					snt.add(outLine);
					orderedNodes.add(sn);
					// increase line id 
					cnt++;	

				}
				
			}
			
			
			
		
			 
			
		
			
			
			
		//	System.out.println(""+str.asString());
			
			out.flush();out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
