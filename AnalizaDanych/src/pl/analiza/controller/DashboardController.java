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
import pl.analiza.model.CategoryStats;
import pl.analiza.service.CsvService;
import pl.analiza.service.DatabaseService;
import pl.analiza.service.ExcelService;
import pl.analiza.service.MathService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Kontroler widoku głównego (Dashboard).
 * <p>
 * Odpowiada za obsługę interakcji użytkownika, wiązanie danych z tabelami i wykresami,
 * oraz komunikację z serwisami (baza danych, pliki, obliczenia).
 * </p>
 */
public class DashboardController {

    // --- ELEMENTY GUI (Wstrzykiwane przez FXML) ---
    @FXML private TabPane mainTabPane;
    @FXML private Label lblStatus;

    // Sekcja Produktów
    @FXML private TextField txtProduct, txtCategory, txtQuantity, txtPrice, txtSearch;
    @FXML private CheckBox chkAvailable;
    @FXML private TableView<DataPoint> tableView;
    @FXML private TableColumn<DataPoint, String> colProduct, colCategory;
    @FXML private TableColumn<DataPoint, Integer> colQuantity;
    @FXML private TableColumn<DataPoint, Double> colPrice;
    @FXML private TableColumn<DataPoint, Boolean> colAvailable;

    // Sekcja Kategorii
    @FXML private ListView<String> listCategories;
    @FXML private TextField txtNewCategory;

    // Sekcja Statystyk (KPI i Tabela)
    @FXML private Label lblKpiTotalValue, lblKpiCount, lblKpiAvgPrice;
    @FXML private TableView<CategoryStats> tableStats;
    @FXML private TableColumn<CategoryStats, String> colStatCat;
    @FXML private TableColumn<CategoryStats, Integer> colStatCount;
    @FXML private TableColumn<CategoryStats, Double> colStatAvg;
    @FXML private TableColumn<CategoryStats, Double> colStatTotal;

    // Sekcja Kalkulatora
    @FXML private ComboBox<String> comboStatsField, comboStatsOp;
    @FXML private TextArea txtStatsLog;

    // Sekcja Wykresów
    @FXML private BarChart<String, Number> barChart;
    @FXML private NumberAxis axisY;
    @FXML private ComboBox<String> comboChartType;

    // --- DANE I SERWISY ---
    /** Główna lista danych wyświetlana w tabeli produktów. */
    private final ObservableList<DataPoint> masterData = FXCollections.observableArrayList();

    /** Lista filtrowana używana do wyszukiwania. */
    private FilteredList<DataPoint> filteredData;

    /** Lista kategorii pobierana z bazy. */
    private final ObservableList<String> categoriesData = FXCollections.observableArrayList();

    // Instancje serwisów logiki biznesowej
    private final DatabaseService dbService = new DatabaseService();
    private final MathService mathService = new MathService();
    private final CsvService csvService = new CsvService();
    private final ExcelService excelService = new ExcelService();

    /**
     * Metoda inicjalizująca kontroler.
     * Wywoływana automatycznie po załadowaniu pliku FXML.
     */
    @FXML
    public void initialize() {
        setupTable();      // Konfiguracja kolumn tabeli produktów
        setupChart();      // Konfiguracja wykresu
        setupStats();      // Konfiguracja zakładki statystyk
        setupSearch();     // Konfiguracja filtra wyszukiwania

        refreshCategoriesList(); // Pobranie kategorii z bazy
        loadDataFromDB();        // Pobranie produktów z bazy na starcie

        logToStats("System gotowy.");
    }

    // --- METODY POMOCNICZE (ODŚWIEŻANIE) ---

    /**
     * Centralna metoda odświeżająca wszystkie widoki.
     * Powinna być wywoływana po każdej zmianie w danych (masterData).
     */
    private void refreshAllViews() {
        tableView.refresh();     // Odświeżenie tabeli głównej
        updateChart();           // Prerysowanie wykresu
        updateDashboardStats();  // Przeliczenie statystyk i KPI
    }

    // --- LOGIKA STATYSTYK ---

