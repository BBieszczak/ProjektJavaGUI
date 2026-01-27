import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis obsługujący połączenie z bazą PostgreSQL.
 * Zawiera metody zapisu i odczytu danych przy użyciu JDBC.
 */
public class DatabaseService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/magazyn";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Barbie";

    /**
     * Zapisuje listę produktów do bazy danych.
     * Najpierw czyści tabele (TRUNCATE), a potem dodaje nowe rekordy.
     * Obsługuje transakcje.
     *
     * @param data Lista produktów do zapisania.
     * @throws SQLException Błąd SQL (np. brak połączenia).
     */
    public void saveToDatabase(List<DataPoint> data) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        conn.setAutoCommit(false); // Start transakcji

        try {
            Statement stmt = conn.createStatement();

            System.out.println("Czyszczenie tabel...");
            stmt.execute("TRUNCATE TABLE produkty RESTART IDENTITY");
            stmt.execute("TRUNCATE TABLE kategorie RESTART IDENTITY CASCADE");
            stmt.close();

            String checkCatSQL = "SELECT id FROM kategorie WHERE nazwa = ?";
            String insertCatSQL = "INSERT INTO kategorie (nazwa) VALUES (?) RETURNING id";
            String insertProdSQL = "INSERT INTO produkty (nazwa, ilosc, cena, dostepnosc, kategoria_id) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement checkCatStmt = conn.prepareStatement(checkCatSQL);
            PreparedStatement insertCatStmt = conn.prepareStatement(insertCatSQL);
            PreparedStatement insertProdStmt = conn.prepareStatement(insertProdSQL);

            for (DataPoint dp : data) {
                // 1. Sprawdzanie lub dodawanie kategorii
                int catId = -1;

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

                // 2. Dodawanie produktu
                insertProdStmt.setString(1, dp.getProduct());
                insertProdStmt.setInt(2, dp.getQuantity());
                insertProdStmt.setDouble(3, dp.getPrice());
                insertProdStmt.setBoolean(4, dp.isAvailable());
                insertProdStmt.setInt(5, catId);

                insertProdStmt.executeUpdate();
            }

            conn.commit(); // Zatwierdzenie
            System.out.println("Zapisano dane do bazy.");

        } catch (SQLException e) {
            conn.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            conn.close();
        }
    }

    /**
     * Pobiera wszystkie produkty z bazy danych.
     *
     * @return Lista produktów pobrana z bazy.
     * @throws SQLException Błąd zapytania SQL.
     */
    public List<DataPoint> loadFromDatabase() throws SQLException {
        List<DataPoint> list = new ArrayList<>();
        String query = "SELECT p.nazwa, p.ilosc, p.cena, p.dostepnosc, k.nazwa AS kategoria_nazwa " +
                "FROM produkty p JOIN kategorie k ON p.kategoria_id = k.id";

        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            String prod = rs.getString("nazwa");
            String cat = rs.getString("kategoria_nazwa");
            int qty = rs.getInt("ilosc");
            double price = rs.getDouble("cena");
            boolean avail = rs.getBoolean("dostepnosc");

            DataPoint dp = new DataPoint(prod, cat, qty, price, avail);
            list.add(dp);
        }

        rs.close();
        stmt.close();
        conn.close();

        return list;
    }
}