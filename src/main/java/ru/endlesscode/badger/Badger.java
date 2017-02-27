package ru.endlesscode.badger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Badger extends Application {
    private static Badger instance;

    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    public static void gotoCreatingNew() {
        try {
            instance.replaceSceneContent("CreateNewProject");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean openProjectExistsDialog(File path) {
        String message = String.format("Папка \'%s\' не пуста, вы уверены, что хотите продолжить " +
                "(всё содержимое папки будет удалено)?", path.getAbsolutePath());
        return showWarning("Папка не пуста", message);
    }

    public static boolean openCreateDirDialog(File path) {
        String message = String.format("Каталог \'%s\' не найден. Хотите создать его?", path.getAbsolutePath());
        return showWarning("Каталог не найден", message);
    }

    private static boolean showWarning(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = showAlert(alert, header);

        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static Optional<ButtonType> showAlert(Alert alert, String header) {
        alert.setHeaderText(header);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(getPrimaryStage());

        return alert.showAndWait();
    }

    private static Parent loadViewFromFXML(String viewName) throws IOException {
        viewName = "/view/" + viewName + ".fxml";

        FXMLLoader loader = new FXMLLoader(Badger.class.getResource(viewName));
        return loader.load();
    }

    @Contract(pure = true)
    public static Stage getPrimaryStage() {
        return instance.stage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        stage = primaryStage;
        stage.setTitle("Badger");
        gotoStart();
        stage.show();
    }

    private void gotoStart() {
        try {
            replaceSceneContent("Start");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceSceneContent(String viewName) throws IOException {
        Scene scene = new Scene(loadViewFromFXML(viewName));
        stage.setScene(scene);
        stage.sizeToScene();
    }
}
