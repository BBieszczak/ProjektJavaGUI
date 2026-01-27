import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

/**
 * Kontroler główny aplikacji (Dashboard).
 * Odpowiada za logikę interfejsu użytkownika, obsługę zdarzeń (kliknięć)
 * oraz komunikację między warstwą widoku (FXML) a serwisami logicznymi.
 *
 * @author Bartosz Bieszczak
 * @version 1.0
 */
public class DashboardController {

    // --- Elementy GUI ---
    @FXML private TextField txtProduct, txtCategory, txtQuantity, txtPrice;
    @FXML private CheckBox chkAvailable;
    @FXML private ComboBox<String> comboField, comboOperation, comboChartType;
    @FXML private Label lblResult, lblStatus;
    @FXML private TableView<DataPoint> tableView;
    @FXML private TableColumn<DataPoint, String> colProduct, colCategory;
    @FXML private TableColumn<DataPoint, Integer> colQuantity;
    @FXML private TableColumn<DataPoint, Double> colPrice;
    @FXML private TableColumn<DataPoint, Boolean> colAvailable;
    @FXML private BarChart<String, Number> barChart;
    @FXML private NumberAxis axisY;

    /** Lista obserwowalna przechowywująca dane wyświetlane w tabeli. */
    private ObservableList<DataPoint> data = FXCollections.observableArrayList();

    // --- Serwisy ---
    private final DatabaseService dbService = new DatabaseService();
    private final MathService mathService = new MathService();
    private final CsvService csvService = new CsvService();
    private final ExcelService excelService = new ExcelService();

    /**
     * Metoda inicjalizująca kontroler.
     * Wywoływana automatycznie po załadowaniu pliku FXML.
     * Konfiguruje tabelę, listy rozwijane oraz próbuje pobrać dane z bazy.
     */
    @FXML
    public void initialize() {
        setupTable();
        setupComboBoxes();
        loadDataFromDB();
    }

    // --- CRUD ---

    /**
     * Obsługuje przycisk "Dodaj".
     * Pobiera dane z formularza, waliduje je i dodaje do tabeli.
     */
    @FXML private void handleAdd() {
        try {
            // Używamy nowej metody z walidacją
            DataPoint newPoint = readFormWithValidation();
            data.add(newPoint);
            refreshView();
            handleClear();
            lblStatus.setText("Dodano wpis (lokalnie).");
        } catch (IllegalArgumentException e) {
            // Wyłapujemy błędy walidacji (puste pola, złe liczby)
            showAlert("Błąd walidacji", e.getMessage());
        } catch (Exception e) {
            showAlert("Błąd krytyczny", e.getMessage());
        }
    }

