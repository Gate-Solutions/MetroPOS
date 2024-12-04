package org.gate.metropos.Controllers.Reports;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Reports.ReportCriteria;
import org.gate.metropos.models.Reports.SalesReport;
import org.gate.metropos.models.Sale;
import org.gate.metropos.services.BranchService;
import org.gate.metropos.utils.AlertUtils;
import org.gate.metropos.utils.ServiceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminReportsController extends BaseReportsController {
    @FXML private ComboBox<Branch> branchSelector;
    private BranchService branchService;

    @FXML private TableColumn<Sale, LocalDate> dateColumn;
    @FXML private TableColumn<Sale, String> invoiceColumn;
    @FXML private TableColumn<Sale, BigDecimal> amountColumn;
    @FXML private TableColumn<Sale, BigDecimal> discountColumn;
    @FXML private TableColumn<Sale, BigDecimal> netAmountColumn;
    @FXML private TableColumn<Sale, String> cashierColumn;



    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<Integer> monthComboBox;




    @Override

    public void initialize() {
        super.initialize();
        branchService = new BranchService();
        setupBranchSelector();
        setupTableColumns();
        setupReportTypeControls();
    }

    private void setupReportTypeControls() {
        reportTypeComboBox.getItems().clear();
        reportTypeComboBox.getItems().addAll("Daily", "Range", "Monthly", "Yearly");
        reportTypeComboBox.setValue("Daily");

        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear; year >= currentYear - 4; year--) {
            yearComboBox.getItems().add(year);
        }
        yearComboBox.setValue(currentYear);

        for (int month = 1; month <= 12; month++) {
            monthComboBox.getItems().add(month);
        }
        monthComboBox.setValue(LocalDate.now().getMonthValue());

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
        Branch selectedBranch = branchSelector.getValue();
        if (selectedBranch != null) {
            currentBranchId = selectedBranch.getId();
        }

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
        Map<String, Number> salesDistribution = new LinkedHashMap<>();
        salesDistribution.put("Total Sales", report.getTotalSales());
        salesDistribution.put("Discounts", report.getTotalDiscounts());
        salesDistribution.put("Net Amount", report.getNetAmount());

        updateSalesBarChart(salesDistribution);
        updatePieChart(salesDistribution);

        Map<String, Number> trendData = new LinkedHashMap<>();
        report.getDetails().forEach(sale ->
                trendData.put(sale.getInvoiceDate().toString(), sale.getNetAmount())
        );
        updateTrendLineChart(trendData);

        if (showBranchComparison()) {
            updateBranchComparisonChart();
        }

        reportTable.getItems().clear();
        reportTable.getItems().addAll(report.getDetails());
        updateSummaryStatistics(report);
    }


    @Override
    protected void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        netAmountColumn.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        cashierColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreator().getName()));

        // Format currency columns
        amountColumn.setCellFactory(column -> new TableCell<Sale, BigDecimal>() {
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

        discountColumn.setCellFactory(column -> new TableCell<Sale, BigDecimal>() {
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

        netAmountColumn.setCellFactory(column -> new TableCell<Sale, BigDecimal>() {
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







    private void setupBranchSelector() {
        ServiceResponse<List<Branch>> response = branchService.getAllBranches();

        if (response.isSuccess()) {
            branchSelector.getItems().addAll(response.getData());

            // Set up branch display format in ComboBox
            branchSelector.setConverter(new StringConverter<Branch>() {
                @Override
                public String toString(Branch branch) {
                    return branch != null ? branch.getName() + " (" + branch.getBranchCode() + ")" : "";
                }

                @Override
                public Branch fromString(String string) {
                    return null;
                }
            });

            branchSelector.setOnAction(e -> {
                Branch selectedBranch = branchSelector.getValue();
                if (selectedBranch != null) {
                    currentBranchId = selectedBranch.getId();
                    generateReport();
                }
            });
        } else {
            AlertUtils.showError("Error\", \"Failed to load branches", response.getMessage());
        }
    }

    @Override
    protected ReportCriteria buildCriteria() {
        ReportCriteria criteria = super.buildCriteria();
        Branch selectedBranch = branchSelector.getValue();
        if (selectedBranch != null) {
            criteria.setBranchId(selectedBranch.getId());
        }
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

        // Additional branch comparison if needed
        if (showBranchComparison()) {
            updateBranchComparisonChart();
        }
    }





    private boolean showBranchComparison() {
        return branchSelector.getValue() == null;
    }

    private void updateBranchComparisonChart() {
        ServiceResponse<List<Branch>> branchResponse = branchService.getAllBranches();
        if (branchResponse.isSuccess()) {
            Map<String, Number> branchComparison = new LinkedHashMap<>();

            branchResponse.getData().forEach(branch -> {
                ReportCriteria branchCriteria = buildCriteria();
                branchCriteria.setBranchId(branch.getId());
                SalesReport branchReport = reportsService.getSalesReport(branchCriteria);
                branchComparison.put(branch.getName(), branchReport.getTotalSales());
            });


            updateSalesBarChart(branchComparison);
        }
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
