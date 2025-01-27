package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.*;

/**
 * The Level class constitutes a level in the Tanks game.
 * Contains information about the terrain, background, trees, tank colors, 
 * player start positions, and wind force.
 */
public class Level {
    private App app;
    private PImage background;
    private int[][] terrain;
    private String layoutFile;
    private PImage treeImage;
    private String foreGroundColor;
    private int[] RGBForeGroundColor = new int[3];
    private HashMap<Character, PVector> playerStartPositions;
    private HashMap<Character, String> tankColors;
    private List <PVector> vertexCoords;
    private List <Tank> tanks = new ArrayList<Tank>();
    private List<PVector> treePositions;
    private static final int TREE_IMG_HORIZONTAL_OFFSET = -15;
    private static final int TREE_IMG_VERTICAL_OFFSET = -30;
    private float[] smoothHeights;
    private List<PVector> updatedTreePositions = new ArrayList<PVector>();
    private GameManager gameManager;
    private Random random = new Random();
    private float windForce;
    private PImage windImage;
    private boolean wasRemoval;
    
    /**
     * Constructs a Level object.
     * 
     * @param app             The PApplet App instance.
     * @param gameManager     The GameManager instance managing the game.
     * @param layoutFile      The file path to the level layout.
     * @param backgroundPath  The file path to the background image.
     * @param treeImagePath   The file path to the tree image.
     * @param foreGroundColor The foreground color of the level.
     * @param tankColors      A map of tank characters to their respective colors.
     */
    public Level(App app, GameManager gameManager, String layoutFile, String backgroundPath, String treeImagePath, String foreGroundColor, HashMap<Character, String> tankColors) {
        this.app = app;
        this.gameManager = gameManager;
        this.layoutFile = layoutFile;
        this.background = (backgroundPath != null) ? app.loadImage(backgroundPath) : null;
        if (treeImagePath != null) {
            this.treeImage = app.loadImage(treeImagePath);
        }
        this.foreGroundColor = foreGroundColor;
        this.tankColors = tankColors;
        this.playerStartPositions = new HashMap<>();
        this.treePositions = new ArrayList<>();
        this.vertexCoords = new ArrayList<PVector>();
        this.loadTerrain(layoutFile);
        this.windForce = random.nextFloat() * 70 - 35;
        this.windImage = (app != null) ? app.loadImage("src/main/resources/Tanks/wind.png") : null;
    }

    public int[][] getTerrainData() {
        return terrain;
    }

    public int[] getRGBForeGroundColor() {
        return RGBForeGroundColor;
    }

    // for testing
    public void addTank(Tank tank) {
        tanks.add(tank);
    }

    public boolean removalStatus() {
        return wasRemoval;
    }

    public void setRemovalStatus(boolean status) {
        wasRemoval = status;
    }

    public void setVertexCoordinates(List <PVector> vertexCoords) {
        this.vertexCoords = vertexCoords;
    }

    public float getVertexHeightAtX(float x) {
        return (vertexCoords.get((int)x)).y;
    }

    public List<PVector> getTreePositions() {
        return treePositions;
    }

    public List<PVector> getVertexCoordinates() {
        return vertexCoords;
    }

    public void resetTanks() {
        tanks.clear();
    }

    public void setTanks(Tank tank) {
        tanks.add(tank);
    }

    /**
     * Retrieves a copy of the list of tanks currently in the game.
     * This method accesses the list of tanks without modifying the original list, preventing
     * accidental modifications to the primary list of tanks.
     *
     * @return A new copy of {@code List<Tank>} containing all the tanks currently managed in this level.
     */
    public List<Tank> getTanks() {
        return new ArrayList<>(tanks);
    }

    /**
     * Retrieves the current wind force value.
     *
     * @return The current wind force as a float, with positive values
     * represent wind blowing to the right and 
     * negative values represent wind blowing to the left.
     */
    public float getWindForce() {
        return this.windForce;
    }

    /**
     * Adjusts the wind force with a randomly generated number
     * within the range of - 5 to +  (both inclusive)
     */
    public void adjustWind() {
        float adjustment = random.nextFloat() * 10 - 5;
        windForce += adjustment;
    }

