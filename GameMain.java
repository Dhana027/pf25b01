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

    private void handleLogout() {
        // Untu menghentikan backsound
        SoundEffect.BACKSOUND.stop();

        // Mereset username yang login
        loggedInUsername = null;

        // Menghapus frame yang ada
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.dispose();
        }

        // Untuk memulai aplikasi dari awal dan memunculkan tampilan login
        main(null);
    }
    
    private void selectGameMode() {
        Object[] modeOptions = {"Player vs Player", "Player vs Bot", "Logout"};
        int modeChoice = JOptionPane.showOptionDialog(
                this,
                "Choose an option:", "Game Menu",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, modeOptions, modeOptions[0]);

        if (modeChoice == 2 || modeChoice == JOptionPane.CLOSED_OPTION) { // 2 merujuk pada "Logout"
            handleLogout();
            return;
        }

        gameMode = (modeChoice == 0) ? 1 : 2;

        if (gameMode == 2) { //Jika Player vs Bot
            Object[] difficultyOptions = {"Easy", "Medium", "Hard"};
            int difficultyChoice = JOptionPane.showOptionDialog(this, "Select Bot Difficulty", "Difficulty",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, difficultyOptions, difficultyOptions[1]);

            if (difficultyChoice == JOptionPane.CLOSED_OPTION) {
                selectGameMode(); //Kembali ke menu utama jika pemain mengcancel
                return;
            }
            difficultyLevel = difficultyChoice + 1; // 1, 2, or 3
        }

        Object[] symbolOptions = {"Play as X", "Play as O"};
        int symbolChoice = JOptionPane.showOptionDialog(
                null, "Choose your symbol:", "Symbol",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, symbolOptions, symbolOptions[0]);

        if (symbolChoice == JOptionPane.CLOSED_OPTION) {
            selectGameMode(); // Kembali ke menu utama
            return;
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

        currentAI.setSeed(aiSeed); //Memberitahu AI seed apa yang akan digunakan
        int[] move = currentAI.move();

        if (move != null) {
            currentState = board.stepGame(aiSeed, move[0], move[1]);
            playMoveSound();
        }

        currentPlayer = playerSeed; //Mengembalikan giliran permainan ke player atau user
    }

    private void playMoveSound() {
        if (currentState == State.PLAYING) {
            if (aiSeed == Seed.CROSS) {
                SoundEffect.EAT_FOOD.play(); // Suara untuk X
            } else {
                SoundEffect.EXPLODE.play();  // Suara untuk O
            }
        } else if (currentState == State.DRAW) {
            SoundEffect.SERI.play();
        } else {
            SoundEffect.DIE.play();
        }
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
            public void mouseClicked(MouseEvent e) {
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
                                SoundEffect.EAT_FOOD.play(); // Sound effect untuk X
                            } else {
                                SoundEffect.EXPLODE.play(); // Sound effect untuk O
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
                            timer.setRepeats(false); // Memastikan bahwa timer hanya berjalan sekali
                            timer.start();
                        }
                    }
                } else {
                    DatabaseManager.updateStatistics(currentState, playerSeed, loggedInUsername);
                    updateScore(currentState);

                    int response = JOptionPane.showOptionDialog(null,
                            "Game Over. What do you want to do?",
                            "Game Finished",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"Play Again", "Change Mode"},
                            "Play Again");

                    if (response == 0) {
                        resetBoardOnly(); // main lagi dan score tetap sama
                    } else {
                        scoreX = 0;
                        scoreO = 0;
                        updateScoreLabel();
                        newGame(); // mereset game dan score
                    }
                }
                // Refresh the drawing canvas
                repaint();
            }
        });

        // Setup the status bar (JLabel) to display status message
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(false);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.CENTER);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        super.setLayout(new BorderLayout());
        scoreLabel = new JLabel("Score - X: 0 | O: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(FONT_STATUS);
        scoreLabel.setBackground(Color.WHITE);
        scoreLabel.setOpaque(false);
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setPreferredSize(new Dimension(300, 30));
        super.add(scoreLabel, BorderLayout.PAGE_START);
        super.add(statusBar, BorderLayout.PAGE_END);
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        // account for statusBar in height
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));

        // Set up Game
        initGame();
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
        selectGameMode();
        if (loggedInUsername != null) {
            resetBoardOnly();
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

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 128));

        g2d.fillRect(0, 0, getWidth(), scoreLabel.getHeight());
        g2d.fillRect(0, getHeight() - statusBar.getHeight(), getWidth(), statusBar.getHeight());

        board.paint(g);  // ask the game board to paint itself

        // Print status-bar message
        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.WHITE);
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
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found!", "Driver Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        if (performLogin()) {
            // Initialize and play background music in a loop
            SoundEffect.initGame();
            SoundEffect.BACKSOUND.loop();
            // Run GUI construction codes in Event-Dispatching thread for thread safety
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = new JFrame(TITLE);
                    // Set the content-pane of the JFrame to an instance of main JPanel
                    GameMain gamePanel = new GameMain();
                    frame.setContentPane(gamePanel); // set main panel
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();                         // auto size
                    frame.setLocationRelativeTo(null);    // center the window
                    frame.setVisible(true);               // show the window
                    gamePanel.newGame();
                }
            });


        } else {
            //Login was canceled or failed
            System.out.println("Login canceled. Program terminated.");
        }
    }

    private static void performRegistration() {
        JTextField usernameField = new JTextField(10);
        usernameField.setBackground(new Color(190, 170, 120));
        JTextField firstnameField = new JTextField(10);
        firstnameField.setBackground(new Color(190, 170, 120));
        JTextField lastnameField = new JTextField(10);
        lastnameField.setBackground(new Color(190, 170, 120));
        JPasswordField passwordField = new JPasswordField(10);
        passwordField.setBackground(new Color(190, 170, 120));
        JRadioButton maleButton = new JRadioButton("Male");
        maleButton.setSelected(true);
        JRadioButton femaleButton = new JRadioButton("Female");
        ButtonGroup sexGroup = new ButtonGroup();
        sexGroup.add(maleButton);
        sexGroup.add(femaleButton);
        JPanel sexPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sexPanel.add(maleButton);
        sexPanel.add(femaleButton);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("First Name:"));
        panel.add(firstnameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastnameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Sex:"));
        panel.add(sexPanel);

        int result = JOptionPane.showConfirmDialog(null, panel, "Register",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String firstname = firstnameField.getText();
            String lastname = lastnameField.getText();
            String sex = maleButton.isSelected() ? "Male" : "Female";

            if (username.isEmpty() || password.isEmpty() || firstname.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username, Password, and First Name cannot be empty.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (DatabaseManager.usernameExists(username)) {
                    JOptionPane.showMessageDialog(null, "Username already exists. Please choose another one.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (DatabaseManager.registerUser(username, password, firstname, lastname, sex)) {
                        JOptionPane.showMessageDialog(null, "Registration successful! Please login.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Registration failed. Please try again.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database error during registration.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private static boolean performLogin() {
        // Membuat panel custom untuk bagian login
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(180, 255, 120)); // biru
        JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
        labels.setBackground(new Color(180, 255, 120));
        labels.add(new JLabel("Username", SwingConstants.RIGHT));
        labels.add(new JLabel("Password", SwingConstants.RIGHT));
        panel.add(labels, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
        controls.setBackground(new Color(180, 255, 120)); // kuning
        JTextField usernameField = new JTextField(10);
        usernameField.setBackground(new Color(190, 170, 120)); // abu-abu
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBackground(new Color(190, 170, 120)); // abu-abu
        controls.add(usernameField);
        controls.add(passwordField);
        panel.add(controls, BorderLayout.CENTER);


        // Membuat pilihan untuk melakukan "Login", "Register", atau "Cancel"
        Object[] options = {"Login", "Register", "Cancel"};

        // Mengatur warna button
        UIManager.put("OptionPane.background", new Color(180, 255, 120)); // hijau muda
        UIManager.put("Panel.background", new Color(180, 255, 120));
        UIManager.put("Button.background", new Color(140, 238, 190)); // hijau terang
        UIManager.put("Button.foreground", Color.BLACK);

        while (true) {
            // Menggunakan showOptionDialog agar bisa mempunyai 3 button
            int choice = JOptionPane.showOptionDialog(null, panel, "Login",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) { // Indeks 0: button "Login"
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                try {
                    String dbPassword = DatabaseManager.getPasswordFromDB(username);
                    if (dbPassword != null && dbPassword.equals(password)) {
                        loggedInUsername = username;
                        JOptionPane.showMessageDialog(null, "Login Success! Welcome, " + username + ".");
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(null, "Wrong username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Connection to database failed.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return false;
                }
            } else if (choice == 1) { // Indeks 1: button "Register"
                performRegistration();
                // Setelah registrasi, loop berlanjut agar pengguna bisa login
            } else { // Kondisi untuk pilihan lainnya (Cancel atau menutup dialog)
                return false;
            }
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





