package frontoffice;

import frontoffice.utils.SceneNavigator;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Travagir - Frontoffice");
        try {
            String iconUrl = "https://res.cloudinary.com/dzxxigjkk/image/upload/v1770949791/images_qlsaxx.png";
            primaryStage.getIcons().add(new Image(iconUrl));
        } catch (Exception ignored) {
        }

        SceneNavigator.init(primaryStage);
        SceneNavigator.goToLogin();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
