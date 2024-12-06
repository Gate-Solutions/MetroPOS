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
import org.gate.metropos.models.Branch;
import org.gate.metropos.services.BranchService;
import org.gate.metropos.utils.ServiceResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BranchesController {
    @FXML private TableView<Branch> branchesTable;
    @FXML private Button addBranchBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;


    private ObservableList<Branch> allBranches = FXCollections.observableArrayList();
    private FilteredList<Branch> filteredBranches;
    private final BranchService branchService ;

    public BranchesController() {
        branchService = new BranchService();
    }



    @FXML
    public void initialize() {
        branchesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadBranches();
        setupTable();
        addBranchBtn.setOnAction(e->openAddBranchWindow());
        setupFilters();

    }

    private void loadBranches() {
        ServiceResponse<List<Branch>> response = branchService.getAllBranches();
        if (response.isSuccess()) {
            allBranches.clear();
            Collections.sort(response.getData(), Comparator.comparing(Branch::getId));
            allBranches.addAll(response.getData());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load branches: " + response.getMessage());
            alert.showAndWait();
        }
    }




    private void setupTable() {

        // Branch ID Column
        TableColumn<Branch, Long> idCol = new TableColumn<>("Branch ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Branch Code Column
        TableColumn<Branch, String> codeCol = new TableColumn<>("Branch Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("branchCode"));

        // Name Column
        TableColumn<Branch, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        // City Column
        TableColumn<Branch, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));

        // Address Column
        TableColumn<Branch, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Phone Column
        TableColumn<Branch, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Status Column
        TableColumn<Branch, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setCellFactory(column -> new TableCell<Branch, Boolean>() {
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

        // Employees Column
        TableColumn<Branch, Integer> employeesCol = new TableColumn<>("Employees");
        employeesCol.setCellValueFactory(new PropertyValueFactory<>("numberOfEmployees"));

        branchesTable.getColumns().addAll(
                idCol, codeCol, nameCol, cityCol, addressCol, phoneCol, statusCol, employeesCol
        );


        //for update branch
        branchesTable.setRowFactory(tv -> {
            TableRow<Branch> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openUpdateBranchWindow(row.getItem());
                }
            });
            return row;
        });
    }


    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Active", "Inactive"
        ));
        statusFilter.setValue("All");


        filteredBranches = new FilteredList<>(allBranches, p -> true);


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });

        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });


        branchesTable.setItems(filteredBranches);
    }

    private void updateFilters() {
        filteredBranches.setPredicate(branch -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            // Search filter
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchText = searchField.getText().toLowerCase();
                matchesSearch = branch.getName().toLowerCase().contains(searchText) ||
                        branch.getBranchCode().toLowerCase().contains(searchText) ||
                        branch.getCity().toLowerCase().contains(searchText) ||
                        branch.getAddress().toLowerCase().contains(searchText) ||
                        branch.getPhone().toLowerCase().contains(searchText);
            }

            // Status filter
            String statusValue = statusFilter.getValue();
            if (!"All".equals(statusValue)) {
                matchesStatus = ("Active".equals(statusValue) && branch.isActive()) ||
                        ("Inactive".equals(statusValue) && !branch.isActive());
            }

            return matchesSearch && matchesStatus;
        });
    }


    private void openAddBranchWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/SuperAdminScreens/add-branch.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Branch");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadBranches();


        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }



    private void openUpdateBranchWindow(Branch branch) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/gate/metropos/SuperAdminScreens/update-branch.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Update Branch");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            UpdateBranchController controller = loader.getController();
            controller.setBranch(branch);

            stage.showAndWait();


            loadBranches();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
