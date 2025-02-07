Description of Project:
A game in Java using the Processing library for graphics and gradle as a dependency manager. This project hopes to demonstrate the use of object-oriented programming (OOP) design principles. In the game, players control tanks which can aim and fire at each other. Players gain score for hitting another player’s tank, causing them to lose health. The game has 3 different levels. After all levels are completed, the player with the highest carry-over score wins. The game can then be restarted by pressing "R" on the keyboard.

To run the game:
1. Ensure you have java installed:
    - Recommended: java 8.0.402-amzn
2. Ensure Gradle is installed on your system.
3. Clone the repository by running "git clone https://github.com/superpiggyng/A-Java-Tank-Game"
4.
   - Run the game with "gradle run" if gradle is installed on your system
   - Run the game with "./gradlew run" instead of gradle build if gradle is not installed on your system.

Encapsulation and Single Responsibility Principle:
The benefits of encapsulation became clear when I needed to change the terrain processing logic late in the project. Since the terrain-related code was contained within the ProcessTerrain class, I could make changes without affecting other parts of the program. This showed me the importance of keeping related functionality together allowing for ease of management and future improvements to the code.

The InputManager class is responsible solely for handling input events and delegating the corresponding actions to the Tanks class. The Tank class, in turn, is responsible for executing the actions based on the input commands.

The GameManager class encapsulates the game's state and logic, including information about levels, players, and gameplay mechanics.

The Level class encapsulates level-specific information, such as terrain, background, trees, tank colors, player start positions, and wind force.

The ProcessTerrain class encapsulates the terrain processing logic.

The Projectile class encapsulates the logic for projectile mechanisms. It also handles the creation and disposal of Explosion objects.

Inheritance and Polymorphism:
As an example, my program utilizes inheritance and polymorphism by implementing the falling abstract class. The FreeFalling and ParachuteFalling classes “is-a” type of falling, providing their own specific implementations, while also having access to the behaviors of the parent class. For example, the parachuteFalling class includes behavior specific to parachute deployment, that the freeFalling class does not.

Enums
The command enum defines a fixed set of possible commands for a tank. InputManager sets the state of the tank's command based on user input, and the Tank class continuously checks for the command state in each frame/loop to execute the corresponding actions promptly.

Anonymous classes:
The use of anonymous classes in my program allowed me to encapsulate small, specific functionality within a concise code block, removing the need for separate class declarations.

Getters and setters:
Initially, I used protected variables but soon realized the repercussions of this approach. By not using getters and setters, I frequently modified data that was not intended to be changed. It also made it challenging to track modifications over time. Implementing getters and setters allowed me to better manage the information in variables.

Finally, I saw how global variables persist throughout the program, while function variables reset each time the function is called, allowing me to better understand them.

Extensions:
Sound effects: The sound effects extension for tank turret movement, tank movement, explosions, parachute deployment, powerup sounds and background music.

Teleportation Feature: The player clicks on the screen to select a destination and presses 'T' to teleport the tank, if they have enough score, with the cost being 15. The teleportation destination is determined by the y-coordinate of the player's click, ensuring that the tank lands at the corresponding height on the terrain. If the player selects a coordinate that falls outside the bounds of the game board, the tank's position is adjusted to the nearest rightmost or leftmost edge.

