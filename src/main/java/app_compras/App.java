package app_compras;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;

        // Carga la vista inicial (login)
        cargarVista("views/login");
        stage.setTitle("Gestor de Compras del Hogar");
        stage.show();
    }

    /**
     * Carga una vista FXML y ajusta automáticamente el tamaño de la ventana.
     * Cada vista usará su prefWidth y prefHeight definidos en el FXML.
     */
    public static void setRoot(String fxml) throws IOException {
        cargarVista(fxml);
    }

    /**
     * Método reutilizable para cambiar la escena actual.
     */
    private static void cargarVista(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/" + fxml + ".fxml"));
        Parent root = loader.load();

        Scene newScene = new Scene(root);
        mainStage.setScene(newScene);
        mainStage.sizeToScene(); // 🔹 Ajusta automáticamente al tamaño del FXML
        mainStage.centerOnScreen(); // 🔹 Centra la ventana en pantalla
    }

    public static void main(String[] args) {
        launch();
    }
}
