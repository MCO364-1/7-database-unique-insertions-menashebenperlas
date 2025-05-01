import java.io.IOException;
import java.sql.*;
import java.util.*;

public class PeopleDatabaseApp {

    public static void main(String[] args) {

        Properties props = new Properties();

        try {
            props.load(PeopleDatabaseApp.class.getClassLoader().getResourceAsStream("info.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = props.getProperty("info.url");
        String username = props.getProperty("info.username");
        String password = props.getProperty("info.password");

        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("Connected to database!");

            System.out.print("Enter your first name: ");
            String firstName = scanner.nextLine();
            System.out.print("Enter your last name: ");
            String lastName = scanner.nextLine();

            String insertSQL = "INSERT INTO People (FirstName, LastName) VALUES (?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.executeUpdate();
                System.out.println("Name added to the database.");
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("That name already exists in the database.");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate")) {
                    System.out.println("That name already exists in the database.");
                } else {
                    throw e;
                }
            }

            String countSQL = "SELECT COUNT(*) FROM People";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSQL)) {
                if (rs.next()) {
                    System.out.println("Total records: " + rs.getInt(1));
                }
            }

            String alphaCountSQL = "SELECT LEFT(FirstName, 1) AS Initial, COUNT(*) AS Total " +
                    "FROM People GROUP BY LEFT(FirstName, 1) ORDER BY Initial";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(alphaCountSQL)) {
                System.out.println("\nCount by first letter:");
                while (rs.next()) {
                    String initial = rs.getString("Initial").toUpperCase();
                    int count = rs.getInt("Total");
                    System.out.println(initial + ": " + count);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}