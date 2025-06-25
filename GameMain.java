import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.*;
/**
 * Tic-Tac-Toe: Two-player Graphic version with better OO design.
 * The Board and Cell classes are separated in their own classes.
 */
public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L; // to prevent serializable warning

    // Define named constants for the drawing graphics
    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG = Color.BLACK;
    public static final Color COLOR_BG_STATUS = new Color(220, 127, 127);
    public static final Color COLOR_CROSS = new Color(239, 105, 80);  // Red #EF6950
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225); // Blue #409AE1
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);




    // Define game objects
    private Board board;         // the game board
    private State currentState;  // the current state of the game
    private Seed currentPlayer;  // the current player
    private JLabel statusBar;    // for displaying status message
    private int gameMode;
    private static String loggedInUsername;
    private Seed playerSeed;
    private Seed aiSeed;
    private int scoreX = 0;
    private int scoreO = 0;
    private JLabel scoreLabel;

    private int difficultyLevel;
    private AIPlayer aiPlayerEasy;
    private AIPlayer aiPlayerMedium;
    private AIPlayer aiPlayerHard;

    private Image backgroundImage;

    private void selectGameMode() {
        Object[] modeOptions = {"Player vs Player", "Player vs Bot"};
        int modeChoice = JOptionPane.showOptionDialog(
                null, "Choose Game Mode:", "Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, modeOptions, modeOptions[0]);

        if (modeChoice == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        gameMode = (modeChoice == 0) ? 1 : 2;

        if (gameMode == 2) { // Jika Player vs Bot
            Object[] difficultyOptions = {"Easy", "Medium", "Hard"};
            int difficultyChoice = JOptionPane.showOptionDialog(this, "Select Bot Difficulty", "Difficulty",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, difficultyOptions, difficultyOptions[1]);

            if (difficultyChoice == -1) System.exit(0);
            difficultyLevel = difficultyChoice + 1; // 1, 2, or 3
        }

        Object[] symbolOptions = {"Play as X", "Play as O"};
        int symbolChoice = JOptionPane.showOptionDialog(
                null, "Choose your symbol:", "Symbol",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, symbolOptions, symbolOptions[0]);

        if (symbolChoice == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        playerSeed = (symbolChoice == 0) ? Seed.CROSS : Seed.NOUGHT;
        aiSeed = (playerSeed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
    }


    private void AImove() {
        if (currentState != State.PLAYING) return;

        AIPlayer currentAI;
        switch (difficultyLevel) {
            case 1: currentAI = aiPlayerEasy; break;
            case 2: currentAI = aiPlayerMedium; break;
            default: currentAI = aiPlayerHard; break;
        }

        currentAI.setSeed(aiSeed); // Beritahu AI bidak apa yang ia gunakan
        int[] move = currentAI.move();

        if (move != null) {
            currentState = board.stepGame(aiSeed, move[0], move[1]);
            playMoveSound();
        }

        currentPlayer = playerSeed; // Kembalikan giliran ke pemain manusia
    }

    private void playMoveSound() {
        if (currentState == State.PLAYING) SoundEffect.EAT_FOOD.play();
        else if (currentState == State.DRAW) SoundEffect.SERI.play();
        else SoundEffect.DIE.play();
    }


    /**
     * Constructor to setup the UI and game components
     */
    public GameMain() {
        try {
            URL imgURL = getClass().getClassLoader().getResource("images/TicTacToe_BG.png");
            if (imgURL != null) {
                backgroundImage = new ImageIcon(imgURL).getImage();
            } else {
                System.err.println("File gambar tidak ditemukan: images/TicTacToe_BG.png");
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar latar belakang.");
            e.printStackTrace();
        }
        // This JPanel fires MouseEvent
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {  // mouse-clicked handler
                int mouseX = e.getX();
                int mouseY = e.getY();
                // Get the row and column clicked
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;




                if (currentState == State.PLAYING) {
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        // Update cells[][] and return the new game state after the move
                        currentState = board.stepGame(currentPlayer, row, col);
                        // Play appropriate sound clip
                        if (currentState == State.PLAYING) {
                            if (currentPlayer == Seed.CROSS) {
                                SoundEffect.EAT_FOOD.play(); // Sound khusus untuk X
                            } else {
                                SoundEffect.EXPLODE.play(); // Sound khusus untuk O
                            }
                        } else {
                            if (currentState == State.DRAW) {
                                SoundEffect.SERI.play(); // Sound saat draw
                            } else {
                                SoundEffect.DIE.play(); // Sound saat menang
                            }
                        }
                        // Switch player
                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                        if (gameMode == 2 && currentState == State.PLAYING) {
                            Timer timer = new Timer(500, ae -> {
                                AImove();
                                repaint();
                            });
                            timer.setRepeats(false); // Pastikan timer hanya berjalan sekali
                            timer.start();
                        }
                    }
                } else {
                    updateStatistics(currentState);
                    updateScore(currentState);

                    int response = JOptionPane.showOptionDialog(null,
                            "Game Over. What do you want to do?",
                            "Game Finished",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"Play Again", "Quit"},
                            "Play Again");

                    if (response == 0) {
                        resetBoardOnly(); // main lagi, score tetap
                    } else {
                        scoreX = 0;
                        scoreO = 0;
                        updateScoreLabel();
                        newGame(); // reset game dan score
                    }
                }
                // Refresh the drawing canvas
                repaint();  // Callback paintComponent().
            }
        });

        // Setup the status bar (JLabel) to display status message
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        super.setLayout(new BorderLayout());
        scoreLabel = new JLabel("Score - X: 0 | O: 0");
        scoreLabel.setFont(FONT_STATUS);
        scoreLabel.setBackground(Color.WHITE);
        scoreLabel.setOpaque(true);
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setPreferredSize(new Dimension(300, 30));
        super.add(scoreLabel, BorderLayout.PAGE_START);
        super.add(statusBar, BorderLayout.PAGE_END); // same as SOUTH
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        // account for statusBar in height
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));

        // Set up Game
        initGame();
        newGame();
    }

    /**
     * Initialize the game (run once)
     */
    public void initGame() {
        board = new Board();  // allocate the game-board
        aiPlayerEasy = new AIPlayerRandom(board);
        aiPlayerMedium = new AIPlayerOffensive(board);
        aiPlayerHard = new AIPlayerDefensiveHard(board);
    }

    /**
     * Reset the game-board contents and the current-state, ready for new game
     */
    public void newGame() {
        GameMain.this.selectGameMode();
        resetBoardOnly();
        board.newGame();

        currentState = State.PLAYING;
        currentPlayer = Seed.CROSS;

        if (gameMode == 2 && playerSeed == Seed.NOUGHT) {
            AImove();
        }
    }

    /**
     * Custom painting codes on this JPanel
     */
    @Override
    public void paintComponent(Graphics g) {  // Callback via repaint()
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            setBackground(COLOR_BG);
        }

        board.paint(g);  // ask the game board to paint itself

        // Print status-bar message
        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText((currentPlayer == Seed.CROSS) ? "X's Turn" : "O's Turn");
        } else if (currentState == State.DRAW) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("It's a Draw! Click to play again.");
        } else if (currentState == State.CROSS_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'X' Won! Click to play again.");
        } else if (currentState == State.NOUGHT_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'O' Won! Click to play again.");
        }
    }


    /** The entry "main" method */
    public static void main(String[] args) {
        if (performLogin()) {
            // Initialize and play background music in a loop
            SoundEffect.initGame();
            SoundEffect.BACKGROUND.loop();
            // Run GUI construction codes in Event-Dispatching thread for thread safety
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = new JFrame(TITLE);
                    // Set the content-pane of the JFrame to an instance of main JPanel
                    frame.setContentPane(new GameMain()); // set main panel
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();                         // auto size
                    frame.setLocationRelativeTo(null);    // center the window
                    frame.setVisible(true);               // show the window
                }
            });


        } else {
            //Login was canceled or failed
            System.out.println("Login canceled. Program terminated.");
        }
    }


    static String getPassword(String username) throws ClassNotFoundException {
        String host, port, databaseName, userName, password;
        String pass = "";
        host = "mysql-bdc0fb9-sedanayoga-c1d0.b.aivencloud.com";
        port = "18480";
        databaseName = "defaultdb";
        userName = "avnadmin";
        password = "AVNS_sC5VSCXgbjts3LLEcoN";
//        for (int i = 0; i < args.length - 1; i++) {
//            switch (args[i].toLowerCase(Locale.ROOT)) {
//                case "-host": host = args[++i]; break;
//                case "-username": userName = args[++i]; break;
//                case "-password": password = args[++i]; break;
//                case "-database": databaseName = args[++i]; break;
//                case "-port": port = args[++i]; break;
//            }
//        }
        // JDBC allows to have nullable username and password
        if (host == null || port == null || databaseName == null) {
            System.out.println("Host, port, database information is required");
            return "err";
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (final Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, password);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT password from gameuser where username='"+username+"'")) {


            while (resultSet.next()) {
                //System.out.println("Username: " + resultSet.getString("username"));
                pass = resultSet.getString("password");
            }
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        return pass;
    }


    private static boolean performLogin() {
        // Membuat panel custom untuk dialog login
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
        labels.add(new JLabel("Username", SwingConstants.RIGHT));
        labels.add(new JLabel("Password", SwingConstants.RIGHT));
        panel.add(labels, BorderLayout.WEST);


        JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
        JTextField usernameField = new JTextField(10);
        controls.add(usernameField);
        JPasswordField passwordField = new JPasswordField();
        controls.add(passwordField);
        panel.add(controls, BorderLayout.CENTER);


        while (true) { // Loop sampai login berhasil atau dibatalkan
            int result = JOptionPane.showConfirmDialog(null, panel, "Login",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);


            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());


                try {
                    String truePass = getPassword(username);
                    if (!password.isEmpty() && password.equals(truePass)) {
                        loggedInUsername = username; // Simpan username global
                        JOptionPane.showMessageDialog(null, "Login Success! Welcome, " + username + ".");
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(null, "Wrong username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
                        // Loop akan berlanjut
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Connection to database failed.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return false; // Keluar jika ada error DB
                }
            } else {
                return false; // menekan Cancel atau menutup dialog
            }
        }
    }

    private void updateStatistics(State result) {
        String host = "mysql-bdc0fb9-sedanayoga-c1d0.b.aivencloud.com";
        String port = "18480";
        String databaseName = "defaultdb";
        String userName = "avnadmin";
        String password = "AVNS_sC5VSCXgbjts3LLEcoN";

        String fieldToUpdate = null;

        if ((result == State.CROSS_WON && playerSeed == Seed.CROSS) ||
                (result == State.NOUGHT_WON && playerSeed == Seed.NOUGHT)) {
            fieldToUpdate = "won";
        } else if ((result == State.CROSS_WON && playerSeed == Seed.NOUGHT) ||
                (result == State.NOUGHT_WON && playerSeed == Seed.CROSS)) {
            fieldToUpdate = "lose";
        } else if (result == State.DRAW) {
            fieldToUpdate = "draw";
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require",
                userName, password);
             Statement stmt = conn.createStatement()) {

            if (fieldToUpdate != null) {
                stmt.executeUpdate("UPDATE gameuser SET " + fieldToUpdate + " = " + fieldToUpdate + " + 1 WHERE username = '" + loggedInUsername + "'");
            }

            stmt.executeUpdate("UPDATE gameuser SET play = play + 1 WHERE username = '" + loggedInUsername + "'");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateScore(State result) {
        if (result == State.CROSS_WON) {
            scoreX++;
        } else if (result == State.NOUGHT_WON) {
            scoreO++;
        }
        updateScoreLabel();
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score - X: " + scoreX + " | O: " + scoreO);
    }

    private void resetBoardOnly() {
        board.newGame();
        currentState = State.PLAYING;
        currentPlayer = Seed.CROSS;

        if (gameMode == 2 && playerSeed == Seed.NOUGHT) {
            AImove();
        }
        repaint();
    }
}





