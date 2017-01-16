/**
 * 
 */
package syn2lin4;


/**
 * @author Dr. Bernd Bohnet, 14.08.2011
 * 
 * 
 */
final public class Order implements Comparable <Order> {

	final StringBuilder nodes = new StringBuilder();
	float p=0;

	/**
	 * @param oo
	 * @param f 
	 * @return
	 */
	public Order add(Order oo, float f) {

		Order o = new Order();
		o.nodes.append(nodes);
		o.nodes.append(oo.nodes);
		o.p=p+oo.p+f;
		return o;
	}
	
	
	public String toString() {
		
		StringBuilder s = new StringBuilder();
		for(int i=0;i<nodes.length();i++) s.append((int)nodes.charAt(i)).append(" ");
		
		return ""+s+"\t"+p+"\t"+isGold()+"\n";
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Order o) { 
		return p==o.p?0:p<o.p?1:-1;
	}


	/**
	 * @return
	 */
	public boolean isGold() {
		for(int k=0;k<nodes.length();k++) if ((int)nodes.charAt(k) != k+1 ) return false;
		return true;
	}
	
	/**
	 * @return
	 */
	public boolean isPartGold() {
	
		int min=Integer.MAX_VALUE;
		for(int k=0;k<nodes.length();k++) if ((int)nodes.charAt(k) <min )min=(int)nodes.charAt(k);
		
		for(int k=0;k<nodes.length();k++) if ((int)nodes.charAt(k) != (min+k) ) return false;
		return true;
	}
	
}
