package pl.analiza.service;

import pl.analiza.model.DataPoint;
import pl.analiza.model.CategoryStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serwis realizujący operacje matematyczne i statystyczne na danych.
 * <p>
 * Oferuje metody do obliczania podstawowych wskaźników (suma, średnia, mediana itp.)
 * oraz generowania raportów grupowanych.
 * </p>
 */
public class MathService {

    /**
     * Wykonuje wybraną operację matematyczną na określonym polu zbioru danych.
     *
     * @param data      Lista produktów (dane wejściowe).
     * @param field     Pole, na którym ma być wykonana operacja ("Ilość", "Cena", "Wartość Całkowita").
     * @param operation Rodzaj operacji ("Suma", "Średnia", "Minimum", "Maksimum", "Mediana", "Odchylenie Std.", "Wariancja").
     * @return Wynik obliczeń jako liczba zmiennoprzecinkowa (double).
     */
    public double calculate(List<DataPoint> data, String field, String operation) {
        if (data == null || data.isEmpty()) return 0.0;

        // 1. Ekstrakcja wartości liczbowych z listy obiektów do prostej listy Double
        List<Double> values = new ArrayList<>();
        for (DataPoint dp : data) {
            if ("Ilość".equals(field)) values.add((double) dp.getQuantity());
            else if ("Cena".equals(field)) values.add(dp.getPrice());
            else if ("Wartość Całkowita".equals(field)) values.add(dp.getTotalValue());
        }
        if (values.isEmpty()) return 0.0;

        // 2. Obliczenie sumy (potrzebna do wielu operacji)
        double sum = 0;
        for(Double v : values) sum += v;

        // 3. Wykonanie właściwej operacji statystycznej
        switch (operation) {
            case "Suma": return sum;

            case "Średnia": return sum / values.size();

            case "Minimum": return values.stream().min(Double::compare).orElse(0.0);

            case "Maksimum": return values.stream().max(Double::compare).orElse(0.0);

            case "Mediana":
                values.sort(Double::compareTo); // Sortowanie wymagane do mediany
                int size = values.size();
                // Dla parzystej liczby elementów średnia z dwóch środkowych
                return size % 2 == 0 ? (values.get(size/2 - 1) + values.get(size/2))/2.0 : values.get(size/2);

            case "Odchylenie Std.":
                double avg = sum / values.size();
                // Suma kwadratów różnic od średniej
                double variance = values.stream().mapToDouble(v -> Math.pow(v - avg, 2)).sum() / values.size();
                return Math.sqrt(variance);

            case "Wariancja":
                double avg2 = sum / values.size();
                return values.stream().mapToDouble(v -> Math.pow(v - avg2, 2)).sum() / values.size();

            default: return 0.0;
        }
    }

    /**
     * Generuje statystyki pogrupowane według kategorii.
     *
     * @param data Lista wszystkich produktów.
     * @return Lista obiektów CategoryStats zawierająca podsumowanie dla każdej kategorii.
     */
    public List<CategoryStats> getCategoryStatistics(List<DataPoint> data) {
        Map<String, List<DataPoint>> grouped = new HashMap<>();

        // 1. Grupowanie produktów w mapie (Klucz: Kategoria -> Wartość: Lista produktów)
        for (DataPoint dp : data) {
            grouped.computeIfAbsent(dp.getCategory(), k -> new ArrayList<>()).add(dp);
        }

        // 2. Przetwarzanie grup na obiekty statystyk
        List<CategoryStats> result = new ArrayList<>();
        for (Map.Entry<String, List<DataPoint>> entry : grouped.entrySet()) {
            String catName = entry.getKey();
            List<DataPoint> items = entry.getValue();

            int count = items.size();
            // Suma wartości w danej grupie
            double totalVal = items.stream().mapToDouble(DataPoint::getTotalValue).sum();
            // Średnia cena w danej grupie
            double avgPrice = items.stream().mapToDouble(DataPoint::getPrice).average().orElse(0.0);

            result.add(new CategoryStats(catName, count, avgPrice, totalVal));
        }
        return result;
    }
}