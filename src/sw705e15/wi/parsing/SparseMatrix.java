package sw705e15.wi.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class SparseMatrix
{
	private final List<HashMap<Integer, Double>> rowColumnsRepresentation;
	private final List<HashMap<Integer, Double>> columnRowsRepresentation;

	public SparseMatrix(final int newRows, final int newColumns)
	{
		rowColumnsRepresentation = new ArrayList<>(newRows);

		for (int rowCounter = 0; rowCounter < newRows; rowCounter++)
		{
			rowColumnsRepresentation.add(new HashMap<>());
		}

		columnRowsRepresentation = new ArrayList<>(newColumns);

		for (int columnCounter = 0; columnCounter < newColumns; columnCounter++)
		{
			columnRowsRepresentation.add(new HashMap<>());
		}

		return;
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

		return;
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
		if (value == 0.0)
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

		for (int rowCounter = 0; rowCounter < resultMatrix.rowSize(); rowCounter++)
		{
			for (int columnCounter = 0; columnCounter < resultMatrix.columnSize(); columnCounter++)
			{
				for (int innerRowCounter = 0; innerRowCounter < rightMatrix.rowSize(); innerRowCounter++)
				{
					final Double oldValue = resultMatrix.get(rowCounter, columnCounter);
					final double valueToAdd = SparseMatrix.this.get(rowCounter, innerRowCounter) * rightMatrix.get(innerRowCounter, columnCounter);

					resultMatrix.set(rowCounter, columnCounter, oldValue + valueToAdd);
				}
			}

		}

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
		if (obj == null || !(obj instanceof SparseMatrix))
		{
			return false;
		}

		if (super.equals(obj))
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

		// Check from this matrix to the argument matrix
		for (int rowCounter = 0; rowCounter < this.rowColumnsRepresentation.size(); rowCounter++)
		{
			final HashMap<Integer, Double> row = this.rowColumnsRepresentation.get(rowCounter);

			for (Entry<Integer, Double> entry : row.entrySet())
			{
				final int columnIndex = entry.getKey();

				if (entry.getValue().doubleValue() != matrix.get(rowCounter, columnIndex).doubleValue())
				{
					return false;
				}
			}
		}

		// Check from the argument matrix to this matrix
		for (int rowCounter = 0; rowCounter < matrix.rowColumnsRepresentation.size(); rowCounter++)
		{
			final HashMap<Integer, Double> row = matrix.rowColumnsRepresentation.get(rowCounter);

			for (Entry<Integer, Double> entry : row.entrySet())
			{
				final int columnIndex = entry.getKey();

				if (entry.getValue().doubleValue() != this.get(rowCounter, columnIndex).doubleValue())
				{
					return false;
				}
			}
		}

		return true;
	}
}
