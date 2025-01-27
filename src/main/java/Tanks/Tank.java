package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.*;

/**
 * The Tank class constitutes a tank object in the game.
 * It is responsible for handling logic relating to tank actions and reactions.
 */

public class Tank {
    /**
     * Enum encapsulates the possible commands for a tank.
     */
    protected enum Command{
        MOVE_LEFT, MOVE_RIGHT, ROTATE_RIGHT, ROTATE_LEFT, FIRE, INC_POWER, DEC_POWER, TELEPORT, NOTHING;
    }

    private final float DEFAULT_X_VELOCITY = 0.06f; 
    private final float DEFAULT_TURRET_ROTATION_VELOCITY = 0.0017f;

    private App app;
    private Character player;
    private PVector position;
    private String color;
    private Level level;
    private int[] rgb = new int[3];
    private float turretAngle = app.radians(-90);
    private final float TURRETLENGTH = 20;  // Length of the turret
    private int fuel;
    private int power;
    private int health;

    private PImage fuelImage;
    private float totalDistanceTraveled = 0; // Total distance traveled
    private PVector lastPosition = new PVector(); // Keep track of the last position
    private Projectile currentProjectile;
    private GameManager gameManager;
    private boolean markedForRemoval = false;
    private PImage parachuteImage;

    
    private ParachuteFalling parachute;
    private FreeFalling freefall;
    private Tank tankShooter;

    private float beforeExplosionY;
    private float afterExplosionY;
    private boolean falling;
    private Command command;
    

    private float turretEndX;
    private float turretEndY;
    private Explosion tankExplosion;
    private float explosionStartTime;

    private PVector teleportCoordinates;
    private boolean teleportFlag;

    /**
     * Constructs a new Tank object with the specified parameters.
     * 
     * @param app The PApplet App instance.
     * @param gameManager The GameManager object managing the game.
     * @param player The character of the player associated with the tank.
     * @param position The initial position of the tank.
     * @param color The color of the tank.
     * @param level The level object representing the game terrain and properties.
     */
    public Tank(App app, GameManager gameManager, Character player, PVector position, String color, Level level) {
        this.app = app;
        this.gameManager = gameManager;
        this.player = player;
        this.position = position;
        this.lastPosition = new PVector(position.x, position.y);
        this.color = color;
        this.level = level;
        this.fuel = 250;
        this.health = 100; // explode at 0 with an explosion radius of 15
        this.power = 50; // power can never exceed health
        this.fuelImage = app.loadImage("src/main/resources/Tanks/fuel.png");
        this.parachuteImage = app.loadImage("src/main/resources/Tanks/parachute.png");
    }

    /**
     * Checks if the tank is marked for removal from the game.
     * This is used in the level class for safe removal of dead tanks.
     * @return True if the tank is marked for removal, false otherwise.
     */
    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public int getHealth(){
        return this.health;
    }

    public int getPower() {
        return this.power;
    }

    public int getFuel() {
        return this.fuel;
    }

    public float getTurretX() {
        return turretEndX;
    }

    public float getTurretY() {
        return turretEndY;
    }

    public float getTurretAngle() {
        return turretAngle;
    }

    public int[] getRGB(){
        return rgb;
    }

