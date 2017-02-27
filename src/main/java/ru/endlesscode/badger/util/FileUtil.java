package ru.endlesscode.badger.util;

import javafx.scene.control.Alert;
import ru.endlesscode.badger.Badger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;

public class FileUtil {
    public static String getWorkspacePath() {
        return System.getProperty("user.dir") + File.separatorChar + "Workspace";
    }

    public static String getQuotedSeparator() {
        return Matcher.quoteReplacement(File.separator);
    }

    public static boolean requestDirectoryBuilding(String path) {
        File directory = new File(path);
        if (directory.exists()) {
            return true;
        }

        boolean answer = Badger.openCreateDirDialog(directory);
        return answer && buildDirectory(directory);
    }

    public static boolean buildDirectory(File directory) {
        if (!directory.mkdirs()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, directory.getAbsolutePath());
            Badger.showAlert(alert, "Не удалось создать папку.");
            return false;
        }

        return true;
    }

    public static boolean directoryIsEmpty(File directory) {
        String[] dirContent = directory.list();
        return dirContent == null || dirContent.length == 0;
    }

    public static void deleteDirectoryContent(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            delete(file);
        }
    }

    private static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectoryContent(file);
        }

        Files.delete(file.toPath());
    }

    public static String trimDirectoryName(String name) {
        // Remove all first numbers
        while (!name.isEmpty() && Character.isDigit(name.charAt(0))) {
            name = name.substring(1);
        }

        // Removing all spaces
        return name.replaceAll("\\s", "").trim();
    }

    public static String trimPath(String path) {
        path = path.replaceAll(FileUtil.getQuotedSeparator() + "{2,}", FileUtil.getQuotedSeparator());
        path = path.replaceAll(FileUtil.getQuotedSeparator() + "$", "");

        return path.trim();
    }
}
