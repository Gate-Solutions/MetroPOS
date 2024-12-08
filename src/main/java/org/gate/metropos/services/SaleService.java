package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import org.gate.metropos.enums.UserRole;
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

        User user = employeeRepository.findById(sale.getCreatedBy());
        if (user == null) {
            return new ServiceResponse<>(false, 400, "Invalid user ID: User does not exist", null);
        }

        if (user.getRole()!=UserRole.CASHIER) {

            return new ServiceResponse<>(false, 400, "Invalid user role: Cashier", null);
        }


        if (sale.getBranchId() == null || sale.getCreatedBy() == null) {
            return new ServiceResponse<>(false, 400, "Branch ID and Created By are required", null);
        }

        // Validating branch actually exists
        Branch branch = branchRepository.findById(sale.getBranchId());
        if (branch == null) {
            return new ServiceResponse<>(false, 400, "Invalid branch ID: Branch does not exist", null);
        }
        

        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            return new ServiceResponse<>(false, 400, "Sale items cannot be empty", null);
        }

        sale.setInvoiceNumber(generateInvoiceNumber());

        if (sale.getInvoiceDate() == null) {
            sale.setInvoiceDate(LocalDate.now());
        }

        Sale newSale = saleRepository.createSale(sale);
        return new ServiceResponse<>(true, 200, "Sale created successfully", newSale);
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







    public ServiceResponse<Sale> updateInvoice(Sale sale) {
        // 1. Validate sale exists
        Sale existingSale = saleRepository.findById(sale.getId());
        if (existingSale == null) {
            return new ServiceResponse<>(false, 404, "Sale not found", null);
        }

        // 2. Validate user permissions
        User user = employeeRepository.findById(sale.getCreatedBy());
        if (user == null) {
            return new ServiceResponse<>(false, 400, "Invalid user ID", null);
        }
        if (user.getRole() != UserRole.CASHIER) {
            return new ServiceResponse<>(false, 403, "Only cashiers can update sales", null);
        }

        // 3. Validate branch
        Branch branch = branchRepository.findById(sale.getBranchId());
        if (branch == null) {
            return new ServiceResponse<>(false, 400, "Invalid branch ID", null);
        }

        // 4. Validate items
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            return new ServiceResponse<>(false, 400, "Sale items cannot be empty", null);
        }

        try {
            Sale updatedSale = saleRepository.updateSale(sale);
            return new ServiceResponse<>(true, 200, "Sale updated successfully", updatedSale);
        } catch (IllegalStateException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        }
    }

    public ServiceResponse<Void> deleteInvoice(Long invoiceId) {
        // 1. Validate sale exists
        Sale existingSale = saleRepository.findById(invoiceId);
        if (existingSale == null) {
            return new ServiceResponse<>(false, 404, "Sale not found", null);
        }

        try {
            saleRepository.deleteSale(invoiceId);
            return new ServiceResponse<>(true, 200, "Sale deleted successfully", null);
        } catch (Exception e) {
            return new ServiceResponse<>(false, 500, "Error deleting sale: " + e.getMessage(), null);
        }
    }

}
