package org.gate.metropos.Controllers.DataEntryOperator;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import org.gate.metropos.models.Product;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoice;
import org.gate.metropos.models.Supplier;
import org.gate.metropos.services.EmployeeService;
import org.gate.metropos.services.ProductService;
import org.gate.metropos.services.PurchaseInvoiceService;
import org.gate.metropos.services.SupplierService;
import org.gate.metropos.utils.ServiceResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardController {
    @FXML private Text productCount;
    @FXML private Text supplierCount;
    @FXML private Text invoiceCount;

    private final ProductService productService;
    private final SupplierService supplierService;
    private final PurchaseInvoiceService invoiceService;
    private final ExecutorService executorService;

    public DashboardController() {
        productService = new ProductService();
        supplierService = new SupplierService();
        invoiceService = new PurchaseInvoiceService();
        executorService = Executors.newFixedThreadPool(3);
    }

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        CompletableFuture.runAsync(() -> {
            ServiceResponse<List<Product>> productsResponse = productService.getAllProducts();
            if (productsResponse.isSuccess()) {
                Platform.runLater(() ->
                        productCount.setText(String.valueOf(productsResponse.getData().size()))
                );
            }
        }, executorService);

        CompletableFuture.runAsync(() -> {
            ServiceResponse<List<Supplier>> suppliersResponse = supplierService.getAllSuppliers();
            if (suppliersResponse.isSuccess()) {
                Platform.runLater(() ->
                        supplierCount.setText(String.valueOf(suppliersResponse.getData().size()))
                );
            }
        }, executorService);

        CompletableFuture.runAsync(() -> {
            ServiceResponse<List<PurchaseInvoice>> invoicesResponse = invoiceService.getInvoicesByBranch(EmployeeService.getLoggedInEmployee().getBranchId());
            if (invoicesResponse.isSuccess()) {
                Platform.runLater(() ->
                        invoiceCount.setText(String.valueOf(invoicesResponse.getData().size()))
                );
            }
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
