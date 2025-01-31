package org.gate.metropos.Controllers.DataEntryOperator;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.WindowUtils;

import java.io.IOException;

public class DataEntryDashboardController {
    @FXML private Button dashboardBtn;
    @FXML private Button productsBtn;
    @FXML private Button suppliersBtn;
    @FXML private Button purchaseInvoicesBtn;
    @FXML private Button logoutBtn;
    @FXML private StackPane contentArea;

    @FXML
    private void initialize() {
        dashboardBtn.setOnAction(e -> showDashboard());
        productsBtn.setOnAction(e -> showProducts());
        suppliersBtn.setOnAction(e -> showSuppliers());
        purchaseInvoicesBtn.setOnAction(e -> showPurchaseInvoices());
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
            WindowUtils.ResizeDashboardWindow(contentArea);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading dashboard");
            System.err.println("Error loading " + fxml + ": " + e.getMessage());
        }
    }


    private void showDashboard() {
        loadView("/dataEntryScreens/dashboardContent.fxml");
    }

    private void showProducts() {
        contentArea.getChildren().clear();
        loadView("/dataEntryScreens/manage-products.fxml");
        updateActiveButton(productsBtn);
    }

    private void showSuppliers() {
        contentArea.getChildren().clear();
        loadView("/BranchManagerScreens/manage-suppliers.fxml");
        updateActiveButton(suppliersBtn);
    }

    private void showPurchaseInvoices() {
        contentArea.getChildren().clear();
        loadView("/dataEntryScreens/managePurchaseInvoices.fxml");
        updateActiveButton(purchaseInvoicesBtn);
    }

    private void updateActiveButton(Button activeButton) {
        dashboardBtn.getStyleClass().remove("active");
        productsBtn.getStyleClass().remove("active");
        suppliersBtn.getStyleClass().remove("active");
        purchaseInvoicesBtn.getStyleClass().remove("active");

        activeButton.getStyleClass().add("active");
    }

    private void handleLogout() {
        if (!AlertUtils.showConfirmation("Are you sure you want to logout ? " ))
            return;
        EmployeeService em = new EmployeeService();
        em.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/login.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();

        } catch (IOException e) {
            AlertUtils.showError("Error loading login screen");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
