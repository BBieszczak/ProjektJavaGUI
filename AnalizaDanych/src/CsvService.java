import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis do obsługi plików CSV.
 */
public class CsvService {

    /**
     * Wczytuje dane z pliku tekstowego CSV.
     * Oczekuje separatora średnik (;).
     *
     * @param file Plik wejściowy.
     * @return Lista wczytanych obiektów.
     * @throws IOException Błąd odczytu pliku.
     */
    public List<DataPoint> load(File file) throws IOException {
        // Inicjalizacja listy wynikowej
        List<DataPoint> result = new ArrayList<>();

        // Try-with-resources: automatycznie zamyka plik po zakończeniu
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // Pętla czytająca plik linia po linii
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";"); // Podział linii wg separatora

                // Sprawdzenie, czy wiersz ma wymaganą liczbę kolumn
                if (parts.length >= 5) {
                    try {
                        // Pobranie danych i usunięcie zbędnych spacji (trim)
                        String prod = parts[0].trim();
                        String cat = parts[1].trim();
                        int qty = Integer.parseInt(parts[2].trim());

                        // Normalizacja ceny: zamiana polskiego przecinka na kropkę
                        double price = Double.parseDouble(parts[3].trim().replace(",", "."));
                        boolean avail = Boolean.parseBoolean(parts[4].trim());

                        // Dodanie poprawnego obiektu do listy
                        result.add(new DataPoint(prod, cat, qty, price, avail));
                    } catch (NumberFormatException e) {
                        // Obsługa błędów parsowania liczby (np. tekst zamiast cyfry)
                        System.out.println("Pominięto wiersz: " + line);
                    }
                }
            }
        }
        return result;
    }
}