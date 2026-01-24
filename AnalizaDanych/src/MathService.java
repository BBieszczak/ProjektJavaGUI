import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Serwis do obliczeń statystycznych
public class MathService {

    public double calculate(List<DataPoint> data, String field, String operation) {
        if (data == null || data.isEmpty()) return 0.0;

        // Krok 1: Wyodrębnienie odpowiednich liczb z obiektów DataPoint do prostej listy
        List<Double> values = new ArrayList<>();
        for (DataPoint dp : data) {
            switch (field) {
                case "Ilość": values.add((double) dp.getQuantity()); break;
                case "Cena": values.add(dp.getPrice()); break;
                case "Wartość Całkowita": values.add(dp.getTotalValue()); break;
            }
        }

        if (values.isEmpty()) return 0.0;

        // Krok 2: Wykonanie wybranej operacji matematycznej
        switch (operation) {
            case "Suma": return values.stream().mapToDouble(d -> d).sum();
            case "Średnia": return values.stream().mapToDouble(d -> d).average().orElse(0);
            case "Minimum": return values.stream().mapToDouble(d -> d).min().orElse(0);
            case "Maksimum": return values.stream().mapToDouble(d -> d).max().orElse(0);
            case "Liczność": return values.size();
            case "Rozstęp":
                double min = values.stream().mapToDouble(d -> d).min().orElse(0);
                double max = values.stream().mapToDouble(d -> d).max().orElse(0);
                return max - min;
            case "Mediana":
                Collections.sort(values); // Mediana wymaga posortowania
                int size = values.size();
                if (size % 2 == 0) return (values.get(size/2 - 1) + values.get(size/2)) / 2.0;
                else return values.get(size/2);
            case "Wariancja": return calculateVariance(values);
            case "Odchylenie Std.": return Math.sqrt(calculateVariance(values));
            default: return 0.0;
        }
    }

    // Metoda prywatna do obliczania wariancji (pomocnicza)
    private double calculateVariance(List<Double> values) {
        double mean = values.stream().mapToDouble(d -> d).average().orElse(0);
        double temp = 0;
        for (double a : values) temp += (a - mean) * (a - mean);
        return temp / values.size();
    }
}