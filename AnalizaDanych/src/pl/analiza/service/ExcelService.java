package pl.analiza.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pl.analiza.model.DataPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa serwisowa odpowiedzialna za import danych z plików Excel (.xlsx).
 * Wykorzystuje zewnętrzną bibliotekę Apache POI.
 */
public class ExcelService {

    /**
     * Wczytuje dane z pierwszego arkusza pliku Excel.
     * Obsługuje różne typy komórek (tekst, liczba, boolean) i konwertuje je na DataPoint.
     *
     * @param file Plik Excel (.xlsx).
     * @return Lista wczytanych produktów.
     * @throws IOException Błąd wejścia/wyjścia.
     */
    public List<DataPoint> load(File file) throws IOException {
        List<DataPoint> result = new ArrayList<>();

        // Automatyczne zamknięcie strumienia pliku (try-with-resources)
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Pobranie pierwszego arkusza (indeks 0)
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // Pomiń nagłówek (pierwszy wiersz)
                if (row.getRowNum() == 0) {
                    continue;
                }

                // --- Pobieranie danych z komórek z obsługą typów ---

                // 1. Produkt (Kolumna A)
                String product = "";
                Cell c0 = row.getCell(0);
                if (c0 != null) {
                    // Sprawdzenie czy to tekst, czy liczba wpisana jako tekst
                    if (c0.getCellType() == CellType.STRING) product = c0.getStringCellValue();
                    else if (c0.getCellType() == CellType.NUMERIC) product = String.valueOf(c0.getNumericCellValue());
                }

                // 2. Kategoria (Kolumna B)
                String category = "";
                Cell c1 = row.getCell(1);
                if (c1 != null) {
                    if (c1.getCellType() == CellType.STRING) category = c1.getStringCellValue();
                    else if (c1.getCellType() == CellType.NUMERIC) category = String.valueOf(c1.getNumericCellValue());
                }

                // 3. Ilość (Kolumna C)
                int quantity = 0;
                Cell c2 = row.getCell(2);
                if (c2 != null) {
                    if (c2.getCellType() == CellType.NUMERIC) quantity = (int) c2.getNumericCellValue();
                    else if (c2.getCellType() == CellType.STRING) {
                        try { quantity = Integer.parseInt(c2.getStringCellValue()); } catch(Exception e){}
                    }
                }

                // 4. Cena (Kolumna D)
                double price = 0.0;
                Cell c3 = row.getCell(3);
                if (c3 != null) {
                    if (c3.getCellType() == CellType.NUMERIC) price = c3.getNumericCellValue();
                    else if (c3.getCellType() == CellType.STRING) {
                        try { price = Double.parseDouble(c3.getStringCellValue().replace(",", ".")); } catch(Exception e){}
                    }
                }

                // 5. Dostępność (Kolumna E)
                boolean available = false;
                Cell c4 = row.getCell(4);
                if (c4 != null) {
                    if (c4.getCellType() == CellType.BOOLEAN) available = c4.getBooleanCellValue();
                    else if (c4.getCellType() == CellType.STRING) available = Boolean.parseBoolean(c4.getStringCellValue());
                }

                result.add(new DataPoint(product, category, quantity, price, available));
            }
        }
        return result;
    }
}