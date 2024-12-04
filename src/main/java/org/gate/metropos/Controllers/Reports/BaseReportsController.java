package org.gate.metropos.Controllers.Reports;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.gate.metropos.models.Reports.*;
import org.gate.metropos.models.Sale;
import org.gate.metropos.services.ReportsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public abstract class BaseReportsController {
    @FXML protected DatePicker startDatePicker;
    @FXML protected DatePicker endDatePicker;
    @FXML protected ComboBox<String> reportTypeComboBox;
    @FXML protected Button generateReportBtn;
    @FXML protected TabPane reportTabs;
    @FXML protected BarChart<String, Number> salesBarChart;
    @FXML protected LineChart<String, Number> trendLineChart;
    @FXML protected PieChart distributionPieChart;
    @FXML protected VBox reportContainer;
    @FXML protected Label totalSalesLabel;
    @FXML protected Label totalDiscountsLabel;
    @FXML protected Label netRevenueLabel;
    @FXML protected TableView<Sale> reportTable;
    @FXML protected TableColumn<Sale, LocalDate> dateColumn;
    @FXML protected TableColumn<Sale, String> invoiceColumn;
    @FXML protected TableColumn<Sale, BigDecimal> amountColumn;
    @FXML protected TableColumn<Sale, BigDecimal> discountColumn;
    @FXML protected TableColumn<Sale, BigDecimal> netAmountColumn;
    @FXML protected TableColumn<Sale, String> cashierColumn;



    protected ReportsService reportsService;
    protected Long currentBranchId;

    public void initialize() {
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reportsService = new ReportsService();
        setupControls();
        setupEventHandlers();
    }

    protected void setupTableColumns() {
        dateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getInvoiceDate()));
        invoiceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        amountColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalAmount()));
        discountColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDiscount()));
        netAmountColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getNetAmount()));
        cashierColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreator().getName()));
    }

    protected void updateTables(ReportCriteria criteria) {
        SalesReport report = reportsService.getSalesReport(criteria);
        reportTable.getItems().clear();
        reportTable.getItems().addAll(report.getDetails());

        String currencyFormat = "₱ %,.2f";
        totalSalesLabel.setText(String.format(currencyFormat, report.getTotalSales()));
        totalDiscountsLabel.setText(String.format(currencyFormat, report.getTotalDiscounts()));
        netRevenueLabel.setText(String.format(currencyFormat, report.getNetAmount()));
    }



    protected void setupControls() {
        reportTypeComboBox.getItems().addAll("Daily", "Monthly", "Yearly");
        reportTypeComboBox.setValue("Daily");

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
    }

    protected void setupEventHandlers() {
        generateReportBtn.setOnAction(e -> generateReport());
    }

    protected void generateReport() {
        ReportCriteria criteria = buildCriteria();
        updateCharts(criteria);
        updateTables(criteria);
    }

    protected ReportCriteria buildCriteria() {
        return ReportCriteria.builder()
                .startDate(startDatePicker.getValue())
                .endDate(endDatePicker.getValue())
                .branchId(currentBranchId)
                .build();
    }

    protected abstract void updateCharts(ReportCriteria criteria);

    protected void updateSalesBarChart(Map<String, Number> data) {
        salesBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
        salesBarChart.getData().add(series);
    }

    protected void updateTrendLineChart(Map<String, Number> data) {
        trendLineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
        trendLineChart.getData().add(series);
    }

    protected void updatePieChart(Map<String, Number> data) {
        distributionPieChart.getData().clear();
        data.forEach((key, value) ->
                distributionPieChart.getData().add(new PieChart.Data(key, value.doubleValue()))
        );
    }
}
