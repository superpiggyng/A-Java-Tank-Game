package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;


/**
 * The FreeFalling class extends the Falling abstract class.
 * A FreeFalling is a type of falling, a free fall.
 * The tank is Falling and the type of Falling is a FreeFalling.
 * The FreeFalling ends when the object touches the ground.
 */
public class FreeFalling extends Falling {
    private final float DESCENT_RATE = 120;
    private boolean falling = true;

    /**
     * Constructs a ParachuteFalling object.
     * 
     * @param app            The PApplet app instance.
     * @param tank           The tank associated with the falling.
     * @param objectFallingY The initial Y position of the object.
     * @param endDescentY    The final Y position of the object.
     */
    public FreeFalling(App app, Tank tank, float objectFallingY, float endDescentY) {
        super(app, tank, objectFallingY, endDescentY);
    }

    public void setFalling(boolean fallingStatus) {
        falling = fallingStatus;
    }

    public boolean isActive() {
        return falling;
    }

    /**
     * Updates the position of the falling object based on the descent rate.
     * Stops the descent when the object touches the ground, sets the falling status to false.
     * 
     * @param deltaTime The time elapsed since the last update, in milliseconds.
     */
    @Override
    public void update(float deltaTime) {
        if (falling) {
            objectFallingY += DESCENT_RATE * (deltaTime / 1000.0f);
            tank.setTankPositionY(objectFallingY);
            if (hasTouchedGround()) {
                objectFallingY = endDescentY;
                tank.setTankPositionY(objectFallingY);
                falling = false;
                
            }
        }
    }


    /**
     * Displays the tank at it's current position.
     * The y-coordinate of objectFallingY is calculated by the 
     * update method.
     * 
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */
    @Override 
    public void display(float deltaTime) {
        if (falling) {
            float tankY = tank.getVisualTank(objectFallingY);
            update(deltaTime);
            app.stroke(0);
            app.strokeWeight(1);
            app.ellipse(tank.getPosition().x, objectFallingY, 30, 15);
            app.ellipse(tank.getPosition().x, tankY, 15, 15); 
            tank.drawTurret(tankY);
        }
    } 

}