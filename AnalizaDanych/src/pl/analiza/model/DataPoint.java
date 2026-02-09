package pl.analiza.model;

/**
 * Model danych reprezentujący pojedynczy produkt w systemie.
 * <p>
 * Jest to klasa typu POJO (Plain Old Java Object), która przechowuje informacje
 * o produkcie i jest wykorzystywana przez TableView oraz serwisy danych.
 * </p>
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
     * @param category  Nazwa kategorii, do której produkt należy.
     * @param quantity  Ilość sztuk na stanie (musi być liczbą całkowitą).
     * @param price     Cena jednostkowa produktu.
     * @param available Status dostępności (true = dostępny, false = niedostępny).
     */
    public DataPoint(String product, String category, int quantity, double price, boolean available) {
        this.product = product;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.available = available;
    }

    /** Pobiera nazwę produktu. @return Nazwa produktu. */
    public String getProduct() { return product; }

    /** Ustawia nazwę produktu. @param product Nowa nazwa. */
    public void setProduct(String product) { this.product = product; }

    /** Pobiera kategorię produktu. @return Nazwa kategorii. */
    public String getCategory() { return category; }

    /** Ustawia kategorię produktu. @param category Nowa kategoria. */
    public void setCategory(String category) { this.category = category; }

    /** Pobiera ilość produktu. @return Ilość sztuk. */
    public int getQuantity() { return quantity; }

    /** Ustawia ilość produktu. @param quantity Nowa ilość. */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /** Pobiera cenę jednostkową. @return Cena. */
    public double getPrice() { return price; }

    /** Ustawia cenę jednostkową. @param price Nowa cena. */
    public void setPrice(double price) { this.price = price; }

    /** Sprawdza status dostępności. @return true jeśli dostępny. */
    public boolean isAvailable() { return available; }

    /** Ustawia status dostępności. @param available Nowy status. */
    public void setAvailable(boolean available) { this.available = available; }

    /**
     * Oblicza wartość całkowitą magazynu dla tego produktu.
     * Wzór: ilość * cena.
     *
     * @return Wartość całkowita jako double.
     */
    public double getTotalValue() { return quantity * price; }
}