package sw705e15.wi.learning;


import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jblas.*;
import org.jblas.util.Random;

import sw705e15.wi.parsing.SparseMatrix;

public class Processing {

	// the total amount of ratings
	private static final HashMap<Integer, Integer> observedUserRatings = new HashMap<>();
	private static final HashMap<Integer, Integer> observedMovieRatings = new HashMap<>();

	// summation of the ratings
	private static final HashMap<Integer, Integer> summarizedUserRatings = new HashMap<>();
	private static final HashMap<Integer, Integer> summarizedMovieRatings = new HashMap<>();

	private static double observedRatingsOverall = 0;
	private static double summarizedRatingsOverall = 0;
	private static double totalNormalization = 0;

	private static final int K = 9;
	private static final double SCALE_CONSTANT = 0.0001;
	
	private static SparseMatrix A;
	private static SparseMatrix B;
	
	
	public Processing(final SparseMatrix input) {
		
		SparseMatrix newData = preProcessing(input);
		
		funkSVD(newData);
		
		
	}

	static public SparseMatrix preProcessing(final SparseMatrix initialMatrix) {
			
			// Iterate through all rows and count their amount of ratings; Movies
			for (int rowCount = 0; rowCount < initialMatrix.rowSize(); rowCount++) {
				
				int amountOfRatingsForMovie = 0;
				int summarizedRatingsForMovie = 0;
				
				for (int columnCount = 0; columnCount < initialMatrix.columnSize(); columnCount++) {
					
					double currentVal = initialMatrix.get(rowCount, columnCount);
					observedRatingsOverall++;
					summarizedRatingsOverall += currentVal;
					amountOfRatingsForMovie++;
					summarizedRatingsForMovie += currentVal;
				}
					
					observedMovieRatings.put(rowCount, amountOfRatingsForMovie);
					summarizedMovieRatings.put(rowCount, summarizedRatingsForMovie);
			}

			
			for (int columnCount = 0; columnCount < initialMatrix.columnSize(); columnCount++) {
				
				int amountOfRatingsForUser = 0;
				int summarizedRatingsForUser = 0;
				
				for (int rowCount = 0; rowCount < initialMatrix.rowSize(); rowCount++) {
					
					double currentVal = initialMatrix.get(rowCount, columnCount);
					amountOfRatingsForUser++;
					summarizedRatingsForUser += currentVal;
				}
					
					observedUserRatings.put(columnCount, amountOfRatingsForUser);
					summarizedUserRatings.put(columnCount, summarizedRatingsForUser);
			}

			totalNormalization = (1.0 / observedRatingsOverall) * summarizedRatingsOverall;

			for (int rowCount = 0; rowCount < initialMatrix.rowSize(); rowCount++) {

				double movieNormalization = (1.0 / observedMovieRatings.get(rowCount)) * summarizedMovieRatings.get(rowCount);
				
				for (int columnCount = 0; columnCount < initialMatrix.columnSize(); columnCount++) {
					double initialValue = initialMatrix.get(rowCount, columnCount);
					double userNormalization = (1.0 / observedUserRatings.get(columnCount)) * summarizedUserRatings.get(columnCount);
					double newValue = initialValue - movieNormalization - userNormalization + totalNormalization;
					initialMatrix.set(rowCount, columnCount, newValue);
				}
			}

			return initialMatrix;
	}
	
	
	public void funkSVD(final SparseMatrix data)
	{
		A = new SparseMatrix(data.rowSize(), K);
		B = new SparseMatrix(K, data.columnSize());
		
		for (int rowCount = 0; rowCount < A.rowSize(); rowCount++) {
			for (int colCount = 0; colCount < A.columnSize(); colCount++) {
				A.set(rowCount, colCount, Math.random());
			}
		}
		
		for (int rowCount = 0; rowCount < B.rowSize(); rowCount++) {
			for (int colCount = 0; colCount < B.columnSize(); colCount++) {
				B.set(rowCount, colCount, Math.random());
			}
		}
		double oldDataError = Double.MAX_VALUE;
		// SKIFT DET HER LOOP NÅR VI VED HVOR LANG TID VI SKAL RENDE
		while(true)
		{
			double dataError = calculateError(data);
			if(dataError > oldDataError)
			{
				System.out.println("VI HAR VUNDET: " + "GAMMEL = " + oldDataError + " NY = " + dataError);
			}
			oldDataError = dataError;
			stochasticGradientDescentStepForA(data);
			stochasticGradientDescentStepForB(data);
		}
	}
	
