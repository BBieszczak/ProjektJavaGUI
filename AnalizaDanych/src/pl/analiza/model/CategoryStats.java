package pl.analiza.model;

/**
 * Klasa modelu przechowująca zagregowane statystyki dla pojedynczej kategorii.
 * <p>
 * Używana do wyświetlania podsumowań w tabeli statystyk. Obiekty tej klasy są niemutowalne.
 * </p>
 */
public class CategoryStats {
    private String category;
    private int count;
    private double avgPrice;
    private double totalValue;

    /**
     * Tworzy nowy obiekt statystyk dla kategorii.
     *
     * @param category   Nazwa kategorii.
     * @param count      Liczba produktów w tej kategorii.
     * @param avgPrice   Średnia cena produktów w tej kategorii.
     * @param totalValue Sumaryczna wartość produktów (ilość * cena) w kategorii.
     */
    public CategoryStats(String category, int count, double avgPrice, double totalValue) {
        this.category = category;
        this.count = count;
        this.avgPrice = avgPrice;
        this.totalValue = totalValue;
    }

    /** Pobiera nazwę kategorii. */
    public String getCategory() { return category; }

    /** Pobiera liczbę produktów w kategorii. */
    public int getCount() { return count; }

    /** Pobiera średnią cenę w kategorii. */
    public double getAvgPrice() { return avgPrice; }

    /** Pobiera łączną wartość produktów w kategorii. */
    public double getTotalValue() { return totalValue; }
}