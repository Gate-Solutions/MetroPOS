package org.gate.metropos.Controllers.CashierControllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.Product;
import org.gate.metropos.models.Sale;
import org.gate.metropos.models.SaleItem;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.ProductService;
import org.gate.metropos.services.SaleService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class AddSaleController {
    @FXML private Label titleLabel;
    @FXML private DatePicker invoiceDatePicker;
    @FXML private TextField notesField;
    @FXML private TableView<SaleItem> productsTable;
    @FXML private Label totalAmountLabel;
    @FXML private TextField discountField;
    @FXML private Label netAmountLabel;
    @FXML private Button addProductBtn;
    @FXML private Button cancelBtn;
    @FXML private Button saveSaleBtn;

    private final SaleService saleService;
    private final ProductService productService;
    private ObservableList<SaleItem> saleItems;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal netAmount;
    private Sale saleToUpdate;
    private boolean isUpdateMode = false;

    public AddSaleController() {
        saleService = new SaleService();
        productService = new ProductService();
        saleItems = FXCollections.observableArrayList();
        totalAmount = BigDecimal.ZERO;
        discount = BigDecimal.ZERO;
        netAmount = BigDecimal.ZERO;
    }

    @FXML
    public void initialize() {
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupButtons();
        setupDiscountField();
        productsTable.setItems(saleItems);
        invoiceDatePicker.setValue(LocalDate.now());
    }

    private void setupTable() {
        TableColumn<SaleItem, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(data -> {
            Product product = productService.findById(data.getValue().getProductId()).getData();
            return new SimpleStringProperty(product.getName());
        });

        TableColumn<SaleItem, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setCellFactory(col -> new TableCell<>() {
            private final TextField field = new TextField();
            {
                field.setOnAction(e -> {
                    try {
                        int value = Integer.parseInt(field.getText());
                        if (value > 0) {
                            SaleItem item = getTableView().getItems().get(getIndex());
                            item.setQuantity(value);
                            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(value)));
                            updateAmounts();
                        }
                    } catch (NumberFormatException ex) {
                        field.setText(String.valueOf(getItem()));
                    }
                });
            }

            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    field.setText(quantity.toString());
                    setGraphic(field);
                }
            }
        });

        TableColumn<SaleItem, BigDecimal> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<SaleItem, BigDecimal> totalPriceCol = new TableColumn<>("Total");
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<SaleItem, Void> actionsCol = new TableColumn<>("Action");
        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.setOnAction(event -> {
                    SaleItem item = getTableView().getItems().get(getIndex());
                    removeProduct(item);
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
                productCol, quantityCol, unitPriceCol, totalPriceCol, actionsCol
        );
    }

    private void setupButtons() {
        addProductBtn.setOnAction(e -> showAddProductDialog());
        cancelBtn.setOnAction(e -> closeWindow());
        saveSaleBtn.setOnAction(e -> saveSale());
    }

    private void setupDiscountField() {
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                discount = new BigDecimal(newValue);
            } catch (NumberFormatException e) {
                discount = BigDecimal.ZERO;
            }
            updateAmounts();
        });
    }

    private void showAddProductDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/dataEntryScreens/selectProductDialog.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Select Product");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            Product selectedProduct = (Product) stage.getUserData();
            if (selectedProduct != null) {
                addProductToSale(selectedProduct);
            }
        } catch (IOException ex) {
            AlertUtils.showError("Error opening product selection window");
        }
    }

    private void addProductToSale(Product product) {
        SaleItem item = new SaleItem();
        item.setProductId(product.getId());
        item.setQuantity(1);
        item.setUnitPrice(product.getSalePrice());
        item.setTotalPrice(product.getSalePrice());

        saleItems.add(item);
        updateAmounts();
    }

    private void removeProduct(SaleItem item) {
        saleItems.remove(item);
        updateAmounts();
    }

    private void updateAmounts() {
        totalAmount = saleItems.stream()
                .map(SaleItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        netAmount = totalAmount.subtract(discount);

        totalAmountLabel.setText(totalAmount.toString());
        netAmountLabel.setText(netAmount.toString());
    }

    public void setSaleForUpdate(Sale sale) {
        this.saleToUpdate = sale;
        this.isUpdateMode = true;
        titleLabel.setText("Update Sale");
        populateFields();
    }

    private void populateFields() {
        invoiceDatePicker.setValue(saleToUpdate.getInvoiceDate());
        notesField.setText(saleToUpdate.getNotes());
        discountField.setText(saleToUpdate.getDiscount().toString());

        saleItems.clear();
        saleItems.addAll(saleToUpdate.getItems());
        updateAmounts();

        saveSaleBtn.setText("Update Sale");
    }

    private void saveSale() {
        if (!validateSale()) return;
        Employee em = EmployeeService.getLoggedInEmployee();

        Sale sale = new Sale();
        if (isUpdateMode) {
            sale.setId(saleToUpdate.getId());
        }
        sale.setInvoiceDate(invoiceDatePicker.getValue());
        sale.setNotes(notesField.getText());
        sale.setTotalAmount(totalAmount);
        sale.setDiscount(discount);
        sale.setNetAmount(netAmount);
        sale.setItems(new ArrayList<>(saleItems));
        sale.setBranchId(em.getBranchId());
        sale.setCreatedBy(em.getId());

        ServiceResponse<Sale> response;
        if (isUpdateMode) {
            response = saleService.updateInvoice(sale);
        } else {
            response = saleService.createSale(sale);
        }

        if (response.isSuccess()) {
            AlertUtils.showSuccess(isUpdateMode ? "Sale updated successfully" : "Sale created successfully");
            closeWindow();
        } else {
            AlertUtils.showError("Failed to " + (isUpdateMode ? "update" : "create") + " sale", response.getMessage());
        }
    }

    private boolean validateSale() {
        if (saleItems.isEmpty()) {
            AlertUtils.showError("Please add at least one product");
            return false;
        }

        if (invoiceDatePicker.getValue() == null) {
            AlertUtils.showError("Please select a sale date");
            return false;
        }

        if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
            AlertUtils.showError("Net amount cannot be negative");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
