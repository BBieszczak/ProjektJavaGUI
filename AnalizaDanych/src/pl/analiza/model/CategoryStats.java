package pl.analiza.model;

public class CategoryStats {
    private String category;
    private int count;
    private double avgPrice;
    private double totalValue;

    public CategoryStats(String category, int count, double avgPrice, double totalValue) {
        this.category = category;
        this.count = count;
        this.avgPrice = avgPrice;
        this.totalValue = totalValue;
    }

    public String getCategory() { return category; }
    public int getCount() { return count; }
    public double getAvgPrice() { return avgPrice; }
    public double getTotalValue() { return totalValue; }
}