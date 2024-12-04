package org.gate.metropos.Controllers.DataEntryOperator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.Getter;
import org.gate.metropos.models.Category;
import org.gate.metropos.models.Product;
import org.gate.metropos.services.CategoryService;
import org.gate.metropos.services.ProductService;
import org.gate.metropos.utils.ServiceResponse;

import java.math.BigDecimal;
import java.util.List;

public class SelectProductDialogController {
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private TableView<Product> productsTable;

    private final ProductService productService;
    private final CategoryService categoryService;
    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProducts;
    @Getter
    private Product selectedProduct;

    public SelectProductDialogController() {
        productService = new ProductService();
        categoryService = new CategoryService();
    }

    @FXML
    public void initialize() {
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupFilters();
        loadProducts();
    }

    private void setupTable() {
        TableColumn<Product, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategory().getName()));

        TableColumn<Product, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("originalPrice"));

        TableColumn<Product, Void> selectCol = new TableColumn<>("Action");
        selectCol.setCellFactory(column -> new TableCell<>() {
            private final Button selectButton = new Button("Select");
            {
                selectButton.setOnAction(event -> {
                    selectedProduct = getTableView().getItems().get(getIndex());
                    closeDialog(true);
                });
                selectButton.getStyleClass().add("primary-table-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : selectButton);
            }
        });

        productsTable.getColumns().addAll(codeCol, nameCol, categoryCol, priceCol, selectCol);
    }

    private void setupFilters() {
        ServiceResponse<List<Category>> response = categoryService.getAllCategories();
        if (response.isSuccess()) {
            List<Category> categories = response.getData();
            categoryFilter.setItems(FXCollections.observableArrayList(categories));
        }

        filteredProducts = new FilteredList<>(allProducts, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());

        productsTable.setItems(filteredProducts);
    }

    private void updateFilters() {
        filteredProducts.setPredicate(product -> {
            boolean matchesSearch = true;
            boolean matchesCategory = true;

            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                matchesSearch = product.getName().toLowerCase().contains(searchText) ||
                        product.getCode().toLowerCase().contains(searchText);
            }

            if (categoryFilter.getValue() != null) {
                matchesCategory = product.getCategory().getId().equals(categoryFilter.getValue().getId());
            }

            return matchesSearch && matchesCategory;
        });
    }

    private void loadProducts() {
        ServiceResponse<List<Product>> response = productService.getAllProducts();
        if (response.isSuccess()) {
            allProducts.clear();
            allProducts.addAll(response.getData());
        }
    }

    private void closeDialog(boolean success) {
        Stage stage = (Stage) productsTable.getScene().getWindow();
        stage.setUserData(success ? selectedProduct : null);
        stage.close();
    }

}
