/**
 * 
 */
package sem2syn2;

/**
 * @author Dr. Bernd Bohnet, 31.01.2010
 * 
 * 
 */
public class Data {

	/**
	 * @param maxLenInstances
	 * @param length
	 */
	public Data(int maxLenInstances, int types) {
		len = maxLenInstances;
		this.types =types;
		edge = new float [len][len][types];
	}

	int len, types;
	
	final public float[][][] edge;
}
