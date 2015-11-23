package sw705e15.wi.learning;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jblas.*;

public class Processing {

	// the total amount of ratings
	private static HashMap<Integer, Integer> observedUserRatings = new HashMap<>();
	private static HashMap<Integer, Integer> observedMovieRatings = new HashMap<>();

	// summation of the ratings
	private static HashMap<Integer, Integer> summarizedUserRatings = new HashMap<>();
	private static HashMap<Integer, Integer> summarizedMovieRatings = new HashMap<>();

	private static double observedRatingsOverall = 0;
	private static double summarizedRatingsOverall = 0;

	private static double totalNormalization = 0;
	
	private enum ProcessingTypes{PRE, POST};

	public Processing(final DoubleMatrix input) {
		DoubleMatrix data = new DoubleMatrix(new double[][]{{4, 2, 5}, {3, 1, 4}, {1, 4, 2}});
		DoubleMatrix newData = preProcessing(data, ProcessingTypes.PRE);
		DoubleMatrix newData2 = preProcessing(newData, ProcessingTypes.POST);
	}

	static public DoubleMatrix preProcessing(final DoubleMatrix initialMatrix, final ProcessingTypes procType) {
		
		switch (procType) {
		case PRE:
			
			// Iterate through all rows and count their amount of ratings; Movies
			for (int elementCount = 0; elementCount < initialMatrix.getRows(); elementCount++) {
				for (int columnCount = 0; columnCount < initialMatrix.getColumns(); columnCount++) {
					
					List<Double> currentRow = initialMatrix.getRow(elementCount).elementsAsList();
					int amountOfRatingsForMovie = 0;
					int summarizedRatingsForMovie = 0;
					for (Double rowElement : currentRow) {
						if (rowElement != 0) {
							observedRatingsOverall++;
							summarizedRatingsOverall += rowElement;
							amountOfRatingsForMovie++;
							summarizedRatingsForMovie += rowElement;
						}
					}
					
					observedMovieRatings.put(elementCount, amountOfRatingsForMovie);
					summarizedMovieRatings.put(elementCount, summarizedRatingsForMovie);
				}
			}

			// Iterate through all columns and count their amount of ratings; Users
			for (int elementCount = 0; elementCount < initialMatrix.getColumns(); elementCount++) {
				List<Double> currentColumn = initialMatrix.getColumn(elementCount).elementsAsList();
				int amountOfRatingsForUser = 0;
				int summarizedRatingsForUser = 0;
				for (Double columnElement : currentColumn) {
					if (columnElement != 0) {
						amountOfRatingsForUser++;
						summarizedRatingsForUser += columnElement;
					}
				}
				observedUserRatings.put(elementCount, amountOfRatingsForUser);
				summarizedUserRatings.put(elementCount, summarizedRatingsForUser);
			}

			totalNormalization = (1.0 / observedRatingsOverall) * summarizedRatingsOverall;

			for (int rowCount = 0; rowCount < initialMatrix.getRows(); rowCount++) {

				double movieNormalization = (1.0 / observedMovieRatings.get(rowCount)) * summarizedMovieRatings.get(rowCount);
				for (int columnCount = 0; columnCount < initialMatrix.getColumns(); columnCount++) {
					double initialValue = initialMatrix.get(rowCount, columnCount);
					double userNormalization = (1.0 / observedUserRatings.get(columnCount))
							* summarizedUserRatings.get(columnCount);
					double newValue = initialValue - movieNormalization - userNormalization + totalNormalization;
					initialMatrix.put(rowCount, columnCount, newValue);
				}
			}

			return initialMatrix;
			
		case POST:
			
			for (int rowCount = 0; rowCount < initialMatrix.getRows(); rowCount++) {

				double movieNormalization = (1.0 / observedMovieRatings.get(rowCount)) * summarizedMovieRatings.get(rowCount);
				for (int columnCount = 0; columnCount < initialMatrix.getColumns(); columnCount++) {
					double initialValue = initialMatrix.get(rowCount, columnCount);
					double userNormalization = (1.0 / observedUserRatings.get(columnCount))
							* summarizedUserRatings.get(columnCount);
					double newValue = initialValue + movieNormalization + userNormalization - totalNormalization;
					initialMatrix.put(rowCount, columnCount, newValue);
				}
			}

			return initialMatrix;
			
		default:
			System.out.println("Did not understand processingtype enum value, returning null.");
			return null;
		}
		
	}
	
	
	public DoubleMatrix funkSVD(final DoubleMatrix data)
	{
		return data;
	}

}