import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

public class DashboardController {

    // --- GUI ---
    @FXML private TextField txtProduct, txtCategory, txtQuantity, txtPrice;
    @FXML private ComboBox<String> comboField, comboOperation, comboChartType;
    @FXML private Label lblResult, lblStatus;
    @FXML private TableView<DataPoint> tableView;
    @FXML private TableColumn<DataPoint, String> colProduct, colCategory;
    @FXML private TableColumn<DataPoint, Integer> colQuantity;
    @FXML private TableColumn<DataPoint, Double> colPrice;
    @FXML private BarChart<String, Number> barChart;
    @FXML private NumberAxis axisY;

    private ObservableList<DataPoint> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Konfiguracja tabeli
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        tableView.setItems(data);

        // Listener wyboru w tabeli
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) fillForm(newV);
        });

        // Konfiguracja ComboBoxów
        comboField.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboOperation.setItems(FXCollections.observableArrayList("Suma", "Średnia", "Minimum", "Maksimum"));

        // Konfiguracja Wykresu
        comboChartType.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboChartType.setValue("Ilość"); // Domyślna wartość

        // W Javie 11 lepiej użyć pełnego zapisu dla zdarzenia
        comboChartType.setOnAction(e -> updateChart());

        // Dane startowe
        data.add(new DataPoint("Laptop", "IT", 10, 2500.0));
        data.add(new DataPoint("Myszka", "IT", 50, 40.0));

        updateChart();
    }

    // --- CRUD ---
    @FXML private void handleAdd() {
        try {
            data.add(readForm());
            updateChart();
            handleClear();
            lblStatus.setText("Dodano wpis.");
        } catch (Exception e) { showAlert("Błąd", e.getMessage()); }
    }

    @FXML private void handleUpdate() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                DataPoint temp = readForm();
                selected.setProduct(temp.getProduct());
                selected.setCategory(temp.getCategory());
                selected.setQuantity(temp.getQuantity());
                selected.setPrice(temp.getPrice());
                tableView.refresh();
                updateChart();
                lblStatus.setText("Zaktualizowano wpis.");
            } catch (Exception e) { showAlert("Błąd", "Błędne dane"); }
        }
    }

    @FXML private void handleDelete() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            data.remove(selected);
            updateChart();
            handleClear();
        }
    }

    @FXML private void handleClear() {
        txtProduct.clear(); txtCategory.clear(); txtQuantity.clear(); txtPrice.clear();
        tableView.getSelectionModel().clearSelection();
    }

    // --- OBLICZENIA (POPRAWIONE DLA JAVA 11) ---
    @FXML private void handleCalculate() {
        String field = comboField.getValue();
        String op = comboOperation.getValue();

        if (field == null || op == null || data.isEmpty()) {
            lblResult.setText("Brak danych lub nie wybrano opcji.");
            return;
        }

        // POPRAWKA: Użycie klasycznego switch zamiast expression
        DoubleStream stream;
        switch (field) {
            case "Ilość":
                stream = data.stream().mapToDouble(DataPoint::getQuantity);
                break;
            case "Cena":
                stream = data.stream().mapToDouble(DataPoint::getPrice);
                break;
            default: // "Wartość Całkowita"
                stream = data.stream().mapToDouble(DataPoint::getTotalValue);
                break;
        }

        double res = 0;

        // POPRAWKA: Zwykłe if-else zamiast nowej składni switch
        if (op.equals("Suma")) {
            res = stream.sum();
        } else if (op.equals("Średnia")) {
            res = stream.average().orElse(0);
        } else if (op.equals("Minimum")) {
            res = stream.min().orElse(0);
        } else if (op.equals("Maksimum")) {
            res = stream.max().orElse(0);
        }

        lblResult.setText(String.format("%.2f", res));
    }

    // --- WYKRESY (POPRAWIONE DLA JAVA 11) ---
    private void updateChart() {
        barChart.getData().clear();
        String type = comboChartType.getValue();
        if (type == null) type = "Ilość";

        axisY.setLabel(type);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(type);

        for (DataPoint dp : data) {
            Number val = 0;
            // POPRAWKA: Klasyczny switch
            switch (type) {
                case "Ilość":
                    val = dp.getQuantity();
                    break;
                case "Cena":
                    val = dp.getPrice();
                    break;
                case "Wartość Całkowita":
                    val = dp.getTotalValue();
                    break;
            }
            series.getData().add(new XYChart.Data<>(dp.getProduct(), val));
        }
        barChart.getData().add(series);
    }

    // --- I/O (Pliki i Baza) ---

    @FXML private void handleImportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(null);

        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length == 4) {
                        // Parsowanie z obsługą potencjalnych błędów
                        String prod = parts[0];
                        String cat = parts[1];
                        int qty = Integer.parseInt(parts[2].trim());
                        double price = Double.parseDouble(parts[3].trim().replace(",", "."));

                        data.add(new DataPoint(prod, cat, qty, price));
                    }
                }
                updateChart();
                lblStatus.setText("Zaimportowano z CSV.");
            } catch (Exception e) {
                showAlert("Błąd Importu", "Nie udało się odczytać pliku: " + e.getMessage());
            }
        }
    }

    @FXML private void handleImportExcel() {
        showAlert("Informacja", "Wymagana biblioteka Apache POI dla plików .xlsx.");
    }

    @FXML private void handleSaveToDB() {
        File dbFile = new File("database.csv");
        try (PrintWriter writer = new PrintWriter(dbFile)) {
            for (DataPoint dp : data) {
                writer.println(dp.toCSV());
            }
            lblStatus.setText("Zapisano dane do pliku database.csv");

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setHeaderText("Sukces");
            info.setContentText("Dane zapisane w pliku: " + dbFile.getAbsolutePath());
            info.showAndWait();
        } catch (FileNotFoundException e) {
            showAlert("Błąd Bazy", "Nie można zapisać do pliku.");
        }
    }

    // --- Helpers ---
    private DataPoint readForm() {
        return new DataPoint(txtProduct.getText(), txtCategory.getText(),
                Integer.parseInt(txtQuantity.getText()),
                Double.parseDouble(txtPrice.getText().replace(",", ".")));
    }

    private void fillForm(DataPoint dp) {
        txtProduct.setText(dp.getProduct());
        txtCategory.setText(dp.getCategory());
        txtQuantity.setText(String.valueOf(dp.getQuantity()));
        txtPrice.setText(String.valueOf(dp.getPrice()));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}