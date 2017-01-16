/**
 * 
 */
package dsynt2synt2;

import is2.data.*;

import java.util.ArrayList;

import sem2syn.DSyntConverter.Word;
import util.DB;

/**
 * @author Dr. Bernd Bohnet, 23.03.2011
 * 
 * 
 */
public class Decoder {

	static FV gold = new FV(), pred = new FV();


	/**
	 * @param is
	 * @param n
	 * @param pipe
	 */
	public static int[][] decode(Instances[] is, int n, Pipe pipe, ParametersFloat params, float upd) {


		long[] vs = new long[Pipe._FC];

		F2SF f = (F2SF)params.getFV(); 

		int[][] rules = new int[is[Pipe.DEEP].length(n)][2];

		int len =is[Pipe.DEEP].length(n);
		for(int ruleset=0;ruleset<2;ruleset++) {
			for(int next =0;next<len;next++) {		

				pipe.addCF(is[Pipe.DEEP], n,next, ruleset,vs);

				int best =0;
				float bestScore= Float.MIN_VALUE;

				for(int t=0;t<pipe.types.length;t++) {

					int rule = t <<Pipe.s_type;
					f.clear();
					for(int k=0;k<vs.length;k++) {
						if (vs[k]==Long.MIN_VALUE) break;
						f.add(pipe.li.l2i(vs[k]+rule));
					}
					//	DB.println("score "+f.getScore()+" "+t);
					float score = f.getScoreF();
					if (score>bestScore) {
						best=t;
						bestScore = score;
					}
				}	


				int prule = best<<Pipe.s_type;
				
				if (upd>0) {
				int grule =is[Pipe.DEEP].arg[n][next][ruleset];
				
				if (grule==-1) continue;
				
				if (grule<0) grule=0;
				else grule = grule<<Pipe.s_type;

			//	if (grule==-1) DB.println("should be never  grule== -1 ");

				if (prule!=grule && upd>=0) {


					gold.clear(); pred.clear();
					for(int j=0;j<vs.length;j++) {


						if (vs[j]==Long.MIN_VALUE) break;
						pred.add(pipe.li.l2i(vs[j]+prule));
						gold.add(pipe.li.l2i(vs[j]+grule));					
					}
					// please change!!!!!!!!!!!!!
					params.update(gold, pred,  upd, 1.0f);
				}

				}

				rules[next][ruleset]=best;
			}
		}
		return rules;

	}

	public static ArrayList<Integer> getChildren(short[] heads, int h) {

		ArrayList<Integer> cs = new ArrayList<Integer>();
		for(int i=0;i<heads.length;i++) {

			if (heads[i]==h) cs.add(i);

		}
		return cs;
	}

	/**
	 * Apply a rule on a deep syntactic tree
	 * 
	 * @param rules
	 * @param deep
	 * @param rules 
	 * @return
	 */
	public static SentenceData09 apply(int[][] rules, SentenceData09 deep, String[] rule) {

		ArrayList<Word> snt = new ArrayList<Word>();

		for(int n=0; n<deep.length();n++) {
			Word w = new Word(deep, n,null);
			snt.add(w);
		}

		for(int n=0; n<deep.length();n++) {

			if (deep.heads[n]>=0)snt.get(n).head = snt.get(deep.heads[n]);
			if (deep.pheads[n]>=0)snt.get(n).phead = snt.get(deep.pheads[n]);
			snt.get(n).plable = deep.plabels[n];
			snt.get(n).lable = deep.labels[n];
			snt.get(n).feats= deep.pfeats[n];
			snt.get(n).lemma = deep.lemmas[n];
		}


		ArrayList<Word> newNodes = new ArrayList<Word>();
		for(int r=0;r< deep.length();r++) {

			// apply rule 
			//DB.println(snt.get(r)+"\t\t "+snt.get(r).head+"  rule:"+rule[rules[r][0]]);


			if (rule[rules[r][0]].length()>2 && ! rule[rules[r][0]].equals("<None>")) {
			//	DB.println(snt.get(r)+"\t\t "+snt.get(r).head+"  rule:"+rule[rules[r][0]]);

				Word w = new Word();
				String[] rl = rule[rules[r][0]].split(":");

				if (rl[0].equals("ie")) {
					w.lemma = rl[2];
					//				w.ppos = rl[3];
					w.plable =rl[1];
					w.lable =rl[1];

					// add as new node later
					newNodes.add(w);				

					// the head of the dependent gets the new head
					w.head=snt.get(r).head;
					w.phead=snt.get(r).phead;

					//				snt.get(r)
					snt.get(r).lable=rl[3];
					snt.get(r).plable=rl[3];
					snt.get(r).head = w;
					snt.get(r).phead = w;
				}

			}



			if (rule[rules[r][1]].length()>2 && ! rule[rules[r][1]].equals("<None>")) {
			//	DB.println("in\t"+snt.get(r)+"\t\t "+"  rule "+rule[rules[r][1]]);

				Word w = new Word();
				String[] rl = rule[rules[r][1]].split(":");

				if (rl[0].equals("in")) {
					
					
					w.lemma = rl[2];
					//				w.ppos = rl[3];
					w.lable =rl[1];
					w.plable =rl[1];

					// add as new node later
					newNodes.add(w);				

					// the head is the dependent
					w.head=snt.get(r);
					w.phead=snt.get(r);

//					snt.get(r).lable=rl[3];
				}
				
				
			

			}


		}

		// add the new words at the end
		snt.addAll(newNodes);

		// remove the root
		snt.remove(0);

		// add the new heads

		SentenceData09 s = new SentenceData09();
		String[] forms = new String[snt.size()];
		for(int k=0;k<forms.length;k++) {

			forms[k]=snt.get(k).form;
			if (forms[k]==null ||forms[k].isEmpty())forms[k]="_";
		}
		s.init(forms);
		s.lemmas = new String[s.length()];
		s.pfeats = new String[s.length()];
		s.ofeats = new String[s.length()];
		s.plabels = new String[s.length()];
		s.pheads = new int[s.length()];


		for(int k=0;k<forms.length;k++) {
			s.lemmas[k]=snt.get(k).lemma;

			// root removed therefore + 1
			s.heads[k] = snt.indexOf(snt.get(k).head)+1;
			s.pheads[k] = snt.indexOf(snt.get(k).phead)+1;
			s.ppos[k] = snt.get(k).ppos;
			s.gpos[k] = snt.get(k).gpos;
			s.pfeats[k] = snt.get(k).feats;
			s.ofeats[k] = snt.get(k).feats;
			s.plabels[k] = snt.get(k).plable;
			s.labels[k] = snt.get(k).lable;
		}
		return s;
	}


}