	private SparseMatrix stochasticGradientDescentStepForA(final SparseMatrix data)
	{
		int randomMovieIndex = Random.nextInt(A.rowSize());
		int randomUserIndex = Random.nextInt(B.columnSize());
		
		double originalValue = data.get(randomMovieIndex, randomUserIndex);
		double[] addend = new double[K]; 
		
		for (int count = 0; count < K; count++) {
			
			double currentAValue = A.get(randomMovieIndex, count);
			double currentBValue = B.get(count, randomUserIndex);
			
			double sum = 0;
			
			for (int i = 0; i < K; i++) {
				double temporaryAValue = A.get(randomMovieIndex, i);
				double temporaryBValue = B.get(i, randomUserIndex);
				sum += temporaryAValue * temporaryBValue;
			}
			addend[count] = currentAValue + SCALE_CONSTANT * (originalValue - sum) * currentBValue;
		}
		
		for (int addendCount = 0; addendCount < addend.length; addendCount++) {
			A.set(randomMovieIndex, addendCount, addend[addendCount]);
		}
		
		return A;
	}
	
	private SparseMatrix stochasticGradientDescentStepForB(final SparseMatrix data)
	{
		int randomMovieIndex = Random.nextInt(A.rowSize());
		int randomUserIndex = Random.nextInt(B.columnSize());
		
		double originalValue = data.get(randomMovieIndex, randomUserIndex);
		double[] addend = new double[K]; 
		
		for (int count = 0; count < K; count++) {
			
			double currentAValue = A.get(randomMovieIndex, count);
			double currentBValue = B.get(count, randomUserIndex);
			
			double sum = 0;
			
			for (int i = 0; i < K; i++) {
				double temporaryAValue = A.get(randomMovieIndex, i);
				double temporaryBValue = B.get(i, randomUserIndex);
				sum += temporaryAValue * temporaryBValue;
			}
			addend[count] = currentBValue + SCALE_CONSTANT * (originalValue - sum) * currentAValue;
		}
		
		for (int addendCount = 0; addendCount < addend.length; addendCount++) {
			B.set(addendCount, randomUserIndex, addend[addendCount]);
		}
		
		return B;
	}
	
	private HashMap<Integer, Double> prediction(final SparseMatrix initialMatrix, final int userID, final List<Integer> movieIDs)
	{
		HashMap<Integer, Double> userMoviePredictions = new HashMap<>();
		for (Integer movieID : movieIDs) {
			
			double predictionValue = 0;
			
			for (int k = 0; k < K; k++) {
				predictionValue += A.get(movieID, k) * B.get(k, userID);
			}
			userMoviePredictions.put(movieID, predictionValue);
		}
		
		double userNormalization = (1.0 / observedUserRatings.get(userID)) * summarizedUserRatings.get(userID);
		for (Entry<Integer, Double> mapEntry : userMoviePredictions.entrySet()) {
			double movieNormalization = (1.0 / observedMovieRatings.get(mapEntry.getKey())) * summarizedMovieRatings.get(mapEntry.getKey());
			userMoviePredictions.put(mapEntry.getKey(), mapEntry.getValue() + userNormalization + movieNormalization - totalNormalization);
		}

		return userMoviePredictions;	
	}
	
	
	public static double calculateError(final SparseMatrix R) {

        double errorSum = 0d;

        for (int movieIndex = 0; movieIndex < A.rowSize(); movieIndex++) {
            for (int userIndex = 0; userIndex < B.columnSize(); userIndex++) {
                double ratingR = R.get(movieIndex, userIndex);

                double tempSum = 0d;
                for (int k = 0; k < K; k++) {
                    double ratingAB = A.get(movieIndex, k) * B.get(k, userIndex);
                    tempSum += ratingAB;
                }

                // Add the squared temp sum to the error sum
                errorSum += Math.pow(ratingR - tempSum, 2);
            }
        }

        return errorSum;
    }

}