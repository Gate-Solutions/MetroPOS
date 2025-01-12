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
import javafx.util.StringConverter;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageCashiersController {
    @FXML
    private TableView<Employee> cashiersTable;
    @FXML private Button addCashierBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<UserRole> roleFilter;

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
        List<UserRole> roles = Arrays.asList(UserRole.CASHIER, UserRole.DATA_ENTRY_OPERATOR);
        List<Employee> employees = new ArrayList<>();
        for (UserRole role : roles) {
            ServiceResponse<List<Employee>> response = employeeService.getEmployeesByRole(role);
            if (response.isSuccess()) {
                employees.addAll(response.getData());
            }
        }
        allCashiers.clear();
        allCashiers.addAll(employees);
        updateFilters();
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

        TableColumn<Employee, UserRole> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setCellFactory(column -> new TableCell<Employee, UserRole>() {
            @Override
            protected void updateItem(UserRole role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                } else {
                    setText(role.equals(UserRole.DATA_ENTRY_OPERATOR) ? "Data Entry" : "Cashier");
                }
            }
        });

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
            private final Button removeButton = new Button("Remove Employee");
            {
                removeButton.setMaxWidth(Double.MAX_VALUE);
                removeButton.getStyleClass().add("primary-table-button");
                removeButton.setOnAction(event -> {
                    Employee cashier = getTableView().getItems().get(getIndex());
                    if (!cashier.isActive()) {
                        AlertUtils.showError("Already Inactive", "This employee is already inactive");
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
                idCol, empNoCol, nameCol, usernameCol, emailCol, roleCol, statusCol, actionCol
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

        roleFilter.setItems(FXCollections.observableArrayList(
                UserRole.CASHIER, UserRole.DATA_ENTRY_OPERATOR
        ));
        roleFilter.setConverter(new StringConverter<UserRole>() {
            @Override
            public String toString(UserRole role) {
                if (role == null) return "All";
                return role.equals(UserRole.DATA_ENTRY_OPERATOR) ? "Data Entry" : "Cashier";
            }

            @Override
            public UserRole fromString(String string) {
                return null;
            }
        });

        filteredCashiers = new FilteredList<>(allCashiers, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        cashiersTable.setItems(filteredCashiers);
        updateFilters();
    }

    private void updateFilters() {
        filteredCashiers.setPredicate(cashier -> {
            if (cashier == null) return false;

            // Get current filter values
            String searchText = searchField.getText();
            String statusValue = statusFilter.getValue();
            UserRole selectedRole = roleFilter.getValue();

            // Search text matching
            boolean matchesSearch = true;
            if (searchText != null && !searchText.isEmpty()) {
                searchText = searchText.toLowerCase();
                matchesSearch = (cashier.getName() != null && cashier.getName().toLowerCase().contains(searchText)) ||
                        (cashier.getUsername() != null && cashier.getUsername().toLowerCase().contains(searchText)) ||
                        (cashier.getEmail() != null && cashier.getEmail().toLowerCase().contains(searchText)) ||
                        (cashier.getEmployeeNo() != null && cashier.getEmployeeNo().toLowerCase().contains(searchText));
            }

            // Status matching - maintain the selected status across role changes
            boolean matchesStatus = true;
            if (statusValue != null) {
                switch (statusValue) {
                    case "Active":
                        matchesStatus = cashier.isActive();
                        break;
                    case "Inactive":
                        matchesStatus = !cashier.isActive();
                        break;
                    default: // "All"
                        matchesStatus = true;
                }
            }

            // Role matching
            boolean matchesRole = selectedRole == null ||
                    (cashier.getRole() != null && cashier.getRole().equals(selectedRole));

            return matchesSearch && matchesStatus && matchesRole;
        });
    }

    private void confirmAndRemoveCashier(Employee cashier) {
        if (AlertUtils.showConfirmation("Are you sure you want to remove employee " + cashier.getName() + "?")) {
            ServiceResponse<Void> response = employeeService.setEmployeeStatus(cashier.getId(), false);
            if (response.isSuccess()) {
                AlertUtils.showSuccess("Employee removed successfully");
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
            stage.setTitle("Add New Employee");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadCashiers();
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Failed to open add employee window");
        }
    }

    private void openUpdateCashierWindow(Employee cashier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/BranchManagerScreens/add-update-cashier.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Employee");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            AddUpdateCashierController controller = loader.getController();
            controller.setEmployeeForUpdate(cashier);

            stage.showAndWait();
            loadCashiers();
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Failed to open update employee window");
        }
    }
}
