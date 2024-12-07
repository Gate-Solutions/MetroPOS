package org.gate.metropos.Controllers.SuperAdminControllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.Reports.PurchaseReport;
import org.gate.metropos.models.Reports.SalesReport;
import org.gate.metropos.services.BranchService;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.ReportsService;
import org.gate.metropos.utils.ServiceResponse;
import org.gate.metropos.utils.SessionManager;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML
    private ComboBox<Branch> branchSelector;
    @FXML private Label totalBranchesLabel;
    @FXML private Label activeBranchesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalSalesTodayLabel;
    @FXML private AreaChart<String, Number> profitChart;
    @FXML private Label totalProfitLabel;
    @FXML private TableView<Branch> branchTable;
    @FXML private TableColumn<Branch, String> branchNameColumn;
    @FXML private TableColumn<Branch, String> managerColumn;
    @FXML private TableColumn<Branch, String> todaySalesColumn;
    @FXML private TableColumn<Branch, String> statusColumn;
    @FXML private Label activeUsersLabel;


    private ReportsService reportsService;
    private BranchService branchService;
    private EmployeeService userService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {


        branchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reportsService = new ReportsService();
        branchService = new BranchService();
        userService = new EmployeeService();
        setupTable();
        setupBranchSelector();
        loadStatistics();
        setupProfitChart();

    }

    private void setupBranchSelector() {
        if (SessionManager.isSuperAdmin()) {
            ServiceResponse<List<Branch>> response = branchService.getAllBranches();
            if (response.isSuccess()) {
                branchSelector.getItems().addAll(response.getData());
                branchSelector.setConverter(new StringConverter<Branch>() {
                    @Override
                    public String toString(Branch branch) {
                        return branch != null ? branch.getName() : "All Branches";
                    }

                    @Override
                    public Branch fromString(String string) {
                        return null;
                    }
                });
                branchSelector.setOnAction(e -> updateProfitChart());
            }
        } else {
            branchSelector.setVisible(false);
        }
    }

    private void loadStatistics() {
        // Load total branches
        ServiceResponse<List<Branch>> branchResponse = branchService.getAllBranches();
        if (branchResponse.isSuccess()) {
            long totalBranches = branchResponse.getData().size();
            long activeBranches = branchResponse.getData().stream()
                    .filter(Branch::isActive).count();
            totalBranchesLabel.setText(String.valueOf(totalBranches));
            activeBranchesLabel.setText(activeBranches + " Active");

        }

        // Load total users
        ServiceResponse<List<Employee>> userCount = userService.getAllEmployees();
        if (userCount.isSuccess()) {
            totalUsersLabel.setText(String.valueOf(userCount.getData().size()));
            activeUsersLabel.setText(String.valueOf(userCount.getData().stream().filter(Employee::isActive).count() + " Active"));
        }

        // Load today's sales
        SalesReport todayReport = reportsService.getDailyReport(LocalDate.now(), null);
        totalSalesTodayLabel.setText(String.format("₱ %,.2f", todayReport.getTotalSales()));
    }

    private void setupProfitChart() {
        profitChart.getXAxis().setAnimated(false);
        profitChart.getYAxis().setAnimated(false);
        updateProfitChart();
    }

    private void updateProfitChart() {
        Branch selectedBranch = branchSelector.getValue();
        Long branchId = selectedBranch != null ? selectedBranch.getId() : null;

        LocalDate now = LocalDate.now();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Profit");
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (int i = 2; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int year = date.getYear();
            int month = date.getMonthValue();

            SalesReport salesReport = reportsService.getMonthlyReport(year, month, branchId);
            PurchaseReport purchaseReport = reportsService.getMonthlyPurchaseReport(
                    year, month, branchId, null);

            BigDecimal sales = salesReport.getTotalSales();
            BigDecimal purchases = purchaseReport.getTotalPurchases();
            BigDecimal profit = sales.subtract(purchases);
            totalProfit = totalProfit.add(profit);

            String monthName = date.getMonth().toString().charAt(0) +
                    date.getMonth().toString().substring(1).toLowerCase();

            series.getData().add(new XYChart.Data<>(monthName, profit.doubleValue()));
        }

        profitChart.getData().clear();
        profitChart.getData().add(series);

        // Style the area chart
        series.getNode().setStyle(
                "-fx-stroke: #2980b9; " +
                        "-fx-stroke-width: 2px;"
        );

        // Style the fill area
        Node fill = series.getNode().lookup(".chart-series-area-fill");
        if (fill != null) {
            fill.setStyle(
                    "-fx-fill: linear-gradient(to bottom, " +
                            "rgba(41,128,185,0.3), rgba(41,128,185,0.1));"
            );
        }

        // Style the line
        Node line = series.getNode().lookup(".chart-series-area-line");
        if (line != null) {
            line.setStyle("-fx-stroke: #2980b9;");
        }

        // Add hover effect to data points
        series.getData().forEach(data -> {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle(
                        "-fx-background-color: #2980b9; " +
                                "-fx-background-radius: 5px; " +
                                "-fx-padding: 5px;"
                );

                // Tooltip showing exact value
                Tooltip tooltip = new Tooltip(String.format("₱ %,.2f", data.getYValue()));
                Tooltip.install(node, tooltip);
            }
        });

        totalProfitLabel.setText(String.format("Total Profit: ₱ %,.2f", totalProfit));
    }



    private void setupTable() {
        branchNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        managerColumn.setCellValueFactory(cellData -> {
            ServiceResponse<String>response = branchService.getManagerName(cellData.getValue().getId());

            return new SimpleStringProperty(response.getData());
        });

        todaySalesColumn.setCellValueFactory(cellData -> {
            SalesReport todayReport = reportsService.getDailyReport(
                    LocalDate.now(),
                    cellData.getValue().getId()
            );
            return new SimpleStringProperty(String.format("₱ %,.2f", todayReport.getTotalSales()));
        });

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));

        // Style the status column
        statusColumn.setCellFactory(column -> new TableCell<Branch, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equals("Active")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                    }
                }
            }
        });

        loadTableData();
    }

    private void loadTableData() {
        ServiceResponse<List<Branch>> response = branchService.getAllBranches();
        if (response.isSuccess()) {
            branchTable.getItems().clear();
            branchTable.getItems().addAll(response.getData());
        }
    }



}
