import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa serwisowa odpowiedzialna za import danych z plików Excel (.xlsx).
 * Wykorzystuje zewnętrzną bibliotekę Apache POI do odczytu zawartości arkuszy.
 */
public class ExcelService {

    /**
     * Wczytuje dane z pliku Excel i mapuje je na listę obiektów DataPoint.
     * Metoda otwiera plik, pobiera pierwszy arkusz i iteruje po wierszach,
     * konwertując wartości komórek na odpowiednie typy danych.
     *
     * @param file Plik Excel (.xlsx) wskazany przez użytkownika.
     * @return Lista obiektów DataPoint z wczytanymi danymi.
     * @throws IOException Rzucany w przypadku błędu wejścia/wyjścia (np. plik jest uszkodzony lub otwarty w innym programie).
     */
    public List<DataPoint> load(File file) throws IOException {
        List<DataPoint> result = new ArrayList<>();

        // Użycie try-with-resources dla automatycznego zamknięcia strumienia pliku
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Pobieramy pierwszy arkusz z skoroszytu (indeks 0)
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // Pomiń pierwszy wiersz, ponieważ zakładamy, że zawiera nagłówki kolumn
                if (row.getRowNum() == 0) {
                    continue;
                }

                // --- Odczyt danych z komórek z zabezpieczeniem przed pustymi wartościami ---

                // 1. Produkt (Tekst lub Liczba traktowana jako tekst)
                String product = "";
                Cell c0 = row.getCell(0);
                if (c0 != null) {
                    if (c0.getCellType() == CellType.STRING) product = c0.getStringCellValue();
                    else if (c0.getCellType() == CellType.NUMERIC) product = String.valueOf(c0.getNumericCellValue());
                }

                // 2. Kategoria
                String category = "";
                Cell c1 = row.getCell(1);
                if (c1 != null) {
                    if (c1.getCellType() == CellType.STRING) category = c1.getStringCellValue();
                    else if (c1.getCellType() == CellType.NUMERIC) category = String.valueOf(c1.getNumericCellValue());
                }

                // 3. Ilość (Liczba całkowita)
                int quantity = 0;
                Cell c2 = row.getCell(2);
                if (c2 != null) {
                    if (c2.getCellType() == CellType.NUMERIC) quantity = (int) c2.getNumericCellValue();
                    else if (c2.getCellType() == CellType.STRING) {
                        try { quantity = Integer.parseInt(c2.getStringCellValue()); } catch(Exception e){}
                    }
                }

                // 4. Cena (Liczba zmiennoprzecinkowa)
                double price = 0.0;
                Cell c3 = row.getCell(3);
                if (c3 != null) {
                    if (c3.getCellType() == CellType.NUMERIC) price = c3.getNumericCellValue();
                    else if (c3.getCellType() == CellType.STRING) {
                        try { price = Double.parseDouble(c3.getStringCellValue().replace(",", ".")); } catch(Exception e){}
                    }
                }

                // 5. Dostępność (Wartość logiczna)
                boolean available = false;
                Cell c4 = row.getCell(4);
                if (c4 != null) {
                    if (c4.getCellType() == CellType.BOOLEAN) available = c4.getBooleanCellValue();
                    else if (c4.getCellType() == CellType.STRING) available = Boolean.parseBoolean(c4.getStringCellValue());
                }

                // Dodanie przetworzonego wiersza do listy wynikowej
                result.add(new DataPoint(product, category, quantity, price, available));
            }
        }
        return result;
    }
}