package org.gate.metropos.Controllers.CashierControllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import org.gate.metropos.models.BranchProduct;
import org.gate.metropos.models.Reports.ReportCriteria;
import org.gate.metropos.models.Reports.SalesReport;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.ProductService;
import org.gate.metropos.services.ReportsService;
import org.gate.metropos.utils.ServiceResponse;


import java.util.List;

public class DashboardController {
    @FXML private Text productCount;
    @FXML private Text saleAmount;
    @FXML private Text invoiceCount;

    private final ProductService productService;
    private final ReportsService reportService;

    public DashboardController() {
        productService = new ProductService();
        reportService = new ReportsService();
    }

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Get products count
        ServiceResponse<List<BranchProduct>> productsResponse =
                productService.getProductsByBranch(EmployeeService.getLoggedInEmployee().getBranchId());
        if (productsResponse.isSuccess()) {
            productCount.setText(String.valueOf(productsResponse.getData().size()));
        }

        // Get sales report for today
        ReportCriteria criteria = new ReportCriteria();
        criteria.setBranchId(EmployeeService.getLoggedInEmployee().getBranchId());
        criteria.setEmployeeId(EmployeeService.getLoggedInEmployee().getId());

        SalesReport report = reportService.getSalesReport(criteria);
        if (report != null) {
            saleAmount.setText(report.getTotalSales().toString());
            invoiceCount.setText(String.valueOf(report.getDetails().size()));
        }
    }
}
