package org.gate.metropos.Controllers.BranchManagerControllers;

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
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.Supplier;
import org.gate.metropos.services.SupplierService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.util.List;


public class ManageSuppliersController {
    @FXML private TableView<Supplier> suppliersTable;
    @FXML private Button addSupplierBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private ObservableList<Supplier> allSuppliers = FXCollections.observableArrayList();
    private FilteredList<Supplier> filteredSuppliers;
    private final SupplierService supplierService;

    public ManageSuppliersController() {
        supplierService = new SupplierService();
    }

    @FXML
    public void initialize() {
        suppliersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupFilters();
        loadSuppliers();
        addSupplierBtn.setOnAction(e -> openAddSupplierWindow());
    }

    private void setupTable() {
        TableColumn<Supplier, String> idCol = new TableColumn<>("Id");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Supplier, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Supplier, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

        TableColumn<Supplier, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Supplier, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Supplier, String> ntnCol = new TableColumn<>("NTN");
        ntnCol.setCellValueFactory(new PropertyValueFactory<>("ntnNumber"));

        TableColumn<Supplier, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setCellFactory(column -> new TableCell<Supplier, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item ? "Active" : "Inactive"));
            }
        });

        TableColumn<Supplier, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.setOnAction(event -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    if (!supplier.isActive()) {
                        AlertUtils.showError("Already Inactive");
                        return;
                    }
                    confirmAndRemoveSupplier(supplier);
                });
                removeButton.setMaxWidth(Double.MAX_VALUE);
                removeButton.getStyleClass().add("primary-table-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        suppliersTable.getColumns().addAll(
               idCol, nameCol, companyCol, emailCol, phoneCol, ntnCol, statusCol, actionCol
        );

        suppliersTable.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openUpdateSupplierWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        statusFilter.setValue("Active");

        filteredSuppliers = new FilteredList<>(allSuppliers, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        suppliersTable.setItems(filteredSuppliers);
    }

    private void updateFilters() {
        filteredSuppliers.setPredicate(supplier -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                matchesSearch = supplier.getName().toLowerCase().contains(searchText) ||
                        supplier.getCompanyName().toLowerCase().contains(searchText) ||
                        supplier.getEmail().toLowerCase().contains(searchText) ||
                        supplier.getPhone().toLowerCase().contains(searchText);
            }

            String status = statusFilter.getValue();
            if ("Active".equals(status)) {
                matchesStatus = supplier.isActive();
            } else if ("Inactive".equals(status)) {
                matchesStatus = !supplier.isActive();
            }

            return matchesSearch && matchesStatus;
        });
    }

    private void loadSuppliers() {
        ServiceResponse<List<Supplier>> response = supplierService.getAllSuppliers();
        if (response.isSuccess()) {
            allSuppliers.clear();
            allSuppliers.addAll(response.getData());
        } else {
            AlertUtils.showError("Failed to load suppliers", response.getMessage());
        }
    }

    private void confirmAndRemoveSupplier(Supplier supplier) {
        if (AlertUtils.showConfirmation("Are you sure you want to remove " + supplier.getName() + "?" )) {
            ServiceResponse<Void> response = supplierService.setSupplierStatus(supplier.getId(), false);
            if (response.isSuccess()) {
                loadSuppliers();
                AlertUtils.showSuccess("Supplier removed successfully");
            } else {
                AlertUtils.showError("Failed to remove supplier", response.getMessage());
            }
        }
    }

    private void openAddSupplierWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/BranchManagerScreens/add-update-supplier.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Supplier");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadSuppliers();
        } catch (IOException ex) {
            AlertUtils.showError("Error", "Failed to open add supplier window");
        }
    }

    private void openUpdateSupplierWindow(Supplier supplier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/BranchManagerScreens/add-update-supplier.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Supplier");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            AddUpdateSupplierController controller = loader.getController();
            controller.setSupplierForUpdate(supplier);

            stage.showAndWait();
            loadSuppliers();
        } catch (IOException ex) {
            AlertUtils.showError("Error", "Failed to open update supplier window");
        }
    }
}
