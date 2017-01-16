/*
 * Created on 18.08.2004
 *
 */
package util;

/**
 * @author Bernd Bohnet
 * @version 18.08.2004
 *
 * attribute value representation, e.g.  color=red
 */
public class AV {
	
	/**
	 * @param Ai
	 * @param V
	 */
	public AV(String Ai, String V) {
		
		A = Ai;
		this.V = V;
		
	}

	/** A property */
  public String A;
  
  /** A value */
  public String V;
  
  public String toString() {
  	return A+" "+V;
  }

}
