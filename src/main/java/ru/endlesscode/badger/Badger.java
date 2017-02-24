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

    public static void gotoCreatingNew() {
        try {
            instance.replaceSceneContent("CreateNewProject");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean openCreateDirDialog(String message, File path) {
        message = String.format(message, path.getAbsolutePath());

        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Каталог не найден");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(getPrimaryStage());

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(getPrimaryStage());

        alert.showAndWait();
    }

    private void replaceSceneContent(String viewName) throws IOException {
        Scene scene = new Scene(loadViewFromFXML(viewName));
        stage.setScene(scene);
        stage.sizeToScene();
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
}
