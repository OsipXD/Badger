package ru.endlesscode.badger.util;

import javafx.scene.control.Alert;
import ru.endlesscode.badger.Badger;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

public class FileUtil {
    public static String getWorkspacePath() {
        return System.getProperty("user.dir") + File.separatorChar + "Workspace";
    }

    public static String getQuotedSeparator() {
        return Matcher.quoteReplacement(File.separator);
    }

    public static boolean buildDirectory(String path) {
        try {
            return requestDirectoryBuilding(path);
        } catch (Exception e) {
            Badger.showAlert(Alert.AlertType.ERROR, e.getMessage(), path);
            return false;
        }
    }

    private static boolean requestDirectoryBuilding(String path) throws IOException {
        File directory = new File(path);
        if (directory.exists()) {
            return true;
        }

        boolean answer = Badger.openCreateDirDialog("Путь не найден: %s. Хотите создать его?", directory);
        if (answer && !directory.mkdirs()) {
            throw new IOException("Не удалось создать папку.");
        }

        return answer;
    }
}
