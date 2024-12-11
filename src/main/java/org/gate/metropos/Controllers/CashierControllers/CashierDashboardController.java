package org.gate.metropos.Controllers.CashierControllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class CashierDashboardController {
    @FXML private Button dashboardBtn;
    @FXML private Button productsBtn;
    @FXML private Button salesInvoicesBtn;
    @FXML private Button logoutBtn;
    @FXML private StackPane contentArea;

    @FXML
    private void initialize() {
        dashboardBtn.setOnAction(e -> showDashboard());
        productsBtn.setOnAction(e -> showProducts());
        salesInvoicesBtn.setOnAction(e -> showSalesInvoices());
        logoutBtn.setOnAction(e -> handleLogout());

        // Show dashboard by default
        showDashboard();
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos" + fxml));
            if (loader.getLocation() == null) {
                System.err.println("Could not find FXML file: " + fxml);
                return;
            }
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading dashboard");
            System.err.println("Error loading " + fxml + ": " + e.getMessage());
        }
    }


    private void showDashboard() {
        loadView("/CashierScreens/dashboardContent.fxml");
    }

    private void showProducts() {
        contentArea.getChildren().clear();
        loadView("/CashierScreens/viewProducts.fxml");
        updateActiveButton(productsBtn);
    }


    private void showSalesInvoices() {
        contentArea.getChildren().clear();
        loadView("/CashierScreens/manageSales.fxml");
        updateActiveButton(salesInvoicesBtn);
    }

    private void updateActiveButton(Button activeButton) {
        dashboardBtn.getStyleClass().remove("active");
        productsBtn.getStyleClass().remove("active");
        salesInvoicesBtn.getStyleClass().remove("active");

        activeButton.getStyleClass().add("active");
    }

    private void handleLogout() {
        // TODO: Implement logout
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
