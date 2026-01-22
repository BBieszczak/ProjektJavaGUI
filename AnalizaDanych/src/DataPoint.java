public class DataPoint {
    private String product;
    private String category;
    private int quantity;
    private double price;

    public DataPoint(String product, String category, int quantity, double price) {
        this.product = product;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    // Gettery i Settery
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTotalValue() {
        return quantity * price;
    }

    // Pomocnicza metoda do zapisu w formacie CSV
    public String toCSV() {
        return product + ";" + category + ";" + quantity + ";" + price;
    }
}