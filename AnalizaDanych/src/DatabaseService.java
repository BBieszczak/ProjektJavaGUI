import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis JDBC do obsługi bazy PostgreSQL (tabela produkty i kategorie).
 */
public class DatabaseService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/magazyn";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Barbie";

    /**
     * Zapisuje listę danych do bazy w jednej transakcji.
     */
    public void saveToDatabase(List<DataPoint> data) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        conn.setAutoCommit(false); // Ręczne zarządzanie transakcją

        try {
            Statement stmt = conn.createStatement();

            // Czyszczenie starych danych i resetowanie liczników ID
            System.out.println("Czyszczenie tabel...");
            stmt.execute("TRUNCATE TABLE produkty RESTART IDENTITY");
            stmt.execute("TRUNCATE TABLE kategorie RESTART IDENTITY CASCADE");
            stmt.close();

            // Zapytania SQL do sprawdzania i wstawiania danych
            String checkCatSQL = "SELECT id FROM kategorie WHERE nazwa = ?";
            String insertCatSQL = "INSERT INTO kategorie (nazwa) VALUES (?) RETURNING id";
            String insertProdSQL = "INSERT INTO produkty (nazwa, ilosc, cena, dostepnosc, kategoria_id) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement checkCatStmt = conn.prepareStatement(checkCatSQL);
            PreparedStatement insertCatStmt = conn.prepareStatement(insertCatSQL);
            PreparedStatement insertProdStmt = conn.prepareStatement(insertProdSQL);

            for (DataPoint dp : data) {
                int catId = -1;

                // Pobierz ID kategorii lub dodaj nową, jeśli nie istnieje
                checkCatStmt.setString(1, dp.getCategory());
                ResultSet rs = checkCatStmt.executeQuery();

                if (rs.next()) {
                    catId = rs.getInt("id");
                } else {
                    insertCatStmt.setString(1, dp.getCategory());
                    ResultSet rsInsert = insertCatStmt.executeQuery();
                    if (rsInsert.next()) {
                        catId = rsInsert.getInt("id");
                    }
                }
                rs.close();

                // Wstawienie produktu powiązanego z kategorią
                insertProdStmt.setString(1, dp.getProduct());
                insertProdStmt.setInt(2, dp.getQuantity());
                insertProdStmt.setDouble(3, dp.getPrice());
                insertProdStmt.setBoolean(4, dp.isAvailable());
                insertProdStmt.setInt(5, catId);

                insertProdStmt.executeUpdate();
            }

            conn.commit(); // Zatwierdzenie zmian
            System.out.println("Zapisano dane do bazy.");

        } catch (SQLException e) {
            conn.rollback(); // Wycofanie zmian w razie błędu
            e.printStackTrace();
            throw e;
        } finally {
            conn.close(); // Zamknięcie połączenia
        }
    }

    /**
     * Pobiera listę produktów wraz z nazwami ich kategorii.
     */
    public List<DataPoint> loadFromDatabase() throws SQLException {
        List<DataPoint> list = new ArrayList<>();
        String query = "SELECT p.nazwa, p.ilosc, p.cena, p.dostepnosc, k.nazwa AS kategoria_nazwa " +
                "FROM produkty p JOIN kategorie k ON p.kategoria_id = k.id";

        // Pobranie danych przy użyciu JOIN
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                list.add(new DataPoint(
                        rs.getString("nazwa"),
                        rs.getString("kategoria_nazwa"),
                        rs.getInt("ilosc"),
                        rs.getDouble("cena"),
                        rs.getBoolean("dostepnosc")
                ));
            }
        }

        return list;
    }
}