    /**
     * Obsługuje przycisk "Aktualizuj".
     * Nadpisuje wybrany w tabeli wiersz danymi z formularza.
     */
    @FXML private void handleUpdate() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Błąd", "Nie wybrano elementu do edycji.");
            return;
        }

        try {
            DataPoint form = readFormWithValidation(); // Walidacja przy edycji też jest ważna!

            selected.setProduct(form.getProduct());
            selected.setCategory(form.getCategory());
            selected.setQuantity(form.getQuantity());
            selected.setPrice(form.getPrice());
            selected.setAvailable(form.isAvailable());

            refreshView();
            lblStatus.setText("Zaktualizowano wpis.");
        } catch (IllegalArgumentException e) {
            showAlert("Błąd walidacji", e.getMessage());
        }
    }

    /**
     * Obsługuje przycisk "Usuń".
     * Usuwa zaznaczony wiersz z tabeli.
     */
    @FXML private void handleDelete() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            data.remove(selected);
            refreshView();
            handleClear();
            lblStatus.setText("Usunięto wpis.");
        } else {
            lblStatus.setText("Wybierz wiersz, aby usunąć.");
        }
    }

    /**
     * Czyści wszystkie pola formularza i odznacza wiersz w tabeli.
     */
    @FXML private void handleClear() {
        txtProduct.clear();
        txtCategory.clear();
        txtQuantity.clear();
        txtPrice.clear();
        chkAvailable.setSelected(false);
        tableView.getSelectionModel().clearSelection();
    }

    // --- WALIDACJA ---

    /**
     * Pobiera dane z pól tekstowych, sprawdza ich poprawność i tworzy obiekt DataPoint.
     * * @return Nowy obiekt DataPoint utworzony z wprowadzonych danych.
     * @throws IllegalArgumentException Jeśli dane są niepoprawne (puste, nie są liczbami, ujemne).
     */
    private DataPoint readFormWithValidation() throws IllegalArgumentException {
        // 1. Sprawdzenie czy pola tekstowe nie są puste
        String prod = txtProduct.getText().trim();
        String cat = txtCategory.getText().trim();

        if (prod.isEmpty()) {
            throw new IllegalArgumentException("Pole 'Produkt' nie może być puste.");
        }
        if (cat.isEmpty()) {
            throw new IllegalArgumentException("Pole 'Kategoria' nie może być puste.");
        }

        // 2. Walidacja Ilości (Czy jest liczbą całkowitą?)
        int qty;
        try {
            qty = Integer.parseInt(txtQuantity.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Pole 'Ilość' musi być liczbą całkowitą (np. 10).");
        }

        
        double price;
        try {
            // Zamiana przecinka na kropkę dla wygody użytkownika
            price = Double.parseDouble(txtPrice.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Pole 'Cena' musi być liczbą (np. 99.99).");
        }

        // 4. Walidacja logiczna (Czy liczby nie są ujemne?)
        if (qty < 0) {
            throw new IllegalArgumentException("Ilość nie może być ujemna.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Cena nie może być ujemna.");
        }

        return new DataPoint(prod, cat, qty, price, chkAvailable.isSelected());
    }

    // --- Reszta metod (Baza, Import, Obliczenia) z dodanym JavaDoc ---

    /**
     * Zapisuje aktualny stan tabeli do bazy danych PostgreSQL.
     */
    @FXML private void handleSaveToDB() {
        try {
            dbService.saveToDatabase(data);
            lblStatus.setText("Zapisano w bazie PostgreSQL.");
            showAlert("Sukces", "Baza zaktualizowana.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Błąd Bazy", e.getMessage());
        }
    }

    private void loadDataFromDB() {
        try {
            List<DataPoint> loaded = dbService.loadFromDatabase();
            data.setAll(loaded);
            refreshView();
            lblStatus.setText("Pobrano dane z bazy.");
        } catch (Exception e) {
            lblStatus.setText("Brak połączenia z bazą.");
        }
    }

    /**
     * Wykonuje wybraną operację matematyczną na danych.
     */
    @FXML private void handleCalculate() {
        String field = comboField.getValue();
        String op = comboOperation.getValue();

        if (field == null || op == null) {
            lblResult.setText("Wybierz opcje.");
            return;
        }

        double result = mathService.calculate(data, field, op);
        lblResult.setText(String.format("%s (%s):\n%.4f", op, field, result));
    }

    @FXML private void handleImportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            try {
                List<DataPoint> imported = csvService.load(file);
                data.addAll(imported);
                refreshView();
                lblStatus.setText("Zaimportowano CSV.");
            } catch (Exception e) { showAlert("Błąd Importu", e.getMessage()); }
        }
    }

    @FXML private void handleImportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            try {
                List<DataPoint> imported = excelService.load(file);
                data.addAll(imported);
                refreshView();
                lblStatus.setText("Zaimportowano Excel.");
            } catch (Exception e) { showAlert("Błąd Excel", e.getMessage()); }
        }
    }

    // --- Helpers ---

    /** Odświeża tabelę i wykresy. */
    private void refreshView() {
        tableView.refresh();
        updateChart();
    }

    private void updateChart() {
        barChart.getData().clear();
        String type = comboChartType.getValue() == null ? "Ilość" : comboChartType.getValue();
        axisY.setLabel(type);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(type);

        for (DataPoint dp : data) {
            Number val = 0;
            switch (type) {
                case "Ilość": val = dp.getQuantity(); break;
                case "Cena": val = dp.getPrice(); break;
                case "Wartość Całkowita": val = dp.getTotalValue(); break;
            }
            series.getData().add(new XYChart.Data<>(dp.getProduct(), val));
        }
        barChart.getData().add(series);
    }

    private void setupTable() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        colAvailable.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item ? "Tak" : "Nie");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        tableView.setItems(data);
        tableView.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv != null) fillForm(nv);
        });
    }

    private void setupComboBoxes() {
        comboField.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboOperation.setItems(FXCollections.observableArrayList(
                "Suma", "Średnia", "Minimum", "Maksimum", "Mediana", "Odchylenie Std.", "Wariancja", "Rozstęp", "Liczność"));
        comboChartType.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboChartType.setValue("Ilość");
        comboChartType.setOnAction(e -> updateChart());
    }

    /**
     * Wypełnia pola formularza danymi z wybranego obiektu.
     * @param dp Obiekt DataPoint wybrany z tabeli.
     */
    private void fillForm(DataPoint dp) {
        txtProduct.setText(dp.getProduct()); txtCategory.setText(dp.getCategory());
        txtQuantity.setText(String.valueOf(dp.getQuantity()));
        txtPrice.setText(String.valueOf(dp.getPrice())); chkAvailable.setSelected(dp.isAvailable());
    }

    /**
     * Wyświetla okno dialogowe z komunikatem.
     * @param title Tytuł okna.
     * @param content Treść komunikatu.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING); // Używamy WARNING dla błędów walidacji
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}