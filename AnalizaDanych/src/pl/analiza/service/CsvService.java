package pl.analiza.service;

import pl.analiza.model.DataPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis odpowiedzialny za import danych z plików tekstowych CSV.
 */
public class CsvService {

    /**
     * Wczytuje dane z pliku CSV i konwertuje je na listę obiektów DataPoint.
     * <p>
     * Oczekiwany format pliku:
     * Produkt;Kategoria;Ilość;Cena;Dostępność
     * </p>
     *
     * @param file Plik wejściowy wybrany przez użytkownika.
     * @return Lista wczytanych produktów.
     * @throws IOException Błąd podczas odczytu pliku z dysku.
     */
    public List<DataPoint> load(File file) throws IOException {
        List<DataPoint> result = new ArrayList<>();

        // Użycie try-with-resources dla bezpiecznego zamknięcia strumienia
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // Pętla odczytująca plik linia po linii do końca
            while ((line = br.readLine()) != null) {
                // Podział linii według średnika
                String[] parts = line.split(";");

                // Walidacja: czy wiersz ma wystarczającą liczbę kolumn (5)
                if (parts.length >= 5) {
                    try {
                        // Oczyszczenie danych ze spacji (trim)
                        String prod = parts[0].trim();
                        String cat = parts[1].trim();
                        int qty = Integer.parseInt(parts[2].trim());

                        // Obsługa formatu ceny (zamiana polskiego przecinka na kropkę)
                        double price = Double.parseDouble(parts[3].trim().replace(",", "."));
                        boolean avail = Boolean.parseBoolean(parts[4].trim());

                        result.add(new DataPoint(prod, cat, qty, price, avail));
                    } catch (NumberFormatException e) {
                        // Logowanie błędu, jeśli dane w kolumnie liczbowej są nieprawidłowe
                        System.out.println("Błąd formatu liczby w linii: " + line);
                    }
                }
            }
        }
        return result;
    }
}