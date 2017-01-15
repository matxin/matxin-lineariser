/**
 * 
 */
package syn2lin4;

/**
 * @author Dr. Bernd Bohnet, 31.01.2010
 * 
 * 
 */
public class DataNN {


	/**
	 * @param maxLenInstances
	 * @param length
	 */
	public DataNN(int maxLenInstances) {
		len = maxLenInstances;
		abh = new float [len][len];
		trigrams = new float [len][len][len];
	}

	int len;
	
	// n x before node y 
	final public float[][] abh;

	final public float[][][] trigrams;

	
}
