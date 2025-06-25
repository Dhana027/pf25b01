import java.sql.*;


public class MySqlExample {
    public static void main(String[] args) throws ClassNotFoundException {
        String host, port, databaseName, userName, password;
        host = "mysql-tictactoe-testingnewdb246.c.aivencloud.com";
        port = "18905";
        databaseName = "tictactoedb";
        userName = "";
        password = "";
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
            return;
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (final Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, password);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT username from gameuser WHERE")) {


            while (resultSet.next()) {
                System.out.println("Username: " + resultSet.getString("username"));
            }
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }
}

