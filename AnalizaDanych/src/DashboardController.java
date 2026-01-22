import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardController {

    // --- GUI ---
    @FXML private TextField txtProduct, txtCategory, txtQuantity, txtPrice;
    @FXML private CheckBox chkAvailable; // ZMIANA NA CHECKBOX

    @FXML private ComboBox<String> comboField, comboOperation, comboChartType;
    @FXML private Label lblResult, lblStatus;

    @FXML private TableView<DataPoint> tableView;
    @FXML private TableColumn<DataPoint, String> colProduct, colCategory;
    @FXML private TableColumn<DataPoint, Integer> colQuantity;
    @FXML private TableColumn<DataPoint, Double> colPrice;
    @FXML private TableColumn<DataPoint, Boolean> colAvailable; // ZMIANA TYPU KOLUMNY

    @FXML private BarChart<String, Number> barChart;
    @FXML private NumberAxis axisY;

    private ObservableList<DataPoint> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Konfiguracja kolumn
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        // Formatowanie kolumny Available (żeby pisało Tak/Nie zamiast true/false)
        colAvailable.setCellFactory(col -> new TableCell<DataPoint, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Tak" : "Nie");
                    // Opcjonalnie: Kolorowanie tekstu
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: red;");
                }
            }
        });

        tableView.setItems(data);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) fillForm(newV);
        });

        // Konfiguracja ComboBoxów
        // Z obliczeń usuwamy Dostępność (Boolean), zostawiamy tylko liczby
        comboField.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));

        comboOperation.setItems(FXCollections.observableArrayList(
                "Suma", "Średnia", "Minimum", "Maksimum",
                "Mediana", "Odchylenie Std.", "Wariancja", "Rozstęp", "Liczność"
        ));

        // Z wykresów też usuwamy Dostępność jako oś Y
        comboChartType.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboChartType.setValue("Ilość");
        comboChartType.setOnAction(e -> updateChart());

        // Dane startowe
        data.add(new DataPoint("Laptop", "IT", 10, 2500.0, true));
        data.add(new DataPoint("Myszka", "IT", 50, 40.0, true));
        data.add(new DataPoint("Monitor", "IT", 0, 800.0, false)); // Niedostępny

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
                selected.setAvailable(temp.isAvailable()); // Update Boolean
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
        chkAvailable.setSelected(false);
        tableView.getSelectionModel().clearSelection();
    }

    // --- OBLICZENIA I ANALIZA ---
    @FXML private void handleCalculate() {
        String field = comboField.getValue();
        String op = comboOperation.getValue();

        if (field == null || op == null || data.isEmpty()) {
            lblResult.setText("Brak danych lub nie wybrano opcji.");
            return;
        }

        // 1. Pobieranie danych do listy
        List<Double> values = new ArrayList<>();

        // Klasyczny Switch (Java 11 safe)
        switch (field) {
            case "Ilość":
                for(DataPoint dp : data) values.add((double)dp.getQuantity());
                break;
            case "Cena":
                for(DataPoint dp : data) values.add(dp.getPrice());
                break;
            default: // Wartość Całkowita
                for(DataPoint dp : data) values.add(dp.getTotalValue());
                break;
        }

        double result = 0;

        // 2. Wykonywanie operacji
        if (op.equals("Suma")) {
            result = values.stream().mapToDouble(d -> d).sum();
        }
        else if (op.equals("Średnia")) {
            result = values.stream().mapToDouble(d -> d).average().orElse(0);
        }
        else if (op.equals("Minimum")) {
            result = values.stream().mapToDouble(d -> d).min().orElse(0);
        }
        else if (op.equals("Maksimum")) {
            result = values.stream().mapToDouble(d -> d).max().orElse(0);
        }
        else if (op.equals("Liczność")) {
            result = values.size();
        }
        else if (op.equals("Rozstęp")) {
            double min = values.stream().mapToDouble(d -> d).min().orElse(0);
            double max = values.stream().mapToDouble(d -> d).max().orElse(0);
            result = max - min;
        }
        else if (op.equals("Mediana")) {
            Collections.sort(values);
            int size = values.size();
            if (size == 0) result = 0;
            else if (size % 2 == 0) {
                // Parzysta liczba elementów: średnia z dwóch środkowych
                result = (values.get(size/2 - 1) + values.get(size/2)) / 2.0;
            } else {
                // Nieparzysta: środkowy element
                result = values.get(size/2);
            }
        }
        else if (op.equals("Wariancja") || op.equals("Odchylenie Std.")) {
            if (values.size() > 0) {
                double mean = values.stream().mapToDouble(d -> d).average().orElse(0);
                double temp = 0;
                for(double a : values) {
                    temp += (a - mean) * (a - mean);
                }
                double variance = temp / values.size();

                if (op.equals("Wariancja")) {
                    result = variance;
                } else {
                    result = Math.sqrt(variance);
                }
            } else {
                result = 0;
            }
        }

        lblResult.setText(String.format("%s (%s):\n%.4f", op, field, result));
    }

    // --- WYKRESY ---
    private void updateChart() {
        barChart.getData().clear();
        String type = comboChartType.getValue();
        if (type == null) type = "Ilość";

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

    // --- I/O ---

    @FXML private void handleImportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(null);

        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length >= 5) {
                        String prod = parts[0];
                        String cat = parts[1];
                        int qty = Integer.parseInt(parts[2].trim());
                        double price = Double.parseDouble(parts[3].trim().replace(",", "."));
                        // Parsowanie boolean (ignoruje wielkość liter, np "True", "TRUE", "true")
                        boolean avail = Boolean.parseBoolean(parts[4].trim());

                        data.add(new DataPoint(prod, cat, qty, price, avail));
                    }
                }
                updateChart();
                lblStatus.setText("Zaimportowano z CSV.");
            } catch (Exception e) {
                showAlert("Błąd Importu", e.getMessage());
            }
        }
    }

    @FXML private void handleImportExcel() {
        showAlert("Informacja", "Wymagana biblioteka Apache POI.");
    }

    @FXML private void handleSaveToDB() {
        File dbFile = new File("database.csv");
        try (PrintWriter writer = new PrintWriter(dbFile)) {
            for (DataPoint dp : data) {
                writer.println(dp.toCSV());
            }
            lblStatus.setText("Zapisano dane do pliku.");
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setHeaderText("Sukces");
            info.setContentText("Baza zaktualizowana: " + dbFile.getAbsolutePath());
            info.showAndWait();
        } catch (FileNotFoundException e) {
            showAlert("Błąd Bazy", "Nie można zapisać do pliku.");
        }
    }

    // --- Helpers ---
    private DataPoint readForm() {
        return new DataPoint(
                txtProduct.getText(),
                txtCategory.getText(),
                Integer.parseInt(txtQuantity.getText()),
                Double.parseDouble(txtPrice.getText().replace(",", ".")),
                chkAvailable.isSelected() // Odczyt z CheckBoxa
        );
    }

    private void fillForm(DataPoint dp) {
        txtProduct.setText(dp.getProduct());
        txtCategory.setText(dp.getCategory());
        txtQuantity.setText(String.valueOf(dp.getQuantity()));
        txtPrice.setText(String.valueOf(dp.getPrice()));
        chkAvailable.setSelected(dp.isAvailable()); // Ustawienie CheckBoxa
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}