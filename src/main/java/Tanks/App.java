package Tanks;

import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;


import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;

import ddf.minim.*;


public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 0;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int FPS = 30;

    private String configPath;

    private GameManager gameManager;

    private Minim minim;
    private AudioPlayer backgroundSound;
    private AudioPlayer bombSound;
    private AudioPlayer movingSound;
    private AudioPlayer turretSound;
    private AudioPlayer parachuteSound;
    private AudioPlayer powerUpSound;


    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);

		JSONObject config = loadJSONObject(configPath);

        gameManager = GameManager.getInstance(this);
        gameManager.loadLevels(config.getJSONArray("levels"), config.getJSONObject("player_colours"));
        minim = new Minim(this);

        // Sound effects sourced from pixa bay
        // https://pixabay.com/music/fantasy-dreamy-childrens-best-adventure-ever-122726/
        backgroundSound = minim.loadFile("sounds/background2.mp3");
        // https://pixabay.com/sound-effects/search/explosion/
        bombSound = minim.loadFile("sounds/bombsound.mp3");
        // https://pixabay.com/sound-effects/tank-moving-143104/
        movingSound = minim.loadFile("sounds/tankMoving.mp3");

        // https://pixabay.com/sound-effects/turret-fire-85337/
        turretSound = minim.loadFile("sounds/tankTurret.mp3");

        // https://pixabay.com/sound-effects/power-punch-192118/
        parachuteSound = minim.loadFile("sounds/parachuteSound.mp3");
        parachuteSound.setGain(20);

        // https://pixabay.com/sound-effects/power-up-sparkle-1-177983/
        powerUpSound = minim.loadFile("sounds/repairSound.mp3");
        powerUpSound.setGain(100);
    }

    public AudioPlayer getBombSound() {
        return bombSound;
    }

    public AudioPlayer getParachuteSound() {
        return parachuteSound;
    }

    public AudioPlayer getPowerUpSound() {
        return powerUpSound;
    }

    public AudioPlayer getTurretSound() {
        return turretSound;
    }

    public AudioPlayer getMovingSound() {
        return movingSound;
    }



    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(){
        gameManager.keyPressed(keyCode);
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        gameManager.keyReleased(keyCode);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        gameManager.mouseClicked(mouseX, mouseY);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }


    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        clear();
        background(255);
        if (!backgroundSound.isPlaying()) {
            backgroundSound.rewind();
            backgroundSound.play();
        }
        gameManager.updateGame();
    }



    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }

}