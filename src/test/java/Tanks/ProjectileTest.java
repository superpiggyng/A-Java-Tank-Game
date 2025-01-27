package Tanks;

import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import processing.core.PVector;
import processing.core.PImage;
import java.util.*;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Random;

import ddf.minim.*;

public class ProjectileTest {

    @Test
    public void testProjectileUpdate() {
        App app = new App();
        app.loop();
        app.runSketch(new String[] {"App"}, app);
        app.setup();
        app.delay(1000);

        GameManager gameManager = new GameManager(app);
        Level level = new Level(app, gameManager, "level1.txt", "src/main/resources/Tanks/basic.png", "src/main/resources/Tanks/tree1.png", "255,255,255", new HashMap<Character, String>());
        Tank tankA = new Tank(app, gameManager, 'A', new PVector(100, 100), "255,0,0", level);
        Tank tankB = new Tank(app, gameManager, 'B', new PVector(200, 200), "0,255,0", level);

        PVector startPosition = new PVector(100, 100);
        float angleRAD = 0.5f; 
        float power = 50; 
        // Create a new Projectile instance
        Projectile projectile = new Projectile(app, level, tankA, startPosition, angleRAD, power);

        // Update the Projectile
        float deltaTime = 33.0f; 
        for (int i = 0; i < 100; i ++) {
            projectile.update(deltaTime);
        } // approx 3 seconds

        assertTrue(projectile.getShouldRemove()); // should be removed after hitting terrain or boundary
        
        Projectile projectileB = new Projectile(app, level, tankA, startPosition, angleRAD, power);

        float powerToMagnitude = projectileB.mapPowerToMagnitude(100);
        assertEquals(powerToMagnitude, 18);

    }

    @Test
    public void projectileDisplayTest() {

        App mockApp = mock(App.class);
        Tank mockTank = mock(Tank.class);
        Level mockLevel = mock(Level.class);
        int[] expectedRGB = {255, 128, 0};
        when(mockTank.getRGB()).thenReturn(expectedRGB);
        when(mockLevel.getWindForce()).thenReturn(10.0f);


        PVector startPosition = new PVector(100, 200);

        Projectile projectile = new Projectile(mockApp, mockLevel, mockTank, startPosition, 0.5f, 50);


        float deltaTime = 33.0f;
        float timesquared = deltaTime * deltaTime;
        
        // verifying that the projectile is displayed with the correct color
        projectile.display(deltaTime);
        verify(mockApp).fill(255,0,0);
        verify(mockApp).fill(mockTank.getRGB()[0], mockTank.getRGB()[1], mockTank.getRGB()[2]);
        // verify that the projectile has moved from it's last position after the update method was called within
        // display
        assertNotEquals(startPosition, projectile.getPosition());

        float x = 10.0f;
        float y = 20.0f;

        projectile.createExplosion(x, y);
        assertNotNull(projectile.getExplosion());
    }

    @Test
    public void testExplosionDisplay() {
        App mockApp = mock(App.class);
        Minim mockMinim = mock(Minim.class);
        AudioPlayer mockAudioPlayer = mock(AudioPlayer.class);
        when(mockMinim.loadFile("bombsound.mp3")).thenReturn(mockAudioPlayer);
        when(mockApp.getBombSound()).thenReturn(mock(AudioPlayer.class));

        float positionX = 100;
        float positionY = 200;
        float radiusExplosion = 30;

        Explosion explosion = new Explosion(mockApp, positionX, positionY, radiusExplosion);

        float deltaTime = 33.0f;
        explosion.display(deltaTime);

        verify(mockApp.getBombSound()).play();
        verify(mockApp).fill(255, 0, 0);
        verify(mockApp).fill(255, 165, 0);
        verify(mockApp).fill(255, 255, 0);
        verify(mockApp.getBombSound()).rewind();

    }

    @Test
    public void testHitTerrain() {
        App mockApp = mock(App.class);
        Tank mockTank = mock(Tank.class);
        Level mockLevel = mock(Level.class);
        PVector startPosition = new PVector(100, 200);

        Projectile projectile = new Projectile(mockApp, mockLevel, mockTank, startPosition, 0.5f, 50);



        float newX = 10.0f;
        float newY = 200.0f;
        float deltaTime = 33.0f;

        List<PVector> vertexCoords = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            vertexCoords.add(new PVector(i, 0));
        }

        //  populate the initial vertexcoords list
        when(mockLevel.getVertexCoordinates()).thenReturn(vertexCoords);

        // get the vertex height at index 10, return current y to be 300
        when(mockLevel.getVertexHeightAtX(10)).thenReturn(300.0f);
        
        // if the projectile is newY it is higher so it would be false
        boolean result = projectile.hitTerrain(newX, newY);
        assertFalse(result);

        // Reset the behavior of mockLevel
        reset(mockLevel);

        when(mockLevel.getVertexCoordinates()).thenReturn(vertexCoords);

        // if the projectile is newY it is lower than terrain
        //results in hit
        when(mockLevel.getVertexHeightAtX(10)).thenReturn(190.0f);
        result = projectile.hitTerrain(newX, newY);
        assertTrue(result);

       

    }

    @Test
    public void damageTest() {
        App mockApp = mock(App.class);
        Tank mockTank = mock(Tank.class);
        Level mockLevel = mock(Level.class);
        PVector startPosition = new PVector(100, 200);

        Projectile projectile = new Projectile(mockApp, mockLevel, mockTank, startPosition, 0.5f, 50);

        // assuming that the tank was 15 px away from the explosion
        float actualDamageInRadius = projectile.calculateDamage(15);

        // expected damage is 30hp at 15px distance
        assertEquals(30, actualDamageInRadius, 0.001);

        // edge case outside of radius by 1px no damage sustained 
        float actualDamageOutsideTerrain = projectile.calculateDamage(31);
        assertEquals(0, actualDamageOutsideTerrain, 0.001);
    }

    @Test
    public void calculateDistanceTest() {
        App mockApp = mock(App.class);
        Tank mockTank = mock(Tank.class);
        Level mockLevel = mock(Level.class);
        PVector startPosition = new PVector(100, 200);

        Projectile projectile = new Projectile(mockApp, mockLevel, mockTank, startPosition, 0.5f, 50);
        when(mockTank.getTankCentre()).thenReturn(new PVector(200, 200));

        // impact point of the projectile is impactX and impactY
        // tank centre is mocked
        float impactX = 190;
        float impactY = 190;
        float expectedDX = impactX - 200;
        float expectedDY = impactY - 200;
        // manual calculation of distance
        float expectedDistance = (float) (Math.sqrt(expectedDX * expectedDX + expectedDY * expectedDY));
        float actualDistance = projectile.calculateDistanceFromImpact(impactX, impactY, mockTank);
        assertEquals(expectedDistance, actualDistance, 0.001);

    }

    

}