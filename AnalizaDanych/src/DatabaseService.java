import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    // Ustaw swoje dane logowania
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/magazyn";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Barbie";

    public void saveToDatabase(List<DataPoint> data) throws SQLException {
        String truncateProducts = "TRUNCATE TABLE produkty RESTART IDENTITY";
        String truncateCategories = "TRUNCATE TABLE kategorie RESTART IDENTITY CASCADE";

        String checkCatSQL = "SELECT id FROM kategorie WHERE nazwa = ?";
        String insertCatSQL = "INSERT INTO kategorie (nazwa) VALUES (?) RETURNING id";
        String insertProdSQL = "INSERT INTO produkty (nazwa, ilosc, cena, dostepnosc, kategoria_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false); // Transakcja

            // 1. Czyszczenie
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(truncateProducts);
                stmt.execute(truncateCategories);
            }

            // 2. Zapis
            try (PreparedStatement checkCatStmt = conn.prepareStatement(checkCatSQL);
                 PreparedStatement insertCatStmt = conn.prepareStatement(insertCatSQL);
                 PreparedStatement insertProdStmt = conn.prepareStatement(insertProdSQL)) {

                for (DataPoint dp : data) {
                    // Logika Kategorii (3NF)
                    int catId = getOrCreateCategory(conn, checkCatStmt, insertCatStmt, dp.getCategory());

                    // Logika Produktu
                    insertProdStmt.setString(1, dp.getProduct());
                    insertProdStmt.setInt(2, dp.getQuantity());
                    insertProdStmt.setDouble(3, dp.getPrice());
                    insertProdStmt.setBoolean(4, dp.isAvailable());
                    insertProdStmt.setInt(5, catId);
                    insertProdStmt.addBatch();
                }
                insertProdStmt.executeBatch();
            }
            conn.commit();
        }
    }

    public List<DataPoint> loadFromDatabase() throws SQLException {
        List<DataPoint> list = new ArrayList<>();
        String query = "SELECT p.nazwa, p.ilosc, p.cena, p.dostepnosc, k.nazwa AS kategoria_nazwa " +
                "FROM produkty p JOIN kategorie k ON p.kategoria_id = k.id";

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

    // Metoda pomocnicza do obsługi kategorii
    private int getOrCreateCategory(Connection conn, PreparedStatement checkStmt, PreparedStatement insertStmt, String catName) throws SQLException {
        checkStmt.setString(1, catName);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            insertStmt.setString(1, catName);
            ResultSet rsInsert = insertStmt.executeQuery();
            if (rsInsert.next()) {
                return rsInsert.getInt("id");
            }
        }
        throw new SQLException("Nie udało się pobrać ID kategorii.");
    }
}