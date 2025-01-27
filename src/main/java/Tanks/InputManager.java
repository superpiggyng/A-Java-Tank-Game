package Tanks;

import javax.annotation.Nullable;
import processing.core.PVector;

// The idea to implement an inputManager class was derived from:
// https://gameprogrammingpatterns.com/command.html

/**
 * Manages input controls for controlling a tank object.
 */
public class InputManager {
    private Tank tankInPlay;
    private App app;
    private GameManager gameManager;
    private final static boolean PRESSED = true;
    private final static boolean RELEASED = !PRESSED;
    private boolean prevCommandLeft, actualCommandLeft;
    private boolean prevCommandRight, actualCommandRight;
    private boolean prevCommandRotateRight, actualCommandRotateRight;
    private boolean prevCommandRotateLeft, actualCommandRotateLeft;
    private boolean prevCommandFire, actualCommandFire;
    private boolean prevCommandIncPower, actualCommandIncPower;
    private boolean prevCommandDecPower, actualCommandDecPower;
    private boolean prevCommandTeleport, actualCommandTeleport;

    private boolean fired;
    private long prevFrameTime;
    private float deltaTime;

    private PVector teleportPosition = new PVector();

    /**
     * Constructs an InputManager object.
     * @param tankInPlay The tank object to be controlled by the input manager.
     */
    public InputManager(@Nullable Tank tankInPlay) {
        this.tankInPlay = tankInPlay;
    }

    /**
     * Sets the tank object to be controlled by the input manager.
     * @param tankInPlay The tank object to be controlled.
     */
    public void setTankInPlay(Tank tankInPlay) {
        this.tankInPlay = tankInPlay;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public boolean getFiredStatus() {
        return fired;
    }

    public void setFiredStatus(boolean status) {
        fired = status;
    }

    public PVector getTeleport() {
        return teleportPosition;
    }

    /**
     * This function is called from the app 
     * with the keycode parameter passed to the game manager 
     * and finally to this.
     *
     * @param keyCode - The key code from Processing.
     */
    public void keyPressed(int keyCode){
        if (keyCode == 37){
            actualCommandLeft = PRESSED;
        }
        else if (keyCode == 39){
            actualCommandRight = PRESSED;
        }
        else if (keyCode == 38){
            actualCommandRotateLeft = PRESSED;
        }
        else if (keyCode == 40){
            actualCommandRotateRight = PRESSED;
        }
        else if (keyCode == ' ') {
            actualCommandFire = PRESSED;

        }

        else if (keyCode == 87) {
            actualCommandIncPower = PRESSED;
        }

        else if (keyCode == 83) {
            actualCommandDecPower = PRESSED;
        }

        else if (keyCode == 84) {
            actualCommandTeleport = PRESSED;
        }
    }

    /**
    * Handles the mouse click event and sets the teleport position.
    * Used for teleportation
    * @param mouseX The x-coordinate of the mouse click position.
    * @param mouseY The y-coordinate of the mouse click position.
    */
    public void mouseClicked(int mouseX, int mouseY){
        float teleportX = mouseX;
        float teleportY = mouseY;
        teleportPosition = new PVector(teleportX, teleportY);
    }


    /**
     * This function is called from the app 
     * with the keycode parameter passed to the game manager 
     * and finally to this.
     *
     * @param keyCode - The key code from Processing.
     */
    public void keyReleased(int keyCode){
        if (keyCode == 37){
            actualCommandLeft = RELEASED;
        }
        else if (keyCode == 39){
            actualCommandRight = RELEASED;
        }
        else if (keyCode == 38){
            actualCommandRotateLeft = RELEASED;
        }
        else if (keyCode == 40){
            actualCommandRotateRight = RELEASED;
        }
        
        else if (keyCode == ' ') {
            actualCommandFire = RELEASED;
            fired = true;
        }

        else if (keyCode == 87) {
            actualCommandIncPower = RELEASED;
        }

        else if (keyCode == 83) {
            actualCommandDecPower = RELEASED;
        }
        else if (keyCode == 84) {
            actualCommandTeleport = RELEASED;
        }
    }
    
    /**
     * Updates the input manager, including 
     * time intervals and button states.
     */
    public void update(){
        deltaTime = System.currentTimeMillis()-prevFrameTime;
        if (tankInPlay != null) {
            updateButtons();
            prevFrameTime = System.currentTimeMillis();
        }
    }

    /**
     * Updates the state of input buttons.
     * Calls the relevant methods on tankInPlay to carry out
     * the desired actions
     * or end the current action
     */
    public void updateButtons() {
        if (actualCommandRight != prevCommandRight){
            if (actualCommandRight == PRESSED && tankInPlay.hasFuel()) {
                tankInPlay.playMovingSound();
                tankInPlay.startToMoveRight();
            }
            else {
                tankInPlay.stopMovingSound();
                tankInPlay.stopCommand();
            }
        }
        
        if (actualCommandLeft != prevCommandLeft){
            if (actualCommandLeft == PRESSED && tankInPlay.hasFuel()) {
                tankInPlay.playMovingSound();
                tankInPlay.startToMoveLeft();
            }
            else {
                tankInPlay.stopMovingSound();
                tankInPlay.stopCommand();
            }
        }
        if (actualCommandRotateRight != prevCommandRotateRight){
            if (actualCommandRotateRight == PRESSED) {
                tankInPlay.playTurretSound();
                tankInPlay.startToRotateRight();
            }
            else {
                tankInPlay.stopTurretSound();
                tankInPlay.stopCommand();
            }
        }
        if (actualCommandRotateLeft != prevCommandRotateLeft){
            if (actualCommandRotateLeft == PRESSED) {
                tankInPlay.playTurretSound();
                tankInPlay.startToRotateLeft();
            }
            else {
                tankInPlay.stopTurretSound();
                tankInPlay.stopCommand();
            }
        }

        if (actualCommandFire != prevCommandFire) {
            if (actualCommandFire == PRESSED) {
                tankInPlay.fire();
            }
            else {
                tankInPlay.stopCommand();
             }
        }

        if (actualCommandIncPower != prevCommandIncPower) {
            if (actualCommandIncPower == PRESSED) {
                tankInPlay.startIncPower();
            } else {
                tankInPlay.stopCommand();
            }
        }

        if (actualCommandDecPower != prevCommandDecPower) {
            if (actualCommandDecPower == PRESSED) {
                tankInPlay.startDecPower();
            } else {
                tankInPlay.stopCommand();
            }
        }

        if (actualCommandTeleport != prevCommandTeleport) {
            if (actualCommandTeleport == PRESSED && (gameManager.getPlayerScore(tankInPlay.getPlayerCharacter()) >= 15)) {
                PVector tankPosition = tankInPlay.getPosition();
                float tolerance = 2f; 
                if (PVector.dist(tankPosition, teleportPosition) > tolerance) {
                    tankInPlay.startTeleport(teleportPosition);
                    tankInPlay.setTeleportFlag(true);
                }
                } else {
                    tankInPlay.stopCommand();
                }
            }


        resetButtonsStatements();
    }

     /**
     * Resets the state of input buttons.
     */
    private void resetButtonsStatements() {
        prevCommandRight = actualCommandRight;
        prevCommandLeft = actualCommandLeft;
        prevCommandRotateLeft = actualCommandRotateLeft;
        prevCommandRotateRight = actualCommandRotateRight;
        prevCommandFire = actualCommandFire;
        prevCommandDecPower = actualCommandDecPower;
        prevCommandIncPower = actualCommandIncPower;
        prevCommandTeleport = actualCommandTeleport;
    }
}