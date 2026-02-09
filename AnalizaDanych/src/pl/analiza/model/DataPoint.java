package pl.analiza.model;

/**
 * Model danych reprezentujący pojedynczy produkt w magazynie.
 * Klasa typu POJO (Plain Old Java Object) używana do przechowywania danych w tabeli.
 */
public class DataPoint {
    private String product;
    private String category;
    private int quantity;
    private double price;
    private boolean available;

    /**
     * Konstruktor tworzący nowy obiekt produktu.
     *
     * @param product   Nazwa produktu.
     * @param category  Nazwa kategorii.
     * @param quantity  Ilość sztuk (liczba całkowita).
     * @param price     Cena jednostkowa.
     * @param available Status dostępności (true = dostępny).
     */
    public DataPoint(String product, String category, int quantity, double price, boolean available) {
        this.product = product;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.available = available;
    }

    /**
     * Pobiera nazwę produktu.
     * @return Nazwa produktu.
     */
    public String getProduct() { return product; }

    /**
     * Ustawia nazwę produktu.
     * @param product Nowa nazwa.
     */
    public void setProduct(String product) { this.product = product; }

    /**
     * Pobiera kategorię.
     * @return Nazwa kategorii.
     */
    public String getCategory() { return category; }

    /**
     * Ustawia kategorię.
     * @param category Nowa kategoria.
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * Pobiera ilość.
     * @return Ilość sztuk.
     */
    public int getQuantity() { return quantity; }

    /**
     * Ustawia ilość.
     * @param quantity Nowa ilość.
     */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /**
     * Pobiera cenę.
     * @return Cena produktu.
     */
    public double getPrice() { return price; }

    /**
     * Ustawia cenę.
     * @param price Nowa cena.
     */
    public void setPrice(double price) { this.price = price; }

    /**
     * Sprawdza dostępność.
     * @return true jeśli produkt jest dostępny.
     */
    public boolean isAvailable() { return available; }

    /**
     * Ustawia dostępność.
     * @param available Nowy status dostępności.
     */
    public void setAvailable(boolean available) { this.available = available; }

    /**
     * Oblicza wartość całkowitą (ilość * cena).
     * @return Wartość jako double.
     */
    public double getTotalValue() { return quantity * price; }
}