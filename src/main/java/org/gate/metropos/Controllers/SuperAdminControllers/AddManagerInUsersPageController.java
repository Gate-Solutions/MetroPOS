package org.gate.metropos.Controllers.SuperAdminControllers;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.services.BranchService;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.ServiceResponse;

import java.math.BigDecimal;
import java.util.List;

public class AddManagerInUsersPageController {
    @FXML private ComboBox<Branch> branchComboBox;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField managerNameField;
    @FXML private TextField employeeNoField;
    @FXML private TextField salaryField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private final BranchService branchService;
    private final EmployeeService employeeService;

    public AddManagerInUsersPageController() {
        branchService = new BranchService();
        employeeService = new EmployeeService();
    }

    @FXML
    public void initialize() {
        loadAvailableBranches();
        setupButtons();

    }

    private void loadAvailableBranches() {
        ServiceResponse<List<Branch>> response = branchService.getBranchesWithoutActiveManagers();
        if (response.isSuccess()) {
//            if (response.getData().isEmpty()) {
//                showAlert(Alert.AlertType.ERROR,"No Available Branches", "There are no Branches available to add Manager");
//                closeWindow();
//
//            }
            branchComboBox.setItems(FXCollections.observableArrayList(response.getData()));
            branchComboBox.setCellFactory(param -> new ListCell<Branch>() {
                @Override
                protected void updateItem(Branch branch, boolean empty) {
                    super.updateItem(branch, empty);
                    if (empty || branch == null) {
                        setText(null);
                    } else {
                        setText(branch.getName() + " (" + branch.getBranchCode() + ")");
                    }
                }
            });
            branchComboBox.setButtonCell(branchComboBox.getCellFactory().call(null));
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load branches: " + response.getMessage());
        }
    }

    private void setupButtons() {
        cancelBtn.setOnAction(e -> closeWindow());
        saveBtn.setOnAction(e -> saveManager());
    }



    private void saveManager() {
        if (!validateInputs()) return;

        Branch selectedBranch = branchComboBox.getValue();
        try {
            BigDecimal salary = new BigDecimal(salaryField.getText().trim());
            ServiceResponse<Employee> response = employeeService.createEmployee(
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    "password", // Default password
                    UserRole.BRANCH_MANAGER,
                    managerNameField.getText().trim(),
                    employeeNoField.getText().trim(),
                    salary,
                    selectedBranch.getId()
            );

            if (response.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Branch manager added successfully");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", response.getMessage());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid salary amount");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add branch manager: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (branchComboBox.getValue() == null) {
            errors.append("Please select a branch\n");
        }

        if (usernameField.getText().trim().isEmpty()) {
            errors.append("Username is required\n");
        } else if (!usernameField.getText().matches("^[a-zA-Z0-9_]{3,20}$")) {
            errors.append("Username must be 3-20 characters long and contain only letters, numbers, and underscores\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append("Email is required\n");
        } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("Please enter a valid email address\n");
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
                BigDecimal salary = new BigDecimal(salaryField.getText().trim());
                if (salary.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.append("Salary must be greater than zero\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Please enter a valid salary amount\n");
            }
        }

        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }
        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
