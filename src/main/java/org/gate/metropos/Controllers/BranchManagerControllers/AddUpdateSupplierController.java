package org.gate.metropos.Controllers.BranchManagerControllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.gate.metropos.models.Supplier;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.SupplierService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;
import org.gate.metropos.utils.SessionManager;

public class AddUpdateSupplierController {
    @FXML private TextField nameField;
    @FXML private TextField companyNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField ntnField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button createSupplierBtn;
    @FXML private Button cancelBtn;
    @FXML private Label Main_Label;

    private final SupplierService supplierService;
    private Supplier supplierToUpdate;
    private boolean isUpdateMode = false;

    public AddUpdateSupplierController() {
        supplierService = new SupplierService();
    }

    @FXML
    public void initialize() {
        setupButtonActions();
    }

    private void setupButtonActions() {
        createSupplierBtn.setOnAction(e -> handleCreateSupplier());
        cancelBtn.setOnAction(e -> closeWindow());
    }

    public void setSupplierForUpdate(Supplier supplier) {
        this.supplierToUpdate = supplier;
        this.isUpdateMode = true;
        populateFields();
    }

    private void populateFields() {
        if (supplierToUpdate != null) {
            nameField.setText(supplierToUpdate.getName());
            companyNameField.setText(supplierToUpdate.getCompanyName());
            emailField.setText(supplierToUpdate.getEmail());
            phoneField.setText(supplierToUpdate.getPhone());
            ntnField.setText(supplierToUpdate.getNtnNumber());
            activeCheckBox.setSelected(supplierToUpdate.isActive());

            activeCheckBox.setVisible(true);
            createSupplierBtn.setText("Update Supplier");
            Main_Label.setText("Update Supplier");
        } else {
            activeCheckBox.setVisible(false);
        }
    }

    private void handleCreateSupplier() {
        if (!validateInputs()) return;

        try {
            Supplier supplier = buildSupplierFromFields();
            ServiceResponse<Supplier> response;

            if (isUpdateMode) {
                response = supplierService.updateSupplier(supplier);
            } else {
                System.out.println(supplier);
                response = supplierService.createSupplier(supplier);

            }

            if (response.isSuccess()) {
                AlertUtils.showSuccess(isUpdateMode ?
                        "Supplier updated successfully" :
                        "Supplier created successfully");
                closeWindow();
            } else {
                AlertUtils.showError(response.getMessage());
            }
        } catch (Exception e) {
            AlertUtils.showError("Error: " + e.getMessage());
        }
    }

    private Supplier buildSupplierFromFields() {
        return Supplier.builder()
                .id(isUpdateMode ? supplierToUpdate.getId() : null)
                .name(nameField.getText().trim())
                .companyName(companyNameField.getText().trim())
                .email(emailField.getText().trim())
                .phone(phoneField.getText().trim())
                .ntnNumber(ntnField.getText().trim())
                .isActive(!isUpdateMode || activeCheckBox.isSelected())
                .build();
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("Name is required\n");
        }

        // Company name validation
        if (companyNameField.getText().trim().isEmpty()) {
            errorMessage.append("Company name is required\n");
        }

        // Email validation
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errorMessage.append("Email is required\n");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorMessage.append("Invalid email format\n");
        }

        // Phone validation
        String phone = phoneField.getText().trim();

        if (phone.isEmpty()) {
            errorMessage.append("Phone number is required\n");
        } else if (!phone.matches("\\d{11}")) {

            errorMessage.append("Phone number must be 11 digits\n");
        }

        // NTN validation
        String ntn = ntnField.getText().trim();
        if (ntn.isEmpty()) {
            errorMessage.append("NTN number is required\n");
        }
//        else if (!ntn.matches("\\d{7}")) {
//            errorMessage.append("NTN must be 7 digits\n");
//        }

        if (!errorMessage.isEmpty()) {
            AlertUtils.showError("Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }


    private void closeWindow() {
        Stage stage = (Stage) createSupplierBtn.getScene().getWindow();
        stage.close();
    }
}
