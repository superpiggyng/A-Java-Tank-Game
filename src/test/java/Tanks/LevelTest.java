package Tanks;

import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import processing.core.PVector;
import java.util.*;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.junit.jupiter.api.BeforeEach;


public class LevelTest {
    private App app;
    private GameManager gameManager;
    private Level level;
    private List <Tank> tanks;

    
    @BeforeEach
    public void setUp() {
        app = new App();
        app.loop();
        app.runSketch(new String[] {"App"}, app);
        app.setup();
        app.delay(1000);
        gameManager = GameManager.getInstance(app);
        level = gameManager.getCurrentLevel();
        gameManager.initializeCurrentLevel();
        level.drawSmoothTerrain();
    }

    @Test
    public void modifyTerrainTest() {
        tanks = level.getTanks();
        Tank tankShooter = mock(Tank.class);

        float[] beforeExplosionCoordinates = level.storeCoordinates();

        // i clicked on the screen to get a coordinate where the tank would be affected
        level.modifyTerrainAt(415,359,30, tankShooter);

        float[] afterExplosionCoordinates = level.storeCoordinates();

        // Assert that the terrain has changed before and after the explosion
        assertTrue(beforeExplosionCoordinates != afterExplosionCoordinates);

        // assert that the length of the smoothHeights array is 896
        // even after modification
        assertEquals(896, gameManager.getSmoothHeights().length);
        
        // tank should be falling as it should get affected by the explosion
        Tank affectedTank = level.getTanks().get(1);
        assertTrue(affectedTank.getFalling());
        app.dispose();
    }

    @Test
    public void handleImpactTest() {
        tanks = level.getTanks();

        // this point on the screen would result in the terrain falling
        PVector startPosition = new PVector(762, 376);
        float angleRAD = 0.5f; 
        float power = 50; 

        // Create a new Projectile instance
        Projectile projectile = new Projectile(app, level, tanks.get(3), startPosition, angleRAD, power);

        PVector explosionPosition = projectile.explosionPosition();

        level.handleImpact(projectile, explosionPosition);

        // set all parachute count to 0 so they will be affected by falling 
        for (int i = 0; i < tanks.size(); i++) {
            gameManager.setParachuteCount(tanks.get(i).getPlayerCharacter(), 0);
        }

        boolean atLeastOneTankHealthChanged = false;
        for (Tank tank : tanks) {
            if (tank.getHealth() < 100) {
                atLeastOneTankHealthChanged = true;
                break;
            }
        }

        // test that freefalling would result in hp damage
        assertTrue(atLeastOneTankHealthChanged);

        app.dispose();
    }
}