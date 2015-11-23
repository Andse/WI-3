package sw705e15.wi.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

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

	public SparseMatrix(final double[][] matrix)
	{
		this(matrix.length, matrix[0].length);

		for (int rowCounter = 0; rowCounter < matrix.length; rowCounter++)
		{
			final double[] row = matrix[rowCounter];

			for (int columnCounter = 0; columnCounter < row.length; columnCounter++)
			{
				final double entry = row[columnCounter];

				this.set(rowCounter, columnCounter, entry);
			}
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
		if(value == 0.0)
		{
			rowColumnsRepresentation.get(rowIndex).remove(columnIndex);
			columnRowsRepresentation.get(columnIndex).remove(rowIndex);
			return;
		}
		
		rowColumnsRepresentation.get(rowIndex).put(columnIndex, value);
		columnRowsRepresentation.get(columnIndex).put(rowIndex, value);
	}

	public Double get(final int rowIndex, final int columnIndex)
	{
		final Double entry = rowColumnsRepresentation.get(rowIndex).get(columnIndex);

		return entry == null ? 0 : entry;
	}

	public SparseMatrix sub(final SparseMatrix subtrahend)
	{
		if (subtrahend == null)
		{
			throw new IllegalArgumentException("Argument was null");
		}
		else if (this.rowSize() != subtrahend.rowSize() || this.columnSize() != subtrahend.columnSize())
		{
			throw new IllegalArgumentException("Matrix sizes did not match");
		}

		final SparseMatrix resultMatrix = new SparseMatrix(rowSize(), columnSize());

		for (int rowCounter = 0; rowCounter < rowColumnsRepresentation.size(); rowCounter++)
		{
			final HashMap<Integer, Double> row = rowColumnsRepresentation.get(rowCounter);

			for (Entry<Integer, Double> entry : row.entrySet())
			{
				resultMatrix.set(rowCounter, entry.getKey(), entry.getValue() - subtrahend.get(rowCounter, entry.getKey()));
			}
		}

		return resultMatrix;
	}

	public SparseMatrix add(final SparseMatrix addend)
	{
		if (addend == null)
		{
			throw new IllegalArgumentException("Argument was null");
		}
		else if (this.rowSize() != addend.rowSize() || this.columnSize() != addend.columnSize())
		{
			throw new IllegalArgumentException("Matrix sizes did not match");
		}

		final SparseMatrix resultMatrix = new SparseMatrix(rowSize(), columnSize());

		for (int rowCounter = 0; rowCounter < rowColumnsRepresentation.size(); rowCounter++)
		{
			final HashMap<Integer, Double> row = rowColumnsRepresentation.get(rowCounter);

			for (Entry<Integer, Double> entry : row.entrySet())
			{
				resultMatrix.set(rowCounter, entry.getKey(), entry.getValue() + addend.get(rowCounter, entry.getKey()));
			}
		}

		return resultMatrix;
	}
	
	public SparseMatrix prod(final SparseMatrix rightMatrix)
	{
		if (rightMatrix == null)
		{
			throw new IllegalArgumentException("Argument was null");
		}
		else if (this.columnSize() != rightMatrix.rowSize())
		{
			throw new IllegalArgumentException("Matrix sizes did not match");
		}

		final SparseMatrix resultMatrix = new SparseMatrix(this.rowSize(), rightMatrix.columnSize());

		this.rowOrderIterator().forEachRemaining(new Consumer<Iterator<Entry<Integer, Double>>>()
		{
			int rowCounter = 0;
			
			@Override
			public void accept(final Iterator<Entry<Integer, Double>> entriesIterator)
			{
				entriesIterator.forEachRemaining(new Consumer<Entry<Integer, Double>>()
				{
					@Override
					public void accept(final Entry<Integer, Double> entry)
					{
						final int columnIndex = entry.getKey();
						
						resultMatrix.set(rowCounter, columnIndex, resultMatrix.get(rowCounter, columnIndex) + entry.getValue() * rightMatrix.get(entry.getKey(), rowCounter));
					}
				});
				
				rowCounter++;
			}
		});
		
		return resultMatrix;
	}

	public Iterator<Iterator<Entry<Integer, Double>>> rowOrderIterator()
	{
		return new Iterator<Iterator<Entry<Integer, Double>>>()
		{
			int index = 0;

			@Override
			public boolean hasNext()
			{
				if (index < rowColumnsRepresentation.size())
				{
					return true;
				}

				return false;
			}

			@Override
			public Iterator<Entry<Integer, Double>> next()
			{
				if (this.hasNext())
				{
					final List<Entry<Integer, Double>> row = new ArrayList<>(rowColumnsRepresentation.get(index++).entrySet());
					
					Collections.sort(row, new Comparator<Entry<Integer, Double>>()
					{
						@Override
						public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2)
						{
							return Integer.compare(o1.getKey(), o2.getKey());
						}
					});
					
					return row.iterator();
				}
				
				return null;
			}
		};
	}

	public Iterator<Iterator<Entry<Integer, Double>>> columnOrderIterator()
	{
		return new Iterator<Iterator<Entry<Integer, Double>>>()
		{
			int index = 0;

			@Override
			public boolean hasNext()
			{
				if (index < columnRowsRepresentation.size())
				{
					return true;
				}

				return false;
			}

			@Override
			public Iterator<Entry<Integer, Double>> next()
			{
				if (this.hasNext())
				{
					final List<Entry<Integer, Double>> column = new ArrayList<>(columnRowsRepresentation.get(index++).entrySet());
					
					Collections.sort(column, new Comparator<Entry<Integer, Double>>()
					{
						@Override
						public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2)
						{
							return Integer.compare(o1.getKey(), o2.getKey());
						}
					});
					
					return column.iterator();
				}
				
				return null;
			}
		};
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		if(obj == null || !(obj instanceof SparseMatrix))
		{
			return false;
		}
		
		if(super.equals(obj))
		{
			return true;
		}
			
		return this.equals((SparseMatrix) obj);
	}
	
	public boolean equals(final SparseMatrix matrix)
	{
		if (matrix == null)
		{
			throw new IllegalArgumentException("Argument was null");
		}
		else if (this.rowSize() != matrix.rowSize() || this.columnSize() != matrix.columnSize())
		{
			throw new IllegalArgumentException("Matrix sizes did not match");
		}

		for (int rowCounter = 0; rowCounter < rowColumnsRepresentation.size(); rowCounter++)
		{
			final HashMap<Integer, Double> row = rowColumnsRepresentation.get(rowCounter);

			for (Entry<Integer, Double> entry : row.entrySet())
			{
				final int columnIndex = entry.getKey();
				
				if(this.get(rowCounter, columnIndex) != matrix.get(rowCounter, columnIndex))
				{
					return false;
				}
			}
		}
		
		return true;
	}
}
