package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.*;

/**
 * The Projectile class represents a projectile fired by a tank.
 * It handles the projectile's movement, collision detection with terrain, 
 * methods for calculating the distance and resulting damage caused from impact upon explosion.
 * 
 * Creates an explosion object if the projectile hits terrain.
 */
public class Projectile {
    private PVector position;
    private PVector velocity;
    private PVector trajectory;
    private final float GRAVITY = 0.36f;
    private App app;
    private Level level;
    private Tank tank;
    private float radiusTerrainExplosion = 30;
    private float angleRAD;
    private boolean shouldRemove = false;
    private float deltaTime;
    private float acceleration;
    private final int MAXDAMAGE = 60; //max damage tank can sustain per explosion
    private float accumulatedTime = 0;
    private Explosion explosion;
    private float explosionStartTime;

    /**
     * Constructs a new Projectile object.
     * 
     * @param app The PApplet App instance.
     * @param level The level in which the projectile exists.
     * @param tank The tank that fired the projectile.
     * @param startPosition The starting position of the projectile.
     * @param angleRAD The initial angle of the turret in radians.
     * @param power The power of the projectile.
     */
    public Projectile(App app, Level level, Tank tank, PVector startPosition, float angleRAD, float power) {
        this.app = app;
        this.level = level;
        this.tank = tank; // i have the info regarding who fired the projectile
        this.position = startPosition.copy();
        this.angleRAD = angleRAD;
        this.velocity = PVector.fromAngle(angleRAD).mult(mapPowerToMagnitude(power));
        this.radiusTerrainExplosion = 30;
    }

    public boolean getShouldRemove() {
        return shouldRemove;
    }

    public void setShouldRemove(boolean statusRemove) {
        shouldRemove = statusRemove;
    }

    public PVector explosionPosition() {
        return position;
    }
    
    public Tank getShooter() {
        return tank;
    }

    public Explosion getExplosion() {
        return explosion;
    }

    public PVector getVelocity() {
        return velocity;
    }

    public void endExplosion() {
        explosion = null;
    }

    public PVector getPosition() {
        return position;
    }

    public float getRadiusTerrainExplosion() {
        return radiusTerrainExplosion;
    }

        /**
     * Maps the power level from the range of 0 to 100 to a corresponding magnitude value
     * within the range of 2 to 18. 
     * It is used to determine the initial velocity magnitude of the projectile.
     * 
     *
     * @param power The power level of the projectile, ranging from 0 to 100.
     * @return The mapped magnitude value of the projectile's initial velocity.
     */
    public float mapPowerToMagnitude(float power) {
        return app.map(power, 0, 100, 2, 18);
    }

    /**
     * Updates the projectile's position based on last frame time, velocity, wind effect, and gravity effect.
     * The x-coordinate is affected by wind
     * The y-coordinate is affected by gravity
     * Formula for displacement (s) using initial velocity (v), acceleration (a), and time (t)
     * Source: https://www.calculatorsoup.com/calculators/physics/displacement_v_a_t.php
     * 
     * @param deltaTime The time elapsed since the last update.
     */
    public void update(float deltaTime) {
        if (!shouldRemove) {
        
            deltaTime = deltaTime / 20;
            float timesquared = deltaTime * deltaTime;
            float windEffect = (level.getWindForce() * 0.03f) * timesquared * 0.5f;
            float gravityEffect = (GRAVITY * timesquared * 0.5f);

            float newX = position.x + velocity.x * deltaTime + windEffect;
            float newY = position.y + velocity.y * deltaTime + gravityEffect;
            position.set(newX, newY);


            velocity.y += GRAVITY * deltaTime;

            if (this.hitTerrain(newX, newY)) {
                shouldRemove = true;
                createExplosion(newX, newY);
                level.handleImpact(this, position);
                level.modifyTerrainAt(position.x, position.y, radiusTerrainExplosion, tank);
                return;
            } 

            if (BoundaryCollision()) {
                shouldRemove = true;
                return;
            }
        }
    }


    /**
     * Checks if the projectile has exceeded the side boundaries of the game window.
     * 
     * @return {@code true} if the projectile has collided with the boundaries, {@code false} otherwise.
     */
    private boolean BoundaryCollision() {
        if (position.x < 0 || position.x > app.WIDTH) {
            return true;
        }
        return false;
    }

    /**
     * Calculates the distance of the explosion point (impactX, impactY) to the centre (hitpoint) of the tank.
     * 
     * @param impactX The x-coordinate of the explosion.
     * @param impactY The y-coordinate of the explosion.
     * @param tank The tank object whose distance from impact is to be calculated.
     * @return The distance from the impact point to the centre(hitpoint) of the tank.
     */

    public float calculateDistanceFromImpact(float impactX, float impactY, Tank tank) {
        PVector hitPoint = tank.getTankCentre();
        float dx = impactX - hitPoint.x;
        float dy = impactY - hitPoint.y;
        return (float) (Math.sqrt(dx * dx + dy * dy));
    }

    /**
     * Calculates the damage based on the distance from the explosion.
     * If the distance is greater than the radius of the explosion, no damage is sustained.
     * 
     * @param distance The distance from the explosion center to the impacted point.
     * @return The amount of damage sustained at the distance.
     */

    public float calculateDamage(float distance) {

        if (distance > radiusTerrainExplosion) {
            return 0;
        } else {
            float damageSustained = (distance / radiusTerrainExplosion) * MAXDAMAGE;
            return (MAXDAMAGE - damageSustained);
        }

    }

    /**
     * Checks if the projectile has hit the terrain
     * by comparing the height of the terrain at the projectile's x position with a tolerance of 0.1f
     * 
     * 
     * @param newX The x-coordinate of the projectile's new position, used to retrieve the corresponding terrain height.
     * @param newY The y-coordinate of the projectile's new position to compare against the terrain height.
     * @return True if the projectile has hit the terrain, false otherwise.
     */
    public boolean hitTerrain(float newX, float newY) {
        List<PVector> vertexCoords = level.getVertexCoordinates();
        int index = Math.round(newX);
        if (index < 0 || index >= vertexCoords.size()) {
            return false;
        } 

        float terrainHeightAtX = level.getVertexHeightAtX(index);
        float tolerance = 0.1f;
        if (newY >= terrainHeightAtX - tolerance) {
            return true;
        }

        return false;
    }


    /**
     * Creates a new {@link Explosion} object at the position where the projectile hits the terrain
     * 
     * @param x The x-coordinate where the explosion should occur.
     * @param y The y-coordinate where the explosion should occur.
     */
    public void createExplosion(float x, float y) {
        explosion = new Explosion(app, x, y, radiusTerrainExplosion);
    }



    /**
     * Draws the projectile, if the shouldRemove flag is false
     * as this represents that the projectile is still in motion
     * Calls for an updte of the projectile's position for the current frame
     * Sets the color of the projectile based on the tank's color
     * Draws the projectile as an ellipse at it's current position
     * 
     * If an explosion object has been created, the explosion is drawn.
     * Checks if the explosion has completed, and if it has, the method sets the explosion to null.
     * 
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */
    public void display(float deltaTime) {
        if (!shouldRemove) {
            update(deltaTime);
            app.fill(255,0,0);
            int[] rgb = tank.getRGB();
            app.fill(rgb[0], rgb[1], rgb[2]);
            app.ellipse(position.x, position.y, 10, 10);
        } 

        if (explosion != null) {
            explosion.display(deltaTime);
            if (explosion.isComplete()) {
                explosion = null;
            }
        }

    }
}