    public Character getPlayerCharacter() {
        return player;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    public void setTeleportFlag(boolean status) {
        teleportFlag = status;
    }

    /**
     * Plays the turret rotation sound.
     */
    public void playTurretSound() {
        if (!app.getTurretSound().isPlaying()) {
            app.getTurretSound().loop();
        }
    }

    /**
     * Stops the turret rotation sound.
     */
    public void stopTurretSound() {
        app.getTurretSound().pause();
    }


    /**
     * Plays the moving sound, if the tank has fuel.
     */
    public void playMovingSound() {
        if (hasFuel()) {
            app.getMovingSound().loop();
        }
    }

    /**
     * Stops the moving sound.
     */
    public void stopMovingSound() {
        app.getMovingSound().pause();
    }

    /**
     * @return {@code true} if the tank has fuel; {@code false} otherwise.
     */
    public boolean hasFuel() {
        return fuel > 0;
    }

    public Projectile getCurrentProjectile() {
        return currentProjectile;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public PVector getPosition() {
        return position;
    }

    public void setTankPositionY(float y) {
        position.y = y;
    }

    // public Command getCommand() {
    //     return command;
    // }

    /**
     * Sets the command state to increase the power of the tank.
     */
    public void startIncPower() {
        command = Command.INC_POWER;
    }

    /**
     * Sets the command state to stop any action.
     */
    public void stopCommand() {
        command = Command.NOTHING;
    }

    /**
     * Sets the command state to decrease the power of the tank.
     */
    public void startDecPower() {
        command = Command.DEC_POWER;
    }

    /**
     * Sets the command state to move the tank left.
     */
    public void startToMoveLeft(){
        command = Command.MOVE_LEFT;
    }

    /**
    * Sets the command state to move the tank right.
    */
    public void startToMoveRight(){
        command = Command.MOVE_RIGHT;
    }

    /**
    * Sets the command state to rotate the tank's turret left.
    */
    public void startToRotateLeft() {
        command = Command.ROTATE_LEFT;
    }

    /**
    * Sets the command state to rotate the tank's turret right.
    */
    public void startToRotateRight() {
        command = Command.ROTATE_RIGHT;
    }

    /**
    * Sets the command state to fire.
    */
    public void fire() {
        command = Command.FIRE;
    }

    /**
    * Decrements the player's score by 15 points.
    * Sets the teleportCoordinates to the provided coordinates 
    * and the command state to TELEPORT.
    *
    * @param coordinates The target coordinates for teleportation.
    */
    public void startTeleport(PVector coordinates) {
        gameManager.decrementPlayerScore(this.player, 15);
        teleportCoordinates = coordinates;
        command = Command.TELEPORT;
    }

    // public void rapidFire() {
    //     command = Command.RAPID_FIRE;
    // }

    public float getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    public void setLastPosition(PVector position) {
        lastPosition = position;
    }

    

    /**
     * Calculates the visual position of the tank.
     * This is so it won't compromise the integrity of
     * the actual stored position of the objects.
     * 
     * @param x The stored x-position of the tank.
     * @return The y-position of the tank for visual display.
     */
    public float getVisualTank(float x) {
        return x - 10;
    }

    public PImage getParachuteImage() {
        return parachuteImage;
    }

    /**
     * Sets the y-coordinate of the tank's position before the explosion.
     * For use to determine the tank's falling distance.
     * 
     * @param y The y-coordinate of the tank's position after the explosion.
     */

    public void setBeforeExplosionY(float y) {
        beforeExplosionY = y;
    }

    
    /**
     * Sets the y-coordinate of the tank's position after the explosion.
     * For use to determine the tank's falling distance.
     * 
     * @param y The y-coordinate of the tank's position after the explosion.
     */
    public void setAfterExplosionY(float y) {
        afterExplosionY = y;
    }

    public boolean getFalling() {
        return falling;
    }

    /**
     * Calls to this method would result 
     * in the tank's falling flag to be set to true
     */

    public void falling() {
        falling = true;
    }

    /**
     * Sets the shooter that cause damage to this tank.
     * @param tankShooter the tank that shot this tank.
     */
    public void setShooter(Tank tankShooter) {
        this.tankShooter = tankShooter;
    }

    /**
     * Calculates the distance between the tank's current height and the new terrain height.
     *
     * @param tankY    The current vertical position of the tank.
     * @param terrainY The new height of the terrain at the tank's current position.
     * @return The absolute vertical difference between the tank and the terrain.
     */
    private float distanceToDescend(float tankY, float terrainY) {
        float yDistance = Math.abs(tankY - terrainY);
        return yDistance;
    }

    /**
     * Applies damage to tank based on the given distance.
     *
     * @param distance The distance fallen by the tank.
     */

    private void calculateHealthFromFalling(float distance) {
        applyDamage(distance);
    }

    /**
     * Determines the hitpoint of the tank by using the centre of the tank's
     * current position.
     * This return value is used to determine distance from impact.
     * @return The coordinates of the tank's position.
     */
    public PVector getTankCentre() {
        return new PVector(position.x, calculateTankY());
    }

    /**
     * Returns the y-coordinate of the tank's position 
     * based on the terrain height at the tank's current x-coordinate.
     *
     * @return The y-coordinate of the tank's position.
     */
    public float calculateTankY() {
        int x = (int) position.x;
        float tankY = level.getVertexHeightAtX(x);
        return tankY;
    }

    /**
     * Formula derived from:
     * https://byjus.com/maths/sine-function/#:~:text=In%20a%20right%2Dangled%20triangle%2C%20the%20sine%20of%20an%20angle,also%20called%20perpendicular)%20and%20hypotenuse.
     * https://byjus.com/maths/cosine-function/
     * 
     * Updates the position of the turret based on its current angle.
     * @param baseY The y-coordinate of the base of the turret.
     */
    private void updateTurretEndPosition(float baseY) {
        turretEndX = position.x + TURRETLENGTH * app.cos(turretAngle);
        turretEndY = baseY + TURRETLENGTH * app.sin(turretAngle);
    }

    /**
     * Rotates the turret to the left based on the elapsed time since the last frame.
     * The rotation angle is determined 
     * by multiplying the default turret rotation velocity by the deltaTime, resulting in approximately
     * ~ 3 rads/s
     * 
     * @param deltaTime The time elapsed since the last frame, in milliseconds.
     */
    private void rotateTurretLeft(float deltaTime) {
        turretAngle -= (deltaTime*DEFAULT_TURRET_ROTATION_VELOCITY);
        if (turretAngle < app.radians(-180) || turretAngle > app.radians(0)) {
            turretAngle = app.radians(-180); // Ensure angle doesn't go below -90 degrees (-PI/2 radians)
        }
    }

    /**
     * Rotates the turret to the right based on the elapsed time since the last frame.
     * The rotation angle is determined 
     * by multiplying the default turret rotation velocity by the deltaTime, resulting in approximately
     * ~ 3 rads/s
     * 
     * @param deltaTime The time elapsed since the last frame, in milliseconds.
     */
    private void rotateTurretRight(float deltaTime) {
        turretAngle += (deltaTime*DEFAULT_TURRET_ROTATION_VELOCITY);
        if (turretAngle > app.radians(0) || turretAngle < app.radians(-180)) {
            turretAngle = app.radians(0); // Ensure angle doesn't exceed 90 degrees (PI/2 radians)
        }
    }

    /**
     * Calls for the updating of the angle of the turret based on the current command.
     * 
     * @param deltaTime The time elapsed since the last frame, in milliseconds.
     */

    private void updateTurretAngle(float deltaTime) {
        if (command == Command.ROTATE_RIGHT){
            rotateTurretRight(deltaTime);
        }
        else if (command == Command.ROTATE_LEFT){
            rotateTurretLeft(deltaTime);
        }
    }

    

    /**
     * Updates the position of the tank based on the current command state and the elapsed time.
     * If there is fuel remaining, it adjusts the tank's position
     * along the x-axis according to deltaTime and the default velocity.
     * 
     * The speed is ~60px/s
     * 
     * @param deltaTime The elapsed time since the last update, in milliseconds.
     */
    private void updatePos(float deltaTime) {
        if (this.fuel > 0) {
            if (command == Command.MOVE_RIGHT){
                position.x+=DEFAULT_X_VELOCITY*deltaTime;
                position.x = app.constrain(position.x, 0, app.width - 1);   
                moveDistanceCheck();  
            }
            else if (command == Command.MOVE_LEFT){
                position.x-=DEFAULT_X_VELOCITY*deltaTime;
                position.x = app.constrain(position.x, 0, app.width - 1);   
                moveDistanceCheck();  
            }  
        }
    }

    /**
    * Updates the tank's position based on the teleport command state.
    * If the current command is TELEPORT, the tank's position is set to the teleport coordinates,
    * and the lastPosition is updated to the new position.
    * The teleport coordinates is determined by the mouseclick before the t is pressed.
    * The teleportFlag is set to false after the teleportation is complete.
    */
    private void updateTeleport() {
        if (command == Command.TELEPORT) {
            int col = (int) teleportCoordinates.x;
            if (col < 0 || col >= level.getVertexCoordinates().size()) {
                if (col < 0) {
                    col = 0;
                } else if (col >= level.getVertexCoordinates().size()) {
                    col = level.getVertexCoordinates().size() - 1;
                }
            }

            float newY = level.getVertexHeightAtX(col);
            position.x = col;
            position.y = newY;
            lastPosition.set(position);
        }
        teleportFlag = false;
    }



    /**
     * Updates the power level of the tank based on the current command state. 
     * If the command is to increase power, 
     * the power level is incremented by 36 units/s. 
     * 
     * If the command is to decrease power,
     * the power level is decremented by 36 units/s.
     * 
     * @param deltaTime The time elapsed since the last update, in milliseconds.
     */
    private void updatePower(float deltaTime) {
        if (command == Command.INC_POWER) {
            float increase = deltaTime * 0.036f;
            this.power += increase;
            this.power = app.constrain(this.power, 0, this.health);
        } 
        else if (command == Command.DEC_POWER){
            float decrease = deltaTime * 0.036f;
            this.power -= decrease;
            this.power = app.constrain(this.power, 0, this.health);
        }
    }

    /**
     * If the tank is commanded to fire, 
     * a new projectile is created with the current turret's end position, angle, and power level.
     * 
     * @param deltaTime The time elapsed since the last update, in milliseconds.
     */
    private void updateFire(float deltaTime) {
        if (command == Command.FIRE) {
            currentProjectile = new Projectile(app, level, this, new PVector(turretEndX, turretEndY), turretAngle, gameManager.getCurrentPower());
        }
    }

    

    // private void updateRapidFire(float deltaTime) {
    // if (command == Command.RAPID_FIRE) {
    //     Projectile newProjectile = new Projectile(app, level, this, new PVector(turretEndX, turretEndY), turretAngle, gameManager.getCurrentPower());
    //     projectiles.add(newProjectile);
    //     }
    // }

    /**
     * Increases the health level of the tank by 20 if the game is not over, 
     * the player has sufficient points available for the player to purchase power up.
     * 
     * 
     * Plays the power-up sound effect.
     * 
     * If the conditions are met, the health level is increased by 20 units, 
     * and the player's score is decremented by 20.
     * 
     */
    public void increaseHealth() {
        if (!gameManager.endGame()) {
            if (gameManager.getPlayerScore(player) >= 20) {
                if (this.health < 100) { // Check if health is less than 100
                    app.getPowerUpSound().play();
                    this.health += 20;
                    this.health = app.constrain(this.health, 0, 100);
                    gameManager.decrementPlayerScore(player, 20);
                    app.getPowerUpSound().rewind();
                }
            }
        }
    }

    /**
     * Increases the fuel level of the tank by 200 if the game is not over, 
     * the player has sufficient points available for the player to purchase power up. 
     * 
     * Plays the power-up sound effect.
     * 
     * If the conditions are met, the fuel level is increased by 200 units, and the player's score is decremented by 10.
     * 
     */
    public void increaseFuel() {
        if (!gameManager.endGame()) {
            if (gameManager.getPlayerScore(player) >= 10) {
                app.getPowerUpSound().play();
                app.getPowerUpSound().play();
                this.fuel += 200;
                gameManager.decrementPlayerScore(player, 10);
                app.getPowerUpSound().rewind();
            }
        }
    }

    /**
     * Checks the distance moved by the tank and updates the fuel accordingly.
     * If lastPosition is null, initializes it with the starting position.
     * Calculates the distance moved from the last position and updates the total distance traveled.
     * Removes 1 unit of fuel per 1px moved on the x axis
     */
    private void moveDistanceCheck() {
        if (lastPosition == null) {
            lastPosition = position.copy(); // Initialize lastPosition with the starting position
        } else {
            float distance = Math.abs(position.x - lastPosition.x); // Calculate the distance moved from the last position

            totalDistanceTraveled += distance;

        }

        fuel -= totalDistanceTraveled;
        fuel = Math.max(this.fuel, 0);
        totalDistanceTraveled = 0;
        lastPosition.set(position);
    }

    /**
     * Triggers an explosion and displays the animation at the tank's position.
     * Used an annonymous class implementing {@code Runnable} for the explosion animation logic
     * 
     * @param radius The radius of the explosion.
     */
    private void tankExplosion(float radius) {
        float maxRadiusRed = radius;
        float maxRadiusOrange = radius/100 * 50;
        float maxRadiusYellow = radius/100 * 20;
        float tankY = calculateTankY();

        Runnable displayExplosion = () -> {
            app.getBombSound().play();
            app.noStroke();
            app.fill(255, 0, 0); // Red
            app.ellipse(position.x, tankY, maxRadiusRed * 2, maxRadiusRed * 2);
            app.fill(255, 165, 0); // Orange
            app.ellipse(position.x, tankY, maxRadiusOrange * 2, maxRadiusOrange * 2);
            app.fill(255, 255, 0); // Yellow
            app.ellipse(position.x, tankY, maxRadiusYellow * 2, maxRadiusYellow * 2);
            app.getBombSound().rewind();
        };

        displayExplosion.run();
    }

    /**
     * Applies damage to the tank's health.
     * 
     * @param damage The amount of damage to apply.
     */
    public void applyDamage(float damage){
        this.health -= (int) damage;
        if (this.health <= 0) {
            tankExplosion(15.0f);
            this.markedForRemoval = true;

        }
    }

    /**
     * Checks if the tank is has fallen under the terrain.
     * Retrives the terrain height at the tank's current x-coordinate from the level.
     * If the terrain height is less than or equal to 0, the tank is marked for removal
     * and removed from the level.
     * 
     * @return {@code true} if the tank is under the terrain, {@code false} otherwise.
     */
    public boolean terrainUnder() {
        int x = (int) position.x;
        int colIndex = x;  
        float terrainHeight = level.getTerrainHeightAtX(colIndex);
        if (terrainHeight <= 0) {
            tankExplosion(30.0f);
            this.markedForRemoval = true;
            return true;
        }

        return false;
    }

    /**
     * Method is called when the falling flag is true
     * Updates and displays the tank's state when it's falling either with a parachute or in free fall.
     * If the tank is falling has remaining parachutes, it initializes
     * a new parachute object, and is responsible for updating and displaying the parachute. 
     * 
     * If the parachute touches the ground,the the drawGroundTank method is called and the flag for falling is reset to false.
     * 
     * The tank is in free fall when there are no remaining parachutes,
     * A new free fall object is initialized, calculates the distance to descend, reduces the affected tank's health,
     * adds score to the tank's shooter, and updates and displays the free fall object. 
     * 
     * When the free fall is over and the tank touches the ground, the drawGroundTank method is called.
     *
     * @param deltaTime The time elapsed since the last frame, used for updating the objects.
     */
    private void updateTanksDrawOrFalling(float deltaTime) {
        if (falling && gameManager.getParachuteCount(player) > 0 && parachute == null) {
            // Initialize the parachute object if it hasn't been initialized yet
            gameManager.setParachuteCount(player, gameManager.getParachuteCount(player) - 1);
            parachute = new ParachuteFalling(app, this, beforeExplosionY, afterExplosionY);
            parachute.update(deltaTime);
            parachute.display(deltaTime);
        }
        
        if (parachute != null) {
            // Update and display the parachute object
            parachute.update(deltaTime);
            parachute.display(deltaTime);
    
            
            // Check if the parachute has touched the ground
            if (parachute.hasTouchedGround()) {
                falling = false;
                parachute = null;
                drawGroundTank();
            }

            return;
        } 

        if (falling && gameManager.getParachuteCount(player) == 0 && freefall == null) {
            freefall = new FreeFalling(app, this, beforeExplosionY, afterExplosionY);
            float distance = distanceToDescend(beforeExplosionY, afterExplosionY);
            calculateHealthFromFalling(distance);
            gameManager.addScore(tankShooter.player, distance);
            freefall.update(deltaTime);
            freefall.display(deltaTime);
        }

        if (freefall != null) {
            freefall.update(deltaTime);
            freefall.display(deltaTime);


            if (freefall.hasTouchedGround()) {
                falling = false;
                freefall = null;
                drawGroundTank();
            }
        }

    }

    /** 
     * Draws information about the current tank in play
     * Including information about the tank/player's character
     * Fuel Level, Health, Power
     */
    private void displayPlayerAndFuel() {
        app.pushStyle();
        if (!(level.getTanks().isEmpty())) {
            app.image(fuelImage, (App.WIDTH/100) * 27, (App.HEIGHT/100) * 3, App.CELLSIZE, App.CELLSIZE);
            app.fill(0);
            app.textSize(24);

            app.text("Player " + gameManager.getCurrentPlayer() + "\'s " + "turn", (App.WIDTH/100) * 13, (App.HEIGHT/100) * 6);

            app.text(gameManager.getCurrentFuel(), (App.WIDTH/100) * 35, (App.HEIGHT/100) * 6);

            app.textSize(20);
            app.text("Health: ", (App.WIDTH/100) * 49, (App.HEIGHT/100) * 6);
            app.text("Power: " + gameManager.getCurrentPower(), (App.WIDTH/100) * 50, (App.HEIGHT/100) * 10);
            drawHealthBar((App.WIDTH/100) * 55, (App.HEIGHT/100) * 4, 100, 20, gameManager.getCurrentHealth(), 100);
            app.fill(0);
            app.text(gameManager.getCurrentHealth(), (App.WIDTH/100) * 72, (App.HEIGHT/100) * 5);

        }
        app.popStyle();
    }

    /**
     * Draws the Health Bar of the Tank with the 
     * background of the bar being the player's color
     * Draws the current power of the tank as a line on the healthbar
     * Updated every frame
     */
    private void drawHealthBar(float x, float y, float width, float height, int health, int maxHealth) {
        // Draw the background of the fuel bar (empty part)
        app.fill(50);
        app.rect(x, y, width, height);
        
        // Map the current fuel value to the width of the fuel bar
        float healthWidth = app.map(health, 0, maxHealth, 0, width);
        
        int[] currentRGB = gameManager.getCurrentPlayerRGB();
        // Draw the foreground of the fuel bar (filled part)
        app.fill(currentRGB[0], currentRGB[1], currentRGB[2]);
        app.rect(x, y, healthWidth, height);

        float powerLine = x + (width * gameManager.getCurrentPower() / 100);
        app.stroke(0);
        app.strokeWeight(3);
        app.line(powerLine, y, powerLine, y + height);
        app.noStroke();
    
    }

    /** 
     * Draws the Turret of the tank as a line with updated positioning every frame
     * @param baseY The y-coordinate of the base of the turret. 
     */

    public void drawTurret(float baseY) {
        updateTurretEndPosition(baseY);

        app.stroke(0);  // Set turret color
        app.strokeWeight(5);  // Set turret thickness
        app.line(position.x, baseY, turretEndX, turretEndY);  // Draw the turret
        app.strokeWeight(1);  // Reset stroke weight for other elements
    }

    /**
     * Updates and draws the tank with the color assigned in the JSON file.
     * If the color is set to "random", generates random RGB values.
     * implemented a special feature where if the color was random then the tanks would continuously
     * be updated with a new color every frame, disco colors.
     * If a specific color is designated then sets the color according to their RGB values, 
     * determined by CSV values
     */

    private void updateColor(){
        if ("random".equals(color)) {
            rgb[0] = (int) (Math.random() * 256);
            rgb[1] = (int) (Math.random() * 256);
            rgb[2] = (int) (Math.random() * 256);
        }

        else {
            String[] components = color.split(",");
            for (int i = 0; i < components.length; i++) {
                rgb[i] = Integer.parseInt(components[i].trim());
            }
        }
    }

    /**
     * Responsible for drawing the tank's main body if it is on the ground.
     * is updated on every frame with the position of the tank
     * 
     */

    private void drawGroundTank() {
        
        float tankY = calculateTankY();
        app.stroke(0);
        app.strokeWeight(1);
        app.ellipse(position.x, tankY, 30, 15); //this is the turret's first body
        app.ellipse(position.x, getVisualTank(tankY), 15, 15); 
        drawTurret(getVisualTank(tankY)); 
    }

    /**
     * Draws and updates relevant attributes of the tank object.
     * Calls for the display of the projectile if one is fired.
     * Responsible for drawing parachute count information.
     * 
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */

    public void draw(float deltaTime) {
        if (!teleportFlag) {
            updatePos(deltaTime);
        }  
        updateTeleport();
        updateTurretAngle(deltaTime);
        updateFire(deltaTime);
        updatePower(deltaTime);

        updateColor();
        displayPlayerAndFuel();

        if (currentProjectile != null) {
            currentProjectile.display(deltaTime);
            if (currentProjectile.getExplosion() != null && currentProjectile.getExplosion().isComplete()) {
                currentProjectile = null;
            }
        }


        app.fill(rgb[0], rgb[1], rgb[2]);

        if (falling) {
            updateTanksDrawOrFalling(deltaTime);
        } else {
            drawGroundTank();
        }
        
        app.image(parachuteImage, (App.WIDTH/100) * 27, (App.HEIGHT/100) * 10, App.CELLSIZE, App.CELLSIZE);
        app.fill(0);
        app.textSize(24);
        app.text(gameManager.getParachuteCount(), (App.WIDTH/100) * 35, (App.HEIGHT/100) * 12);

    }
}