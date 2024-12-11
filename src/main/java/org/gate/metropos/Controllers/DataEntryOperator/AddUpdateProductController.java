package org.gate.metropos.Controllers.DataEntryOperator;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.gate.metropos.models.Category;
import org.gate.metropos.models.Product;
import org.gate.metropos.models.Supplier;
import org.gate.metropos.services.CategoryService;
import org.gate.metropos.services.ProductService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class AddUpdateProductController {
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField originalPriceField;
    @FXML private TextField salePriceField;
    @FXML private TextField cartonPriceField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button createProductBtn;
    @FXML private Button cancelBtn;
    @FXML private Button addCategoryBtn;
    @FXML private Label Main_Label;

    private final ProductService productService;
    private final CategoryService categoryService;
    private Product productToUpdate;
    private boolean isUpdateMode = false;

    public AddUpdateProductController() {
        productService = new ProductService();
        categoryService = new CategoryService();
    }

    @FXML
    public void initialize() {
        activeCheckBox.setSelected(true);
        setupButtonActions();
        loadCategories();
        setupCategoryComboBox();
    }

    private void setupButtonActions() {
        createProductBtn.setOnAction(e -> handleCreateProduct());
        cancelBtn.setOnAction(e -> closeWindow());
        addCategoryBtn.setOnAction(e -> openAddCategoryDialog());
    }

    public void setProductToUpdate(Product product) {
        this.productToUpdate = product;
        this.isUpdateMode = true;
        populateFields();
    }

    private void populateFields() {
        if (productToUpdate != null) {
            codeField.setText(productToUpdate.getCode());
            codeField.setEditable(false);
            codeField.setStyle("-fx-background-color: #f4f4f4;");

            nameField.setText(productToUpdate.getName());
            categoryComboBox.setValue(productToUpdate.getCategory());
            originalPriceField.setText(productToUpdate.getOriginalPrice().toString());
            salePriceField.setText(productToUpdate.getSalePrice().toString());
            cartonPriceField.setText(productToUpdate.getPriceOfCarton().toString());
            activeCheckBox.setSelected(productToUpdate.isActive());

            activeCheckBox.setVisible(true);
            createProductBtn.setText("Update Product");
            Main_Label.setText("Update Product");
        } else {
            activeCheckBox.setVisible(false);
        }
    }

    private void loadCategories() {
        ServiceResponse<List<Category>> response = categoryService.getAllCategories();
        if (response.isSuccess()) {
            categoryComboBox.setItems(FXCollections.observableArrayList(response.getData()));
        }
    }

    private void setupCategoryComboBox() {
        categoryComboBox.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty ? "" : category.getName());
            }
        });
        categoryComboBox.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty ? "" : category.getName());
            }
        });
    }

    private void openAddCategoryDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/DataEntryScreens/AddCategory.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Category");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Reload categories after adding new one
            loadCategories();
        } catch (IOException e) {
            AlertUtils.showError("Error opening category dialog");
        }
    }

    private void handleCreateProduct() {
        if (!validateInputs()) return;

        try {
            Product product = buildProductFromFields();
            ServiceResponse<Product> response;

            if (isUpdateMode) {
                response = productService.updateProduct(product);
            } else {
                response = productService.createProduct(product);
            }

            if (response.isSuccess()) {
                AlertUtils.showSuccess(isUpdateMode ?
                        "Product updated successfully" :
                        "Product created successfully");
                closeWindow();
            } else {
                AlertUtils.showError(response.getMessage());
            }
        } catch (Exception e) {
            AlertUtils.showError("Error: " + e.getMessage());
        }
    }

    private Product buildProductFromFields() {
        return Product.builder()
                .id(isUpdateMode ? productToUpdate.getId() : null)
                .code(codeField.getText().trim())
                .name(nameField.getText().trim())
                .category(categoryComboBox.getValue())
                .originalPrice(new BigDecimal(originalPriceField.getText().trim()))
                .salePrice(new BigDecimal(salePriceField.getText().trim()))
                .priceOfCarton(new BigDecimal(cartonPriceField.getText().trim()))
                .isActive(!isUpdateMode || activeCheckBox.isSelected())
                .build();
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (codeField.getText().trim().isEmpty()) {
            errorMessage.append("Product code is required\n");
        }

        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("Product name is required\n");
        }

        if (categoryComboBox.getValue() == null) {
            errorMessage.append("Category must be selected\n");
        }

        try {
            String originalPrice = originalPriceField.getText().trim();
            if (originalPrice.isEmpty()) {
                errorMessage.append("Original price is required\n");
            } else {
                BigDecimal price = new BigDecimal(originalPrice);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Original price must be greater than 0\n");
                }
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Invalid original price format\n");
        }

        try {
            String salePrice = salePriceField.getText().trim();
            if (salePrice.isEmpty()) {
                errorMessage.append("Sale price is required\n");
            } else {
                BigDecimal price = new BigDecimal(salePrice);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Sale price must be greater than 0\n");
                }
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Invalid sale price format\n");
        }

        try {
            String cartonPrice = cartonPriceField.getText().trim();
            if (cartonPrice.isEmpty()) {
                errorMessage.append("Carton price is required\n");
            } else {
                BigDecimal price = new BigDecimal(cartonPrice);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Carton price must be greater than 0\n");
                }
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Invalid carton price format\n");
        }

        if (errorMessage.isEmpty()) {
            BigDecimal originalPrice = new BigDecimal(originalPriceField.getText().trim());
            BigDecimal salePrice = new BigDecimal(salePriceField.getText().trim());
            BigDecimal cartonPrice = new BigDecimal(cartonPriceField.getText().trim());

            if (salePrice.compareTo(originalPrice) < 0) {
                errorMessage.append("Sale price cannot be less than original price\n");
            }
            if (cartonPrice.compareTo(originalPrice) < 0) {
                errorMessage.append("Carton price cannot be less than original price\n");
            }
        }

        if (!errorMessage.isEmpty()) {
            AlertUtils.showError("Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) createProductBtn.getScene().getWindow();
        stage.close();
    }
}
