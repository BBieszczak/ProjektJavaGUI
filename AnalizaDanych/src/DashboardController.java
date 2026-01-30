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
 * Główny kontroler UI aplikacji (Dashboard).
 * Łączy widok FXML z logiką biznesową i bazą danych.
 */
public class DashboardController {

    // --- Komponenty GUI (FXML) ---
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

    // Główna lista danych powiązana z tabelą
    private ObservableList<DataPoint> data = FXCollections.observableArrayList();

    // Instancje serwisów do obsługi logiki i danych
    private final DatabaseService dbService = new DatabaseService();
    private final MathService mathService = new MathService();
    private final CsvService csvService = new CsvService();
    private final ExcelService excelService = new ExcelService();

    /**
     * Inicjalizacja kontrolera: konfiguracja tabeli, list rozwijanych i ładowanie danych.
     */
    @FXML
    public void initialize() {
        setupTable();
        setupComboBoxes();
        loadDataFromDB();
    }

    // --- Metody obsługi zdarzeń (CRUD) ---

    /** Dodaje nowy produkt po walidacji formularza. */
    @FXML private void handleAdd() {
        try {
            DataPoint newPoint = readFormWithValidation();
            data.add(newPoint);
            refreshView();
            handleClear();
            lblStatus.setText("Dodano wpis (lokalnie).");
        } catch (IllegalArgumentException e) {
            showAlert("Błąd walidacji", e.getMessage());
        } catch (Exception e) {
            showAlert("Błąd krytyczny", e.getMessage());
        }
    }

    /** Aktualizuje zaznaczony element tabeli danymi z formularza. */
    @FXML private void handleUpdate() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Błąd", "Nie wybrano elementu do edycji.");
            return;
        }
        try {
            DataPoint form = readFormWithValidation();
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

    /** Usuwa zaznaczony element z listy i tabeli. */
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

    /** Resetuje pola tekstowe i wybór w tabeli. */
    @FXML private void handleClear() {
        txtProduct.clear();
        txtCategory.clear();
        txtQuantity.clear();
        txtPrice.clear();
        chkAvailable.setSelected(false);
        tableView.getSelectionModel().clearSelection();
    }

    // --- Walidacja danych ---

    /** Pobiera dane z UI i sprawdza ich poprawność merytoryczną. */
    private DataPoint readFormWithValidation() throws IllegalArgumentException {
        String prod = txtProduct.getText().trim();
        String cat = txtCategory.getText().trim();

        if (prod.isEmpty() || cat.isEmpty()) throw new IllegalArgumentException("Pola tekstowe są puste.");

        int qty;
        double price;
        try {
            qty = Integer.parseInt(txtQuantity.getText().trim());
            price = Double.parseDouble(txtPrice.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ilość i Cena muszą być liczbami.");
        }

        if (qty < 0 || price < 0) throw new IllegalArgumentException("Liczby nie mogą być ujemne.");

        return new DataPoint(prod, cat, qty, price, chkAvailable.isSelected());
    }

    // --- Komunikacja z bazą i plikami ---

    /** Wysyła aktualną listę do bazy danych PostgreSQL. */
    @FXML private void handleSaveToDB() {
        try {
            dbService.saveToDatabase(data);
            lblStatus.setText("Zapisano w bazie.");
            showAlert("Sukces", "Baza zaktualizowana.");
        } catch (Exception e) {
            showAlert("Błąd Bazy", e.getMessage());
        }
    }

    /** Wczytuje dane startowe z bazy. */
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

    /** Oblicza statystyki na podstawie wybranych opcji matematycznych. */
    @FXML private void handleCalculate() {
        String field = comboField.getValue();
        String op = comboOperation.getValue();
        if (field == null || op == null) return;

        double result = mathService.calculate(data, field, op);
        lblResult.setText(String.format("%s (%s):\n%.2f", op, field, result));
    }

    /** Otwiera dialog wyboru pliku i importuje dane CSV. */
    @FXML private void handleImportCSV() {
        File file = new FileChooser().showOpenDialog(null);
        if (file != null) {
            try {
                data.addAll(csvService.load(file));
                refreshView();
            } catch (Exception e) { showAlert("Błąd CSV", e.getMessage()); }
        }
    }

    /** Otwiera dialog wyboru pliku i importuje dane Excel. */
    @FXML private void handleImportExcel() {
        File file = new FileChooser().showOpenDialog(null);
        if (file != null) {
            try {
                data.addAll(excelService.load(file));
                refreshView();
            } catch (Exception e) { showAlert("Błąd Excel", e.getMessage()); }
        }
    }

    // --- Aktualizacja widoku ---

    /** Odświeża komponenty tabeli i wykresu. */
    private void refreshView() {
        tableView.refresh();
        updateChart();
    }

    /** Przebudowuje wykres słupkowy na podstawie aktualnej listy produktów. */
    private void updateChart() {
        barChart.getData().clear();
        String type = comboChartType.getValue() == null ? "Ilość" : comboChartType.getValue();
        axisY.setLabel(type);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (DataPoint dp : data) {
            Number val = type.equals("Ilość") ? dp.getQuantity() : (type.equals("Cena") ? dp.getPrice() : dp.getTotalValue());
            series.getData().add(new XYChart.Data<>(dp.getProduct(), val));
        }
        barChart.getData().add(series);
    }

    /** Definiuje powiązania kolumn tabeli z polami obiektu DataPoint. */
    private void setupTable() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        // Warunkowe kolorowanie komórek dostępności
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

    /** Inicjuje listy rozwijane stałymi wartościami opcji. */
    private void setupComboBoxes() {
        comboField.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboOperation.setItems(FXCollections.observableArrayList("Suma", "Średnia", "Minimum", "Maksimum", "Mediana", "Liczność"));
        comboChartType.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboChartType.setValue("Ilość");
        comboChartType.setOnAction(e -> updateChart());
    }

    /** Przenosi dane z obiektu do pól edycyjnych formularza. */
    private void fillForm(DataPoint dp) {
        txtProduct.setText(dp.getProduct()); txtCategory.setText(dp.getCategory());
        txtQuantity.setText(String.valueOf(dp.getQuantity()));
        txtPrice.setText(String.valueOf(dp.getPrice())); chkAvailable.setSelected(dp.isAvailable());
    }

    /** Wyświetla standardowe okno ostrzeżenia. */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}