package org.gate.metropos.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertUtils {
    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Convenience methods for common alerts
    public static void showError(String content) {
        showAlert(Alert.AlertType.ERROR, "Error", null, content);
    }

    public static void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, null, content);
    }

    public static void showSuccess(String content) {
        showAlert(Alert.AlertType.INFORMATION, "Success", null, content);
    }

    public static void showSuccess(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, null, content);
    }

    public static void showWarning(String content) {
        showAlert(Alert.AlertType.WARNING, "Warning", null, content);
    }

    public static boolean showConfirmation(String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
