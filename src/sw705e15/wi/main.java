package sw705e15.wi;

import sw705e15.wi.parsing.SparseMatrix;
import sw705e15.wi.learning.*;

public class main {

	public static void main(String[] args) {
		
		SparseMatrix data = new SparseMatrix(new double[][]{{4, 2, 5}, {3, 1, 4}, {1, 4, 2}});
		
		Processing processing = new Processing(data);

	}

}
