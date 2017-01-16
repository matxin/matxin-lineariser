package util;

public class Info {

	/**
	 * print the help message
	 */
	public static void printHelpMessage() {
	
		System.out.println("\nFBN - Full Brevity Referring Expression Generation algorithm using Nearest Neighbor\n");
		System.out.println("For details about the algorithm, please cf. \n(Bohnet, 2007) http://www.vis.uni-stuttgart.de/~bohnetbd/2007-bohnet-is-gre.pdf");
	
		System.out.println("\nversion 2.1 (2008/03/04)\n");
		System.out.println("\n -h          prints this message");
		System.out.println();
		System.out.println(" -x <dir1> [-o <dir2>] \n" +
				           "             cross validation (leave one out) on all sets found in the directory <dir>");
		System.out.println("             <dir1> contains the trails in xml format as provided");
		System.out.println("             <dir2> the directory in which the results are stored");
		System.out.println("             precondition: the trials contain a description part ");
		System.out.println("             example: java -cp fbn/classes gre.FBN -x train/furniture/");
		System.out.println("             prints as result the average dice score of the cross validation");
		System.out.println();
		System.out.println(" -t <dir1> -c <dir2> [-o <dir3>] ");
		System.out.println("             refereing expression generation for all trials found in <dir2> base on the training data of <dir1>");
		System.out.println("             <dir1> training data");
		System.out.println("             <dir2> test data");
		System.out.println("             <dir3> result output directory");
		System.out.println("             example: java -cp fbn/classes gre.FBN -t train/furniture/ -c distribution/furniture/ -o result");
	
	}

}
