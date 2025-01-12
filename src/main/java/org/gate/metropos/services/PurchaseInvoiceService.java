package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoice;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoiceItem;
import org.gate.metropos.repositories.PurchaseInvoiceRepository;
import org.gate.metropos.utils.ServiceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
public class PurchaseInvoiceService {
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;

    public PurchaseInvoiceService() {
        this.purchaseInvoiceRepository = new PurchaseInvoiceRepository();
    }

    public ServiceResponse<PurchaseInvoice> createInvoice(PurchaseInvoice invoice) {
        try {

            if (isInvoiceDataValid(invoice)) {
                return new ServiceResponse<>(false, 400, "Invalid invoice data", null);
            }

            if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
                invoice.setInvoiceNumber(generateInvoiceNumber());
            }

            Long invoiceId = purchaseInvoiceRepository.createInvoice(invoice);
            PurchaseInvoice newInvoice = purchaseInvoiceRepository.findById(invoiceId);
            return new ServiceResponse<>(true, 200, "Invoice created successfully", newInvoice);

        } catch (IllegalStateException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        } catch (Exception e) {
            return new ServiceResponse<>(false, 500, "Error creating invoice: " + e.getMessage(), null);
        }
    }

    public ServiceResponse<PurchaseInvoice> updateInvoice(PurchaseInvoice invoice) {
        try {

            if (purchaseInvoiceRepository.findById(invoice.getId()) == null) {
                return new ServiceResponse<>(false, 404, "Invoice not found", null);
            }

            if (purchaseInvoiceRepository.isInvoiceNumberExists(invoice.getInvoiceNumber(), invoice.getId())) {
                return new ServiceResponse<>(false, 400, "Invoice number already exists", null);
            }

            if (isInvoiceDataValid(invoice)) {
                return new ServiceResponse<>(false, 400, "Invalid invoice data", null);
            }

//            if(invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
//                invoice.setInvoiceNumber(generateInvoiceNumber());
//            }
            PurchaseInvoice updatedInvoice = purchaseInvoiceRepository.updateInvoice(invoice);
            return new ServiceResponse<>(true, 200, "Invoice updated successfully", updatedInvoice);

        } catch (IllegalStateException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        } catch (Exception e) {
//            e.printStackTrace();
            return new ServiceResponse<>(false, 500, "Error updating invoice: " + e.getMessage(), null);
        }
    }

    public ServiceResponse<Boolean> deleteInvoice(Long invoiceId) {
        try {
            boolean deleted = purchaseInvoiceRepository.deleteInvoice(invoiceId);
            if (!deleted) {
                return new ServiceResponse<>(false, 404, "Invoice not found", false);
            }
            return new ServiceResponse<>(true, 200, "Invoice deleted successfully", true);
        } catch (IllegalStateException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), false);
        } catch (Exception e) {
            return new ServiceResponse<>(false, 500, "Error deleting invoice: " + e.getMessage(), false);
        }
    }

    public ServiceResponse<PurchaseInvoice> getInvoice(Long id) {
        try {
            PurchaseInvoice invoice = purchaseInvoiceRepository.findById(id);
            if (invoice == null) {
                return new ServiceResponse<>(false, 404, "Invoice not found", null);
            }
            return new ServiceResponse<>(true, 200, "Invoice retrieved successfully", invoice);
        } catch (Exception e) {
            return new ServiceResponse<>(false, 500, "Error retrieving invoice", null);
        }
    }

    public ServiceResponse<List<PurchaseInvoice>> getInvoicesByBranch(Long branchId) {
        try {
            List<PurchaseInvoice> invoices = purchaseInvoiceRepository.getInvoicesByBranch(branchId);
            return new ServiceResponse<>(true, 200, "Invoices retrieved successfully", invoices);
        } catch (Exception e) {
            return new ServiceResponse<>(false, 500, "Error retrieving invoices", null);
        }
    }

    private boolean isInvoiceDataValid(PurchaseInvoice invoice) {
        if (invoice.getSupplierId() == null || invoice.getBranchId() == null ||
                invoice.getCreatedBy() == null || invoice.getInvoiceDate() == null) {
            return true;
        }

        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            return true;
        }

        for (PurchaseInvoiceItem item : invoice.getItems()) {
            if (item.getProductId() == null || item.getQuantity() == null ||
                    item.getUnitPrice() == null || item.getTotalPrice() == null) {
                return true;
            }
            if (item.getQuantity() <= 0 || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return true;
            }
        }

        return false;
    }

    private String generateInvoiceNumber() {
        // Format: INV-YYYYMMDD-XXXX
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", (int) (Math.random() * 10000));
        return "INV-" + datePart + "-" + randomPart;
    }

}
