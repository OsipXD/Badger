package ru.endlesscode.badger.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.badger.Badger;
import ru.endlesscode.badger.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CreatingController {
    @FXML
    public TextField projectPathField;
    @FXML
    public TextField projectNameField;
    @FXML
    public CheckBox createProjectFolder;
    @FXML
    public Button nextButton;
    @FXML
    public TabPane stepTabs;

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
        projectNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            setProjectPath(projectPath);
            nextButton.setDisable(newValue.trim().isEmpty());
        });
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
        boolean pathBuilt = FileUtil.requestDirectoryBuilding(projectPath);
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

    public void onNextClicked(ActionEvent actionEvent) {
        Step currentStep = getCurrentStep();
        switch (currentStep) {
            case NAME:
                boolean projectCreated = buildProjectDir();
                if (!projectCreated) {
                    return;
                }
                break;
        }

        gotoNextTab();
    }

    private boolean buildProjectDir() {
        boolean result = FileUtil.requestDirectoryBuilding(projectPath);
        if (!result) {
            return false;
        }

        File projectDir = new File(projectPathField.getText());

        if (!projectDir.isDirectory()) {
            result = FileUtil.buildDirectory(projectDir);
        } else if (!FileUtil.directoryIsEmpty(projectDir)) {
            boolean deleteFiles = Badger.openProjectExistsDialog(projectDir);
            if (deleteFiles) {
                tryToDeleteProjectDirContent(projectDir);
            }
        }

        return result;
    }

    private void tryToDeleteProjectDirContent(File projectDir) {
        try {
            FileUtil.deleteDirectoryContent(projectDir);
        } catch (IOException ignored) {}
    }

    private void gotoNextTab() {
        List<Tab> tabs = stepTabs.getTabs();

        int selectedTab = getSelectedTabId();
        int nextTab = selectedTab + 1;
        if (nextTab == tabs.size()) {
            return;
        }
        if (nextTab + 1 == tabs.size()) {
            nextButton.setText("Готово");
        }

        tabs.get(selectedTab).setDisable(true);
        tabs.get(nextTab).setDisable(false);
        stepTabs.getSelectionModel().select(nextTab);
    }

    private Step getCurrentStep() {
        return Step.values()[getSelectedTabId()];
    }

    private int getSelectedTabId() {
        return stepTabs.getSelectionModel().getSelectedIndex();
    }

    private enum Step {
        NAME, DESIGN, END
    }
}
