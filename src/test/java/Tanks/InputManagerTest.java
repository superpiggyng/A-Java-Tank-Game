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


public class InputManagerTest {
    private App app;
    private GameManager gameManager;
    private InputManager inputManager;
    private Level level;
    private Tank mockTank;
    private List <Tank> tanks;
    PApplet mockPApplet;
    KeyEvent mockKeyEvent;

    @BeforeEach
    public void setUp() {
        mockPApplet = mock(PApplet.class);
        mockKeyEvent = mock(KeyEvent.class);
        mockTank = mock(Tank.class);
        gameManager = mock(GameManager.class);
        inputManager = new InputManager(mockTank);
    }

    @Test
    public void testMoveLeftInput() {
        when(mockKeyEvent.getKeyCode()).thenReturn(37);
        when(mockTank.hasFuel()).thenReturn(true);
        PVector mockPosition = new PVector(100, 200);
        when(mockTank.getPosition()).thenReturn(mockPosition);

        // when(mockPApplet.keyCode()).thenReturn(37);

        float initialPosition = mockTank.getPosition().x;


        float deltaTime = 33.0f;

        mockTank.setFuel(100);


        // move left
        inputManager.keyPressed(37);
        inputManager.update();
        verify(mockTank).playMovingSound();
        verify(mockTank).startToMoveLeft();
    }

    @Test
    public void testMoveRightInput() {
        when(mockKeyEvent.getKeyCode()).thenReturn(39);
        when(mockTank.hasFuel()).thenReturn(true);

        inputManager.keyPressed(39);
        inputManager.update();
        verify(mockTank).playMovingSound();
        verify(mockTank).startToMoveRight();
        inputManager.keyReleased(39);

        // when(mockTank.hasFuel()).thenReturn(false);
        // inputManager.keyPressed(39);
        // inputManager.updateButtons();
        // verify(mockTank).stopCommand();
        // inputManager.keyReleased(39);
    }

    @Test
    public void testRotateTurretLeft() {
        when(mockKeyEvent.getKeyCode()).thenReturn(38);

        inputManager.keyPressed(38);
        inputManager.updateButtons();
        verify(mockTank).playTurretSound();
        verify(mockTank).startToRotateLeft();
        inputManager.keyReleased(38);

        // inputManager.keyPressed(38);
        // inputManager.updateButtons();
        // verify(mockTank).stopCommand();
        // inputManager.keyReleased(38);
    }

    @Test
    public void testRotateTurretRight() {
        when(mockKeyEvent.getKeyCode()).thenReturn(40);

        inputManager.keyPressed(40);
        inputManager.update();
        verify(mockTank).playTurretSound();
        verify(mockTank).startToRotateRight();
        inputManager.keyReleased(40);

    }

    @Test
    public void testIncreasePower() {
        when(mockKeyEvent.getKeyCode()).thenReturn(87);

        inputManager.keyPressed(87);
        inputManager.updateButtons();
        verify(mockTank).startIncPower();
        inputManager.keyReleased(87);

    }

    @Test
    public void testDecreasePower() {
        when(mockKeyEvent.getKeyCode()).thenReturn(83);

        inputManager.keyPressed(83);
        inputManager.updateButtons();
        verify(mockTank).startDecPower();
        inputManager.keyReleased(83);

    }
}