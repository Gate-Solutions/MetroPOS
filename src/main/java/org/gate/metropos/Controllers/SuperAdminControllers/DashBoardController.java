package org.gate.metropos.Controllers.SuperAdminControllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.gate.metropos.utils.WindowUtils;

import java.io.IOException;
import java.util.Optional;

public class DashBoardController {
    @FXML private Button dashboardBtn;
    @FXML private Button branchesBtn;
    @FXML private Button usersBtn;
    @FXML private Button reportsBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        loadView("home.fxml");
        WindowUtils.ResizeDashboardWindow(contentArea);
        dashboardBtn.setOnAction(e -> loadView("home.fxml"));
        branchesBtn.setOnAction(e -> loadView("branches.fxml"));
        usersBtn.setOnAction(e -> loadView("users.fxml"));
        reportsBtn.setOnAction(e -> loadView("reports.fxml"));
        settingsBtn.setOnAction(e -> loadView("settings.fxml"));
        logoutBtn.setOnAction(e -> handleLogout());
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/SuperAdminScreens/" + fxml));
            if (loader.getLocation() == null) {
                System.err.println("Could not find FXML file: " + fxml);
                return;
            }
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading " + fxml + ": " + e.getMessage());
        }
    }

    private void handleLogout() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Logout");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/login.fxml"));
                Scene loginScene = new Scene(loader.load());

                Stage currentStage = (Stage) logoutBtn.getScene().getWindow();

                currentStage.setScene(loginScene);
                currentStage.setTitle("Login");
                currentStage.setWidth(900);
                currentStage.setHeight(600);
                currentStage.centerOnScreen();



            } catch (IOException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Failed to load login screen");
                errorAlert.showAndWait();
            }
        }
    }
}
