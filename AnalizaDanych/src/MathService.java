import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serwis do obliczeń matematycznych.
 * Realizuje proste operacje statystyczne na liście danych.
 */
public class MathService {

    /**
     * Oblicza wybraną statystykę dla zadanego pola.
     * Wykorzystuje pętle for-each dla czytelności.
     *
     * @param data      Lista danych wejściowych.
     * @param field     Pole, po którym liczymy (np. "Cena").
     * @param operation Rodzaj operacji (np. "Suma").
     * @return Wynik obliczeń (double).
     */
    public double calculate(List<DataPoint> data, String field, String operation) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }

        // 1. Wyciągnięcie wartości liczbowych do osobnej listy
        List<Double> values = new ArrayList<>();

        for (DataPoint dp : data) {
            if ("Ilość".equals(field)) {
                values.add((double) dp.getQuantity());
            } else if ("Cena".equals(field)) {
                values.add(dp.getPrice());
            } else if ("Wartość Całkowita".equals(field)) {
                values.add(dp.getTotalValue());
            }
        }

        if (values.isEmpty()) {
            return 0.0;
        }

        // 2. Wykonanie odpowiednich obliczeń
        double wynik = 0.0;

        if ("Suma".equals(operation)) {
            double suma = 0;
            for (Double v : values) {
                suma += v;
            }
            wynik = suma;

        } else if ("Średnia".equals(operation)) {
            double suma = 0;
            for (Double v : values) {
                suma += v;
            }
            wynik = suma / values.size();

        } else if ("Minimum".equals(operation)) {
            double min = values.get(0);
            for (Double v : values) {
                if (v < min) min = v;
            }
            wynik = min;

        } else if ("Maksimum".equals(operation)) {
            double max = values.get(0);
            for (Double v : values) {
                if (v > max) max = v;
            }
            wynik = max;

        } else if ("Liczność".equals(operation)) {
            wynik = (double) values.size();

        } else if ("Rozstęp".equals(operation)) {
            double min = values.get(0);
            double max = values.get(0);
            for (Double v : values) {
                if (v < min) min = v;
                if (v > max) max = v;
            }
            wynik = max - min;

        } else if ("Mediana".equals(operation)) {
            Collections.sort(values);
            int size = values.size();
            if (size % 2 == 0) {
                double srodek1 = values.get(size / 2 - 1);
                double srodek2 = values.get(size / 2);
                wynik = (srodek1 + srodek2) / 2.0;
            } else {
                wynik = values.get(size / 2);
            }

        } else if ("Wariancja".equals(operation)) {
            double suma = 0;
            for (Double v : values) suma += v;
            double srednia = suma / values.size();

            double temp = 0;
            for (double a : values) {
                temp += (a - srednia) * (a - srednia);
            }
            wynik = temp / values.size();

        } else if ("Odchylenie Std.".equals(operation)) {
            double suma = 0;
            for (Double v : values) suma += v;
            double srednia = suma / values.size();

            double temp = 0;
            for (double a : values) {
                temp += (a - srednia) * (a - srednia);
            }
            double wariancja = temp / values.size();
            wynik = Math.sqrt(wariancja);
        }

        return wynik;
    }
}