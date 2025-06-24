import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
/**
 * Tic-Tac-Toe: Two-player Graphic version with better OO design.
 * The Board and Cell classes are separated in their own classes.
 */
public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L; // to prevent serializable warning


    // Define named constants for the drawing graphics
    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG = Color.BLACK;
    public static final Color COLOR_BG_STATUS = new Color(216, 216, 216);
    public static final Color COLOR_CROSS = new Color(239, 105, 80);  // Red #EF6950
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225); // Blue #409AE1
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);


    // Define game objects
    private Board board;         // the game board
    private State currentState;  // the current state of the game
    private Seed currentPlayer;  // the current player
    private JLabel statusBar;    // for displaying status message
    private int gameMode;
    private AIPlayerTableLookup aiPlayer;

    private void selectGameMode() {
        Object[] options = {"Player vs Player", "Player vs Bot"};
        int choice = JOptionPane.showOptionDialog(
                null, // parent component (null untuk di tengah layar)
                "Choose Game Mode:", // pesan dialog
                "Mode", // judul dialog
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, // tidak ada ikon custom
                options, // teks tombol
                options[0] // tombol default
        );

        if (choice == 0) {
            // Pilihan pertama: Player vs Player
            gameMode = 1;
        } else if (choice == 1) {
            // Pilihan kedua: Player vs Bot
            gameMode = 2;
        } else {
            // Jika pengguna menutup dialog, keluar dari aplikasi
            System.exit(0);
        }
    }

    private void AImove() {
        // Pastikan game masih berjalan sebelum AI bergerak
        if (currentState != State.PLAYING) return;

        // 1. Beritahu AI bidak apa yang sedang ia gunakan (selalu 'O')
        aiPlayer.setSeed(Seed.NOUGHT);

        // 2. Minta AI untuk menentukan gerakan terbaiknya
        int[] move = aiPlayer.move(); // Mendapatkan {baris, kolom} dari AI

        // 3. Lakukan gerakan tersebut di papan
        currentState = board.stepGame(Seed.NOUGHT, move[0], move[1]);

        // 4. Kembalikan giliran ke pemain manusia
        currentPlayer = Seed.CROSS;
    }

    /**
     * Constructor to setup the UI and game components
     */
    public GameMain() {


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
                            GameMain.this.AImove();
                        }
                    }
                } else {        // game over
                    newGame();  // restart the game
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
        aiPlayer = new AIPlayerTableLookup(board);
    }


    /**
     * Reset the game-board contents and the current-state, ready for new game
     */
    public void newGame() {
        GameMain.this.selectGameMode();
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED; // all cells empty
            }
        }
        currentPlayer = Seed.CROSS;    // cross plays first
        currentState = State.PLAYING;  // ready to play
    }


    /**
     * Custom painting codes on this JPanel
     */
    @Override
    public void paintComponent(Graphics g) {  // Callback via repaint()
        super.paintComponent(g);
        setBackground(COLOR_BG); // set its background color


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


    /**
     * The entry "main" method
     */
    public static void main(String[] args) {
        if (performLogin()) {
        // Run GUI construction codes in Event-Dispatching thread for thread safety
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame(TITLE);
                // Set the content-pane of the JFrame to an instance of main JPanel
                frame.setContentPane(new GameMain());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null); // center the application window
                frame.setVisible(true);            // show it
            }
        });
        } else {
            // Jika login gagal atau dibatalkan, program selesai.
            System.out.println("Login Canceled. Program Stopped.");
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
                        JOptionPane.showMessageDialog(null, "Login Success! Welcome, " + username + ".");
                        return true; // Login sukses, keluar dari loop
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
}

