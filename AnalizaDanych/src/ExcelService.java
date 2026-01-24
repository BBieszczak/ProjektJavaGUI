import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelService {

    public List<DataPoint> load(File file) throws IOException {
        List<DataPoint> result = new ArrayList<>();

        // Otwieramy plik Excel
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Pobieramy pierwszy arkusz (indeks 0)
            Sheet sheet = workbook.getSheetAt(0);

            // Iterujemy po wierszach
            for (Row row : sheet) {
                // Pomiń nagłówek (wiersz 0)
                if (row.getRowNum() == 0) {
                    continue;
                }

                // Odczyt komórek (zabezpieczenie przed pustymi komórkami)
                // Zakładamy kolejność: Produkt | Kategoria | Ilość | Cena | Dostępność

                String product = getCellStringValue(row.getCell(0));
                String category = getCellStringValue(row.getCell(1));

                int quantity = (int) getCellNumericValue(row.getCell(2));
                double price = getCellNumericValue(row.getCell(3));

                // Obsługa Boolean (może być zapisany jako PRAWDA/FAŁSZ lub tekst "true")
                boolean available = false;
                Cell cellAvail = row.getCell(4);
                if (cellAvail != null) {
                    if (cellAvail.getCellType() == CellType.BOOLEAN) {
                        available = cellAvail.getBooleanCellValue();
                    } else {
                        available = Boolean.parseBoolean(getCellStringValue(cellAvail));
                    }
                }

                result.add(new DataPoint(product, category, quantity, price, available));
            }
        }
        return result;
    }

    // --- Metody pomocnicze do bezpiecznego wyciągania danych ---

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return "";
    }

    private double getCellNumericValue(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                // Próba parsowania tekstu na liczbę (np. "12.5")
                return Double.parseDouble(cell.getStringCellValue().replace(",", "."));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}