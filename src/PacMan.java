import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {

    // Block class to represent walls and other solid objects
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U = Up, D = Down, L = Left, R = Right
        int velocityX = 0;
        int velocityY = 0;

        // Static reference to walls for collision detection
        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        // Update direction and handle collision with walls
        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        // Update velocity based on direction
        void updateVelocity() {
            int speed = tileSize / 4;
            if (powerModeTimer > 0) {
                speed = tileSize / 8;
            }

            switch (this.direction) {
                case 'U':
                    this.velocityX = 0;
                    this.velocityY = -speed;
                    break;
                case 'D':
                    this.velocityX = 0;
                    this.velocityY = speed;
                    break;
                case 'L':
                    this.velocityX = -speed;
                    this.velocityY = 0;
                    break;
                case 'R':
                    this.velocityX = speed;
                    this.velocityY = 0;
                    break;
            }
        }

        // Reset position to starting point
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    // PowerPellet class to handle power pellets
    class PowerPellet {
        int x, y, width, height;
        long blinkTimer = 0;
        boolean visible = true;

        // Constructor for PowerPellet
        PowerPellet(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 12;
            this.height = 12;
        }

        // Update method to handle blinking effect
        void update() {
            blinkTimer++;
            if (blinkTimer % 20 == 0) {
                visible = !visible;
            }
        }
    }

    private final int rowCount = 21;
    private final int columnCount = 19;
    private final int tileSize = 32;
    private final int boardWidth = columnCount * tileSize;
    private final int boardHeight = rowCount * tileSize;

    private final Image wallImage;
    private final Image blueGhostImage;
    private final Image orangeGhostImage;
    private final Image pinkGhostImage;
    private final Image redGhostImage;
    private final Image scaredGhostImage;

    private final Image pacmanUpImage;
    private final Image pacmanDownImage;
    private final Image pacmanLeftImage;
    private final Image pacmanRightImage;

    private final String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X*                X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X*               *X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    ArrayList<PowerPellet> powerPellets;
    Block pacman;

    Timer gameLoop;
    char[] directions = { 'U', 'D', 'L', 'R' };
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    boolean gameWon = false;

    // Enhanced features
    int highScore = 0;
    int level = 1;
    int powerModeTimer = 0;
    int ghostEatenScore = 200;
    long gameStartTime;
    boolean soundEnabled = true;
    boolean paused = false;
    int animationFrame = 0;

    // UI Colors
    private final Color backgroundColor = new Color(0, 0, 0);
    private final Color wallColor = new Color(0, 0, 255);
    private final Color foodColor = new Color(255, 255, 0);
    private final Color powerPelletColor = new Color(255, 255, 255);
    private final Color textColor = new Color(255, 255, 255);
    private final Color uiColor = new Color(255, 255, 0);

    // Constructor to initialize the game
    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight + 60));
        setBackground(backgroundColor);
        addKeyListener(this);
        setFocusable(true);

        // Load images for walls, ghosts, and Pacman
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./scaredGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        gameStartTime = System.currentTimeMillis();
        loadMap();
        startGame();
    }

    // Start the game loop
    public void startGame() {
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    // Load the map and initialize game objects
    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();
        powerPellets = new ArrayList<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tileMapChar = tileMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tileMapChar) {
                    case 'X':
                        walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                        break;
                    case 'b':
                        ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'o':
                        ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'p':
                        ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'r':
                        ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                        break;
                    case ' ':
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    case '*':
                        powerPellets.add(new PowerPellet(x + 10, y + 10));
                        break;
                    case 'P':
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                        break;
                }
            }
        }
    }

    // Paint the game components
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    // Draw the game components
    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            if (powerModeTimer > 0) {
                g.drawImage(scaredGhostImage, ghost.x, ghost.y, ghost.width, ghost.height, null);
            } else {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(foodColor);
        for (Block food : foods) {
            g.fillOval(food.x, food.y, food.width, food.height);
        }

        g.setColor(powerPelletColor);
        for (PowerPellet pellet : powerPellets) {
            if (pellet.visible) {
                g.fillOval(pellet.x, pellet.y, pellet.width, pellet.height);
            }
        }

        drawUI(g);
    }

    // Draw the UI elements like score, lives, and game status
    private void drawUI(Graphics g) {
        g.setColor(textColor);
        g.setFont(new Font("Arial", Font.BOLD, 16));

        // Top UI bar
        String livesText = "Lives: " + lives;
        String scoreText = "Score: " + score;
        String levelText = "Level: " + level;
        String highScoreText = "High Score: " + highScore;

        g.drawString(livesText, 10, 20);
        g.drawString(scoreText, 120, 20);
        g.drawString(levelText, 250, 20);
        g.drawString(highScoreText, 350, 20);

        // Power mode indicator
        if (powerModeTimer > 0) {
            g.setColor(Color.CYAN);
            g.drawString("POWER MODE: " + (powerModeTimer / 20), 10, boardHeight + 40);
        }

        // Game over screen
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("GAME OVER", boardWidth / 2 - 100, boardHeight / 2);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Press any key to restart", boardWidth / 2 - 80, boardHeight / 2 + 40);
            g.drawString("Final Score: " + score, boardWidth / 2 - 60, boardHeight / 2 + 60);
        }

        // Game won screen
        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("LEVEL COMPLETE!", boardWidth / 2 - 120, boardHeight / 2);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Press any key to continue", boardWidth / 2 - 80, boardHeight / 2 + 40);
        }

        // Pause screen
        if (paused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("PAUSED", boardWidth / 2 - 60, boardHeight / 2);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Press 'P' to resume", boardWidth / 2 - 60, boardHeight / 2 + 40);
        }
    }

    // Move Pacman and handle game logic
    public void move() {
        if (paused)
            return;

        animationFrame++;

        // Update power pellets
        for (PowerPellet pellet : powerPellets) {
            pellet.update();
        }

        // Update power mode timer
        if (powerModeTimer > 0) {
            powerModeTimer--;
        }

        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Wall collision for pacman
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Ghost movement and collision
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (powerModeTimer > 0) {
                    score += ghostEatenScore;
                    ghostEatenScore *= 2;
                    ghost.reset();
                } else {
                    lives--;
                    if (lives == 0) {
                        gameOver = true;
                        if (score > highScore) {
                            highScore = score;
                        }
                        return;
                    }
                    resetPositions();
                }
            }

            // Ghost AI improvement
            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth ||
                        ghost.y <= 0 || ghost.y + ghost.height >= boardHeight) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    ghost.updateDirection(directions[random.nextInt(4)]); // Change direction randomly
                }
            }
        }

        // Check for food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        // Check for power pellet collision
        PowerPellet pelletEaten = null;
        for (PowerPellet pellet : powerPellets) {
            if (pacman.x < pellet.x + pellet.width && pacman.x + pacman.width > pellet.x &&
                    pacman.y < pellet.y + pellet.height && pacman.y + pacman.height > pellet.y) {
                pelletEaten = pellet;
                score += 50;
                powerModeTimer = 300;
                ghostEatenScore = 200;
            }
        }
        if (pelletEaten != null) {
            powerPellets.remove(pelletEaten);
        }

        // Check win condition
        if (foods.isEmpty() && powerPellets.isEmpty()) {
            gameWon = true;
            level++;
        }
    }

    // Check for collision between two blocks
    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x &&
                a.y < b.y + b.height && a.y + a.height > b.y;
    }

    // Reset positions of Pacman and ghosts
    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.updateDirection(directions[random.nextInt(4)]);
        }
        powerModeTimer = 0;
    }

    // Restart the game
    public void restartGame() {
        loadMap();
        resetPositions();
        lives = 3;
        score = 0;
        level = 1;
        gameOver = false;
        gameWon = false;
        paused = false;
        powerModeTimer = 0;
        gameStartTime = System.currentTimeMillis();
        if (!gameLoop.isRunning()) {
            gameLoop.start();
        }
    }

    // Handle next level logic
    public void nextLevel() {
        loadMap();
        resetPositions();
        gameWon = false;
        powerModeTimer = 0;
        if (gameLoop.getDelay() > 30) {
            gameLoop.setDelay(gameLoop.getDelay() - 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            restartGame();
            return;
        }

        if (gameWon) {
            nextLevel();
            return;
        }

        // Handle key events for Pacman movement and game controls
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                pacman.updateDirection('U');
                break;
            case KeyEvent.VK_DOWN:
                pacman.updateDirection('D');
                break;
            case KeyEvent.VK_LEFT:
                pacman.updateDirection('L');
                break;
            case KeyEvent.VK_RIGHT:
                pacman.updateDirection('R');
                break;
            case KeyEvent.VK_P:
                paused = !paused;
                break;
            case KeyEvent.VK_R:
                restartGame();
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }

        // Update Pacman's image based on direction
        switch (pacman.direction) {
            case 'U':
                pacman.image = pacmanUpImage;
                break;
            case 'D':
                pacman.image = pacmanDownImage;
                break;
            case 'L':
                pacman.image = pacmanLeftImage;
                break;
            case 'R':
                pacman.image = pacmanRightImage;
                break;
        }
    }
}
