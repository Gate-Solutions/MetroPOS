package org.gate.metropos.Controllers.SuperAdminControllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import org.gate.metropos.models.Branch;
import org.gate.metropos.services.BranchService;
import org.gate.metropos.utils.ServiceResponse;

public class UpdateBranchController {
    @FXML
    private TextField idField;
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField cityField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button cancelBtn;
    @FXML private Button updateBtn;

    private final BranchService branchService;
    private Branch branch;
    @Getter
    private Branch UpdatedBranch;
    public UpdateBranchController() {
        this.branchService = new BranchService();
    }

    @FXML
    public void initialize() {
        cancelBtn.setOnAction(e -> closeWindow());
        updateBtn.setOnAction(e -> handleUpdate());
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
        populateFields();
    }

    private void populateFields() {
        idField.setText(String.valueOf(branch.getId()));
        codeField.setText(branch.getBranchCode());
        nameField.setText(branch.getName());
        cityField.setText(branch.getCity());
        addressField.setText(branch.getAddress());
        phoneField.setText(branch.getPhone());
        activeCheckBox.setSelected(branch.isActive());
    }

    private void handleUpdate() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Update");
        confirmAlert.setHeaderText("Are you sure you want to update this branch?");
        confirmAlert.setContentText("This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                updateBranch();
            }
        });
    }

    private void updateBranch() {
        Branch updatedBranch = Branch.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .name(nameField.getText())
                .city(branch.getCity())
                .address(addressField.getText())
                .phone(phoneField.getText())
                .isActive(activeCheckBox.isSelected())
                .numberOfEmployees(branch.getNumberOfEmployees())
                .build();


        System.out.println(updatedBranch.toString());
        ServiceResponse<Branch> response = branchService.updateBranch(updatedBranch);

        if (response.isSuccess()) {
            UpdatedBranch = updatedBranch;
            showAlert(Alert.AlertType.INFORMATION, "Success", response.getMessage());
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", response.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
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
