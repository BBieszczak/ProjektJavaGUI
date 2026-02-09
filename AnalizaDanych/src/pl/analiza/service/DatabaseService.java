package pl.analiza.service;

import pl.analiza.model.DataPoint;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis JDBC do komunikacji z bazą danych PostgreSQL.
 * <p>
 * Implementuje logikę zapisu i odczytu danych z zachowaniem struktury
 * znormalizowanej (podział na tabele: kategorie, produkty, magazyn, cennik).
 * </p>
 */
public class DatabaseService {

    // Konfiguracja połączenia z bazą danych
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/magazyn";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Barbie";

    // ==========================================
    // 1. ZAPIS I ODCZYT PRODUKTÓW
    // ==========================================

    /**
     * Zapisuje pełną listę produktów do bazy danych.
     * <p>
     * Działa w trybie transakcyjnym:
     * 1. Czyści obecne tabele.
     * 2. Wstawia nowe dane, dbając o relacje kluczy obcych.
     * </p>
     *
     * @param data Lista produktów do zapisania.
     * @throws SQLException Błąd SQL (np. brak połączenia, błąd klucza).
     */
    public void saveToDatabase(List<DataPoint> data) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        conn.setAutoCommit(false); // Wyłączenie auto-commit w celu obsługi transakcji

        try {
            Statement stmt = conn.createStatement();

            // 1. Czyszczenie tabel (kolejność usuwania: dzieci -> rodzice)
            // CASCADE pozwala na kaskadowe usuwanie, RESTART IDENTITY resetuje liczniki ID
            stmt.execute("TRUNCATE TABLE cennik RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE magazyn RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE produkty RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE kategorie RESTART IDENTITY CASCADE");
            stmt.close();

            // 2. Przygotowanie zapytań (PreparedStatements) dla wydajności i bezpieczeństwa
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
                // A. Obsługa Kategorii (Sprawdź czy istnieje, jeśli nie - dodaj)
                int catId = -1;
                checkCatStmt.setString(1, dp.getCategory());
                ResultSet rsCat = checkCatStmt.executeQuery();

                if (rsCat.next()) {
                    catId = rsCat.getInt("id"); // Kategoria już istnieje
                } else {
                    insertCatStmt.setString(1, dp.getCategory());
                    ResultSet rsInsertCat = insertCatStmt.executeQuery();
                    if (rsInsertCat.next()) {
                        catId = rsInsertCat.getInt("id"); // Nowo dodana kategoria
                    }
                    rsInsertCat.close();
                }
                rsCat.close();

                // B. Dodanie Produktu (powiązanego z kategorią)
                insertProdStmt.setString(1, dp.getProduct());
                insertProdStmt.setInt(2, catId);
                ResultSet rsProd = insertProdStmt.executeQuery();

                int prodId = -1;
                if (rsProd.next()) {
                    prodId = rsProd.getInt("id");
                }
                rsProd.close();

                // C. Uzupełnienie Magazynu (dla danego produktu)
                insertStockStmt.setInt(1, prodId);
                insertStockStmt.setInt(2, dp.getQuantity());
                insertStockStmt.setBoolean(3, dp.isAvailable());
                insertStockStmt.executeUpdate();

                // D. Uzupełnienie Cennika (dla danego produktu)
                insertPriceStmt.setInt(1, prodId);
                insertPriceStmt.setDouble(2, dp.getPrice());
                insertPriceStmt.executeUpdate();
            }

            conn.commit(); // Zatwierdzenie transakcji (wszystko się udało)

        } catch (SQLException e) {
            conn.rollback(); // Wycofanie zmian w przypadku błędu
            throw e;
        } finally {
            conn.close(); // Zamknięcie połączenia
        }
    }

    /**
     * Pobiera produkty z bazy danych, łącząc informacje z 4 tabel.
     *
     * @return Lista obiektów DataPoint.
     * @throws SQLException Błąd zapytania SQL.
     */
    public List<DataPoint> loadFromDatabase() throws SQLException {
        List<DataPoint> list = new ArrayList<>();

        // Zapytanie złączeniowe (JOIN) pobierające komplet danych o produkcie
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
                // Mapowanie wyniku SQL na obiekt Java
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
    // 2. ZARZĄDZANIE KATEGORIAMI (CRUD)
    // ==========================================

    /** Pobiera listę nazw wszystkich kategorii posortowaną alfabetycznie. */
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

    /** Dodaje nową kategorię do bazy. */
    public void addCategory(String name) throws SQLException {
        String sql = "INSERT INTO kategorie (nazwa) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    /** Aktualizuje nazwę istniejącej kategorii. */
    public void updateCategory(String oldName, String newName) throws SQLException {
        String sql = "UPDATE kategorie SET nazwa = ? WHERE nazwa = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, oldName);
            pstmt.executeUpdate();
        }
    }

    /** Usuwa kategorię z bazy danych. */
    public void deleteCategory(String name) throws SQLException {
        String sql = "DELETE FROM kategorie WHERE nazwa = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }
}