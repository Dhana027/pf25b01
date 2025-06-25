import java.sql.*;

public class DatabaseManager {

    /**
     * Membuat dan mengembalikan koneksi ke database.
     * Dibuat private karena hanya akan digunakan oleh metode lain di dalam kelas ini.
     */
    private static Connection getConnection() throws SQLException {
        String host = "mysql-bdc0fb9-sedanayoga-c1d0.b.aivencloud.com";
        String port = "18480";
        String databaseName = "defaultdb";
        String userName = "avnadmin";
        String password = "AVNS_sC5VSCXgbjts3LLEcoN";
        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, password);
    }

    /**
     * Mengambil password pengguna dari database berdasarkan username.
     */
    public static String getPasswordFromDB(String username) throws SQLException {
        String dbPassword = null;
        String sql = "SELECT password FROM gameuser WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    dbPassword = rs.getString("password");
                }
            }
        }
        return dbPassword;
    }

    /**
     * Mengecek apakah sebuah username sudah ada di database.
     */
    public static boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM gameuser WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Mendaftarkan pengguna baru ke dalam database.
     */
    public static boolean registerUser(String username, String password, String firstname, String lastname, String sex) throws SQLException {
        String sql = "INSERT INTO gameuser (username, password, firstname, lastname, sex, play, won, lose, draw) VALUES (?, ?, ?, ?, ?, 0, 0, 0, 0)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Di aplikasi nyata, password harus di-hash!
            pstmt.setString(3, firstname);
            pstmt.setString(4, lastname);
            pstmt.setString(5, sex);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Memperbarui statistik permainan (menang, kalah, seri) untuk seorang pengguna.
     */
    public static void updateStatistics(State result, Seed playerSeed, String loggedInUsername) {
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

        // Keluar jika tidak ada yang perlu diupdate (misal, game belum selesai)
        if (fieldToUpdate == null) return;

        // Perhatikan: PreparedStatement tidak bisa digunakan untuk nama kolom dinamis seperti ini.
        // String concatenation di sini bisa diterima karena nilai fieldToUpdate dikontrol oleh program, bukan input user.
        String sql = "UPDATE gameuser SET " + fieldToUpdate + " = " + fieldToUpdate + " + 1, play = play + 1 WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loggedInUsername);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}