/**
 * 
 */
package morph2;

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
		abh = new float [len][1];
	}

	int len;
	
	final public float[][] abh;

	
}
