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

    // --- Dane ---
    private ObservableList<DataPoint> data = FXCollections.observableArrayList();

    // --- SERWISY (Tu nastąpiła zmiana) ---
    private final DatabaseService dbService = new DatabaseService();
    private final MathService mathService = new MathService();

    // Zamiast jednego FileService, mamy dwa wyspecjalizowane
    private final CsvService csvService = new CsvService();
    private final ExcelService excelService = new ExcelService();

    @FXML
    public void initialize() {
        setupTable();
        setupComboBoxes();
        loadDataFromDB();
    }

    // --- CRUD (Bez zmian) ---
    @FXML private void handleAdd() {
        try {
            data.add(readForm());
            refreshView();
            handleClear();
            lblStatus.setText("Dodano wpis (lokalnie).");
        } catch (Exception e) { showAlert("Błąd", e.getMessage()); }
    }

    @FXML private void handleUpdate() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                DataPoint form = readForm();
                selected.setProduct(form.getProduct());
                selected.setCategory(form.getCategory());
                selected.setQuantity(form.getQuantity());
                selected.setPrice(form.getPrice());
                selected.setAvailable(form.isAvailable());
                refreshView();
                lblStatus.setText("Zaktualizowano wpis.");
            } catch (Exception e) { showAlert("Błąd", "Błędne dane"); }
        }
    }

    @FXML private void handleDelete() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            data.remove(selected);
            refreshView();
            handleClear();
        }
    }

    @FXML private void handleClear() {
        txtProduct.clear(); txtCategory.clear(); txtQuantity.clear(); txtPrice.clear();
        chkAvailable.setSelected(false);
        tableView.getSelectionModel().clearSelection();
    }

    // --- Baza Danych (Bez zmian) ---
    @FXML private void handleSaveToDB() {
        try {
            dbService.saveToDatabase(data);
            lblStatus.setText("Zapisano w bazie PostgreSQL (3NF).");
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

    // --- Obliczenia (Bez zmian) ---
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

    // --- IMPORT PLIKÓW (ZMIANA - Użycie nowych serwisów) ---

    @FXML private void handleImportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(null);

        if (file != null) {
            try {
                // Użycie CsvService
                List<DataPoint> imported = csvService.load(file);
                data.addAll(imported);
                refreshView();
                lblStatus.setText("Zaimportowano CSV.");
            } catch (Exception e) {
                showAlert("Błąd Importu CSV", e.getMessage());
            }
        }
    }

    @FXML private void handleImportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = chooser.showOpenDialog(null);

        if (file != null) {
            try {
                // Użycie ExcelService
                List<DataPoint> imported = excelService.load(file);
                data.addAll(imported);
                refreshView();
                lblStatus.setText("Zaimportowano Excel.");
            } catch (Exception e) {
                showAlert("Info Excel", e.getMessage());
            }
        }
    }

    // --- UI Helpers (Bez zmian) ---
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

    private DataPoint readForm() {
        return new DataPoint(txtProduct.getText(), txtCategory.getText(),
                Integer.parseInt(txtQuantity.getText()),
                Double.parseDouble(txtPrice.getText().replace(",", ".")), chkAvailable.isSelected());
    }

    private void fillForm(DataPoint dp) {
        txtProduct.setText(dp.getProduct()); txtCategory.setText(dp.getCategory());
        txtQuantity.setText(String.valueOf(dp.getQuantity()));
        txtPrice.setText(String.valueOf(dp.getPrice())); chkAvailable.setSelected(dp.isAvailable());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setContentText(content); alert.showAndWait();
    }
}