package org.gate.metropos.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML
    private ComboBox<String> userTypeComboBox;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        userTypeComboBox.getItems().addAll(
                "Super Admin",
                "Branch Manager",
                "Cashier",
                "Data Entry Operator"
        );

        loginButton.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        if (!validateInputs()) {
            return;
        }

        String userType = userTypeComboBox.getValue();
        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println(userType + " " + username + " " + password);
        // Add login logic here
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (userTypeComboBox.getValue() == null) {
            errorMessage.append("Please select a user type\n");
        }

        if (usernameField.getText().trim().isEmpty()) {
            errorMessage.append("Username cannot be empty\n");
        }

        if (passwordField.getText().trim().isEmpty()) {
            errorMessage.append("Password cannot be empty\n");
        }

        if (!errorMessage.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }
}
