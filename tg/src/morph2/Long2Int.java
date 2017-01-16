package morph2;

import is2.data.Long2IntInterface;


/**
 * @author Bernd Bohnet, 01.09.2009
 * 
 * Maps the long values to the int values.
 */
final public class Long2Int implements Long2IntInterface {

	
	public Long2Int() {
		size=0x07ffffff;
	}
	
	
	public Long2Int(int s) {
		size=s;
	}
	
	
	/** Integer counter for long2int */
	final private int size; 
	                       
				
	/* (non-Javadoc)
	 * @see is2.sp09k9992.Long2IntIterface#size()
	 */
	public  int size() {return size;}
		
	/* (non-Javadoc)
	 * @see is2.sp09k9992.Long2IntIterface#start()
	 * has no meaning for this implementation
	 */
	final public void start() {}
		

	/* (non-Javadoc)
	 * @see is2.sp09k9992.Long2IntIterface#l2i(long)
	 */
	final public int l2i(long l) {		
		if (l<0) return -1;
		
		long r= l;// 27
		l = (l>>13)&0xffffffffffffe000L;
		r ^= l;   // 40
		l = (l>>11)&0xffffffffffff0000L;
		r ^= l;   // 51
		l = (l>>9)& 0xfffffffffffc0000L; //53
		r ^= l;  // 60
		l = (l>>7)& 0xfffffffffff00000L; //62
		r ^=l;    //67
		int x = ((int)r) % size;
	
		return x >= 0 ? x : -x ; 
	}
}
