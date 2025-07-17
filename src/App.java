import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class App {
    private static PacMan pacmanGame;
    private static JFrame frame;

    public static void main(String[] args) throws Exception {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize + 60;

        frame = new JFrame("Pac-Man Game");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.BLACK);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.BLACK);

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setForeground(Color.WHITE);

        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> {
            if (pacmanGame != null) {
                pacmanGame.restartGame();
            }
        });

        JMenuItem pauseGame = new JMenuItem("Pause/Resume");
        pauseGame.addActionListener(e -> {
            if (pacmanGame != null) {
                pacmanGame.paused = !pacmanGame.paused;
            }
        });

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        gameMenu.add(newGame);
        gameMenu.add(pauseGame);
        gameMenu.addSeparator();
        gameMenu.add(exit);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(Color.WHITE);

        JMenuItem controls = new JMenuItem("Controls");
        controls.addActionListener(e -> {
            String controlsText = "Controls:\n" +
                    "Arrow Keys - Move Pac-Man\n" +
                    "P - Pause/Resume Game\n" +
                    "R - Restart Game\n" +
                    "ESC - Exit Game\n\n" +
                    "Game Features:\n" +
                    "• Eat dots to score points\n" +
                    "• Eat power pellets to chase ghosts\n" +
                    "• Avoid ghosts or lose a life\n" +
                    "• Clear all dots to advance to next level";
            JOptionPane.showMessageDialog(frame, controlsText, "Game Controls", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> {
            String aboutText = "Pac-Man Game\n\n" +
                    "Features:\n" +
                    "• Classic Pac-Man gameplay\n" +
                    "• Power pellets and ghost eating\n" +
                    "• Multiple levels with increasing difficulty\n" +
                    "• High score tracking\n" +
                    "• Pause functionality\n" +
                    "• Enhanced UI and graphics\n\n";
            JOptionPane.showMessageDialog(frame, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
        });

        helpMenu.add(controls);
        helpMenu.add(about);

        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        // Initialize and add PacMan game component
        pacmanGame = new PacMan();
        frame.add(pacmanGame);
        frame.pack();

        // Show welcome message
        showWelcomeMessage();

        // Set frame properties
        pacmanGame.requestFocus();
        frame.setVisible(true);
    }

    private static void showWelcomeMessage() {
        String welcomeText = "Welcome to Pac-Man!\n\n" +
                "Features:\n" +
                "• Power pellets for eating ghosts\n" +
                "• Multiple levels\n" +
                "• High score tracking\n" +
                "• Pause functionality (P key)\n" +
                "• Enhanced graphics and UI\n\n" +
                "Use arrow keys to move. Good luck!";

        JOptionPane.showMessageDialog(frame, welcomeText, "Welcome!", JOptionPane.INFORMATION_MESSAGE);
    }
}
