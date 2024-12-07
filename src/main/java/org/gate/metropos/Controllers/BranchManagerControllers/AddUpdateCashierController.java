package org.gate.metropos.Controllers.BranchManagerControllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;
import org.gate.metropos.utils.SessionManager;

import java.math.BigDecimal;

public class AddUpdateCashierController {
    @FXML private TextField branchIdField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField cashierNameField;
    @FXML private TextField employeeNoField;
    @FXML private TextField salaryField;
    @FXML private Button createCashierBtn;
    @FXML private CheckBox activeCheckBox;
    @FXML private Label Main_Label;
    @FXML private Button cancelBtn;
    @FXML private ComboBox<UserRole> roleComboBox;

    private final EmployeeService employeeService;
    private Employee currentManager;
    private Long branchId;
    private Employee cashierToUpdate;
    private boolean isUpdateMode = false;




    public AddUpdateCashierController() {
        employeeService = new EmployeeService();
        try {
            // Get current branch manager from session
            currentManager = SessionManager.getCurrentEmployee();
            if (currentManager.getRole() != UserRole.BRANCH_MANAGER) {
                AlertUtils.showError("Access Denied", "Only branch managers can add cashiers");
                closeWindow();
                return;
            }
            branchId = currentManager.getBranchId();
        } catch (IllegalStateException e) {
            AlertUtils.showError("Session Error", "No active session found");
            closeWindow();
        }
    }

    @FXML
    public void initialize() {
        if (branchId != null) {
            branchIdField.setText(branchId.toString());
            setupButtonActions();
            setupRoleComboBox();
//            setupInputValidation();
        }
    }

    public void setEmployeeForUpdate(Employee employee) {
        this.cashierToUpdate = employee;
        this.isUpdateMode = true;
        roleComboBox.setDisable(true);
        populateFields();
    }

    private void setupRoleComboBox() {
        roleComboBox.setItems(FXCollections.observableArrayList(
                UserRole.CASHIER,
                UserRole.DATA_ENTRY_OPERATOR
        ));

        roleComboBox.setConverter(new StringConverter<UserRole>() {
            @Override
            public String toString(UserRole role) {
                return role == UserRole.DATA_ENTRY_OPERATOR ? "Data Entry Operator" : "Cashier";
            }

            @Override
            public UserRole fromString(String string) {
                return null;
            }
        });
        roleComboBox.setValue(UserRole.CASHIER);
    }

    private void populateFields() {
        if (cashierToUpdate != null) {
            usernameField.setText(cashierToUpdate.getUsername());
            emailField.setText(cashierToUpdate.getEmail());
            cashierNameField.setText(cashierToUpdate.getName());
            employeeNoField.setText(cashierToUpdate.getEmployeeNo());
            salaryField.setText(cashierToUpdate.getSalary().toString());
            roleComboBox.setValue(cashierToUpdate.getRole());
            activeCheckBox.setSelected(cashierToUpdate.isActive());

            employeeNoField.setDisable(true);
            usernameField.setDisable(false);
            activeCheckBox.setVisible(true);
            createCashierBtn.setText("Update Cashier");
            Main_Label.setText("Update Cashier");
        } else {

            activeCheckBox.setVisible(false);
        }
    }
    private void setupButtonActions() {
        createCashierBtn.setOnAction(e -> handleCreateCashier());
        cancelBtn.setOnAction(e->closeWindow());

    }



    private void handleCreateCashier() {

        if (!AlertUtils.showConfirmation("Are you sure you want to update employee"))
            return;

        if (!validateInputs()) return;

        try {
            BigDecimal salary = new BigDecimal(salaryField.getText().trim());
            ServiceResponse<Employee> response;

            if (isUpdateMode) {
                cashierToUpdate.setEmail(emailField.getText().trim());
                cashierToUpdate.setName(cashierNameField.getText().trim());
                cashierToUpdate.setEmployeeNo(employeeNoField.getText().trim());
                cashierToUpdate.setUsername(usernameField.getText().trim());
                cashierToUpdate.setSalary(salary);
//                cashierToUpdate.setRole(roleComboBox.getValue());
                cashierToUpdate.setActive(activeCheckBox.isSelected());
                System.out.println("This is from addUpdate Controller"+cashierToUpdate);
                response = employeeService.updateEmployee(cashierToUpdate);
                System.out.println(response);
            } else {

                response = employeeService.createEmployee(
                        usernameField.getText().trim(),
                        emailField.getText().trim(),
                        "",
                        roleComboBox.getValue(),
                        cashierNameField.getText().trim(),
                        employeeNoField.getText().trim(),
                        salary,
                        branchId
                );
            }

            if (response.isSuccess()) {
                AlertUtils.showSuccess(isUpdateMode ?
                        "Cashier updated successfully" :
                        "Cashier created successfully");
                closeWindow();
            } else {
                AlertUtils.showError(response.getMessage());
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Invalid salary format");
        } catch (Exception e) {
            AlertUtils.showError("Error " + (isUpdateMode ? "updating" : "creating") +
                    " cashier: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }




    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        // Username validation
//        TODO: fix input fields
        String username = usernameField.getText();
        if (username == null || (username = username.trim()).isEmpty()) {
            errorMessage.append("Username is required\n");
        } else if (username.length() < 3) {
            errorMessage.append("Username must be at least 3 characters\n");
        }

        // Email validation
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errorMessage.append("Email is required\n");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorMessage.append("Invalid email format\n");
        }

        // Name validation
        if (cashierNameField.getText().trim().isEmpty()) {
            errorMessage.append("Full name is required\n");
        }

        // Employee number validation
        if (employeeNoField.getText().trim().isEmpty()) {
            errorMessage.append("Employee number is required\n");
        }

        // Salary validation
        String salary = salaryField.getText().trim();
        if (salary.isEmpty()) {
            errorMessage.append("Salary is required\n");
        } else {
            try {
                BigDecimal salaryValue = new BigDecimal(salary);
                if (salaryValue.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Salary must be greater than zero\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Invalid salary format\n");
            }
        }

        if (!errorMessage.isEmpty()) {
            AlertUtils.showError("Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) createCashierBtn.getScene().getWindow();
        stage.close();
    }


}
