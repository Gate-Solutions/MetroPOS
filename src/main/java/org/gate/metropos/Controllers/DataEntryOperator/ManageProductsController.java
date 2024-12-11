package org.gate.metropos.Controllers.DataEntryOperator;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.gate.metropos.Controllers.BranchManagerControllers.AddUpdateSupplierController;
import org.gate.metropos.models.BranchProduct;
import org.gate.metropos.models.Category;
import org.gate.metropos.models.Product;
import org.gate.metropos.models.Supplier;
import org.gate.metropos.services.CategoryService;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.ProductService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageProductsController {
    @FXML private TableView<Product> productsTable;
    @FXML private Button addProductBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;

    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProducts;
    private final ProductService productService;
    private final CategoryService categoryService;
    private Map<Long, Integer> branchProductQuantities;

    public ManageProductsController() {
        productService = new ProductService();
        categoryService = new CategoryService();
        branchProductQuantities = new HashMap<>();
    }

    @FXML
    public void initialize() {
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupFilters();
        loadProducts();
        loadBranchProductQuantities();
        addProductBtn.setOnAction(e -> openAddProductWindow());
    }

    private void setupTable() {
        TableColumn<Product, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory().getName()));

        TableColumn<Product, BigDecimal> originalPriceCol = new TableColumn<>("Original Price");
        originalPriceCol.setCellValueFactory(new PropertyValueFactory<>("originalPrice"));

        TableColumn<Product, BigDecimal> salePriceCol = new TableColumn<>("Sale Price");
        salePriceCol.setCellValueFactory(new PropertyValueFactory<>("salePrice"));

        TableColumn<Product, BigDecimal> cartonPriceCol = new TableColumn<>("Carton Price");
        cartonPriceCol.setCellValueFactory(new PropertyValueFactory<>("priceOfCarton"));

        TableColumn<Product, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setCellFactory(column -> new TableCell<Product, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item ? "Active" : "Inactive"));
            }
        });

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(cellData -> {
            Integer quantity = branchProductQuantities.getOrDefault(cellData.getValue().getId(), 0);
            return new SimpleIntegerProperty(quantity).asObject();
        });


        TableColumn<Product, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    if (!product.isActive()) {
                        AlertUtils.showError("Already Inactive");
                        return;
                    }
//                    confirmAndRemoveProduct(product);
                });
                removeButton.getStyleClass().add("primary-table-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        productsTable.getColumns().addAll(
                idCol, codeCol, nameCol, categoryCol, originalPriceCol,
                salePriceCol, cartonPriceCol, quantityCol, statusCol, actionCol
        );

        productsTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openUpdateProductWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        statusFilter.setValue("Active");

        // Load categories from service
        loadCategories();

        filteredProducts = new FilteredList<>(allProducts, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        productsTable.setItems(filteredProducts);
    }

    private void updateFilters() {
        filteredProducts.setPredicate(product -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;
            boolean matchesCategory = true;

            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                matchesSearch = product.getName().toLowerCase().contains(searchText) ||
                        product.getCode().toLowerCase().contains(searchText);
            }

            String status = statusFilter.getValue();
            if ("Active".equals(status)) {
                matchesStatus = product.isActive();
            } else if ("Inactive".equals(status)) {
                matchesStatus = !product.isActive();
            }

            String category = categoryFilter.getValue();
            if (category != null && !"All".equals(category)) {
                matchesCategory = product.getCategory().getName().equals(category);
            }

            return matchesSearch && matchesStatus && matchesCategory;
        });
    }

    private void loadProducts() {
        ServiceResponse<List<Product>> response = productService.getAllProducts();
        if (response.isSuccess()) {
            allProducts.clear();
            allProducts.addAll(response.getData());
            updateFilters();
        } else {
            AlertUtils.showError("Failed to load suppliers", response.getMessage());
        }
        updateFilters();
    }

    private void loadBranchProductQuantities() {
        Long branchId = EmployeeService.getLoggedInEmployee().getBranchId();
        ServiceResponse<List<BranchProduct>> response = productService.getProductsByBranch(branchId);
        if (response.isSuccess()) {
            branchProductQuantities.clear();
            response.getData().forEach(bp ->
                    branchProductQuantities.put(bp.getProductId(), bp.getQuantity())
            );
        }
    }

    private void loadCategories() {
        ServiceResponse<List<Category>> response = categoryService.getAllCategories();
        if(response.isSuccess()) {
            List<Category> categories = response.getData();
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("All");
            categories.forEach(category -> categoryNames.add(category.getName()));
            categoryFilter.setItems(FXCollections.observableList(categoryNames));
            categoryFilter.setValue("All");
            return;
        }
        AlertUtils.showError("Failed to load Categories: " + response.getMessage());
    }

    private void openAddProductWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/dataEntryScreens/addUpdateProducts.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadProducts();
        } catch (IOException ex) {
            AlertUtils.showError("Error", "Failed to open add product window");
        }
    }

    private void openUpdateProductWindow(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/dataEntryScreens/addUpdateProducts.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Product");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            AddUpdateProductController controller = loader.getController();
            controller.setProductToUpdate(product);

            stage.showAndWait();
            loadProducts();
        } catch (IOException ex) {
            AlertUtils.showError("Error", "Failed to open update product window");
        }
    }


}
