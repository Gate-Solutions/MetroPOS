package org.gate.metropos.Controllers.SuperAdminControllers;


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
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.util.List;

public class UsersController {
    @FXML
    private TableView<Employee> employeesTable;
    @FXML private Button addEmployeeBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private ObservableList<Employee> allEmployees = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredEmployees;
    private final EmployeeService employeeService;

    public UsersController() {
        employeeService = new EmployeeService();
    }

    @FXML
    public void initialize() {
        employeesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        setupFilters();
        loadEmployees();
        addEmployeeBtn.setOnAction(e -> openAddManagerWindow());
    }

    private void loadEmployees() {
        ServiceResponse<List<Employee>> response = employeeService.getEmployeesByRole(UserRole.BRANCH_MANAGER);
        if (response.isSuccess()) {
            allEmployees.clear();
            allEmployees.addAll(response.getData());
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load employees: " + response.getMessage());
        }
    }

    private void setupTable() {
        // Employee ID Column
        TableColumn<Employee, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Employee Number Column
        TableColumn<Employee, String> empNoCol = new TableColumn<>("Employee No");
        empNoCol.setCellValueFactory(new PropertyValueFactory<>("employeeNo"));

        // Name Column
        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Username Column
        TableColumn<Employee, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Email Column
        TableColumn<Employee, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Branch ID Column
        TableColumn<Employee, Long> branchCol = new TableColumn<>("Branch ID");
        branchCol.setCellValueFactory(new PropertyValueFactory<>("branchId"));

        // Status Column
        TableColumn<Employee, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setCellFactory(column -> new TableCell<Employee, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Active" : "Inactive");
                }
            }
        });


        TableColumn<Employee, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove Manager");
            {
                removeButton.setMaxWidth(Double.MAX_VALUE);
                removeButton.getStyleClass().add("primary-table-button");

                removeButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    if (!employee.isActive()) {
                        showAlert(Alert.AlertType.ERROR , "Already InActive" , "The manager you are trying to remove is inactive Already");
                        return;
                    }
                    confirmAndRemoveManager(employee);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });


        actionCol.setPrefWidth(150);
        employeesTable.getColumns().addAll(
                idCol, empNoCol, nameCol, usernameCol, emailCol, branchCol, statusCol, actionCol
        );


        employeesTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openUpdateManagerWindow(row.getItem());
                }
            });
            return row;
        });
    }



    private void confirmAndRemoveManager(Employee employee) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Remove Manager");
        confirmDialog.setHeaderText("Remove Branch Manager");
        confirmDialog.setContentText("Are you sure you want to remove " + employee.getName() + " as branch manager?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ServiceResponse<Void> serviceResponse = employeeService.setEmployeeStatus(employee.getId(), false);
                if (serviceResponse.isSuccess()) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Manager removed successfully");
                    loadEmployees(); // Refresh the table
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", serviceResponse.getMessage());
                }
            }
        });
    }
    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Active", "Inactive"
        ));
        statusFilter.setValue("Active");  // Set default to Active

        filteredEmployees = new FilteredList<>(allEmployees, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                updateFilters());

        statusFilter.valueProperty().addListener((observable, oldValue, newValue) ->
                updateFilters());

        employeesTable.setItems(filteredEmployees);

        // Add this line to apply the filter immediately
        updateFilters();
    }





    private void updateFilters() {
        filteredEmployees.setPredicate(employee -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            // Search filter
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchText = searchField.getText().toLowerCase();
                matchesSearch = employee.getName().toLowerCase().contains(searchText) ||
                        employee.getUsername().toLowerCase().contains(searchText) ||
                        employee.getEmail().toLowerCase().contains(searchText) ||
                        employee.getEmployeeNo().toLowerCase().contains(searchText);
            }


            String statusValue = statusFilter.getValue();
            if ("Active".equals(statusValue)) {
                matchesStatus = employee.isActive();
            } else if ("Inactive".equals(statusValue)) {
                matchesStatus = !employee.isActive();
            }

            return matchesSearch && matchesStatus;
        });
    }



    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void openAddManagerWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/gate/metropos/SuperAdminScreens/add-branchManagerInUsers.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Branch Manager");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadEmployees();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add manager window");
        }
    }

    private void openUpdateManagerWindow(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/SuperAdminScreens/update-manager.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Manager");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            UpdateManagerController controller = loader.getController();
            controller.setEmployee(employee);

            stage.showAndWait();

            loadEmployees(); // Refresh the table after update
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
