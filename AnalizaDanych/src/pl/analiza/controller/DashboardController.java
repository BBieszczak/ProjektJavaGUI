package pl.analiza.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.FileChooser;
import pl.analiza.model.DataPoint;
import pl.analiza.model.CategoryStats; // Import nowej klasy!
import pl.analiza.service.CsvService;
import pl.analiza.service.DatabaseService;
import pl.analiza.service.ExcelService;
import pl.analiza.service.MathService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private TabPane mainTabPane;
    @FXML private Label lblStatus;

    // --- PRODUKTY ---
    @FXML private TextField txtProduct, txtCategory, txtQuantity, txtPrice, txtSearch;
    @FXML private CheckBox chkAvailable;
    @FXML private TableView<DataPoint> tableView;
    @FXML private TableColumn<DataPoint, String> colProduct, colCategory;
    @FXML private TableColumn<DataPoint, Integer> colQuantity;
    @FXML private TableColumn<DataPoint, Double> colPrice;
    @FXML private TableColumn<DataPoint, Boolean> colAvailable;

    // --- KATEGORIE ---
    @FXML private ListView<String> listCategories;
    @FXML private TextField txtNewCategory;

    // --- STATYSTYKI (NOWE ELEMENTY) ---
    // KPI Labels
    @FXML private Label lblKpiTotalValue, lblKpiCount, lblKpiAvgPrice;

    // Tabela Statystyk
    @FXML private TableView<CategoryStats> tableStats;
    @FXML private TableColumn<CategoryStats, String> colStatCat;
    @FXML private TableColumn<CategoryStats, Integer> colStatCount;
    @FXML private TableColumn<CategoryStats, Double> colStatAvg;
    @FXML private TableColumn<CategoryStats, Double> colStatTotal;

    // Kalkulator
    @FXML private ComboBox<String> comboStatsField, comboStatsOp;
    @FXML private TextArea txtStatsLog;

    // --- WYKRESY ---
    @FXML private BarChart<String, Number> barChart;
    @FXML private NumberAxis axisY;
    @FXML private ComboBox<String> comboChartType;

    // --- DANE I SERWISY ---
    private final ObservableList<DataPoint> masterData = FXCollections.observableArrayList();
    private FilteredList<DataPoint> filteredData;
    private final ObservableList<String> categoriesData = FXCollections.observableArrayList();

    private final DatabaseService dbService = new DatabaseService();
    private final MathService mathService = new MathService();
    private final CsvService csvService = new CsvService();
    private final ExcelService excelService = new ExcelService();

    @FXML
    public void initialize() {
        setupTable();
        setupChart();
        setupStats();   // Rozszerzona konfiguracja
        setupSearch();
        refreshCategoriesList();
        loadDataFromDB();
        logToStats("System gotowy.");
    }

    // --- METODY POMOCNICZE (ODŚWIEŻANIE) ---

    // Tę metodę wywołujemy zawsze, gdy dane się zmieniają (import, dodanie, usunięcie)
    private void refreshAllViews() {
        tableView.refresh();
        updateChart();
        updateDashboardStats(); // Aktualizacja nowej zakładki
    }

    // --- LOGIKA STATYSTYK (NOWA) ---

    private void setupStats() {
        // Konfiguracja ComboBoxów
        comboStatsField.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboStatsOp.setItems(FXCollections.observableArrayList("Suma", "Średnia", "Minimum", "Maksimum", "Mediana", "Odchylenie Std.", "Wariancja"));
        comboStatsField.setValue("Cena");
        comboStatsOp.setValue("Średnia");

        // Konfiguracja nowej tabeli statystyk
        colStatCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStatCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        colStatAvg.setCellValueFactory(new PropertyValueFactory<>("avgPrice"));
        colStatTotal.setCellValueFactory(new PropertyValueFactory<>("totalValue"));

        // Formatowanie liczb w tabeli
        colStatAvg.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f zł", item));
            }
        });
        colStatTotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f zł", item));
            }
        });
    }

    private void updateDashboardStats() {
        if (masterData.isEmpty()) {
            lblKpiTotalValue.setText("0.00 PLN");
            lblKpiCount.setText("0");
            lblKpiAvgPrice.setText("0.00 PLN");
            tableStats.setItems(FXCollections.emptyObservableList());
            return;
        }

        // 1. Aktualizacja kart KPI
        double totalVal = mathService.calculate(masterData, "Wartość Całkowita", "Suma");
        double avgPrice = mathService.calculate(masterData, "Cena", "Średnia");
        int count = masterData.size();

        lblKpiTotalValue.setText(String.format("%.2f PLN", totalVal));
        lblKpiCount.setText(String.valueOf(count));
        lblKpiAvgPrice.setText(String.format("%.2f PLN", avgPrice));

        // 2. Aktualizacja Tabeli Statystyk wg Kategorii
        tableStats.setItems(FXCollections.observableArrayList(mathService.getCategoryStatistics(masterData)));
    }

    @FXML private void handleCalculateSingle() {
        String f = comboStatsField.getValue(); String o = comboStatsOp.getValue();
        if (f == null || o == null) return;
        double r = mathService.calculate(masterData, f, o);
        logToStats(String.format("Wynik: %s (%s) = %.2f", o, f, r));
    }

    @FXML private void handleCalculateReport() {
        String f = comboStatsField.getValue(); if (f == null) return;
        StringBuilder sb = new StringBuilder("\n--- RAPORT: " + f + " ---\n");
        for(String op : new String[]{"Suma", "Średnia", "Minimum", "Maksimum"})
            sb.append(String.format("%-10s : %.2f\n", op, mathService.calculate(masterData, f, op)));
        logToStats(sb.toString());
    }

    @FXML private void handleClearStats() { txtStatsLog.clear(); }

    private void logToStats(String msg) {
        txtStatsLog.appendText("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + msg + "\n");
    }

    // --- POZOSTAŁA LOGIKA (Zaktualizowana o refreshAllViews) ---

    private void loadDataFromDB() {
        try {
            masterData.setAll(dbService.loadFromDatabase());
            refreshAllViews(); // ZAMIENNIK updateChart()
            setStatus("Pobrano dane z bazy.");
        } catch (Exception e) { setStatus("Brak bazy."); }
    }

    @FXML private void handleAddOrUpdate() {
        // ... (Logika walidacji bez zmian) ...
        try {
            DataPoint form = readFormWithValidation();
            DataPoint selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                masterData.add(form);
                setStatus("Dodano lokalnie.");
            } else {
                selected.setProduct(form.getProduct());
                selected.setCategory(form.getCategory());
                selected.setQuantity(form.getQuantity());
                selected.setPrice(form.getPrice());
                selected.setAvailable(form.isAvailable());
                setStatus("Zaktualizowano lokalnie.");
            }
            handleClear();
            refreshAllViews(); // Aktualizujemy też statystyki!
        } catch (IllegalArgumentException e) { showAlert("Błąd", e.getMessage()); }
    }

    @FXML private void handleDelete() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            masterData.remove(selected);
            handleClear();
            refreshAllViews(); // Aktualizujemy statystyki
            setStatus("Usunięto lokalnie.");
        }
    }

    @FXML private void handleImportCSV() {
        File f = new FileChooser().showOpenDialog(null);
        if (f != null) try { masterData.addAll(csvService.load(f)); refreshAllViews(); setStatus("CSV OK."); } catch (Exception e) { showAlert("Błąd", e.getMessage()); }
    }

    @FXML private void handleImportExcel() {
        File f = new FileChooser().showOpenDialog(null);
        if (f != null) try { masterData.addAll(excelService.load(f)); refreshAllViews(); setStatus("Excel OK."); } catch (Exception e) { showAlert("Błąd", e.getMessage()); }
    }

    // --- METODY "CATEGORY AFFECTS PRODUCTS" ---
    @FXML private void handleEditCategory() {
        String selected = listCategories.getSelectionModel().getSelectedItem();
        String newName = txtNewCategory.getText().trim();
        if (selected == null || newName.isEmpty()) return;
        try {
            dbService.updateCategory(selected, newName);
            refreshCategoriesList();
            loadDataFromDB(); // To odświeży też statystyki
            txtNewCategory.clear();
            setStatus("Zmieniono nazwę.");
        } catch (Exception e) { showAlert("Błąd", e.getMessage()); }
    }

    @FXML private void handleDeleteCategory() {
        String selected = listCategories.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć kategorię i jej produkty?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                dbService.deleteCategory(selected);
                refreshCategoriesList();
                loadDataFromDB(); // To odświeży też statystyki
                setStatus("Usunięto kategorię.");
            } catch (Exception e) { showAlert("Błąd", "Błąd usuwania."); }
        }
    }

    // --- STANDARDOWE METODY (BEZ ZMIAN) ---
    private void setupTable() { /* Skrót: Kod bez zmian jak w poprzednich wersjach */
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));
        colAvailable.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item ? "Tak" : "Nie");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                } else setText(null);
            }
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> { if(nv!=null) fillForm(nv); });
    }

    private void setupSearch() { /* Kod bez zmian */
        filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(dp -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return dp.getProduct().toLowerCase().contains(lower) || dp.getCategory().toLowerCase().contains(lower);
            });
        });
        SortedList<DataPoint> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    @FXML private void handleClear() { txtProduct.clear(); txtCategory.clear(); txtQuantity.clear(); txtPrice.clear(); chkAvailable.setSelected(false); tableView.getSelectionModel().clearSelection(); }
    private DataPoint readFormWithValidation() { /* Kod bez zmian */
        if (txtProduct.getText().isEmpty() || txtCategory.getText().isEmpty()) throw new IllegalArgumentException("Puste pola.");
        return new DataPoint(txtProduct.getText(), txtCategory.getText(), Integer.parseInt(txtQuantity.getText()), Double.parseDouble(txtPrice.getText().replace(",", ".")), chkAvailable.isSelected());
    }
    private void fillForm(DataPoint dp) { txtProduct.setText(dp.getProduct()); txtCategory.setText(dp.getCategory()); txtQuantity.setText(String.valueOf(dp.getQuantity())); txtPrice.setText(String.valueOf(dp.getPrice())); chkAvailable.setSelected(dp.isAvailable()); }
    private void refreshCategoriesList() { try { categoriesData.setAll(dbService.getAllCategories()); listCategories.setItems(categoriesData); } catch (Exception e) {} }
    @FXML private void handleAddCategory() { try { dbService.addCategory(txtNewCategory.getText().trim()); txtNewCategory.clear(); refreshCategoriesList(); } catch (Exception e) {} }
    @FXML private void handleSaveToDB() { try { dbService.saveToDatabase(masterData); refreshCategoriesList(); setStatus("Zapisano."); } catch (Exception e) { showAlert("Błąd", e.getMessage()); } }
    @FXML private void handleExit() { Platform.exit(); }
    private void setupChart() { comboChartType.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita")); comboChartType.setValue("Ilość"); comboChartType.setOnAction(e -> updateChart()); }
    private void updateChart() { /* Kod bez zmian */
        barChart.getData().clear(); String type = comboChartType.getValue(); if(type==null) return; axisY.setLabel(type);
        XYChart.Series<String, Number> s = new XYChart.Series<>(); s.setName(type);
        for(DataPoint dp : masterData) { Number v = type.equals("Ilość")?dp.getQuantity():(type.equals("Cena")?dp.getPrice():dp.getTotalValue()); s.getData().add(new XYChart.Data<>(dp.getProduct(), v)); }
        barChart.getData().add(s);
    }
    private void setStatus(String msg) { lblStatus.setText(msg); }
    private void showAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(c); a.showAndWait(); }
}