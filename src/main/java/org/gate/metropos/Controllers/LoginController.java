package org.gate.metropos.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.User;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.SuperAdminService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;
import org.gate.metropos.utils.SessionManager;

import java.io.IOException;


public class LoginController {
    @FXML
    private ComboBox<String> userTypeComboBox;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    private final SuperAdminService superAdminService ;
    private final EmployeeService employeeService ;

    public LoginController () {
        superAdminService = new SuperAdminService();
        employeeService = new EmployeeService();
    }

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
        showDataEntryDashboard();
        if (!validateInputs()) {
            return;
        }

        String userType = userTypeComboBox.getValue();
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Handle Super Admin login
        if (userType.equals("Super Admin")) {
            User superAdmin = superAdminService.login(username, password);
            if (superAdmin != null) {
                SessionManager.initSuperAdminSession(superAdmin);
                showAdminDashboard();
            } else {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "Invalid Credentials", "Please provide valid credentials");
            }
        }
        // Handle Employee login (Branch Manager, Cashier, Data Entry)
        else {
            System.out.println(username + "     " + password);
            ServiceResponse<Employee> response = employeeService.login(username, password);
            if (response.isSuccess()) {
                Employee employee = response.getData();
                SessionManager.initEmployeeSession(employee);

                if (employee.isFirstTime()) {
//                    showUpdateYourPassword();
                }

                    switch (employee.getRole()) {
                        case BRANCH_MANAGER:
                            showBranchManagerDashboard();
                            break;
                        case CASHIER:
                            showCashierScreen();
                            break;
                        case DATA_ENTRY_OPERATOR:
                            showDataEntryDashboard();
                            break;
                    }

            } else {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "Invalid Credentials", "Please provide valid credentials");
            }
        }
    }

    // Add logout method
    public void logout() {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/login.fxml"));
            Scene loginScene = new Scene(loader.load());
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("Metro POS | Login");
            currentStage.show();
        } catch (IOException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error",
                    "Logout Failed", "Could not return to login screen");
        }
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

    private void showAdminDashboard() {
        try {
            // Load the Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/SuperAdminScreens/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get the current stage from any control (using loginButton here)
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.setTitle("Metro POS | Admin Dashboard");
            // Create new scene with dashboard
            Scene dashboardScene = new Scene(dashboardRoot);

            // Set the new scene on current stage
            currentStage.setScene(dashboardScene);
            currentStage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not load Dashboard");
            alert.setContentText("An error occurred while loading the Dashboard: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }

    }

    private void showBranchManagerDashboard() {
        try {
            // Load the Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/BranchManagerScreens/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get the current stage from any control (using loginButton here)
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.setTitle("Metro POS | Branch Manager");
            // Create new scene with dashboard
            Scene dashboardScene = new Scene(dashboardRoot);

            currentStage.setScene(dashboardScene);
            currentStage.show();
        } catch (IOException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR , "Navigation Error" , "Could not load Dashboard" , "An error occurred while loading the Dashboard: " + e.getMessage());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private void showCashierScreen() {

    }
    private void showDataEntryDashboard() {
        try {
            // Load the Data Entry Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/DataEntryScreens/DataEntryDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get the current stage from loginButton
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.setTitle("Metro POS | Data Entry Dashboard");

            // Create new scene with dashboard
            Scene dashboardScene = new Scene(dashboardRoot);

            // Set the new scene on current stage
            currentStage.setScene(dashboardScene);
            currentStage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not load Data Entry Dashboard");
            alert.setContentText("An error occurred while loading the Dashboard: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private void showUpdateYourPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/EmployeeScreens/change-password.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Change Password");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // After password change, proceed to appropriate dashboard
            // Add your dashboard navigation logic here

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load password change window");
            alert.showAndWait();
            e.printStackTrace();
        }
    }









}
