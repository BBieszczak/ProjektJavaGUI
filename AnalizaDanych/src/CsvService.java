import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvService {

    public List<DataPoint> load(File file) throws IOException {
        List<DataPoint> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Separator: średnik
                String[] parts = line.split(";");

                if (parts.length >= 5) {
                    try {
                        String prod = parts[0].trim();
                        String cat = parts[1].trim();

                        // Próba parsowania liczb. Jeśli to nagłówek (tekst), rzuci wyjątek i przejdzie do catch
                        int qty = Integer.parseInt(parts[2].trim());
                        double price = Double.parseDouble(parts[3].trim().replace(",", "."));

                        // Parsowanie boolean (ignoruje wielkość liter: TRUE, true, True -> true)
                        boolean avail = Boolean.parseBoolean(parts[4].trim());

                        result.add(new DataPoint(prod, cat, qty, price, avail));

                    } catch (NumberFormatException e) {
                        // To wyłapie nagłówek (np. próbę zamiany słowa "Ilość" na int)
                        System.out.println("Pominięto wiersz (prawdopodobnie nagłówek): " + line);
                    }
                }
            }
        }
        return result;
    }
}