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

import org.junit.jupiter.api.BeforeEach;
import ddf.minim.*;

public class DisplayTest {
    private App mockApp;
    private GameManager gameManager;
    private Level level;
    private Tank mockTank;
    private Minim mockMinim;

    @BeforeEach
    public void setUp() {
        mockApp = mock(App.class);
        mockTank = mock(Tank.class);
        gameManager = mock(GameManager.class);
        level = mock(Level.class);
        mockMinim = mock(Minim.class);
    }

    @Test
    public void testFreeFallingDisplay() {
        when(mockTank.getVisualTank(anyFloat())).thenReturn(100f);
        PVector positionMock = new PVector(200, 100);
        when(mockTank.getPosition()).thenReturn(positionMock);

        FreeFalling freeFall = new FreeFalling(mockApp, mockTank, 100.0f, 200.0f);

        
        float deltaTime = 33.0f;
        float DESCENT_RATE = 120.0f;
        float objectFallingY = 100.0f + DESCENT_RATE * (deltaTime / 1000.0f);

        // set the falling state to true in order to enter the update loop
        freeFall.setFalling(true);
        // display the freefall
        freeFall.display(deltaTime);
        
        // check that all elements on the screen are rendered at the correct positions
        // this checks for 1 loop.
        verify(mockApp).stroke(0);
        verify(mockApp).strokeWeight(1);
        verify(mockApp).ellipse(eq(positionMock.x), eq(objectFallingY), eq(30.0f), eq(15.0f));
        verify(mockApp).ellipse(eq(positionMock.x), eq(positionMock.y), eq(15.0f), eq(15.0f));
        verify(mockTank).drawTurret(eq(positionMock.y));

        mockApp.dispose();
    }

    @Test
    public void testParachuteDisplay() {
        when(mockApp.getParachuteSound()).thenReturn(mock(AudioPlayer.class));
        
        when(mockTank.getVisualTank(anyFloat())).thenReturn(100.0f);
        PVector positionMock = new PVector(200.0f, 100.0f);
        when(mockTank.getPosition()).thenReturn(positionMock);
        
        ParachuteFalling parachute = new ParachuteFalling(mockApp, mockTank, 105.0f, 200.0f);
        PImage mockPImage = mock(PImage.class);

        
        float deltaTime = 33.0f;
        float DESCENT_RATE = 60.0f;
        float objectFallingY = 105.0f + DESCENT_RATE * (deltaTime / 1000.0f);

        when(mockTank.getVisualTank(objectFallingY)).thenReturn(100.0f);
        when(mockTank.getParachuteImage()).thenReturn(mockPImage);

        parachute.setDeployed(true);
        parachute.display(deltaTime);
        
        verify(mockApp.getParachuteSound()).play();
        verify(mockApp).stroke(0);
        verify(mockApp).strokeWeight(1);
        verify(mockApp).ellipse(eq(positionMock.x), eq(objectFallingY), eq(30.0f), eq(15.0f));
        verify(mockApp).ellipse(eq(positionMock.x), eq(100.0f), eq(15.0f), eq(15.0f));
        verify(mockTank).drawTurret(eq(positionMock.y));
        verify(mockApp).image(eq(mockTank.getParachuteImage()), eq(parachute.getObjectPosition().x - 30), eq(objectFallingY - 70), eq(64.0f), eq(64.0f));
        verify(mockApp.getParachuteSound()).rewind();

        mockApp.dispose();
    }
}