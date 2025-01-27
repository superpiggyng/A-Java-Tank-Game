package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Represents an explosion object in the game.
 */
public class Explosion {
    private float positionX;
    private float positionY;
    private final int EXPLOSION_DURATION = 200; 
    private App app;


    private float radiusExplosion;
    private float maxRadiusRed;

    private long explosionStartTime;
    private boolean isComplete;

    private float currentRadiusRed = 0;
    private float currentRadiusOrange = 0;
    private float currentRadiusYellow = 0;

    /**
     * Constructs an Explosion object.
     * 
     * @param app The PApplet app instance.
     * @param positionX The x-coordinate position of the explosion.
     * @param positionY The y-coordinate position of the explosion.
     * @param radiusExplosion The radius of the explosion.
     */
    public Explosion(App app, float positionX, float positionY, float radiusExplosion) {
        this.app = app;
        this.positionX = positionX;
        this.positionY = positionY;
        this.radiusExplosion = radiusExplosion;
        this.explosionStartTime = app.millis();
        this.maxRadiusRed = this.radiusExplosion;

    }

    public float getExplosionStartTime() {
        return explosionStartTime;
    }

    // for testing
    public void setExplosionStartTime(long time) {
        explosionStartTime = time;
    }

    public boolean isComplete() {
        long currentTime = app.millis();
        return currentTime > explosionStartTime + EXPLOSION_DURATION;
    }
    
    /**
     * Displays the explosion animation with it 
     * comprising of 3 concentric circles that expand continuously 
     * over a period of 0.2 seconds.  
     * 
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */
    public void display(float deltaTime) {
        deltaTime = deltaTime / 1000.0f;

        float increment = maxRadiusRed / (240 / 1000.0f);

        // Increment the current radius for each circle
        currentRadiusRed += increment * deltaTime;
        currentRadiusOrange += increment*0.5 * deltaTime;
        currentRadiusYellow += increment*0.2 * deltaTime;


        // Display the circles
        app.getBombSound().play();
        app.noStroke();
        app.fill(255, 0, 0); // Red
        app.ellipse(positionX, positionY, currentRadiusRed * 2, currentRadiusRed * 2);
        app.fill(255, 165, 0); // Orange
        app.ellipse(positionX, positionY, currentRadiusOrange * 2, currentRadiusOrange * 2);
        app.fill(255, 255, 0); // Yellow
        app.ellipse(positionX, positionY, currentRadiusYellow * 2, currentRadiusYellow * 2);
        app.getBombSound().rewind();
    }
}