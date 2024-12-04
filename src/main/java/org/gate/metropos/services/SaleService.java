package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import org.gate.metropos.models.*;
import org.gate.metropos.repositories.BranchProductRepository;
import org.gate.metropos.repositories.BranchRepository;
import org.gate.metropos.repositories.EmployeeRepository;
import org.gate.metropos.repositories.SaleRepository;
import org.gate.metropos.utils.ServiceResponse;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
public class SaleService {
    private final SaleRepository saleRepository;
    private final BranchProductRepository branchProductRepository;
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;

    public SaleService() {
        this.saleRepository = new SaleRepository();
        this.branchProductRepository = new BranchProductRepository();
        this.branchRepository = new BranchRepository();
        this.employeeRepository = new EmployeeRepository();
    }

    public ServiceResponse<Sale> createSale(Sale sale) {

        if (sale.getBranchId() == null || sale.getCreatedBy() == null) {
            return new ServiceResponse<>(false, 400, "Branch ID and Created By are required", null);
        }

        // Validating branch actually exists
        Branch branch = branchRepository.findById(sale.getBranchId());
        if (branch == null) {
            return new ServiceResponse<>(false, 400, "Invalid branch ID: Branch does not exist", null);
        }

        // Validating user exists
        User user = employeeRepository.findById(sale.getCreatedBy());
        if (user == null) {
            return new ServiceResponse<>(false, 400, "Invalid user ID: User does not exist", null);
        }
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            return new ServiceResponse<>(false, 400, "Sale items cannot be empty", null);
        }

        for (SaleItem item : sale.getItems()) {
            ServiceResponse<Void> stockResponse = validateAndUpdateStock(
                    sale.getBranchId(),
                    item.getProductId(),
                    item.getQuantity()
            );
            if (!stockResponse.isSuccess()) {
                return new ServiceResponse<>(false, 400, stockResponse.getMessage(), null);
            }
        }

        sale.setInvoiceNumber(generateInvoiceNumber());

        if (sale.getInvoiceDate() == null) {
            sale.setInvoiceDate(LocalDate.now());
        }

        Sale newSale = saleRepository.createSale(sale);
        return new ServiceResponse<>(true, 200, "Sale created successfully", newSale);
    }

    private ServiceResponse<Void> validateAndUpdateStock(Long branchId, Long productId, int quantity) {
        List<BranchProduct> branchProducts = branchProductRepository.getProductsByBranch(branchId);

        BranchProduct branchProduct = branchProducts.stream()
                .filter(bp -> bp.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (branchProduct == null) {
            return new ServiceResponse<>(false, 400,
                    "Product not available in this branch", null);
        }

        if (branchProduct.getQuantity() < quantity) {
            return new ServiceResponse<>(false, 400,
                    "Insufficient stock. Available: " + branchProduct.getQuantity(), null);
        }

        // Updating stock
        int newQuantity = branchProduct.getQuantity() - quantity;
        branchProductRepository.updateQuantity(branchId, productId, newQuantity);

        return new ServiceResponse<>(true, 200, "Stock updated successfully", null);
    }


    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    public ServiceResponse<Sale> getSale(Long id) {
        Sale sale = saleRepository.findById(id);
        if (sale == null) {
            return new ServiceResponse<>(false, 404, "Sale not found", null);
        }
        return new ServiceResponse<>(true, 200, "Sale retrieved successfully", sale);
    }

    public ServiceResponse<List<Sale>> getInvoicesByBranch(Long branchId) {
        // Validate branch exists
        Branch branch = branchRepository.findById(branchId);
        if (branch == null) {
            return new ServiceResponse<>(false, 404, "Branch not found", null);
        }

        List<Sale> sales = saleRepository.findByBranchId(branchId);
        return new ServiceResponse<>(true, 200, "Invoices retrieved successfully", sales);
    }

    public ServiceResponse<Sale> getInvoice(Long id) {
        Sale sale = saleRepository.findById(id);
        if (sale == null) {
            return new ServiceResponse<>(false, 404, "Invoice not found", null);
        }
        return new ServiceResponse<>(true, 200, "Invoice retrieved successfully", sale);
    }

    public ServiceResponse<Void> deleteInvoice(Long invoiceId) {
        // Check if invoice exists
        Sale existingSale = saleRepository.findById(invoiceId);
        if (existingSale == null) {
            return new ServiceResponse<>(false, 404, "Invoice not found", null);
        }

        // Restore stock quantities
        for (SaleItem item : existingSale.getItems()) {
            restoreStock(existingSale.getBranchId(), item.getProductId(), item.getQuantity());
        }

        saleRepository.deleteSale(invoiceId);
        return new ServiceResponse<>(true, 200, "Invoice deleted successfully", null);
    }

    public ServiceResponse<Sale> updateInvoice(Sale invoice) {
        // Validate invoice exists
        Sale existingSale = saleRepository.findById(invoice.getId());
        if (existingSale == null) {
            return new ServiceResponse<>(false, 404, "Invoice not found", null);
        }

        // Restore old quantities
        for (SaleItem item : existingSale.getItems()) {
            restoreStock(existingSale.getBranchId(), item.getProductId(), item.getQuantity());
        }


        for (SaleItem item : invoice.getItems()) {
            ServiceResponse<Void> stockResponse = validateAndUpdateStock(
                    invoice.getBranchId(),
                    item.getProductId(),
                    item.getQuantity()
            );
            if (!stockResponse.isSuccess()) {
                // Rollback restored quantities
                for (SaleItem oldItem : existingSale.getItems()) {
                    validateAndUpdateStock(
                            existingSale.getBranchId(),
                            oldItem.getProductId(),
                            oldItem.getQuantity()
                    );
                }
                return new ServiceResponse<>(false, 400, stockResponse.getMessage(), null);
            }
        }

        Sale updatedSale = saleRepository.updateSale(invoice);
        return new ServiceResponse<>(true, 200, "Invoice updated successfully", updatedSale);
    }


    //helper function for updateInvoice and deleteInvoice
    private void restoreStock(Long branchId, Long productId, int quantity) {
        List<BranchProduct> branchProducts = branchProductRepository.getProductsByBranch(branchId);
        BranchProduct branchProduct = branchProducts.stream()
                .filter(bp -> bp.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (branchProduct != null) {
            int newQuantity = branchProduct.getQuantity() + quantity;
            branchProductRepository.updateQuantity(branchId, productId, newQuantity);
        }
    }
}
