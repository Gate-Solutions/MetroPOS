package org.gate.metropos.Controllers.BranchManagerControllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML
    private Label totalUsersLabel;
    @FXML private Label totalSalesTodayLabel;
    @FXML private AreaChart<String, Number> profitChart;
    @FXML private Label totalProfitLabel;
    @FXML private TableView<Branch> branchTable;
    @FXML private TableColumn<Branch, String> branchNameColumn;
    @FXML private TableColumn<Branch, String> managerColumn;
    @FXML private TableColumn<Branch, String> todaySalesColumn;
    @FXML private TableColumn<Branch, String> statusColumn;
    @FXML private Label branchName;
    private ReportsService reportsService;
    private BranchService branchService;
    private EmployeeService userService;
    private Long currentBranchId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        branchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reportsService = new ReportsService();
        branchService = new BranchService();
        userService = new EmployeeService();

        // Get current branch manager's branch ID
        currentBranchId = SessionManager.getCurrentEmployee().getBranchId();

        setupTable();
        loadStatistics();
        setupProfitChart();
    }

    private void loadStatistics() {

//        ServiceResponse<List<Employee>> branchUsers = userService.getAllEmployees();
        ServiceResponse <Branch> br =branchService.getBranch(SessionManager.getCurrentEmployee().getBranchId());;

        if (br.isSuccess()) {
            totalUsersLabel.setText(String.valueOf( br.getData().getNumberOfEmployees()));
            branchName.setText(br.getData().getName());
        }





        // Load today's sales for the branch
        SalesReport todayReport = reportsService.getDailyReport(LocalDate.now(), currentBranchId);
        totalSalesTodayLabel.setText(String.format("₱ %,.2f", todayReport.getTotalSales()));
    }

    private void setupProfitChart() {
        profitChart.getXAxis().setAnimated(false);
        profitChart.getYAxis().setAnimated(false);
        updateProfitChart();
    }

    private void updateProfitChart() {
        LocalDate now = LocalDate.now();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Profit");
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (int i = 2; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int year = date.getYear();
            int month = date.getMonthValue();

            SalesReport salesReport = reportsService.getMonthlyReport(year, month, currentBranchId);
            PurchaseReport purchaseReport = reportsService.getMonthlyPurchaseReport(
                    year, month, currentBranchId, null);

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

        Node fill = series.getNode().lookup(".chart-series-area-fill");
        if (fill != null) {
            fill.setStyle(
                    "-fx-fill: linear-gradient(to bottom, " +
                            "rgba(41,128,185,0.3), rgba(41,128,185,0.1));"
            );
        }

        Node line = series.getNode().lookup(".chart-series-area-line");
        if (line != null) {
            line.setStyle("-fx-stroke: #2980b9;");
        }

        series.getData().forEach(data -> {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle(
                        "-fx-background-color: #2980b9; " +
                                "-fx-background-radius: 5px; " +
                                "-fx-padding: 5px;"
                );

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
            ServiceResponse<String> response = branchService.getManagerName(currentBranchId);
            return new SimpleStringProperty(response.getData());
        });

        todaySalesColumn.setCellValueFactory(cellData -> {
            SalesReport todayReport = reportsService.getDailyReport(
                    LocalDate.now(),
                    currentBranchId
            );
            return new SimpleStringProperty(String.format("₱ %,.2f", todayReport.getTotalSales()));
        });

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));

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
        ServiceResponse<Branch> response = branchService.getBranch(currentBranchId);
        if (response.isSuccess()) {
            branchTable.getItems().clear();
            branchTable.getItems().add(response.getData());
        }
    }
}
