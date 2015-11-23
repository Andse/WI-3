package sw705e15.wi.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SparseMatrix<T extends Number>
{
	private final List<HashMap<Integer, T>> rowColumnsRepresentation;
	private final List<HashMap<Integer, T>> columnRowsRepresentation;

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
	
	public void set(final int rowIndex, final int columnIndex, T value)
	{
		rowColumnsRepresentation.get(rowIndex).put(columnIndex, value);
		columnRowsRepresentation.get(columnIndex).put(rowIndex, value);
	}
	
	public T get(final int rowIndex, final int columnIndex)
	{
		return rowColumnsRepresentation.get(rowIndex).get(columnIndex);
	}
	
	public SparseMatrix<T> sub(final SparseMatrix<T> subtrahend)
	{
		if(subtrahend == null)
		{
			throw new IllegalArgumentException("Argument was null");
		}
		else if(this.rowSize() != subtrahend.rowSize() || this.columnSize() != subtrahend.columnSize())
		{
			throw new IllegalArgumentException("Matrix sizes did not match");
		}
		
		final SparseMatrix<T> resultMatrix = new SparseMatrix<>(rowSize(), columnSize());
		
		
		for(int rowCounter = 0; rowCounter < rowColumnsRepresentation.size(); rowCounter++)
		{
			
			//HashMap<Integer, T> column 
			
			//resultMatrix.put
		}
		
		return null;
	}
	
	public SparseMatrix<T> add(final SparseMatrix<T> addend)
	{
		if(addend == null)
		{
			throw new IllegalArgumentException("Argument was null");
		}
		else if(this.rowSize() != addend.rowSize() || this.columnSize() != addend.columnSize())
		{
			throw new IllegalArgumentException("Matrix sizes did not match");
		}
		
		return null;
	}
}
