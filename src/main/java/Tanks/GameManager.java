package Tanks;

import processing.data.JSONArray;
import processing.data.JSONObject;

import processing.core.PVector;

import java.util.ArrayList;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import processing.event.MouseEvent;

import java.awt.event.KeyEvent;

/**
 * Manages the game's state and logic, including levels, players, and gameplay mechanics.
 */
public class GameManager {

    private static GameManager instance;
    private ArrayList<Level> levels;
    private int currentLevelIndex = 0;
    private App app;
    private Tank tankInPlay;
    private int currentPlayerIndex = 0;
    private float[] smoothHeights;
    private List <PVector> vertexCoords;
    private List<Tank> initialTanks;

    private float deltaTime;
    private float lastFrameTime;

    private boolean showArrow;
    private int arrowDuration = 2000;
    private int arrowTimeStart;

    private final InputManager inputManager;

    private Map<Character, Integer> parachuteCount;
    private Map<Character, Integer> playerScores;
    private int endGameIndex = 0;

    private long lastScoreTime = 0;
    private long levelTransitionStartTime;

    private final int BOX_WIDTH = 400;
    private final int BOX_HEIGHT = 200;
    private final int BOX_X = (app.WIDTH/2 - 30) - BOX_WIDTH/2;
    private final int BOX_Y = (app.HEIGHT/2 + 50) - BOX_HEIGHT/2;
    private final int BOX_CENTRE = BOX_X + BOX_WIDTH/2;
    private final int TEXT_X = BOX_CENTRE - 150;
    private int[] winningRGB;


    /**
     * Constructs a GameManager object.
     * 
     * @param app The PApplet app instance.
     */
    public GameManager(App app) {
        this.app = app;
        this.levels = new ArrayList<Level>();
        this.inputManager = new InputManager(null);
        this.parachuteCount = new HashMap<Character, Integer>();
        this.playerScores = new HashMap<Character, Integer>();
        inputManager.setApp(app);
        inputManager.setGameManager(this);
    }

