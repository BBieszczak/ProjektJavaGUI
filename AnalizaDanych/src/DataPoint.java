// Model danych - reprezentuje pojedynczy produkt w systemie
public class DataPoint {
    private String product;
    private String category;
    private int quantity;
    private double price;
    private boolean available;

    // Konstruktor inicjalizujący wszystkie pola
    public DataPoint(String product, String category, int quantity, double price, boolean available) {
        this.product = product;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.available = available;
    }

    // --- Gettery i Settery (wymagane przez TableView do wyświetlania danych) ---

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // Dla typu boolean getter zaczyna się od "is"
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    // Metoda pomocnicza obliczająca wartość całkowitą (Ilość * Cena)
    public double getTotalValue() { return quantity * price; }
}