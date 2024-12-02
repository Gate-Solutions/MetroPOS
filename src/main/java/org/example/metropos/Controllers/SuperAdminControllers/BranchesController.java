package org.example.metropos.Controllers.SuperAdminControllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class BranchesController {
//    @FXML private TableView<Branch> branchesTable;
    @FXML private Button addBranchBtn;

    @FXML
    public void initialize() {
        setupTable();
        setupAddButton();
        loadBranches();
    }

    private void setupTable() {
//        TableColumn<Branch, String> idCol = new TableColumn<>("Branch ID");
//        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
//
//        TableColumn<Branch, String> nameCol = new TableColumn<>("Name");
//        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
//
//        TableColumn<Branch, String> locationCol = new TableColumn<>("Location");
//        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
//
//        TableColumn<Branch, String> managerCol = new TableColumn<>("Manager");
//        managerCol.setCellValueFactory(new PropertyValueFactory<>("manager"));
//
//        TableColumn<Branch, String> contactCol = new TableColumn<>("Contact");
//        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
//
//        TableColumn<Branch, String> statusCol = new TableColumn<>("Status");
//        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
//
//        branchesTable.getColumns().addAll(idCol, nameCol, locationCol, managerCol, contactCol, statusCol);
    }

    private void setupAddButton() {
        addBranchBtn.setOnAction(e -> showAddBranchDialog());
    }

    private void showAddBranchDialog() {
        // Create and show add branch dialog
//        Dialog<Branch> dialog = new Dialog<>();
//        dialog.setTitle("Add New Branch");
        // Add dialog content and handling
    }

    private void loadBranches() {
        // Load branches from database
    }
}