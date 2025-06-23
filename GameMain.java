import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.util.*;
import java.sql.*;
/**
 * Tic-Tac-Toe: Two-player Graphic version with better OO design.
 * The Board and Cell classes are separated in their own classes.
 */
public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L; // to prevent serializable warning

    // Define named constants for the drawing graphics
    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_CROSS = new Color(255,120,89); // Red #EF6950
    public static final Color COLOR_NOUGHT = new Color(0, 173, 181);
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.BOLD, 20);
    public static final Color COLOR_STATUS_TEXT = new Color(80, 60, 50);

    // Define game objects
    private Board board;         // the game board
    private State currentState;  // the current state of the game
    private Seed currentPlayer;  // the current player
    private JLabel statusBar;    // for displaying status message
    private ImageIcon backgroundImage;

    /** Constructor to setup the UI and game components */
    public GameMain() {
        //Ambil gambar
        try{
            URL imageUrl = getClass().getResource("TicTacToeBG.png");
            if (imageUrl == null) {
                throw new Exception("File gambar 'TicTacToeBG.png' tidak ditemukan!");
            }
            backgroundImage = new ImageIcon(imageUrl);
        }catch (Exception e){
            backgroundImage = null;
            // Tampilkan pesan error jika gambar tidak ditemukan
            JOptionPane.showMessageDialog(this, e.getMessage(), "Gambar Error", JOptionPane.ERROR_MESSAGE);
        }

        //Atur panel jadi transparan
        setOpaque(false);

        // This JPanel fires MouseEvent
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {  // mouse-clicked handler
                int headerHeight = 100;
                int mouseX = e.getX();
                int mouseY = e.getY() - headerHeight;

                // Get the row and column clicked
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (currentState == State.PLAYING) {
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        // Update cells[][] and return the new game state after the move
                        currentState = board.stepGame(currentPlayer, row, col);
                        // Switch player
                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    }
                } else {        // game over
                    newGame();  // restart the game
                }
                // Refresh the drawing canvas
                repaint();  // Callback paintComponent().
            }
        });

        //Tambah header judul
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false); // Buat panel header transparan
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15)); // Atur layout dan padding atas/bawah

        JLabel titleLabel = new JLabel(TITLE);
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 36));
        titleLabel.setForeground(COLOR_STATUS_TEXT);
        headerPanel.add(titleLabel);

        // Tambahkan ruang kosong di bawah judul sebelum papan permainan
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        // Setup the status bar (JLabel) to display status message
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(245, 245, 220));
        statusBar.setHorizontalAlignment(JLabel.CENTER);
        statusBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        //panel utama
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.PAGE_START); // Tambahkan header di ATAS
        add(statusBar, BorderLayout.PAGE_END);
        setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 150));

        // Set up Game
        initGame();
        newGame();
    }

    /** Initialize the game (run once) */
    public void initGame() {
        board = new Board();  // allocate the game-board
    }

    /** Reset the game-board contents and the current-state, ready for new game */
    public void newGame() {
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED; // all cells empty
            }
        }
        currentPlayer = Seed.CROSS;    // cross plays first
        currentState = State.PLAYING;  // ready to play
    }

    /** Custom painting codes on this JPanel */
    @Override
    public void paintComponent(Graphics g) {  // Callback via repaint()
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback dengan warna solid jika gambar tidak ditemukan
            g.setColor(new Color(213, 237, 240));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(0, 100);
        board.paint(g2d); // ask the game board to paint itself
        g2d.dispose();

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
    public static void main(String[] args) throws ClassNotFoundException {
        boolean wrongPassword = true;
        Scanner sc = new Scanner(System.in);
        do {
            System.out.print("Enter Username:");
            String uName = sc.next();

            System.out.print("Enter Password:");
            String pass = sc.next();
            String truePass = getPassword(uName);
            System.out.println("true pass:" + truePass);
            if (pass.equals(truePass)) {
                wrongPassword = false;
            } else {
                System.out.println("Wrong password, please try again!");
            }
        } while (wrongPassword);

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
}