package org.gate.metropos.Controllers.CashierControllers;

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
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.Sale;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.SaleService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ManageSalesController {
    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private TableView<Sale> salesTable;
    @FXML private Button newSaleBtn;

    private final SaleService saleService;
    private final EmployeeService employeeService;
    private ObservableList<Sale> allSales = FXCollections.observableArrayList();
    private FilteredList<Sale> filteredSales;

    public ManageSalesController() {
        saleService = new SaleService();
        employeeService = new EmployeeService();
    }

    @FXML
    public void initialize() {
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupFilters();
        loadSales();
        setupButtons();
    }

    private void setupTable() {
        TableColumn<Sale, String> invoiceNumberCol = new TableColumn<>("Invoice #");
        invoiceNumberCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));

        TableColumn<Sale, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));

        TableColumn<Sale, BigDecimal> totalAmountCol = new TableColumn<>("Total Amount");
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<Sale, BigDecimal> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));

        TableColumn<Sale, BigDecimal> netAmountCol = new TableColumn<>("Net Amount");
        netAmountCol.setCellValueFactory(new PropertyValueFactory<>("netAmount"));

        TableColumn<Sale, String> createdByCol = new TableColumn<>("Cashier");
        createdByCol.setCellValueFactory(data -> {
            Employee employee = employeeService.getEmployee(data.getValue().getCreatedBy()).getData();
            return new SimpleStringProperty(employee != null ? employee.getName() : "");
        });

        TableColumn<Sale, Void> actionsCol = new TableColumn<>("Action");
        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.setOnAction(event -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    deleteSale(sale);
                });
                removeButton.getStyleClass().add("primary-table-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        salesTable.getColumns().addAll(
                invoiceNumberCol, dateCol, totalAmountCol,
                discountCol, netAmountCol, createdByCol, actionsCol
        );

        salesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
                openSaleForEdit(selectedSale);
            }
        });
    }

    private void setupFilters() {
        filteredSales = new FilteredList<>(allSales, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        dateFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());

        salesTable.setItems(filteredSales);
    }

    private void updateFilters() {
        filteredSales.setPredicate(sale -> {
            boolean matchesSearch = true;
            boolean matchesDate = true;

            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                matchesSearch = sale.getInvoiceNumber().toLowerCase().contains(searchText);
            }

            if (dateFilter.getValue() != null) {
                matchesDate = sale.getInvoiceDate().equals(dateFilter.getValue());
            }

            return matchesSearch && matchesDate;
        });
    }

    private void loadSales() {
        Employee em = EmployeeService.getLoggedInEmployee();
        ServiceResponse<List<Sale>> response = saleService.getInvoicesByBranch(em.getBranchId());
        if (response.isSuccess()) {
            allSales.clear();
            allSales.addAll(response.getData());
        } else {
            AlertUtils.showError("Failed to load sales", response.getMessage());
        }
    }

    private void setupButtons() {
        newSaleBtn.setOnAction(e -> openNewSale());
    }

    private void openNewSale() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/CashierScreens/addSale.fxml"));
            Stage stage = new Stage();
            stage.setTitle("New Sale");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadSales();
        } catch (IOException ex) {
            AlertUtils.showError("Error opening new sale window");
        }
    }

    private void openSaleForEdit(Sale sale) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/CashierScreens/AddSale.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Sale");
            stage.setScene(new Scene(loader.load()));

            AddSaleController controller = loader.getController();
            controller.setSaleForUpdate(sale);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadSales();
        } catch (IOException ex) {
            AlertUtils.showError("Error opening sale update window");
        }
    }

    private void deleteSale(Sale sale) {
        boolean confirmed = AlertUtils.showConfirmation(
                "Delete Sale",
                "Are you sure you want to delete Sale #" + sale.getInvoiceNumber() + "?"
        );

        if (confirmed) {
            ServiceResponse<Void> response = saleService.deleteInvoice(sale.getId());
            if (response.isSuccess()) {
                AlertUtils.showSuccess("Sale deleted successfully");
                loadSales();
            } else {
                AlertUtils.showError("Failed to delete sale", response.getMessage());
            }
        }
    }
}
