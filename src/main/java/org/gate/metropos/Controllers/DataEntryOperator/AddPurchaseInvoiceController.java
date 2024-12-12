package org.gate.metropos.Controllers.DataEntryOperator;

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
import javafx.util.StringConverter;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.Product;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoice;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoiceItem;
import org.gate.metropos.models.Supplier;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.ProductService;
import org.gate.metropos.services.PurchaseInvoiceService;
import org.gate.metropos.services.SupplierService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddPurchaseInvoiceController {
    @FXML private ComboBox<Supplier> supplierComboBox;
    @FXML private DatePicker invoiceDatePicker;
    @FXML private TextField notesField;
    @FXML private TableView<PurchaseInvoiceItem> productsTable;
    @FXML private Label totalAmountLabel;
    @FXML private Button addProductBtn;
    @FXML private Button cancelBtn;
    @FXML private Button saveInvoiceBtn;

    private final PurchaseInvoiceService invoiceService;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final EmployeeService employeeService;
    private ObservableList<PurchaseInvoiceItem> invoiceItems;
    private BigDecimal totalAmount;

    private PurchaseInvoice invoiceToUpdate;
    private boolean isUpdateMode = false;

    public AddPurchaseInvoiceController() {
        invoiceService = new PurchaseInvoiceService();
        supplierService = new SupplierService();
        productService = new ProductService();
        employeeService = new EmployeeService();
        invoiceItems = FXCollections.observableArrayList();
        totalAmount = BigDecimal.ZERO;
    }

    @FXML
    public void initialize() {
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupSupplierComboBox();
        setupButtons();
        productsTable.setItems(invoiceItems);
        invoiceDatePicker.setValue(LocalDate.now());
        updateTotalAmount();
    }


    public void setInvoiceForUpdate(PurchaseInvoice invoice) {
        this.invoiceToUpdate = invoice;
        this.isUpdateMode = true;
        populateFields();
    }

    private void populateFields() {
        supplierComboBox.setValue(supplierService.getSupplier(invoiceToUpdate.getSupplierId()).getData());
        invoiceDatePicker.setValue(invoiceToUpdate.getInvoiceDate());
        notesField.setText(invoiceToUpdate.getNotes());

        invoiceItems.clear();
        invoiceItems.addAll(invoiceToUpdate.getItems());
        updateTotalAmount();

        saveInvoiceBtn.setText("Update Invoice");
    }

    private void setupTable() {
        TableColumn<PurchaseInvoiceItem, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(data -> {
            Product product = productService.findById(data.getValue().getProductId()).getData();
            return new SimpleStringProperty(product.getName());
        });

        TableColumn<PurchaseInvoiceItem, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setCellFactory(col -> new TableCell<>() {
            private final TextField field = new TextField();
            {
                field.setOnAction(e -> {
                    try {
                        int value = Integer.parseInt(field.getText());
                        if (value > 0) {
                            PurchaseInvoiceItem item = getTableView().getItems().get(getIndex());
                            item.setQuantity(value);
                            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(value)));
                            updateTotalAmount();
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

        TableColumn<PurchaseInvoiceItem, BigDecimal> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<PurchaseInvoiceItem, BigDecimal> cartonPriceCol = new TableColumn<>("Carton Price");
        cartonPriceCol.setCellValueFactory(new PropertyValueFactory<>("cartonPrice"));

        TableColumn<PurchaseInvoiceItem, BigDecimal> totalPriceCol = new TableColumn<>("Total");
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<PurchaseInvoiceItem, Void> actionsCol = new TableColumn<>("Action");
        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.setOnAction(event -> {
                    PurchaseInvoiceItem item = getTableView().getItems().get(getIndex());
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
                productCol, quantityCol, unitPriceCol, cartonPriceCol, totalPriceCol, actionsCol
        );
    }

    private void setupSupplierComboBox() {
        ServiceResponse<List<Supplier>> response = supplierService.getAllSuppliers();
        if (response.isSuccess()) {
            supplierComboBox.setItems(FXCollections.observableArrayList(response.getData()));
            supplierComboBox.setConverter(new StringConverter<Supplier>() {
                @Override
                public String toString(Supplier supplier) {
                    return supplier != null ? supplier.getId() + " - " + supplier.getName() : "";
                }

                @Override
                public Supplier fromString(String string) {
                    return null;
                }
            });
        }
    }

    private void setupButtons() {
        addProductBtn.setOnAction(e -> showAddProductDialog());
        cancelBtn.setOnAction(e -> closeWindow());
        saveInvoiceBtn.setOnAction(e -> saveInvoice());
    }

    private void showAddProductDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/DataEntryScreens/selectProductDialog.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Select Product");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            Product selectedProduct = (Product) stage.getUserData();
            if (selectedProduct != null) {
                addProductToInvoice(selectedProduct);
            }
        } catch (IOException ex) {
            AlertUtils.showError("Error opening product selection window");
        }
    }

    private void addProductToInvoice(Product product) {
        PurchaseInvoiceItem item = new PurchaseInvoiceItem();
        item.setProductId(product.getId());
        item.setQuantity(1);
        item.setUnitPrice(product.getOriginalPrice());
        item.setCartonPrice(product.getPriceOfCarton());
        item.setTotalPrice(product.getOriginalPrice());

        invoiceItems.add(item);
        updateTotalAmount();
    }

    private void removeProduct(PurchaseInvoiceItem item) {
        invoiceItems.remove(item);
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        totalAmount = invoiceItems.stream()
                .map(PurchaseInvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAmountLabel.setText(totalAmount.toString());
    }

    private void saveInvoice() {
        if (!validateInvoice()) return;

        PurchaseInvoice invoice = new PurchaseInvoice();
        if (isUpdateMode) {
            invoice.setId(invoiceToUpdate.getId());
            System.out.println(invoice.getId());
        }
        invoice.setSupplierId(supplierComboBox.getValue().getId());
        invoice.setInvoiceDate(invoiceDatePicker.getValue());
        invoice.setNotes(notesField.getText());
        invoice.setTotalAmount(totalAmount);
        invoice.setItems(new ArrayList<>(invoiceItems));

        Employee em = EmployeeService.getLoggedInEmployee();
        invoice.setBranchId(em.getBranchId());
        invoice.setCreatedBy(em.getId());

        ServiceResponse<PurchaseInvoice> response;
        if (isUpdateMode) {

            response = invoiceService.updateInvoice(invoice);
        } else {
            response = invoiceService.createInvoice(invoice);
        }
        if (response.isSuccess()) {
            AlertUtils.showSuccess("Invoice created successfully");
            closeWindow();
        } else {
            System.out.println(response);
            AlertUtils.showError("Failed to create invoice", response.getMessage());
        }
    }

    private boolean validateInvoice() {
        if (supplierComboBox.getValue() == null) {
            AlertUtils.showError("Please select a supplier");
            return false;
        }

        if (invoiceItems.isEmpty()) {
            AlertUtils.showError("Please add at least one product");
            return false;
        }

        if (invoiceDatePicker.getValue() == null) {
            AlertUtils.showError("Please select an invoice date");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
