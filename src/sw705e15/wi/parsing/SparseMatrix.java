package sw705e15.wi.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class SparseMatrix
{
	private final List<HashMap<Integer, Double>> rowColumnsRepresentation;
	private final List<HashMap<Integer, Double>> columnRowsRepresentation;

	public SparseMatrix(final int newRows, final int newColumns)
	{
		rowColumnsRepresentation = new ArrayList<>(newRows);

		for (int columnCounter = 0; columnCounter < newColumns; columnCounter++)
		{
			rowColumnsRepresentation.add(new HashMap<>());
		}

		columnRowsRepresentation = new ArrayList<>(newColumns);

		for (int rowCounter = 0; rowCounter < newRows; rowCounter++)
		{
			columnRowsRepresentation.add(new HashMap<>());
		}
	}

	public int rowSize()
	{
		return rowColumnsRepresentation.size();
	}
	
	public int columnSize()
	{
		return columnRowsRepresentation.size();
	}
	
	public void set(final int rowIndex, final int columnIndex, Double value)
	{
		rowColumnsRepresentation.get(rowIndex).put(columnIndex, value);
		columnRowsRepresentation.get(columnIndex).put(rowIndex, value);
	}
	
	public Double get(final int rowIndex, final int columnIndex)
	{
		return rowColumnsRepresentation.get(rowIndex).get(columnIndex);
	}
	
	public SparseMatrix sub(final SparseMatrix subtrahend)
	{
		if(subtrahend == null)
		{
			throw new IllegalArgumentException("Argument was null");
		}
		else if(this.rowSize() != subtrahend.rowSize() || this.columnSize() != subtrahend.columnSize())
		{
			throw new IllegalArgumentException("Matrix sizes did not match");
		}
		
		final SparseMatrix resultMatrix = new SparseMatrix(rowSize(), columnSize());
		
		for(int rowCounter = 0; rowCounter < rowColumnsRepresentation.size(); rowCounter++)
		{
			final HashMap<Integer, Double> row = rowColumnsRepresentation.get(rowCounter);
			
			for(Entry<Integer, Double> entry : row.entrySet())
			{
				resultMatrix.set(rowCounter, entry.getKey(), entry.getValue() - subtrahend.get(rowCounter, entry.getKey()) );
			}
		}
		
		return resultMatrix;
	}
	
	public SparseMatrix add(final SparseMatrix addend)
	{
		if(addend == null)
		{
			throw new IllegalArgumentException("Argument was null");
		}
		else if(this.rowSize() != addend.rowSize() || this.columnSize() != addend.columnSize())
		{
			throw new IllegalArgumentException("Matrix sizes did not match");
		}
		
		final SparseMatrix resultMatrix = new SparseMatrix(rowSize(), columnSize());
		
		for(int rowCounter = 0; rowCounter < rowColumnsRepresentation.size(); rowCounter++)
		{
			final HashMap<Integer, Double> row = rowColumnsRepresentation.get(rowCounter);
			
			for(Entry<Integer, Double> entry : row.entrySet())
			{
				resultMatrix.set(rowCounter, entry.getKey(), entry.getValue() + addend.get(rowCounter, entry.getKey()));
			}
		}
		
		return resultMatrix;
	}
}
