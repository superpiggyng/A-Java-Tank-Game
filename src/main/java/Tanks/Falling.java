package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Abstract class representing a falling object in the game.
 * Falling objects descend vertically until they touch the ground.
 */
public abstract class Falling {
    protected App app;
    protected PVector objectPosition;
    protected Tank tank;
    protected float objectFallingY;
    protected float endDescentY;

    /**
     * Constructs a Falling object.
     * 
     * @param app The PApplet app instance.
     * @param tank The object that is falling/in mid air
     * @param objectFallingY The initial Y position of the object
     * @param endDescentY The final Y position of the object
     */

    public Falling(App app,Tank tank, float objectFallingY, float endDescentY) {
        this.app = app;
        this.tank = tank;
        this.objectPosition = new PVector(tank.getPosition().x, endDescentY);
        this.objectFallingY = objectFallingY;
        this.endDescentY = endDescentY;
    }

    public float getObjectFallingY() {
        return objectFallingY;
    }

    public PVector getObjectPosition() {
        return objectPosition;
    }

    public float getEndDescentY() {
        return endDescentY;
    }

    /**
     * Primary purpose of the abstract method is to 
     * updates the falling object's state based on the deltaTime since the last update.
     * 
     * until the object has reached ground.
     * 
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */
    public abstract void update(float deltaTime);

    /**
     * Displays the falling object.
     * 
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */
    public abstract void display(float deltaTime);

    /**
     * To determine if the falling object has landed on the ground.
     * 
     * @return True if the falling object has landed on ground, false otherwise.
     */
    public boolean hasTouchedGround() {
        return objectFallingY >= endDescentY;
    }

}