package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.*;

/**
 * The ProcessTerrain class handles the processing of terrain data passed as a 
 * 2D array of integers.
 * 
 * Primary objective is to provide methods to form the terrain as a smooth curve.
 */
public class ProcessTerrain {
    private int[][] terrain; // The terrain data
    private int[] heights; // Heights calculated from terrain
    private float[] replicatedHeights; // Replicated heights for smoothing
    private float[] smoothHeights; // Final smoothed heights
    private App app;
    private List <PVector> vertexCoords;

    /**
     * Constructs a ProcessTerrain object with the given terrain data and app instance application.
     * When a ProcessTerrain object is initialized,
     * it calculates heights from the terrain data, 
     * replicates heights for smoothing,
     * 
     * @param terrain The terrain data represented as a 2D array of integers.
     * @param app     The PApplet App instance.
     */
    public ProcessTerrain(int[][] terrain, App app) {
        this.app = app;
        this.terrain = terrain;
        this.vertexCoords = new ArrayList<PVector>();
        calculateHeights();
        replicateHeights();
    }

    public int[] getHeights() {
        return heights;
    }

    public List<PVector> getOriginalVertexCoords() {
        return vertexCoords;
    }

    /**
     * Calculates the heights of each column in the terrain.
     * For each column in the terrain,
     * it finds the 2 which represents the peak of that column
     * and calculates the height of that column accordingly.
     * The heights are stored in the {@code heights} array.
     */
    public void calculateHeights() {
        heights = new int[terrain[0].length];
        for (int j = 0; j < terrain[0].length; j++) {
            for (int i = terrain.length - 1; i >= 0; i--) {
                if (terrain[i][j] == 2) {
                    // gives the distance from the bottom of the array.
                    heights[j] = terrain.length - i;
                    break;
                }
            }
        }
    }

    /**
     * Replicates the height values from the {@code heights} array to 
     * create a new array {@code replicatedHeights}.
     * 
     * Each height value in the {@code heights} array is repeated {@code app.CELLHEIGHT} 
     * times in the new array.
     * If the {@code replicatedHeights} array is null, it creates a new array and populates 
     * it with replicated heights.
     * 
     */
    private void replicateHeights() {
        if (replicatedHeights == null) {
            replicatedHeights = new float[heights.length * app.CELLHEIGHT];
            int index = 0;
            // Iterate over each height value
            for (int height : heights) {
                // Repeat each height value 32 times
                for (int i = 0; i < app.CELLHEIGHT; i++) {
                    if (index < replicatedHeights.length) {
                        replicatedHeights[index++] = (float) height;
                    }
                }
            }
        }
    }

    /**
     * Applies the moving average calculation to the input data array.
     * 
     * @param data The array containing the data to be smoothed.
     * @return An array containing the recalculated data after applying the moving average calculations.
     */
    private float[] applyMovingAverage(float[] data) {
        float[] results = new float[data.length];

        for (int i = 0; i < data.length; i++) {
            float sum = 0;
            int count = 0;
            // Sum up to the next 31 elements, and the current element
            for (int j = i; j < i + 32 && j < data.length; j++) {
                sum += data[j];
                count++;
            }

            results[i] = sum / count;
        }

        return results;
    }

    /**
     * Smooths out the terrain heights by calling the moving average method twice.
     * If the smoothed heights is null, the method calculates
     * the moving average of the replicated heights and assigns it to the smoothHeights array.
     * 
     * @return An array containing the smoothed terrain heights after applying moving average.
     */

    public float[] smoothTerrain() {
        if (smoothHeights == null) {  // Only if not already initialized
            float[] firstMovingAvg = applyMovingAverage(replicatedHeights);
            smoothHeights = applyMovingAverage(firstMovingAvg);
        } 
        return smoothHeights;
    }

    /**
     * Generates vertex coordinates for the terrain.
     * The x-coordinate is the index of the current height value,
     * The y-coordinate is calculated by by scaling and 
     * inverting the corresponding smoothed height value.
     * The vertex coordinates are stored in the vertexCoords list.
     */
    public void generateVertexCoords() {
        for (int j = 0; j < smoothHeights.length; j++) {
            // Calculate x and y coordinates
            float x = j;  // Scales the x-coordinate across the canvas width
            float y = app.HEIGHT - smoothHeights[j] * app.CELLSIZE;  // Scale and invert the height to draw from the bottom up
            vertexCoords.add(new PVector(x, y));
        }

        vertexCoords.add(new PVector(app.WIDTH, app.HEIGHT));
        vertexCoords.add(new PVector(0, app.HEIGHT));

    }

}
