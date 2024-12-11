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
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.util.List;

public class ManageCashiersController {
    @FXML
    private TableView<Employee> cashiersTable;
    @FXML private Button addCashierBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private ObservableList<Employee> allCashiers = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredCashiers;
    private final EmployeeService employeeService;

    public ManageCashiersController() {
        employeeService = new EmployeeService();
    }

    @FXML
    public void initialize() {
        cashiersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupFilters();
        loadCashiers();
        addCashierBtn.setOnAction(e -> openAddCashierWindow());
    }

    private void loadCashiers() {
        ServiceResponse<List<Employee>> response = employeeService.getEmployeesByRole(UserRole.CASHIER);
        if (response.isSuccess()) {
            allCashiers.clear();
            allCashiers.addAll(response.getData());
        } else {
            AlertUtils.showError("Failed to load cashiers: " + response.getMessage());
        }
    }

    private void setupTable() {
        TableColumn<Employee, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Employee, String> empNoCol = new TableColumn<>("Employee No");
        empNoCol.setCellValueFactory(new PropertyValueFactory<>("employeeNo"));

        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Employee, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Employee, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Employee, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setCellFactory(column -> new TableCell<Employee, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item ? "Active" : "Inactive"));
            }
        });

        TableColumn<Employee, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove Cashier");
            {
                removeButton.setMaxWidth(Double.MAX_VALUE);
                removeButton.getStyleClass().add("primary-table-button");
                removeButton.setOnAction(event -> {
                    Employee cashier = getTableView().getItems().get(getIndex());
                    if (!cashier.isActive()) {
                        AlertUtils.showError("Already Inactive", "This cashier is already inactive");
                        return;
                    }
                    confirmAndRemoveCashier(cashier);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        actionCol.setPrefWidth(150);
        cashiersTable.getColumns().addAll(
                idCol, empNoCol, nameCol, usernameCol, emailCol, statusCol, actionCol
        );

        cashiersTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openUpdateCashierWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        statusFilter.setValue("Active");

        filteredCashiers = new FilteredList<>(allCashiers, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        cashiersTable.setItems(filteredCashiers);
        updateFilters();
    }

    private void updateFilters() {
        filteredCashiers.setPredicate(cashier -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchText = searchField.getText().toLowerCase();
                matchesSearch = cashier.getName().toLowerCase().contains(searchText) ||
                        cashier.getUsername().toLowerCase().contains(searchText) ||
                        cashier.getEmail().toLowerCase().contains(searchText) ||
                        cashier.getEmployeeNo().toLowerCase().contains(searchText);
            }

            String statusValue = statusFilter.getValue();
            if ("Active".equals(statusValue)) {
                matchesStatus = cashier.isActive();
            } else if ("Inactive".equals(statusValue)) {
                matchesStatus = !cashier.isActive();
            }

            return matchesSearch && matchesStatus;
        });
    }

    private void confirmAndRemoveCashier(Employee cashier) {
        if (AlertUtils.showConfirmation("Are you sure you want to remove " + cashier.getName() + " as cashier?")) {
            ServiceResponse<Void> response = employeeService.setEmployeeStatus(cashier.getId(), false);
            if (response.isSuccess()) {
                AlertUtils.showSuccess("Cashier removed successfully");
                loadCashiers();
            } else {
                AlertUtils.showError(response.getMessage());
            }
        }
    }

    private void openAddCashierWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/BranchManagerScreens/add-update-cashier.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Cashier");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadCashiers();
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Failed to open add cashier window");
        }
    }

    private void openUpdateCashierWindow(Employee cashier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/BranchManagerScreens/add-update-cashier.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Cashier");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            AddUpdateCashierController controller = loader.getController();
            controller.setEmployeeForUpdate(cashier);

            stage.showAndWait();
            loadCashiers();
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Failed to open update cashier window");
        }
    }
}
