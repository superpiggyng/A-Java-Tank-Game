package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * ParachuteFalling extends the Falling abstract class.
 * The tank is Falling and the type of Falling is a Parachute fall.
 * The parachute will be deployed until the object touches the ground.
 */
public class ParachuteFalling extends Falling {
    private boolean isDeployed = true;
    private final float DESCENT_RATE = 60.0f; // 60 pixels/s

    /**
     * Constructs a ParachuteFalling object.
     * 
     * @param app            The PApplet App instance.
     * @param tank           The tank associated with the falling.
     * @param objectFallingY The initial Y position of the object.
     * @param endDescentY    The final Y position of the object.
     */
    public ParachuteFalling(App app, Tank tank, float objectFallingY, float endDescentY) {
        super(app, tank, objectFallingY, endDescentY);
    }

    public boolean isDeployed() {
        return isDeployed;
    }

    public void setDeployed(boolean deployStatus) {
        isDeployed = deployStatus;
    }

    /**
     * Updates the position of the falling object based on the descent rate.
     * Stops the descent when the object touches the ground, sets the isDeployed status to false.
     * 
     * @param deltaTime The time elapsed since the last update, in milliseconds.
     */
    @Override
    public void update(float deltaTime) {
        if (isDeployed) {
            objectFallingY += DESCENT_RATE * (deltaTime / 1000.0f);
            tank.setTankPositionY(objectFallingY);

            if (hasTouchedGround()) {
                objectFallingY = endDescentY;
                tank.setTankPositionY(objectFallingY);
                isDeployed = false;
            }
        } 
    }

    /**
     * Calculates the visual position of the parachute.
     * This is so it won't affect the actual stored position of the objects as that
     * might hinder calculations.
     * 
     * @return A PVector representing the visual position of the parachute.
     */
    public PVector getVisualParachutePosition() {
        return new PVector(objectPosition.x - 30, objectFallingY - 70);
    }

    /**
     * Displays the tank and the parachute at it's current position.
     * The y-coordinate of objectFallingY is calculated by the 
     * update method.
     * 
     * Extension is implemented here, parachute deploying sound.
     * 
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */
    @Override 
    public void display(float deltaTime) {
        if (isDeployed) {
            float tankY = tank.getVisualTank(objectFallingY);
            app.getParachuteSound().play();
            update(deltaTime);
            app.stroke(0);
            app.strokeWeight(1);
            app.ellipse(tank.getPosition().x, objectFallingY, 30, 15);
            app.ellipse(tank.getPosition().x, tankY, 15, 15); 
            tank.drawTurret(tankY);
            app.image(tank.getParachuteImage(), getVisualParachutePosition().x, getVisualParachutePosition().y, app.CELLSIZE * 2, app.CELLSIZE * 2);
            app.getParachuteSound().rewind();
        }
    } 
}