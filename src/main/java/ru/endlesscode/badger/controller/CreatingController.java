package ru.endlesscode.badger.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.badger.Badger;
import ru.endlesscode.badger.util.FileUtil;

import java.io.File;

public class CreatingController {
    @FXML
    public TextField projectPathField;
    @FXML
    public TextField projectNameField;
    @FXML
    public CheckBox createProjectFolder;
    @FXML
    public Button nextButton;

    private final DirectoryChooser directoryChooser;

    private String projectPath;

    public CreatingController() {
        directoryChooser = new DirectoryChooser();
    }

    @FXML
    public void initialize() {
        setProjectPath(FileUtil.getWorkspacePath());
        setFiltersForTextFields();
        setListenersForTextFields();
    }

    private void setFiltersForTextFields() {
        EventHandler<KeyEvent> filter = event -> {
            String typedChar = event.getCharacter();
            if (!typedChar.matches("\\w|\\s|-") && !typedChar.equals(File.separator)) {
                event.consume();
            }
        };
        EventHandler<KeyEvent> filterIncludingSeparator = event -> {
            String typedChar = event.getCharacter();
            if (typedChar.equals(File.separator)) {
                event.consume();
            } else {
                filter.handle(event);
            }
        };

        projectPathField.addEventFilter(KeyEvent.KEY_TYPED, filter);
        projectNameField.addEventFilter(KeyEvent.KEY_TYPED, filterIncludingSeparator);
    }

    private void setListenersForTextFields() {
        projectNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                trimTextField(projectNameField);
            }
        });
        projectNameField.textProperty().addListener((observable, oldValue, newValue) -> setProjectPath(projectPath));
        projectPathField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                trimTextField(projectPathField);

                String projectPath = trimmedProjectPath();
                String projectName = trimmedProjectName();
                if (createProjectFolder.isSelected()) {
                    projectPath = projectPath.replaceAll(FileUtil.getQuotedSeparator() + projectName + "$", "");
                }

                setProjectPath(projectPath);
            }
        });
        projectPathField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.endsWith(trimmedProjectName())) {
                createProjectFolder.setSelected(true);
            } else {
                createProjectFolder.setSelected(false);
            }
        });
    }

    private void trimTextField(TextField textField) {
        String projectPath = textField.getText();
        textField.setText(projectPath.trim());
    }

    public void browseProjectDirectory(ActionEvent actionEvent) {
        directoryChooser.setInitialDirectory(new File(projectPathField.getText()).getParentFile());

        File chosenPath = chooseDirectory();
        if (chosenPath != null) {
            setProjectPath(chosenPath.getAbsolutePath());
        }
    }

    @Nullable
    private File chooseDirectory() {
        boolean pathBuilt = FileUtil.buildDirectory(projectPath);
        return pathBuilt ? directoryChooser.showDialog(Badger.getPrimaryStage()) : null;
    }

    public void onCreateProjectFolderChanged(ActionEvent actionEvent) {
        setProjectPath(projectPath);
    }

    private void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
        projectPathField.setText(appendProjectDirToPath());
    }

    private String appendProjectDirToPath() {
        if (!createProjectFolder.isSelected()) {
            return projectPath;
        }

        return projectPath + File.separator + trimmedProjectName();
    }

    private String trimmedProjectName() {
        String projectName = projectNameField.getText();

        // Remove all first numbers
        while (!projectName.isEmpty() && Character.isDigit(projectName.charAt(0))) {
            projectName = projectName.substring(1);
        }

        // Removing all spaces
        return projectName.replaceAll("\\s", "").trim();
    }

    private String trimmedProjectPath() {
        String projectPath = projectPathField.getText();

        // Remove all bad separator chars
        projectPath = projectPath.replaceAll(FileUtil.getQuotedSeparator() + "{2,}", FileUtil.getQuotedSeparator());
        projectPath = projectPath.replaceAll(FileUtil.getQuotedSeparator() + "$", "");

        return projectPath.trim();
    }
}
