package morph2;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import is2.data.SentenceData09;
import is2.io.*;
import is2.util.*;;


public class Evaluator {

	public static class Results {

		public int total;
		public int corr;
		public float las;
		public float ula;
		
	}
	
	public static Results evaluate (String act_file,  String pred_file) throws Exception {

		CONLLReader09 goldReader = new CONLLReader09(act_file, -1);
		CONLLReader09 predictedReader = new CONLLReader09(pred_file, -1);
		
		
		
		BufferedWriter rf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pred_file+".rf"),"UTF8"));;
		BufferedWriter sf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pred_file+".sf"),"UTF8"));;
		BufferedWriter tf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pred_file+".tf"),"UTF8"));;
		
		
		rf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		rf.write("<!DOCTYPE mteval SYSTEM \"ftp://jaguar.ncsl.nist.gov/mt/resources/mteval-xml-v1.5.dtd\">\n");
		rf.write("<mteval>");
		rf.write("<refset setid=\"sample_set\" srclang=\"Arabic\" trglang=\"English\" refid=\"reference01\" SysID=\"bb2\">");
		rf.write("<doc docid=\"sample_document_1\" genre=\"nw\">");
		
		sf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sf.write("<!DOCTYPE mteval SYSTEM \"ftp://jaguar.ncsl.nist.gov/mt/resources/mteval-xml-v1.5.dtd\">\n");
		sf.write("<mteval>\n");
		sf.write("<srcset setid=\"sample_set\" srclang=\"Arabic\">");
		sf.write("<doc docid=\"sample_document_1\" genre=\"nw\">");

		tf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		tf.write("<!DOCTYPE mteval SYSTEM \"ftp://jaguar.ncsl.nist.gov/mt/resources/mteval-xml-v1.5.dtd\">\n");
		tf.write("<mteval>");
		tf.write("<tstset setid=\"sample_set\" srclang=\"Arabic\" trglang=\"English\" SysID=\"NIST\">\n"); 	
		tf.write("<doc docid=\"sample_document_1\" genre=\"nw\">\n");

		
		int total = 0, corr = 0, corrL = 0, dist=0;
		int corrUC = 0;
		int numsent = 0, corrsent = 0, corrsentL = 0;
		SentenceData09 goldInstance = goldReader.getNext();
		SentenceData09 predInstance = predictedReader.getNext();

		int line =0;
		int snt =0;
		while(goldInstance != null) {
			snt++;
			int instanceLength = goldInstance.length();

			if (instanceLength != predInstance.length())
				System.out.println("Lengths do not match on sentence "+numsent);

			String[] goldForms = goldInstance.forms;
			String[] predForms = predInstance.forms;

			boolean whole = true;
			boolean wholeL = true;

			
			StringBuffer gold = new StringBuffer();
			StringBuffer pred = new StringBuffer();
			
			for(int k=1;k<goldForms.length;k++) {
				gold.append((char)k);
				pred.append((char)predInstance.heads[k]);
			}
			
			// NOTE: the first item is the root info added during nextInstance(), so we skip it.
			sf.write("<seg id=\""+snt+"\">");
			tf.write("<seg id=\""+snt+"\">");
			rf.write("<seg id=\""+snt+"\">");
			boolean first=true;
			line++;
			for (int i = 1; i < instanceLength; i++) {

				line ++;
				if(first) first=false;
				else {
					sf.write(" ");
					tf.write(" ");
					rf.write(" ");
				}
				sf.write(goldForms[i]);
				rf.write(goldForms[i]);
				tf.write(predForms[i]);

				if (goldForms[i].toLowerCase().equals(predForms[i].toLowerCase())) {
					corrUC++;
				}
				
				if (goldForms[i].equals(predForms[i])) {
					corr++;
				}
				else { 
					System.out.println(line+" snt "+numsent+"error gold "+goldForms[i]+" "+" pred "+predForms[i]+" "+i+" "+goldInstance.gpos[i]);
					whole = false; wholeL = false; 
				}
			}
			sf.write("</seg>\n");
			rf.write("</seg>\n");
			tf.write("</seg>\n");
			total += instanceLength - 1; // Subtract one to not score fake root token

			dist += Distance.LD(gold.toString(), pred.toString());
			if(whole) corrsent++;
			if(wholeL) corrsentL++;
			numsent++;

			goldInstance = goldReader.getNext();
			predInstance = predictedReader.getNext();
		}

		sf.write("</doc>\n</srcset>\n</mteval>\n");
		tf.write("</doc>\n</tstset>\n</mteval>\n");
		rf.write("</doc>\n</refset>\n</mteval>\n");
		
		rf.flush();rf.close();
		
		tf.flush();tf.close();
		sf.flush();sf.close();
			
		Results r = new Results();
		
		r.total = total;
		r.corr = corr;
		r.las =(float)Math.round(((double)corrL/total)*(double)100000)/1000;
		r.ula =(float)Math.round(((double)corr /total)*(double)100000)/1000;
		System.out.println("Total: " + total+" \tCorrect: " + corr+" "+" acc "+(double)Math.round(((double)corr /total)*(double)100000)/1000+
				" no case count acc " +(double)Math.round(((double)corrUC /total)*(double)100000)/1000);
//		System.out.println("LAS: " + (double)Math.round(((double)corrL/total)*(double)100000)/1000+" \tTotal: " + (double)Math.round(((double)corrsentL/numsent)*100000)/1000+
//				" \tULA: " + (double)Math.round(((double)corr /total)*(double)100000)/1000+" \tTotal: " + (double)Math.round(((double)corrsent /numsent)*100000)/1000);
		System.out.println("String edit dist "+((float)dist/numsent)+" dist "+dist+" avg len "+((float)total/numsent));
		return r;
	}
	
	
	public static float round (double v){
		
		return Math.round(v*10000F)/10000F;
	}
	
}
