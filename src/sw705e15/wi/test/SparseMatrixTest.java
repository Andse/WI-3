package sw705e15.wi.test;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import sw705e15.wi.parsing.SparseMatrix;

public class SparseMatrixTest
{
	@Test
	public void testMatrixProduct()
	{
		final SparseMatrix matrix1 = new SparseMatrix(new double[][]{{1, 2}, {3, 4}}); 
		final SparseMatrix matrix2 = new SparseMatrix(new double[][]{{4, 3}, {2, 1}});
		
		final SparseMatrix expectedMatrix = new SparseMatrix(new double[][]{{8, 5}, {20, 13}});
		
		final SparseMatrix calculatedMatrix = matrix1.prod(matrix2);
		
		Assert.assertTrue(calculatedMatrix.equals(expectedMatrix));		
	}
	
	@Test
	public void testMatrixAddition()
	{
		final SparseMatrix matrix1 = new SparseMatrix(new double[][]{{1, 2}, {3, 4}}); 
		final SparseMatrix matrix2 = new SparseMatrix(new double[][]{{4, 3}, {2, 1}});
		
		final SparseMatrix expectedMatrix = new SparseMatrix(new double[][]{{5, 5}, {5, 5}});
		
		final SparseMatrix calculatedMatrix = matrix1.add(matrix2);
		
		Assert.assertTrue(calculatedMatrix.equals(expectedMatrix));		
	}
	
	@Test
	public void testMatrixSubstraction()
	{
		final SparseMatrix matrix1 = new SparseMatrix(new double[][]{{1, 2}, {3, 4}}); 
		final SparseMatrix matrix2 = new SparseMatrix(new double[][]{{4, 3}, {2, 1}});
		
		final SparseMatrix expectedMatrix = new SparseMatrix(new double[][]{{-3, -1}, {1, 3}});
		
		final SparseMatrix calculatedMatrix = matrix1.sub(matrix2);
		
		Assert.assertTrue(calculatedMatrix.equals(expectedMatrix));		
	}

}
