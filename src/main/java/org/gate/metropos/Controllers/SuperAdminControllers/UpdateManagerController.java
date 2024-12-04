package org.gate.metropos.Controllers.SuperAdminControllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.services.BranchService;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.ServiceResponse;

import java.math.BigDecimal;
import java.util.List;

public class UpdateManagerController {
    @FXML
    private ComboBox<Branch> branchComboBox;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField managerNameField;
    @FXML
    private TextField employeeNoField;
    @FXML
    private TextField salaryField;
    @FXML
    private CheckBox activeCheckBox;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button updateBtn;

    private Employee employee;
    private final BranchService branchService = new BranchService();
    private final EmployeeService employeeService = new EmployeeService();

    @FXML
    private void initialize() {
        setupButtonHandlers();
    }

    private void loadAvailableBranches() {
        System.out.println("Employee in Controller: " + employee);
        ServiceResponse<List<Branch>> response = branchService.getBranchesWithoutActiveManagers(employee);
        System.out.println(response.getData());
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

    private void setupButtonHandlers() {
        cancelBtn.setOnAction(event -> closeWindow());
        updateBtn.setOnAction(event -> updateManager());
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        loadAvailableBranches();
        populateFields();
    }

    private void populateFields() {
        if (employee != null) {
            usernameField.setText(employee.getUsername());
            emailField.setText(employee.getEmail());
            managerNameField.setText(employee.getName());
            employeeNoField.setText(employee.getEmployeeNo());
            salaryField.setText(String.valueOf(employee.getSalary()));
            activeCheckBox.setSelected(employee.isActive());

            // Set the selected branch in combo box
            branchComboBox.getItems().stream()
                    .filter(branch -> branch.getId() == employee.getBranchId())
                    .findFirst()
                    .ifPresent(branch -> branchComboBox.setValue(branch));
        }
    }

    private void updateManager() {
        if (!validateInputs()) {
            return;
        }

        try {
            employee.setName(managerNameField.getText().trim());
            employee.setSalary(BigDecimal.valueOf(Double.parseDouble(salaryField.getText().trim())));
            employee.setBranchId(branchComboBox.getValue().getId());
            employee.setActive(activeCheckBox.isSelected());

            employeeService.updateEmployee(employee);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Manager updated successfully!");
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update manager: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (branchComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a branch");
            return false;
        }

        if (managerNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Name cannot be empty");
            return false;
        }

        try {
            Double.parseDouble(salaryField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid salary");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
