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

import processing.core.PConstants;

public class ExplosionTest {
    @Test
    public void testExplosion() {
        App app = new App();
        app.loop();
        app.runSketch(new String[] {"App"}, app);
        app.setup();
        app.delay(1000);

        GameManager gameManager = new GameManager(app);
        Level level0 = new Level(app, gameManager, "level1.txt", "src/main/resources/Tanks/basic.png", "src/main/resources/Tanks/tree1.png", "255,255,255", new HashMap<Character, String>());

        Tank tankA = new Tank(app, gameManager, 'A', new PVector(100, 100), "255,0,0", level0);

        float radiusTerrainExplosion = 30;
        Explosion explosion = new Explosion(app, tankA.getPosition().x, tankA.getPosition().y, radiusTerrainExplosion);

        try {
            Thread.sleep(300); 
        } catch (InterruptedException e) {
            e.printStackTrace(); 
        }

        assertTrue(explosion.isComplete());

        app.dispose();
    }
}