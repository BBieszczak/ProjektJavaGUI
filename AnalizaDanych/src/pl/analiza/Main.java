package pl.analiza;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Główna klasa startowa aplikacji "System Analizy Danych".
 * <p>
 * Odpowiada za inicjalizację środowiska JavaFX, załadowanie głównego widoku (pliku FXML)
 * oraz wyświetlenie okna aplikacji użytkownikowi.
 * </p>
 */
public class Main extends Application {

    /**
     * Metoda startowa cyklu życia aplikacji JavaFX.
     * <p>
     * Jest wywoływana automatycznie po uruchomieniu aplikacji.
     * Ładuje zasób "dashboard.fxml", tworzy scenę i ustawia tytuł okna.
     * </p>
     *
     * @param primaryStage Główne okno (scena) aplikacji dostarczone przez platformę JavaFX.
     * @throws Exception Wyrzucany w przypadku problemów z ładowaniem pliku widoku (np. brak pliku .fxml).
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ładowanie pliku widoku z zasobów (ścieżka musi zaczynać się od /)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
        Parent root = loader.load();

        // Konfiguracja głównego okna
        primaryStage.setTitle("System Analizy Danych - Bartosz Bieszczak");
        primaryStage.setScene(new Scene(root));

        // Wyświetlenie okna użytkownikowi
        primaryStage.show();
    }

    /**
     * Główny punkt wejścia do programu (metoda main).
     * <p>
     * Uruchamia metodę {@link #launch(String...)}, która inicjuje środowisko JavaFX.
     * </p>
     *
     * @param args Argumenty wiersza poleceń przekazane podczas uruchamiania programu.
     */
    public static void main(String[] args) {
        launch(args);
    }
}