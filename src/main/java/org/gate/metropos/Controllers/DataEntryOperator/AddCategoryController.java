package org.gate.metropos.Controllers.DataEntryOperator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.gate.metropos.models.Category;
import org.gate.metropos.services.CategoryService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

public class AddCategoryController {
    @FXML private TextField categoryNameField;
    @FXML private Button createCategoryBtn;
    @FXML private Button cancelBtn;

    private final CategoryService categoryService;

    public AddCategoryController() {
        categoryService = new CategoryService();
    }

    @FXML
    public void initialize() {
        createCategoryBtn.setOnAction(e -> handleCreateCategory());
        cancelBtn.setOnAction(e -> closeWindow());
    }

    private void handleCreateCategory() {
        String name = categoryNameField.getText().trim();
        if (name.isEmpty()) {
            AlertUtils.showError("Category name is required");
            return;
        }

        ServiceResponse<Category> response = categoryService.createCategory(name);
        if (response.isSuccess()) {
            AlertUtils.showSuccess("Category created successfully");
            closeWindow();
        } else {
            AlertUtils.showError(response.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) createCategoryBtn.getScene().getWindow();
        stage.close();
    }
}