    /**
     * Konfiguruje elementy interfejsu w zakładce statystyk (ComboBoxy, kolumny tabeli).
     */
    private void setupStats() {
        // Ustawienie opcji wyboru dla kalkulatora
        comboStatsField.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboStatsOp.setItems(FXCollections.observableArrayList("Suma", "Średnia", "Minimum", "Maksimum", "Mediana", "Odchylenie Std.", "Wariancja"));
        comboStatsField.setValue("Cena");
        comboStatsOp.setValue("Średnia");

        // Mapowanie kolumn tabeli statystyk do pól obiektu CategoryStats
        colStatCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStatCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        colStatAvg.setCellValueFactory(new PropertyValueFactory<>("avgPrice"));
        colStatTotal.setCellValueFactory(new PropertyValueFactory<>("totalValue"));

        // Formatowanie wyświetlania waluty w tabeli (dodanie "zł")
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

    /**
     * Oblicza i aktualizuje wskaźniki KPI oraz tabelę statystyk per kategoria.
     */
    private void updateDashboardStats() {
        if (masterData.isEmpty()) {
            // Reset widoku, gdy brak danych
            lblKpiTotalValue.setText("0.00 PLN");
            lblKpiCount.setText("0");
            lblKpiAvgPrice.setText("0.00 PLN");
            tableStats.setItems(FXCollections.emptyObservableList());
            return;
        }

        // 1. Obliczenie globalnych wskaźników KPI
        double totalVal = mathService.calculate(masterData, "Wartość Całkowita", "Suma");
        double avgPrice = mathService.calculate(masterData, "Cena", "Średnia");
        int count = masterData.size();

        // Wyświetlenie KPI
        lblKpiTotalValue.setText(String.format("%.2f PLN", totalVal));
        lblKpiCount.setText(String.valueOf(count));
        lblKpiAvgPrice.setText(String.format("%.2f PLN", avgPrice));

        // 2. Obliczenie i wyświetlenie statystyk grupowanych po kategorii
        tableStats.setItems(FXCollections.observableArrayList(mathService.getCategoryStatistics(masterData)));
    }

    /**
     * Obsługa przycisku "Oblicz" w prostym kalkulatorze.
     */
    @FXML private void handleCalculateSingle() {
        String f = comboStatsField.getValue();
        String o = comboStatsOp.getValue();
        if (f == null || o == null) return;

        // Wykonanie obliczeń przez serwis
        double r = mathService.calculate(masterData, f, o);
        logToStats(String.format("Wynik: %s (%s) = %.2f", o, f, r));
    }

    /**
     * Generuje szybki raport tekstowy dla wybranego pola.
     */
    @FXML private void handleCalculateReport() {
        String f = comboStatsField.getValue(); if (f == null) return;
        StringBuilder sb = new StringBuilder("\n--- RAPORT: " + f + " ---\n");
        // Iteracja przez podstawowe operacje statystyczne
        for(String op : new String[]{"Suma", "Średnia", "Minimum", "Maksimum"})
            sb.append(String.format("%-10s : %.2f\n", op, mathService.calculate(masterData, f, op)));
        logToStats(sb.toString());
    }

    @FXML private void handleClearStats() { txtStatsLog.clear(); }

    /** Loguje wiadomość do obszaru tekstowego z czasem. */
    private void logToStats(String msg) {
        txtStatsLog.appendText("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + msg + "\n");
    }

    // --- OPERACJE NA DANYCH (CRUD) ---

    /** Pobiera dane z bazy i aktualizuje widoki. */
    private void loadDataFromDB() {
        try {
            masterData.setAll(dbService.loadFromDatabase());
            refreshAllViews();
            setStatus("Pobrano dane z bazy.");
        } catch (Exception e) { setStatus("Brak połączenia z bazą lub pusta baza."); }
    }

    /**
     * Dodaje nowy produkt lub aktualizuje istniejący (jeśli zaznaczony w tabeli).
     */
    @FXML private void handleAddOrUpdate() {
        try {
            // Walidacja i odczyt z formularza
            DataPoint form = readFormWithValidation();
            DataPoint selected = tableView.getSelectionModel().getSelectedItem();

            if (selected == null) {
                // Tryb dodawania
                masterData.add(form);
                setStatus("Dodano lokalnie.");
            } else {
                // Tryb edycji - aktualizacja pól obiektu
                selected.setProduct(form.getProduct());
                selected.setCategory(form.getCategory());
                selected.setQuantity(form.getQuantity());
                selected.setPrice(form.getPrice());
                selected.setAvailable(form.isAvailable());
                setStatus("Zaktualizowano lokalnie.");
            }
            handleClear(); // Wyczyszczenie formularza
            refreshAllViews(); // Ważne: odświeżenie wykresów i statystyk
        } catch (IllegalArgumentException e) {
            showAlert("Błąd walidacji", e.getMessage());
        }
    }

    /** Usuwa zaznaczony element z listy. */
    @FXML private void handleDelete() {
        DataPoint selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            masterData.remove(selected);
            handleClear();
            refreshAllViews();
            setStatus("Usunięto lokalnie.");
        }
    }

    // --- IMPORT DANYCH ---

    @FXML private void handleImportCSV() {
        File f = new FileChooser().showOpenDialog(null);
        if (f != null) try {
            masterData.addAll(csvService.load(f));
            refreshAllViews();
            setStatus("Zaimportowano CSV.");
        } catch (Exception e) { showAlert("Błąd importu CSV", e.getMessage()); }
    }

    @FXML private void handleImportExcel() {
        File f = new FileChooser().showOpenDialog(null);
        if (f != null) try {
            masterData.addAll(excelService.load(f));
            refreshAllViews();
            setStatus("Zaimportowano Excel.");
        } catch (Exception e) { showAlert("Błąd importu Excel", e.getMessage()); }
    }

    // --- ZARZĄDZANIE KATEGORIAMI ---

    @FXML private void handleEditCategory() {
        String selected = listCategories.getSelectionModel().getSelectedItem();
        String newName = txtNewCategory.getText().trim();
        if (selected == null || newName.isEmpty()) return;
        try {
            // Aktualizacja w bazie i odświeżenie widoków
            dbService.updateCategory(selected, newName);
            refreshCategoriesList();
            loadDataFromDB();
            txtNewCategory.clear();
            setStatus("Zmieniono nazwę kategorii.");
        } catch (Exception e) { showAlert("Błąd Bazy", e.getMessage()); }
    }

    @FXML private void handleDeleteCategory() {
        String selected = listCategories.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Potwierdzenie usunięcia
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć kategorię i przypisane do niej produkty?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                dbService.deleteCategory(selected);
                refreshCategoriesList();
                loadDataFromDB();
                setStatus("Usunięto kategorię.");
            } catch (Exception e) { showAlert("Błąd", "Nie udało się usunąć kategorii."); }
        }
    }

    // --- METODY KONFIGURACYJNE (STANDARDOWE) ---

    /** Konfiguruje kolumny tabeli głównej. */
    private void setupTable() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        // Niestandardowe renderowanie kolumny dostępności (kolorowanie tekstu)
        colAvailable.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item ? "Tak" : "Nie");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: red;");
                } else setText(null);
            }
        });

        // Obsługa wyboru wiersza - wypełnienie formularza
        tableView.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> { if(nv!=null) fillForm(nv); });
    }

    /** Konfiguruje filtrowanie (wyszukiwanie) w czasie rzeczywistym. */
    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, p -> true);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(dp -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                // Szukanie po nazwie produktu LUB kategorii
                return dp.getProduct().toLowerCase().contains(lower) || dp.getCategory().toLowerCase().contains(lower);
            });
        });

        // Powiązanie posortowanej listy z tabelą
        SortedList<DataPoint> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    @FXML private void handleClear() {
        txtProduct.clear(); txtCategory.clear(); txtQuantity.clear(); txtPrice.clear();
        chkAvailable.setSelected(false); tableView.getSelectionModel().clearSelection();
    }

    /** Waliduje pola tekstowe i tworzy obiekt DataPoint. */
    private DataPoint readFormWithValidation() {
        if (txtProduct.getText().isEmpty() || txtCategory.getText().isEmpty())
            throw new IllegalArgumentException("Nazwa i Kategoria są wymagane.");
        // Zamiana przecinka na kropkę dla liczb zmiennoprzecinkowych
        return new DataPoint(txtProduct.getText(), txtCategory.getText(),
                Integer.parseInt(txtQuantity.getText()),
                Double.parseDouble(txtPrice.getText().replace(",", ".")),
                chkAvailable.isSelected());
    }

    private void fillForm(DataPoint dp) {
        txtProduct.setText(dp.getProduct()); txtCategory.setText(dp.getCategory());
        txtQuantity.setText(String.valueOf(dp.getQuantity()));
        txtPrice.setText(String.valueOf(dp.getPrice())); chkAvailable.setSelected(dp.isAvailable());
    }

    private void refreshCategoriesList() {
        try { categoriesData.setAll(dbService.getAllCategories()); listCategories.setItems(categoriesData); } catch (Exception e) {}
    }

    @FXML private void handleAddCategory() {
        try {
            dbService.addCategory(txtNewCategory.getText().trim());
            txtNewCategory.clear(); refreshCategoriesList();
        } catch (Exception e) {}
    }

    /** Zapisuje bieżący stan listy do bazy danych (nadpisując dane). */
    @FXML private void handleSaveToDB() {
        try {
            dbService.saveToDatabase(masterData);
            refreshCategoriesList();
            setStatus("Zapisano dane do bazy.");
        } catch (Exception e) { showAlert("Błąd zapisu", e.getMessage()); }
    }

    @FXML private void handleExit() { Platform.exit(); }

    /** Konfiguracja wykresu słupkowego. */
    private void setupChart() {
        comboChartType.setItems(FXCollections.observableArrayList("Ilość", "Cena", "Wartość Całkowita"));
        comboChartType.setValue("Ilość");
        comboChartType.setOnAction(e -> updateChart());
    }

    /** Aktualizuje dane na wykresie na podstawie wybranego typu. */
    private void updateChart() {
        barChart.getData().clear();
        String type = comboChartType.getValue();
        if(type==null) return;

        axisY.setLabel(type);
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName(type);

        for(DataPoint dp : masterData) {
            Number v = type.equals("Ilość") ? dp.getQuantity() :
                    (type.equals("Cena") ? dp.getPrice() : dp.getTotalValue());
            s.getData().add(new XYChart.Data<>(dp.getProduct(), v));
        }
        barChart.getData().add(s);
    }

    private void setStatus(String msg) { lblStatus.setText(msg); }

    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setContentText(c); a.showAndWait();
    }
}