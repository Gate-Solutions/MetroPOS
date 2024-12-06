package org.gate.metropos.Controllers.Reports;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.gate.metropos.models.Reports.ReportCriteria;
import org.gate.metropos.models.Reports.SalesReport;
import org.gate.metropos.models.Sale;
import org.gate.metropos.utils.SessionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class BranchReportsController extends BaseReportsController {
    @FXML
    private TableColumn<Sale, LocalDate> dateColumn;
    @FXML private TableColumn<Sale, String> invoiceColumn;
    @FXML private TableColumn<Sale, BigDecimal> amountColumn;
    @FXML private TableColumn<Sale, BigDecimal> discountColumn;
    @FXML private TableColumn<Sale, BigDecimal> netAmountColumn;
    @FXML private TableColumn<Sale, String> cashierColumn;
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<Integer> monthComboBox;
    @FXML private HBox dateControls;




    @Override
    public void initialize() {
        super.initialize();
        currentBranchId = SessionManager.getCurrentEmployee().getBranchId();
        setupTableColumns();
        setupReportTypeControls();
        generateReport();
    }
    private void setupReportTypeControls() {
        reportTypeComboBox.getItems().clear();
        // Setup report types with new Range option
        reportTypeComboBox.getItems().addAll("Daily", "Range", "Monthly", "Yearly");
        reportTypeComboBox.setValue("Daily");

        // Rest of the setup remains same
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear; year >= currentYear - 4; year--) {
            yearComboBox.getItems().add(year);
        }
        yearComboBox.setValue(currentYear);

        for (int month = 1; month <= 12; month++) {
            monthComboBox.getItems().add(month);
        }
        monthComboBox.setValue(LocalDate.now().getMonthValue());

        // Set initial visibility
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        yearComboBox.setVisible(false);
        monthComboBox.setVisible(false);
        startDatePicker.setVisible(false);
        endDatePicker.setVisible(false);

        reportTypeComboBox.setOnAction(e -> updateControlsVisibility());
    }

    private void updateControlsVisibility() {
        String selectedType = reportTypeComboBox.getValue();
        switch (selectedType) {
            case "Daily":
                startDatePicker.setVisible(false);
                endDatePicker.setVisible(false);
                yearComboBox.setVisible(false);
                monthComboBox.setVisible(false);
                break;
            case "Range":
                startDatePicker.setVisible(true);
                endDatePicker.setVisible(true);
                yearComboBox.setVisible(false);
                monthComboBox.setVisible(false);
                break;
            case "Monthly":
                startDatePicker.setVisible(false);
                endDatePicker.setVisible(false);
                yearComboBox.setVisible(true);
                monthComboBox.setVisible(true);
                break;
            case "Yearly":
                startDatePicker.setVisible(false);
                endDatePicker.setVisible(false);
                yearComboBox.setVisible(true);
                monthComboBox.setVisible(false);
                break;
        }
        generateReport();
    }
    @Override
    protected void generateReport() {
        String reportType = reportTypeComboBox.getValue();
        SalesReport report;

        switch (reportType) {
            case "Daily":
                report = reportsService.getDailyReport(LocalDate.now(), currentBranchId);
                break;
            case "Range":
                ReportCriteria criteria = ReportCriteria.builder()
                        .startDate(startDatePicker.getValue())
                        .endDate(endDatePicker.getValue())
                        .branchId(currentBranchId)
                        .build();
                report = reportsService.getSalesReport(criteria);
                break;
            case "Monthly":
                report = reportsService.getMonthlyReport(
                        yearComboBox.getValue(),
                        monthComboBox.getValue(),
                        currentBranchId
                );
                break;
            case "Yearly":
                report = reportsService.getYearlyReport(
                        yearComboBox.getValue(),
                        currentBranchId
                );
                break;
            default:
                return;
        }

        updateChartsAndTables(report);
    }
    private void updateChartsAndTables(SalesReport report) {
        // Update charts
        Map<String, Number> salesDistribution = new LinkedHashMap<>();
        salesDistribution.put("Total Sales", report.getTotalSales());
        salesDistribution.put("Discounts", report.getTotalDiscounts());
        salesDistribution.put("Net Amount", report.getNetAmount());

        updateSalesBarChart(salesDistribution);
        updatePieChart(salesDistribution);

        // Update trend chart
        Map<String, Number> trendData = new LinkedHashMap<>();
        report.getDetails().forEach(sale ->
                trendData.put(sale.getInvoiceDate().toString(), sale.getNetAmount())
        );
        updateTrendLineChart(trendData);

        // Update table and summary
        reportTable.getItems().clear();
        reportTable.getItems().addAll(report.getDetails());
        updateSummaryStatistics(report);
    }



    protected void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        netAmountColumn.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        cashierColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreator().getName()));

        setupCurrencyFormat(amountColumn);
        setupCurrencyFormat(discountColumn);
        setupCurrencyFormat(netAmountColumn);
    }

    private void setupCurrencyFormat(TableColumn<Sale, BigDecimal> column) {
        column.setCellFactory(tc -> new TableCell<Sale, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("₱ %,.2f", amount));
                }
            }
        });
    }





    @Override
    protected ReportCriteria buildCriteria() {
        ReportCriteria criteria = super.buildCriteria();
        criteria.setBranchId(currentBranchId);
        return criteria;
    }

    @Override
    protected void updateCharts(ReportCriteria criteria) {
        SalesReport salesReport = reportsService.getSalesReport(criteria);
        // Sales Distribution Chart
        Map<String, Number> salesDistribution = new LinkedHashMap<>();
        salesDistribution.put("Total Sales", salesReport.getTotalSales());
        salesDistribution.put("Discounts", salesReport.getTotalDiscounts());
        salesDistribution.put("Net Amount", salesReport.getNetAmount());

        updateSalesBarChart(salesDistribution);
        updatePieChart(salesDistribution);

        // Sales Trend Chart
        Map<String, Number> trendData = new LinkedHashMap<>();
        salesReport.getDetails().forEach(sale ->
                trendData.put(sale.getInvoiceDate().toString(), sale.getNetAmount())
        );
        updateTrendLineChart(trendData);
    }

    @Override
    protected void updateTables(ReportCriteria criteria) {
        reportTable.getItems().clear();
        SalesReport report = reportsService.getSalesReport(criteria);
        reportTable.getItems().addAll(report.getDetails());
        updateSummaryStatistics(report);
    }

    private void updateSummaryStatistics(SalesReport report) {
        String currencyFormat = "₱ %,.2f";
        totalSalesLabel.setText(String.format(currencyFormat, report.getTotalSales()));
        totalDiscountsLabel.setText(String.format(currencyFormat, report.getTotalDiscounts()));
        netRevenueLabel.setText(String.format(currencyFormat, report.getNetAmount()));

        if (!report.getDetails().isEmpty()) {
            BigDecimal averageSale = report.getTotalSales()
                    .divide(BigDecimal.valueOf(report.getDetails().size()), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}
