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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;



import mtf.MTC;
import mtf.converter.model.Attribute;
import mtf.converter.model.Node;
import mtf.converter.model.Relation;
import mtf.converter.model.Structure;
import mtf.red.Preferences;

import mtf.red.Text;

public class ConvertDS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// 25-dsynt-ssynt_new_15-con-sem2011_11_01_16-20.str
		// 35-dmorph-smorph_15-con-sem2011_11_01_16-20.str

		if (args.length<1) {
//			System.out.println("Conversion of mate structures into conll09: java -cp tg.jar util.Convert ssynt smorph ");
			System.out.println();
			System.out.println("Example:java -cp c:\\workspace\\tg\\classes\\;c:\\workspace\\mtt\\dist\\mate-20120604.jar util.Converter "+
					" c:/Users/bohnetbd/Desktop/PESCaDO/Agg_2011_11_01_16-20/30-ssynt-dmorph_new_15-con-sem2011_11_01_16-20.str  "+
					"c:\\out.conll");
			System.exit(0);

		}
		
		try {
			BufferedReader  dsynt  = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"),32768);
//			BufferedReader  smorph  = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]),"UTF-8"),32768);
		
			BufferedWriter	out =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]),"UTF8"));
			StringBuffer ssyntStr = new StringBuffer();
			
			String line=null;
			
			while((line = dsynt.readLine()) !=null) {
				ssyntStr.append(line);
				ssyntStr.append("\n");
			}
			
			
	//		StringBuffer dmStr = new StringBuffer();
			
		
		
			Preferences prefs = new Preferences();
			prefs.load();
			
			Vector strs =  MTC.parseStructures(ssyntStr.toString()); 
			
			Structure str = (Structure)strs.get(0);
			
				 
			
			
			
			HashMap<String, Node> idnodemap  = new HashMap<String, Node>();
			for(int n =0;n<str.getNodeCount(); n++ ){
				if (str.getNode(n).getName().equals("S")) {
					Node s = str.getNode(n);
				//	System.out.println("found node set "+str.getNode(n).getAllSubNodeSet());
					for(Node x :str.getNode(n).getAllSubNodeSet()) {
					
					
					
					}
					if (s.getAttribute("id") == null) {
						//		System.out.println("node has no id"+x);
								continue;
							}
					String id =s.getAttribute("id").getValue();
					System.out.println("node  id"+id+" "+s);
					idnodemap.put(id, s);
				}
			}
			
		
			HashMap<Node,Integer> node2head = new HashMap<Node,Integer>();
			ArrayList<StringBuffer> snt = new ArrayList<StringBuffer>();
			ArrayList<Node> orderedNodes = new ArrayList<Node>();
			int cnt = 1;
			
			ArrayList<Node> sentences = new ArrayList<Node>();
			for(int k =0; k<1000;k++) {
				
				Node n = idnodemap.get(""+k);
				if (n ==null) continue;
				
				sentences.add(n);
				
			}
			
			
			 for (Node n : sentences){
					
				//	Node n =(Node)(str.getNodes().get(k));
					System.out.println("node "+n);
		//			if (n==null) {
		//				System.out.print("node null "+str.getNodes().get(k).toString());
		//				continue;
		//			}
					
					
					
					
					Attribute a = n.getAttribute("id");
					System.out.println("process sentence "+a);
				
					orderedNodes = new ArrayList(n.getAllSubNodeSet());
					
					
						
					System.out.println("nodes of sentence "+orderedNodes);
					for(int k1=0;k1<orderedNodes.size();k1++) {
							StringBuffer outLine = new StringBuffer();
							Node sn = orderedNodes.get(k1);
							
							outLine.append(k1+1).append("\t").append(sn.getName()).append("\t").append(sn.getName()).append("\t").append(sn.getName());
							
							Attribute dpos = sn.getAttribute("dpos");
							outLine.append("\t").append(dpos==null?"null":dpos.getValue());
							outLine.append("\t").append(dpos==null?"null":dpos.getValue()).append("\t");
							
							
							StringBuffer attrs = new StringBuffer();
							for(Attribute x : sn.getAttributes()) {
								if (x.getName().contains("slex")||x.getName().contains("dlex")
										||x.getName().contains("agree")||x.getName().contains("class")||x.getName().contains("weight")
										||x.getName().contains("actant")||x.getName().contains("spos")||x.getName().contains("dpos")) continue;
								if (attrs.length()>1) attrs.append("|");
									
								attrs.append(x.getName()).append("=").append(x.getValue());
							
							}
							if (attrs.length()==0)attrs.append("_");
							outLine.append(attrs.toString()).append("\t").append(attrs.toString()).append("");
							
							int head =0;
							String relName = "root";
							if (sn.getInRelations()!=null) {
								Relation r = sn.getInRelations().get(0);
								relName = r.getName();
								
								if (r.getSource()==null) {
									DB.println("error head id not found of "+r.getSource());
								} else head  = orderedNodes.indexOf(r.getSource())+1;
								
							}
							
							outLine.append("\t").append(head).append("\t").append(head).append("\t").append(relName).append("\t").append(relName);
							outLine.append("\t").append("_");
							outLine.append("\t").append("_");
							out.write(outLine.toString());
							out.newLine();
							System.out.println("line "+ outLine.toString());
					}
					
					out.newLine();
						
						
						snt.clear();
						orderedNodes.clear();
						node2head.clear();
						
						
						
						cnt =1;
					
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
					// id form lemma lemmao
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
		
			
		//	System.out.println(""+str.asString());
			
			out.flush();out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
