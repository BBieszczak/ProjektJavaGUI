package pl.analiza.service;

import pl.analiza.model.DataPoint;
import pl.analiza.model.CategoryStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MathService {

    // --- Dotychczasowa metoda calculate() pozostaje bez zmian ---
    public double calculate(List<DataPoint> data, String field, String operation) {
        // ... (Twój obecny kod metody calculate) ...
        // Skrót dla czytelności odpowiedzi - tu wklej starą treść metody calculate
        if (data == null || data.isEmpty()) return 0.0;
        List<Double> values = new ArrayList<>();
        for (DataPoint dp : data) {
            if ("Ilość".equals(field)) values.add((double) dp.getQuantity());
            else if ("Cena".equals(field)) values.add(dp.getPrice());
            else if ("Wartość Całkowita".equals(field)) values.add(dp.getTotalValue());
        }
        if (values.isEmpty()) return 0.0;

        double sum = 0;
        for(Double v : values) sum += v;

        switch (operation) {
            case "Suma": return sum;
            case "Średnia": return sum / values.size();
            case "Minimum": return values.stream().min(Double::compare).orElse(0.0);
            case "Maksimum": return values.stream().max(Double::compare).orElse(0.0);
            case "Mediana":
                values.sort(Double::compareTo);
                int size = values.size();
                return size % 2 == 0 ? (values.get(size/2 - 1) + values.get(size/2))/2.0 : values.get(size/2);
            case "Odchylenie Std.":
                double avg = sum / values.size();
                double variance = values.stream().mapToDouble(v -> Math.pow(v - avg, 2)).sum() / values.size();
                return Math.sqrt(variance);
            case "Wariancja":
                double avg2 = sum / values.size();
                return values.stream().mapToDouble(v -> Math.pow(v - avg2, 2)).sum() / values.size();
            default: return 0.0;
        }
    }

    // --- NOWA METODA: Statystyki per Kategoria ---
    public List<CategoryStats> getCategoryStatistics(List<DataPoint> data) {
        Map<String, List<DataPoint>> grouped = new HashMap<>();

        // 1. Grupowanie
        for (DataPoint dp : data) {
            grouped.computeIfAbsent(dp.getCategory(), k -> new ArrayList<>()).add(dp);
        }

        // 2. Obliczanie statystyk dla grup
        List<CategoryStats> result = new ArrayList<>();
        for (Map.Entry<String, List<DataPoint>> entry : grouped.entrySet()) {
            String catName = entry.getKey();
            List<DataPoint> items = entry.getValue();

            int count = items.size();
            double totalVal = items.stream().mapToDouble(DataPoint::getTotalValue).sum();
            double avgPrice = items.stream().mapToDouble(DataPoint::getPrice).average().orElse(0.0);

            result.add(new CategoryStats(catName, count, avgPrice, totalVal));
        }
        return result;
    }
}