    /**
     * Retrieves or initializes the GameManager instance.
     * 
     * @param app The PApplet app instance.
     * @return The GameManager instance.
     */
    public static GameManager getInstance(App app) {
        if (instance == null) {
            instance = new GameManager(app);
        }
        return instance;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public void setCurrentLevelIndex(int levelIndex) {
        currentLevelIndex = levelIndex;
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public int getCurrentPlayerIndex(){
        return currentPlayerIndex;
    }

    public ArrayList<Level> getLevels() {
        return levels;
    }

    public float[] getSmoothHeights() {
        return smoothHeights;
    }

    public List<PVector> getVertexCoordinates() {
        return vertexCoords;
    }

    public Tank getTankInPlay() {
        return tankInPlay;
    }

    public void setTankInPlay(Tank newTank) {
        this.tankInPlay = newTank;
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    public int getCurrentFuel() {
        List<Tank> tanks = getCurrentLevel().getTanks();
        int fuel = tanks.get(currentPlayerIndex).getFuel();
        return Math.max(fuel, 0);
    }

    public int getPlayerScore(Character player) {
        return playerScores.getOrDefault(player, 0);
    }

    public void setParachuteCount(Character player, int count) {
        parachuteCount.put(player, count);
    }

    public int[] getCurrentPlayerRGB() {
        return getCurrentLevel().getTanks().get(currentPlayerIndex).getRGB();
    }

    public int getCurrentHealth() {
        List<Tank> tanks = getCurrentLevel().getTanks();
        return tanks.get(currentPlayerIndex).getHealth();

    }

    public int getCurrentPower() {
        List<Tank> tanks = getCurrentLevel().getTanks();
        if (!tanks.isEmpty()) {
            Tank currentTank = tanks.get(currentPlayerIndex);
            int health = (int) getCurrentHealth();
            return app.constrain(currentTank.getPower(), 0, health);
        }
        throw new IllegalStateException("Cannot get power when there are no tanks");
    }

    public int getParachuteCount() {
        List<Tank> tanks = getCurrentLevel().getTanks();
        if (!tanks.isEmpty()) {
            Tank currentTank = tanks.get(currentPlayerIndex);
            return parachuteCount.getOrDefault(currentTank.getPlayerCharacter(), 0);
        }
        return 0;
    }

    public int getParachuteCount(Character player) {
        return parachuteCount.getOrDefault(player, 0);
    }

    public char getCurrentPlayer() {
        List<Tank> tanks = getCurrentLevel().getTanks();
        if (!tanks.isEmpty()) {
            return tankInPlay.getPlayerCharacter();
        }
        throw new IllegalStateException("Cannot get player when there are no more in play");
    }


    /**
     * Display the arrow pointing above it's position.
     * Sets the showArrow flag to true.
     * Records the start time of the turn.
     */
    private void startPlayerTurn(){
        showArrow = true;
        arrowTimeStart = app.millis();
    }


    /**
     * Creates new Level objects based on the provided configuration files 
     * and stores them in the levels list.
     * 
     * Calls {@link #initializeCurrentLevel()} to initialize the current level.
     * 
     * @param levelsConfig   JSON array containing level configuration data.
     * @param playerColors   JSON object containing player color mappings.
     */
    public void loadLevels(JSONArray levelsConfig, JSONObject playerColors) {
        HashMap<Character, String> tankColors = new HashMap<>();
        Iterable<String> keys = playerColors.keys();
            for (String key : keys) {
                String color = playerColors.getString(key);
                tankColors.put(key.charAt(0), color);
                // this is ridden over by the save sent over by parachute class
                parachuteCount.put(key.charAt(0), 3);
                playerScores.put(key.charAt(0), 0);
            }
            
            for (int i = 0; i < levelsConfig.size(); i++) {
                JSONObject levelConfig = levelsConfig.getJSONObject(i);
                String backgroundPath = null;
                String layoutFile = null;
                String treeImage = null;
                String foreGroundColor = null;

                try {
                    backgroundPath = "src/main/resources/Tanks/" + levelConfig.getString("background");
                    layoutFile = levelConfig.getString("layout");
                    treeImage = "src/main/resources/Tanks/" + levelConfig.getString("trees");
                    foreGroundColor = levelConfig.getString("foreground-colour");
                } catch (RuntimeException e) {
                    System.out.println("");
                }

                Level level = new Level(app, this, layoutFile, backgroundPath, treeImage, foreGroundColor, tankColors);
                levels.add(level);
            }
        initializeCurrentLevel();
    }

    /**
     * Initializes the current level by calling 
     * {@link #initializeTerrain()} to set up terrain, 
     * Load the tanks
     * Saves a copy of the tanks that were present in the beginning of the game.
     * If the current level has tanks, sets the first tank in the list as the tank in play 
     * and starts the player's turn.
     * Otherwise, sets tankInPlay to null.
     * Sets the tankInPlay for the InputManager.
     * Sets the currentPlayerIndex to 0
     */
    public void initializeCurrentLevel() {
        Level currentLevel = getCurrentLevel();
        initializeTerrain();
        currentLevel.loadTanks();
        initialTanks = new ArrayList<>(getCurrentLevel().getTanks());
        if (!currentLevel.getTanks().isEmpty() || !(currentLevel.getTanks().size() == 1)) {
            setTankInPlay(getCurrentLevel().getTanks().get(0));
            startPlayerTurn();
        } else {
            tankInPlay = null;
        }
        inputManager.setTankInPlay(tankInPlay);
        currentPlayerIndex = 0;
    }

    /**
     * Loads the next level in the game.
     * If the game has ended, draws the end game screen.
     * Otherwise, increments the current level index and initializes the new current level.
     */
    private void loadNextLevel() {
        if (endGame()) {
            return;
        } else {
            currentLevelIndex++;
            if (currentLevelIndex >= levels.size()) {
                currentLevelIndex = 0;
            }
            initializeCurrentLevel();
        }
    }

    /**
     * Initializes the terrain for the current level.
     * Retrieves the terrain data from the current level.
     * Processes the terrain data to generate smooth heights.
     * Generates vertex coordinates for rendering.
     */
    private void initializeTerrain() {
        Level currentLevel = getCurrentLevel();
        int[][] terrain = currentLevel.getTerrainData();
        ProcessTerrain processTerrain = new ProcessTerrain(terrain, app);
        smoothHeights = processTerrain.smoothTerrain();
        processTerrain.generateVertexCoords();
        this.vertexCoords = processTerrain.getOriginalVertexCoords();
    }

    /**
     * Sets the index of the current player.
     * Retrieves the list of tanks for the current level.
     * Updates the current player index.
     * Sets the tank in play based on the updated index.
     * Adjusts the wind direction for the current level.
     *
     * @param index The index to set the current tankInPlay to.
     */
    public void setCurrentPlayerIndex(int index){
        List<Tank> tanks = getCurrentLevel().getTanks();
        this.currentPlayerIndex = index;
        setTankInPlay(tanks.get(index));
        getCurrentLevel().adjustWind();
    }

    /**
     * Checks for conditions triggering a transition to the next level.
     * If no firing action is detected, it transitions after a delay of 1000 milliseconds.
     * If a firing action is detected, it immediately transitions to the next level.
     * Resets the start time of the level transition after transitioning.
     */
    private void checkLevelTransition() {
        List<Tank> tanks = getCurrentLevel().getTanks();
        if (tanks.size() == 1 || tanks.isEmpty()) {
            if (!inputManager.getFiredStatus()) {
                if (levelTransitionStartTime == 0) {
                    levelTransitionStartTime = app.millis();
                }
                long elapsedTime = app.millis() - levelTransitionStartTime;
                if (elapsedTime >= 1000) {
                    loadNextLevel();
                    levelTransitionStartTime = 0;
                }

            } else {
                loadNextLevel();
                inputManager.setFiredStatus(false);
                levelTransitionStartTime = 0;
            }
        }
    }

    /**
     * Moves to the next active player in the game.
     * Initiates the wind adjustment for the current level and starts the turn for the new player.
     * Sets the tank in play for the input manager.
     */
    public void nextPlayer() {
        List<Tank> tanks = getCurrentLevel().getTanks();
        // System.out.println("Size of tanks" + tanks.size());
        if (!tanks.isEmpty()) {
            if (getCurrentLevel().removalStatus()) {
                currentPlayerIndex = (currentPlayerIndex) % tanks.size();
                tankInPlay = tanks.get(currentPlayerIndex);
                getCurrentLevel().setRemovalStatus(false);
            } else {
                currentPlayerIndex = (currentPlayerIndex + 1) % tanks.size();
                tankInPlay = tanks.get(currentPlayerIndex);
            }
            getCurrentLevel().adjustWind();
            startPlayerTurn();
        } 

        inputManager.setTankInPlay(tankInPlay);
    }

    /**
     * Retrieves all active players in the game.
     *
     * @return A map with the player characters mapped to each of their Tank objects.
     */
    private Map<Character, Tank> getAllPlayers() {
        Map<Character, Tank> activePlayers = new HashMap<>();
        for (Tank tank : initialTanks) {
            activePlayers.put(tank.getPlayerCharacter(), tank);
        }
        return activePlayers;
    }

    /**
     * Decrements the score of the player by the given amount.
     * If the resulting score is negative, it is set to zero.
     *
     * @param player The character representing the player.
     * @param score The amount to decrement the player's score.
     */
    public void decrementPlayerScore(Character player, int score) {
        int currentScore = getPlayerScore(player);
        int newScore = currentScore - score;
        playerScores.put(player, Math.max(newScore, 0));
    }

    /**
     * Adds the damage value to the score of the player.
     *
     * @param player The character representing the player.
     * @param damage The amount of damage to be converted into a score.
     */
    public void addScore(Character player, float damage) {
        Integer currentScore = playerScores.getOrDefault(player, 0);
        int score = (int) damage;
        playerScores.put(player, currentScore + score);
    }

    /**
     * Sorts the player scores by their descending order.
     *
     * @return A list of player scores sorted in descending order.
     */
    private List<Map.Entry<Character, Integer>> sortScores() {
        List<Map.Entry<Character, Integer>> scoreSorted = new ArrayList<>(playerScores.entrySet());
        Collections.sort(scoreSorted, Collections.reverseOrder(Map.Entry.comparingByValue()));
        return scoreSorted;
    }

    /**
     * Checks if the game has ended.
     * Considers the edge case where the last 2 tanks die at 
     * the same time leaving 0 tanks left in the game.
     * @return True if the game has ended, false otherwise.
     */
    public boolean endGame() {
        return ((currentLevelIndex == levels.size() - 1) && ((getCurrentLevel().getTanks().size() == 1) || (getCurrentLevel().getTanks().isEmpty())));
    }

    /**
     * Restarts the game by resetting key game parameters, clearing scores,
     * and re-initializing the game state to its initial state.
     */
    private void restartGame() {
        currentLevelIndex = 0;
        endGameIndex = 0;
        lastScoreTime = 0;
        playerScores.clear();
        currentPlayerIndex = 0;
        tankInPlay = null;
        initialTanks.clear();

        for (Tank tank : initialTanks) {
            parachuteCount.put(tank.getPlayerCharacter(), 3);
            playerScores.put(tank.getPlayerCharacter(), 0);
        }

        levels.clear();
        app.setup();

        for (Level level : levels) {
            level.resetTanks(); 
        }

        initializeCurrentLevel();
    }
    

    /**
    Retrieves the player with the highest score from the playerScores map.
    @return The character representing the player with the highest score.
    */
    private Character playerWithHighestScore() {
        Character winner = null;
        int highScore = Integer.MIN_VALUE;
        for (Map.Entry<Character, Integer> entry : playerScores.entrySet()) {
            Character player = entry.getKey();
            int score = entry.getValue();

            if (score > highScore) {
                highScore = score;
                winner = player;
            }
        }
        return winner;
    }

    /**
     * Draws the winning player's character on the screen.
     */
    private void drawWinner() {
        app.pushStyle();
        Character winner = playerWithHighestScore();
        Tank winningTank = getAllPlayers().get(winner);
        winningRGB = winningTank.getRGB();
        app.fill(winningRGB[0], winningRGB[1], winningRGB[2], 250);
        app.textSize(30);
        app.text("Player " + winner + " wins!", app.WIDTH/2 - 50, app.HEIGHT / 2 - 140);
        app.popStyle();
    }

    /**
     * Draws the announcement box on the screen.
     */
    private void drawBox() {
        app.pushStyle();
        app.fill(winningRGB[0], winningRGB[1], winningRGB[2], 90);
        app.strokeWeight(3);
        app.rect(BOX_X, BOX_Y, BOX_WIDTH, BOX_HEIGHT);
        app.rect(BOX_X, BOX_Y - 50, BOX_WIDTH, 50);
        app.popStyle();
    }
    

    /**
     * draws the final scores in descending order with 0.7 
     * between each subsequent player
     */
    private void drawScores() {

        app.pushStyle();
        
        Map<Character, Tank> allPlayers = getAllPlayers();


        List<Map.Entry<Character, Integer>> sortedScores = sortScores();
        app.textSize(24);
        app.fill(0,0,0);
        app.text("Final Scores", BOX_X + 100, BOX_Y - 30);

        long currentTime = app.millis();

        if (currentTime - lastScoreTime >= 700) {
            if (endGameIndex < sortedScores.size()) {
                endGameIndex++;
                lastScoreTime = currentTime;
            }
        }
        
        int yIncrementer = 0;
        for (int i = 0; i < endGameIndex; i++) {
            Map.Entry<Character, Integer> entry = sortedScores.get(i);
            Character player = entry.getKey();
            int score = entry.getValue();

            if (allPlayers.containsKey(player)) {
                Tank tank = allPlayers.get(player);
                int[] rgb = tank.getRGB();
                app.fill(rgb[0], rgb[1], rgb[2], 250);

                int textY = (app.HEIGHT / 2 - 40) + yIncrementer;

                app.text("Player " + player, TEXT_X, textY);
                app.text(score, TEXT_X + 300, textY);
                yIncrementer += 50;
            }
        }
        app.popStyle();
    }

    /**
     * Draws an arrow pointing at the specified position.
     *
     * @param x The x-coordinate of the arrow's base.
     * @param y The y-coordinate of the arrow's base.
     */
    private void drawArrow(float x, float y) {
        int arrowLength = 70; // Length of the arrow in pixels
        int arrowHeadSize = 12; // Size of the arrowhead sides

        app.stroke(0); // Set color to black
        app.strokeWeight(3);

        // line
        app.line(x, y - arrowLength, x, y);

        // Draw the arrowhead at the bottom
        app.line(x, y, x - arrowHeadSize, y - arrowHeadSize); // left
        app.line(x, y, x + arrowHeadSize, y - arrowHeadSize); // right
    
    }

    /**
     * Draws the non-end game scoreboard displaying the scores of players.
     * The scoreboard includes player character and their respective scores.
     */
    private void drawScoreBoard() {
        app.noFill();
        app.strokeWeight(3);
        app.rect(app.WIDTH/100 * 80, app.HEIGHT/100 * 12, 200, 26);
        app.textSize(22);
        app.textAlign(App.CENTER, App.CENTER);
        app.text("Scores", (app.WIDTH/100) * 85, (app.HEIGHT/100) * 14);
        app.rect(app.WIDTH/100 * 80, app.HEIGHT/100 * 16, 200, 130);

        int index = 0;
        int x = app.WIDTH / 100 * 91;
        int y = app.HEIGHT / 100 * 18;
        int height = 25;             
        int spacing = 30; 
        app.textSize(22);

        // do this because if not all the other characters will show up
        Map<Character, Tank> activePlayers = getAllPlayers();

        for (Map.Entry<Character, Integer> entry : playerScores.entrySet()) {

            if (activePlayers.containsKey(entry.getKey())) {
                Tank tank = activePlayers.get(entry.getKey());
                int[] rgb = tank.getRGB();
                app.fill(rgb[0], rgb[1], rgb[2]);
                int yPos = y + (spacing * index);

                // Player identifier in color
                app.text("Player " + entry.getKey() + ":", x - 40, yPos);

                // Score in black
                app.fill(0); // Set color to black for the score
                app.text(entry.getValue().toString(), x + 60, yPos);

                index++;
            }
        }
    }

    /**
     * Handles key presses.
     * 
     * @param keyCode The keycode of the pressed key.
     */
    public void keyPressed(int keyCode) {
        inputManager.keyPressed(keyCode);

         if (keyCode == 82) {
             if (endGame()) {
                restartGame();
            } else {
                getCurrentLevel().getTanks().get(currentPlayerIndex).increaseHealth();
            }
        }

        else if (keyCode == 70) {
            getCurrentLevel().getTanks().get(currentPlayerIndex).increaseFuel();

            // delete this  
        } 
    }

    /**
     * Receives key releases event and sends it to the InputManager.
     * 
     * @param keyCode The keycode of the pressed key.
     */
    public void keyReleased(int keyCode) {
        inputManager.keyReleased(keyCode);
    }

    /**
    * Receives the mouse click event and sends it to the InputManager.
    *
    * @param mouseX The x-coordinate of the mouse click position.
    * @param mouseY The y-coordinate of the mouse click position.
    */
    public void mouseClicked(int mouseX, int mouseY) {
        inputManager.mouseClicked(mouseX, mouseY);

    }


    /**
     * Updates the game state every frame by processing input, 
     * updating the level display, showing the arrow for the current player's turn, 
     * checking for level transitions, 
     * displaying the scoreboard or end game scoreboard accordingly.
     */
    public void updateGame() {
            inputManager.update();
            getCurrentLevel().display(inputManager.getDeltaTime());
            if (showArrow && (app.millis() - arrowTimeStart < arrowDuration)) {
                drawArrow(tankInPlay.getPosition().x, tankInPlay.calculateTankY() - 50);
            } else {
                showArrow = false;
            }
            checkLevelTransition();
            if (inputManager.getFiredStatus()) {
                nextPlayer();
                inputManager.setFiredStatus(false);
            }

            if (endGame()) {
                drawWinner();
                drawBox();
                drawScores();
            } else {
                drawScoreBoard();
            }
        }

}