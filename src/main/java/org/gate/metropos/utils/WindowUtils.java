package org.gate.metropos.utils;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WindowUtils {
    public  static void ResizeDashboardWindow(StackPane contentArea) {
        Platform.runLater(() -> {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Set window size to screen size
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            // Center the window
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

            stage.setResizable(true);
            stage.initStyle(StageStyle.DECORATED);
        });
    }


}
