package org.gate.metropos.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SplashScreenController implements Initializable {

    @FXML
    private ProgressBar loadingProgress;

    @FXML
    private Label loadingLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadingProgress.setProgress(0);
        startLoading();
    }

    private void startLoading() {
        Timeline timeline = new Timeline();

        KeyValue keyValue = new KeyValue(loadingProgress.progressProperty(), 1);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(5), event -> {
            updateLoadingText(loadingProgress.getProgress());
        }, keyValue);

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(1);

        // Set what happens when timeline finishes
        timeline.setOnFinished(event -> {
            loadLoginScreen();
        });

        loadingProgress.progressProperty().addListener((observable, oldValue, newValue) -> {
            updateLoadingText(newValue.doubleValue());
        });

        timeline.play();
    }

    private void loadLoginScreen() {
        try {
            // Load the login FXML

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/login.fxml"));
            Parent loginRoot = loader.load();

            // Get the current stage (splash screen stage)
            Stage currentStage = (Stage) loadingProgress.getScene().getWindow();
            currentStage.hide();
            currentStage = new Stage();
            // Load the login scene
            Scene loginScene = new Scene(loginRoot);
            currentStage.setScene(loginScene);
            currentStage.centerOnScreen();
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLoadingText(double progress) {
        if (progress < 0.3) {
            loadingLabel.setText("Initializing system...");
        } else if (progress < 0.6) {
            loadingLabel.setText("Loading components...");
        } else if (progress < 0.8) {
            loadingLabel.setText("Preparing application...");
        } else if (progress < 1) {
            loadingLabel.setText("Almost ready...");
        } else {
            loadingLabel.setText("Welcome to MetroPOS!");
        }
    }

    // Method to launch the splash screen
    public static void showSplashScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(SplashScreenController.class.getResource("/org/gate/metropos/splashScreen.fxml"));
            Parent splashRoot = loader.load();

            // Set up the stage
            Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED); // Remove the top bar
            splashStage.setScene(new Scene(splashRoot));
            splashStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
