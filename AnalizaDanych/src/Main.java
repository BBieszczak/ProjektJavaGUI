import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Główna klasa startowa aplikacji.
 * Odpowiada za załadowanie pliku widoku FXML i wyświetlenie okna.
 */
public class Main extends Application {

    /**
     * Metoda startowa JavaFX.
     * Ładuje zasób "dashboard.fxml" i ustawia scenę główną.
     *
     * @param primaryStage Główne okno (scena) aplikacji.
     * @throws Exception Wyrzucany w przypadku problemów z ładowaniem pliku FXML.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("System Analizy Danych - Bartosz Bieszczak");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Punkt wejścia do programu (metoda main).
     * Uruchamia aplikację JavaFX.
     *
     * @param args Argumenty wiersza poleceń.
     */
    public static void main(String[] args) {
        launch(args);
    }
}