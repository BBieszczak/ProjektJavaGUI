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
        List<DataPoint> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");

                if (parts.length >= 5) {
                    try {
                        String prod = parts[0].trim();
                        String cat = parts[1].trim();
                        int qty = Integer.parseInt(parts[2].trim());
                        double price = Double.parseDouble(parts[3].trim().replace(",", "."));
                        boolean avail = Boolean.parseBoolean(parts[4].trim());

                        result.add(new DataPoint(prod, cat, qty, price, avail));
                    } catch (NumberFormatException e) {
                        System.out.println("Pominięto wiersz: " + line);
                    }
                }
            }
        }
        return result;
    }
}