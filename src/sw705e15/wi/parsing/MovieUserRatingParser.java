package sw705e15.wi.parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jblas.DoubleMatrix;

public class MovieUserRatingParser
{
	@SuppressWarnings("serial")
	public static class UserMovieRatingMatrix extends DoubleMatrix
	{
		private UserMovieRatingMatrix(final int newRows, final int newColumns)
		{
			super(newRows, newColumns);
		}
		
		public final List<Integer> indexToUserIDMapping = new ArrayList<>();
		public final List<Integer> indexToMovieIDMapping = new ArrayList<>();
		public final HashMap<Integer, Integer> userIDToIndexMapping = new HashMap<>();
		public final HashMap<Integer, Integer> movieIDToIndexMapping = new HashMap<>();
		public HashMap<Integer, String> movieIDToMovieTitleMapping = new HashMap<>();
	}

	// Movies: Rows
	// Users: Columns

	public static UserMovieRatingMatrix parserUserRatingMovies(final String trainingDataDirectoryPath, final String moviesDescriptionsFilePath, final String qualifyingAndPredictionFilePath, final String probeFilePath, final int trainingDataLimit) throws IOException
	{
		final File trainingDataDirectory = new File(trainingDataDirectoryPath);
		
		// Limit training data size
		final File[] trainingDataSetDirectoryListing = Arrays.copyOfRange(trainingDataDirectory.listFiles(), 0, trainingDataLimit);

		final HashMap<Integer, HashMap<Integer, Integer>> userIDToRatingForMovie = new HashMap<>();
		final HashSet<Integer> userIDs = new HashSet<>();
		
		for(File trainingDatasetFile : trainingDataSetDirectoryListing)
		{
			final BufferedReader fileReader = new BufferedReader(new FileReader(trainingDatasetFile));
			final int movieId = Integer.parseInt(fileReader.readLine().replace(":", ""));
			
			userIDToRatingForMovie.put(movieId, new HashMap<>());
			
			String line;
			while ((line = fileReader.readLine()) != null)
			{
				final String[] substring = line.split(",");
				
				final int userID = Integer.parseInt(substring[0]);
				final int rating = Integer.parseInt(substring[1]);
				
				userIDToRatingForMovie.get(movieId).put(userID, rating);
				userIDs.add(userID);
			}
			
			fileReader.close();
		}
		
		// Parse movie to title file
		final HashMap<Integer, String> movieIDToMovieTitleMapping = new HashMap<>();
		
		final BufferedReader movieDescriptionFileReader = new BufferedReader(new FileReader(moviesDescriptionsFilePath));
		String line;
		while ((line = movieDescriptionFileReader.readLine()) != null)
		{
			final int indexOfFirstComma = line.indexOf(',');
			final int indexOfSecondComma = line.substring(indexOfFirstComma, line.length()).indexOf(',');
			
			final int movieID = Integer.parseInt(line.substring(0, indexOfFirstComma));
			final String movieTitle = line.substring(indexOfSecondComma, line.length());
			
			movieIDToMovieTitleMapping.put(movieID, movieTitle);
		}
		
		movieDescriptionFileReader.close();
		
		// Parse QUALIFYING AND PREDICTION DATASET
		final HashMap<Integer, ArrayList<Integer>> qualifyingAndPredictionDataSetIdsPairs = new HashMap<>();
		final HashSet<Integer> qualifyingAndPredictionDataSetUserIDs = new HashSet<>();
		
		final BufferedReader qualifyingAndPredictionFileReader = new BufferedReader(new FileReader(moviesDescriptionsFilePath));
		Integer currentMovieID = null;
		while ((line = qualifyingAndPredictionFileReader.readLine()) != null)
		{
			// If line contains ":" then we are at a new movie
			if(line.contains(":"))
			{
				currentMovieID = Integer.parseInt(line.replace(":", ""));
				qualifyingAndPredictionDataSetIdsPairs.put(currentMovieID, new ArrayList<>());
			}
			// If not then we are at a userID
			else 
			{
				final Integer currentUserID = Integer.parseInt(line.substring(0, line.indexOf(',')));
				qualifyingAndPredictionDataSetIdsPairs.get(currentMovieID).add(currentUserID);
				qualifyingAndPredictionDataSetUserIDs.add(currentUserID);
			}
		}
		
		qualifyingAndPredictionFileReader.close();
		
		// Parse THE PROBE DATASET
		final HashMap<Integer, ArrayList<Integer>> probeDataSetIdsPairs = new HashMap<>();
		final HashSet<Integer> probeDataSetUserIDs = new HashSet<>();
		
		final BufferedReader probeFileReader = new BufferedReader(new FileReader(moviesDescriptionsFilePath));
		while ((line = probeFileReader.readLine()) != null)
		{
			if(line.contains(":"))
			{
				currentMovieID = Integer.parseInt(line.replace(":", ""));
				probeDataSetIdsPairs.put(currentMovieID, new ArrayList<>());
			}
			// If not then we are at a userID
			else 
			{
				final Integer currentUserID = Integer.parseInt(line);
				probeDataSetIdsPairs.get(currentMovieID).add(currentUserID);
				probeDataSetUserIDs.add(currentUserID);
			}
		}
		
		probeFileReader.close();
		
		// Extract QUALIFYING AND PREDICTION DATASET from Training Data
		final UserMovieRatingMatrix qualifyingAndPredictionDataMatrix = new UserMovieRatingMatrix(qualifyingAndPredictionDataSetIdsPairs.size(), qualifyingAndPredictionDataSetUserIDs.size());
		qualifyingAndPredictionDataMatrix.movieIDToMovieTitleMapping = movieIDToMovieTitleMapping;
		
		for(Map.Entry<Integer, ArrayList<Integer>> mapEntry : qualifyingAndPredictionDataSetIdsPairs.entrySet())
		{
			final Integer movieID = mapEntry.getKey();
			
			qualifyingAndPredictionDataMatrix.indexToMovieIDMapping.add(movieID);
			qualifyingAndPredictionDataMatrix.movieIDToIndexMapping.put(movieID, qualifyingAndPredictionDataMatrix.indexToMovieIDMapping.size() - 1);
			
			final Integer indexForMovie = qualifyingAndPredictionDataMatrix.movieIDToIndexMapping.get(movieID);
			
			for(Integer userID : mapEntry.getValue())
			{
				// Remove from originally parsed training data and disregard the rating (There should not be any)
				userIDToRatingForMovie.get(movieID).remove(userID);
				
				if(!qualifyingAndPredictionDataMatrix.userIDToIndexMapping.containsKey(userID))
				{
					qualifyingAndPredictionDataMatrix.indexToUserIDMapping.add(userID);
					qualifyingAndPredictionDataMatrix.userIDToIndexMapping.put(userID, qualifyingAndPredictionDataMatrix.indexToUserIDMapping.size() - 1);
				}
				
				final Integer indexForUser = qualifyingAndPredictionDataMatrix.userIDToIndexMapping.get(userID);
				
				qualifyingAndPredictionDataMatrix.put(indexForMovie, indexForUser, 0);
			}
		}
		
		// Extract THE PROBE DATASET from Training Data
		final UserMovieRatingMatrix probeDataMatrix = new UserMovieRatingMatrix(probeDataSetIdsPairs.size(), probeDataSetUserIDs.size());
		probeDataMatrix.movieIDToMovieTitleMapping = movieIDToMovieTitleMapping;
		
		for(Map.Entry<Integer, ArrayList<Integer>> mapEntry : qualifyingAndPredictionDataSetIdsPairs.entrySet())
		{
			final Integer movieID = mapEntry.getKey();
			
			probeDataMatrix.indexToMovieIDMapping.add(movieID);
			probeDataMatrix.movieIDToIndexMapping.put(movieID, probeDataMatrix.indexToMovieIDMapping.size() - 1);
			
			final Integer indexForMovie = probeDataMatrix.movieIDToIndexMapping.get(movieID);
			
			for(Integer userID : mapEntry.getValue())
			{
				// Remove from originally parsed training data and save the rating
				final Integer rating = userIDToRatingForMovie.get(movieID).remove(userID);
				
				if(!probeDataMatrix.userIDToIndexMapping.containsKey(userID))
				{
					probeDataMatrix.indexToUserIDMapping.add(userID);
					probeDataMatrix.userIDToIndexMapping.put(userID, probeDataMatrix.indexToUserIDMapping.size() - 1);
				}
				
				final Integer indexForUser = probeDataMatrix.userIDToIndexMapping.get(userID);
				
				probeDataMatrix.put(indexForMovie, indexForUser, rating);
			}
		}
		
		final UserMovieRatingMatrix trainingDataMatrix = new UserMovieRatingMatrix(userIDToRatingForMovie.size(), userIDs.size());
		
		// Fuck locality!!
		for(Map.Entry<Integer, HashMap<Integer, Integer>> mapEntry : userIDToRatingForMovie.entrySet())
		{
			final Integer movieID = mapEntry.getKey();
			final HashMap<Integer, Integer> userIDRatingMap = mapEntry.getValue(); 
			
			trainingDataMatrix.indexToMovieIDMapping.add(movieID);
			trainingDataMatrix.movieIDToIndexMapping.put(movieID, trainingDataMatrix.indexToMovieIDMapping.size() - 1);
			
			final Integer indexForMovie = trainingDataMatrix.movieIDToIndexMapping.get(movieID);
			
			for(Map.Entry<Integer, Integer> userIDRatingPair : userIDRatingMap.entrySet())
			{
				final Integer userID = userIDRatingPair.getKey();
				final Integer rating = userIDRatingPair.getValue();
				
				if(!trainingDataMatrix.userIDToIndexMapping.containsKey(userID))
				{
					trainingDataMatrix.indexToUserIDMapping.add(userID);
					trainingDataMatrix.userIDToIndexMapping.put(userID, trainingDataMatrix.indexToUserIDMapping.size() - 1);
				}
				
				final Integer indexForUser = trainingDataMatrix.userIDToIndexMapping.get(userID);
				
				trainingDataMatrix.put(indexForMovie, indexForUser, rating);
			}	
		}

		trainingDataMatrix.movieIDToMovieTitleMapping = movieIDToMovieTitleMapping;
		
		return trainingDataMatrix;
	}
}
