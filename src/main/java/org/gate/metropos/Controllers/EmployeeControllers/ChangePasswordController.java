package org.gate.metropos.Controllers.EmployeeControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.ServiceResponse;

public class ChangePasswordController {
    @FXML
    private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button saveButton;

    private final EmployeeService employeeService;

    public ChangePasswordController() {
        this.employeeService = new EmployeeService();
    }

    @FXML
    public void initialize() {
        saveButton.setOnAction(e -> handlePasswordChange());
    }

    private void handlePasswordChange() {
        if (!validateInputs()) {
            return;
        }

        System.out.println(employeeService.getLoggedInEmployee().getPassword());
//        ServiceResponse<Void> response = employeeService.updatePassword(
//                employeeService.getLoggedInEmployee().getId(),
//                employeeService.getLoggedInEmployee().getPassword(), // old password
//                newPasswordField.getText()
//        );
//
//        if (response.isSuccess()) {
//            showAlert(Alert.AlertType.INFORMATION, "Success", response.getMessage());
//            closeWindow();
//        } else {
//            showAlert(Alert.AlertType.ERROR, "Error", response.getMessage());
//        }
    }

    private boolean validateInputs() {
        if (newPasswordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "New password is required");
            return false;
        }

        if (newPasswordField.getText().length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password must be at least 6 characters");
            return false;
        }

        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}