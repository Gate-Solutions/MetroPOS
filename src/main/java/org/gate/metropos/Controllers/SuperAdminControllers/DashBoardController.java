package org.gate.metropos.Controllers.SuperAdminControllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;

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
        System.out.println("Logout clicked");
    }
}