    /**
     * Finds the maximum number of columns among the lines of terrain data.
     *
     * @param lines The array of strings representing the terrain layout.
     * @return The maximum number of columns among the lines of terrain data.
     */
    private int findMaxColumns(String[] lines){
        int max_columns = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() > max_columns) {
                max_columns = lines[i].length();
            }
        }
        return max_columns;
    }

    /**
     * Loads the terrain layout from the given file path and initializes the terrain data.
     * The terrain layout data is read from the txt file.
     * The logic assigns numeric values to represent different terrain features,
     * and populates the terrain array accordingly. 
     * Tree positions is marked in the new terrain array as 3
     * The height of each x pixel's y-coordinate is marked by 2
     * The player start position is saved in a PVector scaled by each cellsize.
     * 
     * @param layoutFile The file path containing the terrain layout data.
     * 
     */
    private void loadTerrain(String layoutFile){
        String[] lines = app.loadStrings(this.layoutFile);
        int maxColumns = findMaxColumns(lines);

        this.terrain = new int[20][maxColumns];

        for (int i = 0; i < lines.length; i++) {
            for (int j = 0; j < lines[i].length(); j++) {
                char ch = lines[i].charAt(j);
                switch (ch) {
                    case 'X':
                        for (int z = i; z < terrain.length; z++) {
                            terrain[z][j] = 1;
                        }
                        terrain[i][j] = 2;
                        break;

                    case 'T':
                        treePositions.add(new PVector(j * App.CELLSIZE, i * App.CELLSIZE));
                        terrain[i][j] = 3;
                        break;
                    
                    
                    default:
                        if (Character.isLetter(ch) || Character.isDigit(ch)) {
                            playerStartPositions.put(ch, new PVector(j* App.CELLSIZE, i * App.CELLSIZE));
                        }
                        break;
                }
            }
            
        }
    }

    /**
     * Parses the foreground color string and stores it into RGB format, delimited by commas.
     * Converts each component to an integer and stores it in the rgbforeGroundColor array.
     * 
     * @throws NumberFormatException if the foreground color components cannot be parsed as integers.
     */
    private void loadForeGround() {
    String[] components = foreGroundColor.split(",");
        try {
            for (int i = 0; i < components.length; i++) {
                RGBForeGroundColor[i] = Integer.parseInt(components[i].trim());
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Failed to load foreground color");
        }
    }

    /**
     * Stores the Y coordinates of the present position of all tanks in the level.
     * 
     * @return An array of floats containing the Y coordinates of the present position of all tanks.
     */
    public float[] storeCoordinates() {
        float[] tankCoordinates = new float[tanks.size()];
        for (int i = 0; i < tanks.size(); i++) {
            tankCoordinates[i] = tanks.get(i).calculateTankY();
        }
        return tankCoordinates;
    }

    /**
     * Modifies the terrain within certain boundaries of the impact point determned by the explosion radius
     * Calculates a semicircle to determine the new heights(y) of the impacted x coordinates
     * For each x in the boundary range determine if the height of the terrain is above the y coordinate - semicircle of the impact point
     * but also below the impact point's y coordinate + semicircle
     * set new height to be point of impact minus semicircle 
     * if height is greater than point of impact + semicircle
     * subtract a full circle from the height
     * formula provided from: https://edstem.org/au/courses/14913/discussion/1878721?comment=4343519
     * 
     * Method also calls storeCoordinates to store the coordinates of each tank's present Y coordinate
     * before explosion and after explosion to determine if the terrain was modified
     * 
     * Sets the before and after coordinates for each tank for use within the tank class
     * 
     * @param xImpact      The x-coordinate of the impact point.
     * @param yImpact      The y-coordinate of the impact point.
     * @param radius       The radius of the explosion.
     * @param tankShooter  The tank that caused the explosion.
     */
    public void modifyTerrainAt(float xImpact, float yImpact, float radius, Tank tankShooter) {
        float[] beforeExplosionCoordinates = storeCoordinates();

        int lowRange = Math.max(0, (int) (xImpact - radius));
        int highRange = Math.min(vertexCoords.size() - 1, (int) (xImpact + radius));
        boolean terrainChanged = false;

        for (int i = lowRange; i <= highRange; i++) {

            float distanceX = Math.abs(i - xImpact);

            if (distanceX <= radius) {
                float semicircleHeight = (float) Math.sqrt(radius * radius - distanceX * distanceX);
                float newHeight = (yImpact + semicircleHeight);
                float upperBound = yImpact - semicircleHeight;
                
                
                PVector terrainPoint = vertexCoords.get(i);
                if (i >= 0 && i < smoothHeights.length) {
                    if (terrainPoint.y < newHeight && terrainPoint.y > upperBound) {
                        terrainPoint.y = newHeight;
                        smoothHeights[i] = (app.HEIGHT - terrainPoint.y) / app.CELLSIZE;
                        terrainChanged = true;
                    } else if (terrainPoint.y < upperBound) {
                        terrainPoint.y += 2 * semicircleHeight;
                        smoothHeights[i] = (app.HEIGHT - terrainPoint.y) / app.CELLSIZE;
                        terrainChanged = true;
                    }
                }
            }

        if (terrainChanged) {
            float[] afterExplosionCoordinates = storeCoordinates();
            for (int j = 0; j < tanks.size(); j++) {
                if (beforeExplosionCoordinates[j] != afterExplosionCoordinates[j]) {
                    tanks.get(j).setBeforeExplosionY(beforeExplosionCoordinates[j]);
                    tanks.get(j).setAfterExplosionY(afterExplosionCoordinates[j]);
                    tanks.get(j).falling();
                    tanks.get(j).setShooter(tankShooter);
                }
            }
        }
        }
    }

    /**
     * Draws the terrain of the game by creating a filled shape based on the smoothHeight values.
     * Calculates the Y height of each x across the board.
     * Draws vertex coordinates for each point on x axis
     * Close off the shape to ensure the terrain is fully filled.
     * 
     * The terrain is drawn without an outline (stroke) and filled with the foreground color retrieved from the json file.
     * This method relies on gameManager and processTerrain to properly process and populate the
     * the {@code smoothHeights} and {@code vertexCoords}.
     * 
    */
    public void drawSmoothTerrain() {
        this.smoothHeights = gameManager.getSmoothHeights();
        this.vertexCoords = gameManager.getVertexCoordinates();

        app.noStroke();
        loadForeGround();
        app.fill(RGBForeGroundColor[0],RGBForeGroundColor[1], RGBForeGroundColor[2]);

        app.beginShape();

        for (int j = 0; j < smoothHeights.length; j++) {
            // Calculate x and y coordinates
            float x = j;  // Scales the x-coordinate across the canvas width
            float y = app.HEIGHT - smoothHeights[j] * App.CELLSIZE;  // Scale and invert the height to draw from the bottom up

            app.vertex(x, y);  // Add vertex point to the shape
        }


        app.vertex(app.width, app.height);
        app.vertex(0, app.HEIGHT);
        app.endShape();
    }


    /**
     * Retrieves the height of the terrain at a specified x-coordinate.
     * This method accesses the {@code smoothHeights} array, which holds the processed heights of the terrain
     * and returns the height at the given x-coordinate.
     *
     * @param x The x-coordinate whose height is to be determined.
     * @return The height of the terrain at the specified x-coordinate.
     * @throws ArrayIndexOutOfBoundsException if the x value is outside the bounds of the {@code smoothHeights} array.
     */
    public float getTerrainHeightAtX(float x) {
        if (x < 0 || x >= smoothHeights.length) {
            throw new IllegalArgumentException("x-coordinate is out of bounds");
        }

        return smoothHeights[(int)x];
    }


    // !!!  one of the testcases should be for drawTrees and whether the original x +-30px
    /**
     * Display trees on the terrain using the provided png image.
     * 
     * This method checks if a tree image is not null and then iterates over a list of tree positions
     * determined by the loadTerrain method.
     * 
     * Each time the function is called it recalculates the tree's Y coordinate with the updated list of
     * smoothHeights array, to ensure that the trees remain at the top of the terrain.
     * The tree positions are updated and stored in {@code updatedTreePositions}.
     * 
     * Assumes that {@code treePositions} contains the coordinates of the tree, and {@code smoothHeights}
     * provides the terrain height at corresponding column.
     *
     *
     * @throws NullPointerException if {@code treeImage} is null, indicates that the image was not loaded properly.
     */
    private void drawTrees() {
        if (treeImage != null) {
            updatedTreePositions.clear(); 

            for (PVector treePos : treePositions) {
                int x = (int) treePos.x;  // x-coordinate of the tree
                int colIndex = x;  // Column index based on x-coordinate
                float terrainHeight = smoothHeights[colIndex];  // Height of terrain at the column
                float treeBaseY = app.height - terrainHeight * App.CELLSIZE;  // Calculate the base y-coordinate for the tree

                float newX = treePos.x;

                PVector updatedTreePos = new PVector(newX, treeBaseY);
                updatedTreePositions.add(updatedTreePos);

                app.image(treeImage, treePos.x + TREE_IMG_HORIZONTAL_OFFSET, treeBaseY + TREE_IMG_VERTICAL_OFFSET, App.CELLSIZE, App.CELLSIZE);
            }
        } else {
            throw new NullPointerException("No tree Image available to load");
        }
    }

    /**
     * Renders the current wind force.
     * This method displays the current value of the wind force.
     * It also displays an image of wind to enhance GUI.
     *
     */
    private void displayWind() {
        app.image(windImage, (app.WIDTH/100) * 90, (app.HEIGHT/100), app.CELLSIZE*2, app.CELLSIZE*2);
        app.fill(0);
        app.textSize(24);
        app.text(Math.round(getWindForce()), (app.WIDTH/100) * 102, (app.HEIGHT/100) * 6);
    }

    /**
     * Initializes and loads tanks for each level.
     * This method is called once per level during initialization to populate the game with tanks.
     * It iterates over each entry in the {@code playerStartPositions} map, where each entry consists
     * of a character representing the player and a corresponding {@link PVector} storing the position of the tank.
     * 
     * For each player, a new {@link Tank} object is created.
     * 
     * These tanks are then added to the {@code tanks} list.
     * 
     * It relies on {@code playerStartPositions} to provide starting coordinates and {@code tankColors} to assign
     * colors to the tanks based on their associated character.
     */
    public void loadTanks() {
        for (Map.Entry<Character, PVector> entry : playerStartPositions.entrySet()) {
            Character player = entry.getKey();
            PVector position = entry.getValue();
            String color = tankColors.get(player);

            Tank newTank = new Tank(app, gameManager, player, position, color, this);
            tanks.add(newTank);
        }

    }

    /**
     * Iterates over the list of tanks, removing those that are marked for removal from the game. 
     * Uses an iterator to safely modify the list during iteration. If a tank is removed, the method
     * adjusts the game manager's current player index to maintain correct turn order. 
     * 
     * Specifically, if a tank before the current player is removed, 
     * the current player index is decremented. If the current 
     * player's tank is removed, 
     * the method calls on gameManager's nextPlayer method.
     *
     * This method is called on every frame before drawing the tanks.
     * 
     */
    private void removeDeadTanks(){
        Iterator<Tank> iterator = tanks.iterator();
        while (iterator.hasNext()) {
            Tank tank = iterator.next();
            int index = tanks.indexOf(tank);
            if (tank.isMarkedForRemoval()) {
                wasRemoval = true;
                iterator.remove();

                int currentPlayerIndex = gameManager.getCurrentPlayerIndex();
                if (index < currentPlayerIndex) {
                    gameManager.setCurrentPlayerIndex(currentPlayerIndex - 1);
                } else if (index == currentPlayerIndex) {
                    gameManager.nextPlayer();
                }
            }
        }
    }

    /**
     * Handles the impact of a projectile on tanks within the explosion radius.
     * This method calculates the distance from the impact point to each tank and applies damage if the
     * tank is within the explosion radius. 
     * 
     * It also handles awarding scoring to the shooter if they damage other tanks
     * (not self-inflicted damage) and deducting health from impacted tanks.
     * 
     * Tanks that are marked for removal after taking damage are collected
     * and removed from the game.
     *
     * @param projectile The projectile that has impacted.
     * @param projectilePosition The position of the projectile at the moment of impact.
     */
    public void handleImpact(Projectile projectile, PVector projectilePosition) {
        List<Tank> tanksToRemove = new ArrayList<Tank>();
        for (Tank tank : tanks) {
            float distance = projectile.calculateDistanceFromImpact(projectilePosition.x, projectilePosition.y, tank);
            if (distance <= projectile.getRadiusTerrainExplosion()) {
                float damage = projectile.calculateDamage(distance);
                // can gain damage from hurting themselves
                tank.applyDamage(damage);
                // cannot gain score for hurting themselves
                if (damage > 0 && tank != projectile.getShooter()) {
                    gameManager.addScore(projectile.getShooter().getPlayerCharacter(), damage);
                }
                if (tank.isMarkedForRemoval()) {
                    tanksToRemove.add(tank);
                }
            }  
        }
        tanksToRemove.forEach(tank -> removeDeadTanks());
    }

    /**
     * This method iterates through all tanks in the level. 
     * If a tank is determined to be under the terrain or has health less than or equal to 0,
     * it is added to a list of tanks to be removed. 
     * After iterating through all tanks, the method calls removeDeadTanks 
     * to remove all the tanks in the removal list.
     */
    private void handleDeadTanks() {
        List<Tank> tanksToRemove = new ArrayList<Tank>();
        for (Tank tank : tanks) {
            if (tank.terrainUnder() || tank.getHealth() <= 0) {
                tanksToRemove.add(tank);
            }
        }
        tanksToRemove.forEach(tank -> removeDeadTanks());
    }


    /**
     * Draws all tanks in the level.
     * This method first creates a copy of the list of tanks to ensure that modifications
     * during iteration do not affect the original list. This avoids concurrent modification exception.
     * Then, it calls the method
     * {@link #handleDeadTanks()} to handle dead tanks. 
     * Lastly, it iterates through the copied list of tanks and calls the draw method for each tank.
     *
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */
    private void drawTanks(float deltaTime) {
        List<Tank> tanksToDraw = new ArrayList<>(tanks);
        handleDeadTanks();
        for (Tank tank : tanksToDraw) {
            tank.draw(deltaTime);
        }
    }

    /**
     * Draws the level components including background, terrain, trees, tanks, and wind.
     *
     * @param deltaTime The time interval between two consecutive frames, in milliseconds.
     */
    public void display(float deltaTime) {
        app.image(this.background, 0, 0);
        drawSmoothTerrain();
        if (treeImage != null) {
            drawTrees();
        }
        drawTanks(deltaTime);
        displayWind();
    }

}
