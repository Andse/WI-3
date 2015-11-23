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

	public void set(final int rowIndex, final int columnIndex, T value)
	{
		rowColumnsRepresentation.get(rowIndex).put(columnIndex, value);
		columnRowsRepresentation.get(columnIndex).put(rowIndex, value);
	}
	
	public T get(final int rowIndex, final int columnIndex)
	{
		return rowColumnsRepresentation.get(rowIndex).get(columnIndex);
	}
	
	public SparseMatrix sub(final SparseMatrix subtrahend)
	{
		return null;
	}
	
	public SparseMatrix add(final SparseMatrix subtrahend)
	{
		return null;
	}
}
