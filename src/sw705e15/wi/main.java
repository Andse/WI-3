package sw705e15.wi;

import sw705e15.wi.parsing.MovieUserRatingParser;
import sw705e15.wi.parsing.MovieUserRatingParser.Result;
import sw705e15.wi.parsing.SparseMatrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import sw705e15.wi.learning.*;

public class main {

	public static void main(String[] args) {
		
		SparseMatrix testdata = new SparseMatrix(new double[][]{
			{2, 9, 5, 2, 8, 7, 3, 7, 5, 1, 5, 9, 4, 7, 6, 5, 9, 9, 5, 7},
			{1, 0, 0, 9, 4, 5, 3, 5, 0, 9, 9, 0, 1, 7, 8, 1, 2, 8, 4, 5},
			{8, 7, 8, 6, 1, 3, 9, 9, 6, 1, 3, 9, 0, 10, 4, 7, 1, 1, 2, 7},
			{6, 7, 7, 4, 6, 7, 3, 5, 2, 4, 8, 9, 5, 8, 5, 9, 5, 3, 5, 7},
			{0, 4, 2, 2, 2, 4, 1, 5, 5, 6, 6, 5, 3, 6, 8, 1, 8, 4, 3, 6},
			{9, 4, 7, 2, 7, 6, 5, 5, 7, 6, 8, 1, 3, 9, 10, 4, 5, 9, 1, 6},
			{5, 10, 6, 5, 5, 9, 9, 2, 6, 7, 8, 2, 3, 1, 5, 3, 4, 4, 1, 9},
			{5, 4, 9, 4, 3, 8, 4, 1, 6, 6, 0, 7, 2, 3, 9, 10, 5, 2, 1, 7},
			{6, 4, 6, 7, 9, 9, 7, 1, 4, 4, 3, 8, 4, 10, 7, 6, 1, 6, 4, 9},
			{8, 2, 9, 1, 8, 9, 6, 9, 2, 1, 7, 0, 7, 8, 6, 3, 1, 5, 1, 5},
			{9, 3, 4, 8, 9, 8, 5, 2, 4, 8, 5, 8, 2, 5, 10, 1, 9, 9, 4, 1},
			{4, 3, 4, 7, 1, 0, 5, 9, 0, 2, 10, 10, 7, 7, 8, 5, 6, 9, 9, 1},
			{1, 9, 5, 1, 2, 0, 9, 7, 6, 7, 7, 3, 4, 4, 10, 8, 3, 8, 4, 7},
			{7, 2, 3, 9, 7, 1, 1, 9, 10, 9, 6, 6, 4, 4, 6, 8, 9, 7, 1, 3},
			{3, 3, 9, 2, 7, 3, 3, 9, 1, 1, 3, 3, 2, 2, 4, 8, 1, 7, 6, 7},
			{8, 4, 5, 6, 8, 0, 4, 1, 0, 10, 3, 2, 2, 4, 5, 2, 5, 9, 9, 7},
			{4, 7, 3, 5, 5, 4, 6, 4, 9, 0, 9, 8, 6, 0, 9, 5, 7, 1, 3, 5},
			{8, 1, 4, 2, 4, 3, 9, 10, 2, 8, 4, 7, 3, 5, 0, 9, 3, 3, 0, 7},
			{2, 9, 7, 0, 6, 5, 5, 6, 0, 8, 8, 3, 6, 3, 2, 1, 4, 0, 1, 5},
			{1, 1, 4, 8, 9, 9, 4, 9, 8, 0, 1, 6, 9, 10, 5, 9, 5, 8, 1, 5}});
		
		final String trainingDataDirectoryPath = "C:\\Users\\Marhlder\\Desktop\\download\\training_set";
		final String moviesDescriptionsFilePath = "C:\\Users\\Marhlder\\Desktop\\download\\movie_titles.txt";
		
		final String qualifyingAndPredictionFilePath = "C:\\Users\\Marhlder\\Desktop\\download\\qualifying.txt";
		final String probeFilePath ="C:\\Users\\Marhlder\\Desktop\\download\\probe.txt";
		
		final int trainingDataLimit = 100;
		
		Result data = null;
		try
		{
			data = MovieUserRatingParser.parserUserRatingMovies(trainingDataDirectoryPath, moviesDescriptionsFilePath, qualifyingAndPredictionFilePath, probeFilePath, trainingDataLimit);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		final Processing processing = new Processing( data.trainingDataMatrix);
		
		List<Integer> movieIds = new ArrayList<>();
		movieIds.add(1);
		
		HashMap<Integer, Double> result = processing.prediction(processing.newData, 30878, movieIds);
		
		double predictedRating = result.get(1); 
		int userIndex = data.probeDataMatrix.userIDToIndexMapping.get(30878);
		int movieIndex = data.probeDataMatrix.movieIDToIndexMapping.get(1);
		double actualRating = data.probeDataMatrix.get(movieIndex, userIndex);
		
		processing.notify();
	}

}
