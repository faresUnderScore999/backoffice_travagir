package frontoffice.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {
    private static Stage stage;
    private static frontoffice.controllers.MainLayoutController mainLayoutController;

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void goToLogin() {
        setRoot("/frontoffice/views/loginView.fxml", 1376, 768);
    }

    public static void goToRegister() {
        setRoot("/frontoffice/views/registerView.fxml", 1376, 768);
    }

    public static void goToDashboard() {
        setRoot("/frontoffice/views/MainLayout.fxml", 1600, 768);
    }

    public static void setMainLayoutController(frontoffice.controllers.MainLayoutController controller) {
        mainLayoutController = controller;
    }

    public static frontoffice.controllers.MainLayoutController getMainLayoutController() {
        return mainLayoutController;
    }

    private static void setRoot(String fxmlPath, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

