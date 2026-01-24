import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Główna klasa aplikacji dziedzicząca po Application z JavaFX
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Wczytanie widoku z pliku .fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        Parent root = loader.load();

        // Ustawienie tytułu i sceny
        primaryStage.setTitle("System Analizy Danych - Bartosz Bieszczak");
        primaryStage.setScene(new Scene(root));
        primaryStage.show(); // Wyświetlenie okna
    }

    public static void main(String[] args) {
        launch(args); // Uruchomienie cyklu życia JavaFX
    }
}