package pl.analiza.service;

import pl.analiza.model.DataPoint;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis JDBC do obsługi bazy PostgreSQL.
 * Obsługuje strukturę 4NF oraz pełny CRUD dla kategorii.
 */
public class DatabaseService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/magazyn";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Barbie";

    // ==========================================
    // 1. OBSŁUGA GŁÓWNA (PRODUKTY - ZAPIS/ODCZYT)
    // ==========================================

    /**
     * Zapisuje listę danych do bazy z zachowaniem normalizacji 4NF.
     * Czyści tabele i wpisuje nowy stan.
     */
    public void saveToDatabase(List<DataPoint> data) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        conn.setAutoCommit(false); // Transakcja

        try {
            Statement stmt = conn.createStatement();

            // 1. Czyszczenie tabel (kolejność usuwania: od dzieci do rodziców)
            stmt.execute("TRUNCATE TABLE cennik RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE magazyn RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE produkty RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE kategorie RESTART IDENTITY CASCADE");
            stmt.close();

            // 2. Przygotowanie zapytań
            String checkCatSQL = "SELECT id FROM kategorie WHERE nazwa = ?";
            String insertCatSQL = "INSERT INTO kategorie (nazwa) VALUES (?) RETURNING id";
            String insertProdSQL = "INSERT INTO produkty (nazwa, kategoria_id) VALUES (?, ?) RETURNING id";
            String insertStockSQL = "INSERT INTO magazyn (produkt_id, ilosc, dostepnosc) VALUES (?, ?, ?)";
            String insertPriceSQL = "INSERT INTO cennik (produkt_id, cena) VALUES (?, ?)";

            PreparedStatement checkCatStmt = conn.prepareStatement(checkCatSQL);
            PreparedStatement insertCatStmt = conn.prepareStatement(insertCatSQL);
            PreparedStatement insertProdStmt = conn.prepareStatement(insertProdSQL);
            PreparedStatement insertStockStmt = conn.prepareStatement(insertStockSQL);
            PreparedStatement insertPriceStmt = conn.prepareStatement(insertPriceSQL);

            for (DataPoint dp : data) {
                // A. Obsługa Kategorii
                int catId = -1;
                checkCatStmt.setString(1, dp.getCategory());
                ResultSet rsCat = checkCatStmt.executeQuery();
                if (rsCat.next()) {
                    catId = rsCat.getInt("id");
                } else {
                    insertCatStmt.setString(1, dp.getCategory());
                    ResultSet rsInsertCat = insertCatStmt.executeQuery();
                    if (rsInsertCat.next()) {
                        catId = rsInsertCat.getInt("id");
                    }
                    rsInsertCat.close();
                }
                rsCat.close();

                // B. Dodanie Produktu
                insertProdStmt.setString(1, dp.getProduct());
                insertProdStmt.setInt(2, catId);
                ResultSet rsProd = insertProdStmt.executeQuery();

                int prodId = -1;
                if (rsProd.next()) {
                    prodId = rsProd.getInt("id");
                }
                rsProd.close();

                // C. Magazyn
                insertStockStmt.setInt(1, prodId);
                insertStockStmt.setInt(2, dp.getQuantity());
                insertStockStmt.setBoolean(3, dp.isAvailable());
                insertStockStmt.executeUpdate();

                // D. Cennik
                insertPriceStmt.setInt(1, prodId);
                insertPriceStmt.setDouble(2, dp.getPrice());
                insertPriceStmt.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    /**
     * Pobiera dane łącząc 4 tabele (JOIN).
     */
    public List<DataPoint> loadFromDatabase() throws SQLException {
        List<DataPoint> list = new ArrayList<>();

        String query = "SELECT p.nazwa AS produkt, k.nazwa AS kategoria, " +
                "m.ilosc, m.dostepnosc, c.cena " +
                "FROM produkty p " +
                "JOIN kategorie k ON p.kategoria_id = k.id " +
                "JOIN magazyn m ON p.id = m.produkt_id " +
                "JOIN cennik c ON p.id = c.produkt_id";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                list.add(new DataPoint(
                        rs.getString("produkt"),
                        rs.getString("kategoria"),
                        rs.getInt("ilosc"),
                        rs.getDouble("cena"),
                        rs.getBoolean("dostepnosc")
                ));
            }
        }
        return list;
    }

    // ==========================================
    // 2. CRUD DLA KATEGORII (WYMAGANE PRZEZ UI)
    // ==========================================

    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT nazwa FROM kategorie ORDER BY nazwa";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("nazwa"));
            }
        }
        return categories;
    }

    public void addCategory(String name) throws SQLException {
        String sql = "INSERT INTO kategorie (nazwa) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    /**
     * Aktualizuje nazwę kategorii.
     */
    public void updateCategory(String oldName, String newName) throws SQLException {
        String sql = "UPDATE kategorie SET nazwa = ? WHERE nazwa = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, oldName);
            pstmt.executeUpdate();
        }
    }

    public void deleteCategory(String name) throws SQLException {
        String sql = "DELETE FROM kategorie WHERE nazwa = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }
}