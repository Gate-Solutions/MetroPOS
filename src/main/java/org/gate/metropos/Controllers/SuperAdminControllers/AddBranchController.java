package org.gate.metropos.Controllers.SuperAdminControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.services.BranchService;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.math.BigDecimal;

public class AddBranchController {
    private final BranchService branchService;


    //add-Branch
    @FXML private TextField branchCodeField;
    @FXML private TextField nameField;
    @FXML private TextField cityField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;


    //add Manager
    @FXML private TextField branchIdField;
    @FXML private TextField managerBranchCodeField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField managerNameField;
    @FXML private TextField employeeNoField;
    @FXML private TextField salaryField;


    private final EmployeeService employeeService;
    private Long createdBranchId;
    private String createdBranchCode;

    public AddBranchController () {
       employeeService = new EmployeeService();
        branchService = new BranchService();
    }



    @FXML
    public void initialize() {
        if (branchCodeField != null) {
            // Branch form initialization
            cancelBtn.setOnAction(e -> closeWindow());
            saveBtn.setOnAction(e -> saveBranch());
            Platform.runLater(() -> branchCodeField.getParent().requestFocus());
        } else {
            // Manager form initialization
            cancelBtn.setOnAction(e -> closeWindow());
            saveBtn.setOnAction(e -> saveManager());
        }
    }


    private void saveManager() {
        if (!validateManagerInputs()) {
            return;
        }
        try {
            BigDecimal salary = new BigDecimal(salaryField.getText().trim());

            ServiceResponse<Employee> response = employeeService.createEmployee(
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                   " ",
                    UserRole.BRANCH_MANAGER,
                    managerNameField.getText().trim(),
                    employeeNoField.getText().trim(),
                    salary,
                    createdBranchId
            );

            if (response.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Branch manager added successfully");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", response.getMessage());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add branch manager: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
    private boolean validateBranchInputs() {
        StringBuilder errors = new StringBuilder();

        // Branch Code validation
        if (branchCodeField.getText().trim().isEmpty()) {
            errors.append("Branch Code is required\n");
        } else if (!branchCodeField.getText().matches("^BR\\d{3}$")) {
            errors.append("Branch Code must be in format BR followed by 3 digits (e.g., BR001)\n");
        }

        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            errors.append("Branch Name is required\n");
        } else if (nameField.getText().length() < 3) {
            errors.append("Branch Name must be at least 3 characters\n");
        }

        // City validation
        if (cityField.getText().trim().isEmpty()) {
            errors.append("City is required\n");
        }

        // Address validation
        if (addressField.getText().trim().isEmpty()) {
            errors.append("Address is required\n");
        }

        // Phone validation
        if (phoneField.getText().trim().isEmpty()) {
            errors.append("Phone number is required\n");
        } else if (!phoneField.getText().matches("^\\+?[0-9-]{10,}$")) {
            errors.append("Please enter a valid phone number\n");
        }

        // Show error alert if there are validation errors
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }
    private boolean validateManagerInputs() {
        StringBuilder errors = new StringBuilder();

        if (usernameField.getText().trim().isEmpty()) {
            errors.append("Username is required\n");
        }
        if (emailField.getText().trim().isEmpty()) {
            errors.append("Email is required\n");
        } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("Invalid email format\n");
        }
        if (managerNameField.getText().trim().isEmpty()) {
            errors.append("Manager name is required\n");
        }
        if (employeeNoField.getText().trim().isEmpty()) {
            errors.append("Employee number is required\n");
        }
        if (salaryField.getText().trim().isEmpty()) {
            errors.append("Salary is required\n");
        } else {
            try {
                new BigDecimal(salaryField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("Invalid salary format\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }
        return true;
    }
        private void saveBranch() {
        if (!validateBranchInputs()) {
            return;
        }


        ServiceResponse<Branch> response = branchService.createBranch(branchCodeField.getText(),nameField.getText(),cityField.getText(),addressField.getText(),phoneField.getText());

        System.out.println(response.toString());
        if (response.isSuccess()) {


            createdBranchId = response.getData().getId();
            createdBranchCode = response.getData().getBranchCode();
            loadManagerForm();




        } else {
            showAlert(Alert.AlertType.ERROR,"Error in adding branch" , response.getMessage());
            return;
        }
        closeWindow();
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void loadManagerForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/SuperAdminScreens/add-branch-manager.fxml"));
            Scene scene = new Scene(loader.load());

            // Get reference to this controller instance
            AddBranchController controller = loader.getController();
            controller.branchIdField.setText(createdBranchId.toString());
            controller.managerBranchCodeField.setText(createdBranchCode);;

            Stage stage = (Stage) saveBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Add Branch Manager");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading manager form" , "");
        }
    }




}