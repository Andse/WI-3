package sw705e15.wi.learning;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.jblas.*;
import org.jblas.util.Random;

import sw705e15.wi.parsing.MovieUserRatingParser.UserMovieRatingMatrix;
import sw705e15.wi.parsing.SparseMatrix;

public class Processing
{
	// the total amount of ratings
	private static final HashMap<Integer, Integer> observedUserRatings = new HashMap<>();
	private static final HashMap<Integer, Integer> observedMovieRatings = new HashMap<>();

	// summation of the ratings
	private static final HashMap<Integer, Integer> summarizedUserRatings = new HashMap<>();
	private static final HashMap<Integer, Integer> summarizedMovieRatings = new HashMap<>();

	private static double observedRatingsOverall = 0;
	private static double summarizedRatingsOverall = 0;
	private static double totalNormalization = 0;

	private static final int K = 40;
	private static final double SCALE_CONSTANT = 0.01;

	private static DoubleMatrix A;
	private static DoubleMatrix B;

	public final UserMovieRatingMatrix newData;

	public Processing(final UserMovieRatingMatrix input)
	{
		System.out.println("Starting preProcessing");

		newData = preProcessing(input);

		System.out.println("Finished preProcessing");

		funkSVD(newData);
	}

	static public UserMovieRatingMatrix preProcessing(final UserMovieRatingMatrix initialMatrix)
	{
		// Iterate through all rows and count their amount of ratings; Movies
		initialMatrix.rowOrderIterator().forEachRemaining(new Consumer<Iterator<Entry<Integer, Double>>>()
		{
			int rowIndexCounter = 0;
			int amountOfRatingsForMovie = 0;
			int summarizedRatingsForMovie = 0;

			@Override
			public void accept(Iterator<Entry<Integer, Double>> t)
			{
				t.forEachRemaining(new Consumer<Entry<Integer, Double>>()
				{
					@Override
					public void accept(Entry<Integer, Double> t)
					{
						final int columnIndex = t.getKey();

						double currentVal = initialMatrix.get(rowIndexCounter, columnIndex);
						observedRatingsOverall++;
						summarizedRatingsOverall += currentVal;
						amountOfRatingsForMovie++;
						summarizedRatingsForMovie += currentVal;
					}
				});

				observedMovieRatings.put(rowIndexCounter, amountOfRatingsForMovie);
				summarizedMovieRatings.put(rowIndexCounter, summarizedRatingsForMovie);
				rowIndexCounter++;

				amountOfRatingsForMovie = 0;
				summarizedRatingsForMovie = 0;
			}
		});

		initialMatrix.columnOrderIterator().forEachRemaining(new Consumer<Iterator<Entry<Integer, Double>>>()
		{
			int columnIndexCounter = 0;
			int amountOfRatingsForUser = 0;
			int summarizedRatingsForUser = 0;

			@Override
			public void accept(Iterator<Entry<Integer, Double>> t)
			{
				t.forEachRemaining(new Consumer<Entry<Integer, Double>>()
				{

					@Override
					public void accept(Entry<Integer, Double> t)
					{
						final int rowIndex = t.getKey();

						double currentVal = initialMatrix.get(rowIndex, columnIndexCounter);
						amountOfRatingsForUser++;
						summarizedRatingsForUser += currentVal;
					}
				});

				observedUserRatings.put(columnIndexCounter, amountOfRatingsForUser);
				summarizedUserRatings.put(columnIndexCounter, summarizedRatingsForUser);
				columnIndexCounter++;

				amountOfRatingsForUser = 0;
				summarizedRatingsForUser = 0;
			}
		});

		totalNormalization = (1.0 / observedRatingsOverall) * summarizedRatingsOverall;

		initialMatrix.rowOrderIterator().forEachRemaining(new Consumer<Iterator<Entry<Integer, Double>>>()
		{
			int rowIndexCounter = 0;

			@Override
			public void accept(Iterator<Entry<Integer, Double>> t)
			{
				double movieNormalization = (1.0 / observedMovieRatings.get(rowIndexCounter)) * summarizedMovieRatings.get(rowIndexCounter);

				t.forEachRemaining(new Consumer<Entry<Integer, Double>>()
				{
					@Override
					public void accept(Entry<Integer, Double> t)
					{
						final int columnIndex = t.getKey();

						double initialValue = initialMatrix.get(rowIndexCounter, columnIndex);
						double userNormalization = (1.0 / observedUserRatings.get(columnIndex)) * summarizedUserRatings.get(columnIndex);
						double newValue = initialValue - movieNormalization - userNormalization + totalNormalization;
						initialMatrix.set(rowIndexCounter, columnIndex, newValue);
					}
				});

				rowIndexCounter++;
			}
		});

		return initialMatrix;
	}

