package org.gate.metropos.Controllers.EmployeeControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.gate.metropos.models.Employee;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.NavigationUtil;
import org.gate.metropos.utils.ServiceResponse;

public class FirstTimePasswordController {
    @FXML
    private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button updateButton;
    @FXML private VBox mainContainer;

    private final EmployeeService employeeService;
    private Employee currentEmployee;

    public FirstTimePasswordController() {
        employeeService = new EmployeeService();
    }

    @FXML
    public void initialize() {

        setupStyles();
        setupActions();
        Platform.runLater(() -> {
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
        });
    }

    private void setupStyles() {
        mainContainer.getStyleClass().add("add-branch-container");
        mainContainer.setSpacing(20);
        mainContainer.setPadding(new Insets(30));

        updateButton.getStyleClass().add("primary-button");

    }

    private void setupActions() {
        updateButton.setOnAction(e -> handlePasswordUpdate());

    }

    private void handlePasswordUpdate() {
        if (!validateInputs()) return;

        ServiceResponse<Void> response = employeeService.updatePassword(
                currentEmployee.getId(),
                currentPasswordField.getText(),
                newPasswordField.getText()
        );

        if (response.isSuccess()) {
            AlertUtils.showSuccess("Password updated successfully!");
            navigateToDashboard();
        } else {
            AlertUtils.showError(response.getMessage());
        }
    }

    private boolean validateInputs() {
        if (currentPasswordField.getText().isEmpty() ||
                newPasswordField.getText().isEmpty() ||
                confirmPasswordField.getText().isEmpty()) {
            AlertUtils.showError("All fields are required");
            return false;
        }

        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            AlertUtils.showError("New passwords don't match");
            return false;
        }

        if (newPasswordField.getText().length() < 6) {
            AlertUtils.showError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    public void setEmployee(Employee employee) {
        this.currentEmployee = employee;
    }

    private void navigateToDashboard() {

        switch (currentEmployee.getRole()) {
            case BRANCH_MANAGER:
                NavigationUtil.showBranchManagerDashboard((Stage) updateButton.getScene().getWindow());
                break;
            case CASHIER:
                NavigationUtil.showCashierScreen((Stage) updateButton.getScene().getWindow());
                break;
            case DATA_ENTRY_OPERATOR:
                NavigationUtil.showDataEntryDashboard((Stage) updateButton.getScene().getWindow());
                break;
        }
    }
}
