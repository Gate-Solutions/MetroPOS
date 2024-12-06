package org.gate.metropos.Controllers.DataEntryOperator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
import java.util.Optional;

public class ManagePurchaseInvoicesController {
    @FXML private TableView<PurchaseInvoice> invoicesTable;
    @FXML private Button addInvoiceBtn;
    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<Supplier> supplierFilter;

    private ObservableList<PurchaseInvoice> allInvoices = FXCollections.observableArrayList();
    private FilteredList<PurchaseInvoice> filteredInvoices;
    private final PurchaseInvoiceService invoiceService;
    private final SupplierService supplierService;
    private final EmployeeService employeeService;
    private final ProductService productService;

    public ManagePurchaseInvoicesController() {
        invoiceService = new PurchaseInvoiceService();
        supplierService = new SupplierService();
        employeeService = new EmployeeService();
        productService = new ProductService();
    }

    @FXML
    public void initialize() {
        invoicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupFilters();
        loadInvoices();
        addInvoiceBtn.setOnAction(e -> openAddInvoiceWindow());
    }

    private void setupTable() {
        TableColumn<PurchaseInvoice, String> invoiceNumberCol = new TableColumn<>("Invoice #");
        invoiceNumberCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));

        TableColumn<PurchaseInvoice, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(data -> {
            PurchaseInvoice invoice = data.getValue();
            Supplier supplier = supplierService.getSupplier(invoice.getSupplierId()).getData();
            return new SimpleStringProperty(supplier != null ? supplier.getName() : "");
        });

        TableColumn<PurchaseInvoice, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));

        TableColumn<PurchaseInvoice, BigDecimal> amountCol = new TableColumn<>("Total Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<PurchaseInvoice, String> createdByCol = new TableColumn<>("Created By");
        createdByCol.setCellValueFactory(data -> {
            PurchaseInvoice invoice = data.getValue();
            Employee employee = employeeService.getEmployee(invoice.getCreatedBy()).getData();
            return new SimpleStringProperty(employee != null ? employee.getName() : "");
        });

        TableColumn<PurchaseInvoice, Void> actionsCol = new TableColumn<>("Action");
        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Remove");
            {
                deleteButton.setOnAction(event -> {
                    PurchaseInvoice invoice = getTableView().getItems().get(getIndex());
                    deleteInvoice(invoice);
                });
                deleteButton.getStyleClass().add("primary-table-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });


        invoicesTable.getColumns().addAll(
                invoiceNumberCol, supplierCol, dateCol, amountCol, createdByCol, actionsCol
        );

        invoicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PurchaseInvoice selectedInvoice = invoicesTable.getSelectionModel().getSelectedItem();
                openInvoiceForEdit(selectedInvoice);
            }
        });
    }

    private void setupFilters() {

        ServiceResponse<List<Supplier>> response = supplierService.getAllSuppliers();
        if (response.isSuccess()) {
            supplierFilter.setItems(FXCollections.observableArrayList(response.getData()));

            supplierFilter.setConverter(new StringConverter<Supplier>() {
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

        filteredInvoices = new FilteredList<>(allInvoices, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        dateFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        supplierFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());

        invoicesTable.setItems(filteredInvoices);
    }

    private void updateFilters() {
        filteredInvoices.setPredicate(invoice -> {
            boolean matchesSearch = true;
            boolean matchesDate = true;
            boolean matchesSupplier = true;

            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                matchesSearch = invoice.getInvoiceNumber().toLowerCase().contains(searchText) ||
                        invoice.getSupplier().getName().toLowerCase().contains(searchText);
            }

            if (dateFilter.getValue() != null) {
                matchesDate = invoice.getInvoiceDate().equals(dateFilter.getValue());
            }

            if (supplierFilter.getValue() != null) {
                matchesSupplier = invoice.getSupplierId().equals(supplierFilter.getValue().getId());
            }

            return matchesSearch && matchesDate && matchesSupplier;
        });
    }

    private void loadInvoices() {
        ServiceResponse<List<PurchaseInvoice>> response = invoiceService.getInvoicesByBranch(1L);
        if (response.isSuccess()) {
            allInvoices.clear();
            allInvoices.addAll(response.getData());
        } else {
            AlertUtils.showError("Failed to load invoices", response.getMessage());
        }
    }

    private void deleteInvoice(PurchaseInvoice invoice) {
        if(AlertUtils.showConfirmation("You sure about that?")) {
            ServiceResponse<Boolean> isDeleted = invoiceService.deleteInvoice(invoice.getId());
            if (isDeleted.isSuccess()) {
                loadInvoices();
                AlertUtils.showSuccess("Invoice Deleted Successfully");
            } else {
                AlertUtils.showError("Failed to delete invoice", isDeleted.getMessage());
            }
        }
    }

    private void openAddInvoiceWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/DataEntryScreens/addPurchaseInvoice.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Create New Invoice");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadInvoices(); // Refresh the table after adding
        } catch (IOException ex) {
            AlertUtils.showError("Error opening add invoice window");
        }
    }

    private void openInvoiceForEdit(PurchaseInvoice invoice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/DataEntryScreens/addPurchaseInvoice.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Invoice");
            stage.setScene(new Scene(loader.load()));

            AddPurchaseInvoiceController controller = loader.getController();
            controller.setInvoiceForUpdate(invoice);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadInvoices(); // Refresh the table after update
        } catch (IOException ex) {
            AlertUtils.showError("Error opening invoice update window");
        }
    }



    private void viewInvoiceDetails(PurchaseInvoice invoice) {
        StringBuilder details = new StringBuilder();

        // Header Information
        details.append("Invoice Details\n")
                .append("═══════════════════════\n\n")
                .append("Invoice #: ").append(invoice.getInvoiceNumber()).append("\n")
                .append("Date: ").append(invoice.getInvoiceDate()).append("\n")
                .append("Supplier: ").append(supplierService.getSupplier(invoice.getSupplierId()).getData().getName()).append("\n")
                .append("Created By: ").append(employeeService.getEmployee(invoice.getCreatedBy()).getData().getName()).append("\n")
                .append("Notes: ").append(invoice.getNotes() != null ? invoice.getNotes() : "N/A").append("\n\n");

        // Items Table Header
        details.append("Items\n")
                .append("═══════════════════════\n")
                .append(String.format("%-30s %-10s %-12s %-12s %-12s\n",
                        "Product", "Quantity", "Unit Price", "Carton Price", "Total"));
        details.append("─".repeat(76)).append("\n");

        // Items Details
        for (PurchaseInvoiceItem item : invoice.getItems()) {
            Product product = productService.findById(item.getProductId()).getData();
            details.append(String.format("%-30s %-10d %-12.2f %-12.2f %-12.2f\n",
                    product.getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getCartonPrice(),
                    item.getTotalPrice()));
        }

        // Footer with Total
        details.append("─".repeat(76)).append("\n")
                .append(String.format("%67s %-12.2f", "Total Amount:", invoice.getTotalAmount()));

        AlertUtils.showSuccess("Invoice Details", details.toString());
    }
}