	private void initializeAAndB(final UserMovieRatingMatrix data)
	{
		A = new DoubleMatrix(data.rowSize(), K);
		B = new DoubleMatrix(K, data.columnSize());

		for (int rowCount = 0; rowCount < A.getRows(); rowCount++)
		{
			for (int colCount = 0; colCount < A.getColumns(); colCount++)
			{
				A.put(rowCount, colCount, Math.random());
			}
		}

		for (int rowCount = 0; rowCount < B.getRows(); rowCount++)
		{
			for (int colCount = 0; colCount < B.getColumns(); colCount++)
			{
				B.put(rowCount, colCount, Math.random());
			}
		}
	}

	double oldDataError;
	double deltaError;

	public void funkSVD(final UserMovieRatingMatrix data)
	{
		initializeAAndB(data);

		oldDataError = Double.MAX_VALUE;
		deltaError = Double.MAX_VALUE;
		// SKIFT DET HER LOOP NÅR VI VED HVOR LANG TID VI SKAL RENDE

		double stepSize = 0.01;

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				System.out.println("DataError: " + oldDataError + " deltaError: " + deltaError);
			}
		}, 5000, 10000);

		while (deltaError > 50)
		{
			final double dataError = calculateError(data);

			/*
			 * if (dataError > oldDataError) { System.out.println(
			 * "VI HAR VUNDET: " + "GAMMEL = " + oldDataError + " NY = " +
			 * dataError); }
			 */

			deltaError = Math.abs(oldDataError - dataError);

			oldDataError = dataError;

			for (int counter = 0; counter < 400; counter++)
			{
				stochasticGradientDescentStepForA(data, stepSize);
				stochasticGradientDescentStepForB(data, stepSize);
			}
		}

		timer.cancel();
	}

	private static final double[] addend = new double[K];

	private DoubleMatrix stochasticGradientDescentStepForA(final UserMovieRatingMatrix data, final double stepSize)
	{
		final int randomMovieIndex = Random.nextInt(A.getRows());
		final int randomUserIndex = Random.nextInt(B.getColumns());

		final double originalValue = data.get(randomMovieIndex, randomUserIndex);

		for (int count = 0; count < K; count++)
		{
			double currentAValue = A.get(randomMovieIndex, count);
			double currentBValue = B.get(count, randomUserIndex);

			double sum = 0;

			for (int i = 0; i < K; i++)
			{
				double temporaryAValue = A.get(randomMovieIndex, i);
				double temporaryBValue = B.get(i, randomUserIndex);
				sum += temporaryAValue * temporaryBValue;
			}

			addend[count] = currentAValue + stepSize * (originalValue - sum) * currentBValue;
		}

		for (int addendCount = 0; addendCount < addend.length; addendCount++)
		{
			A.put(randomMovieIndex, addendCount, addend[addendCount]);
		}

		return A;
	}

	private DoubleMatrix stochasticGradientDescentStepForB(final UserMovieRatingMatrix data, final double stepSize)
	{
		final int randomMovieIndex = Random.nextInt(A.getRows());
		final int randomUserIndex = Random.nextInt(B.getColumns());

		double originalValue = data.get(randomMovieIndex, randomUserIndex);

		for (int count = 0; count < K; count++)
		{
			double currentAValue = A.get(randomMovieIndex, count);
			double currentBValue = B.get(count, randomUserIndex);

			double sum = 0;

			for (int i = 0; i < K; i++)
			{
				double temporaryAValue = A.get(randomMovieIndex, i);
				double temporaryBValue = B.get(i, randomUserIndex);
				sum += temporaryAValue * temporaryBValue;
			}

			addend[count] = currentBValue + stepSize * (originalValue - sum) * currentAValue;
		}

		for (int addendCount = 0; addendCount < addend.length; addendCount++)
		{
			B.put(addendCount, randomUserIndex, addend[addendCount]);
		}

		return B;
	}

	public HashMap<Integer, Double> prediction(final int userID, final List<Integer> movieIDs)
	{
		HashMap<Integer, Double> userMoviePredictions = new HashMap<>();
		final int userIndex = newData.userIDToIndexMapping.get(userID);

		for (Integer movieID : movieIDs)
		{
			double predictionValue = 0;

			for (int k = 0; k < K; k++)
			{
				predictionValue += A.get(movieID, k) * B.get(k, userIndex);
			}
			userMoviePredictions.put(movieID, predictionValue);
		}

		final double userNormalization = (1.0 / observedUserRatings.get(userIndex)) * summarizedUserRatings.get(userIndex);

		for (Entry<Integer, Double> mapEntry : userMoviePredictions.entrySet())
		{
			double movieNormalization = (1.0 / observedMovieRatings.get(mapEntry.getKey())) * summarizedMovieRatings.get(mapEntry.getKey());
			userMoviePredictions.put(mapEntry.getKey(), mapEntry.getValue() + userNormalization + movieNormalization - totalNormalization);
		}

		return userMoviePredictions;
	}

	public double prediction(final int movieID, final int userID)
	{
		// HashMap<Integer, Double> userMoviePredictions = new HashMap<>();
		final int movieIndex = newData.movieIDToIndexMapping.get(movieID);
		final int userIndex = newData.userIDToIndexMapping.get(userID);

		// for (Integer movieID : movieIDs)
		// {
		double predictionValue = 0;

		for (int k = 0; k < K; k++)
		{
			predictionValue += A.get(movieIndex, k) * B.get(k, userIndex);
		}
		// userMoviePredictions.put(movieID, predictionValue);
		// }

		final double userNormalization = (1.0 / observedUserRatings.get(userIndex)) * summarizedUserRatings.get(userIndex);
		final double movieNormalization = (1.0 / observedMovieRatings.get(movieIndex)) * summarizedMovieRatings.get(movieIndex);

		// for (Entry<Integer, Double> mapEntry :
		// userMoviePredictions.entrySet())
		// {

		return predictionValue + userNormalization + movieNormalization - totalNormalization;
	}

	public static double calculateError(final SparseMatrix R)
	{
		final double errorSum[] = new double[1];

		R.rowOrderIterator().forEachRemaining(new Consumer<Iterator<Entry<Integer, Double>>>()
		{
			int rowIndexCounter = 0;

			final Consumer<Entry<Integer, Double>> consumer = new Consumer<Entry<Integer, Double>>()
			{
				@Override
				public void accept(Entry<Integer, Double> t)
				{
					final int columnIndex = t.getKey();
					final double ratingR = t.getValue();

					double tempSum = 0d;
					for (int k = 0; k < K; k++)
					{
						final double ratingAB = A.get(rowIndexCounter, k) * B.get(k, columnIndex);
						tempSum += ratingAB;
					}

					errorSum[0] += Math.pow(ratingR - tempSum, 2);
				}
			};

			@Override
			public void accept(final Iterator<Entry<Integer, Double>> t)
			{
				t.forEachRemaining(consumer);

				rowIndexCounter++;
			}
		});

		return errorSum[0];
	}

}