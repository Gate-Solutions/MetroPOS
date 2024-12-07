package org.gate.metropos.models.PurchaseInvoice;

import lombok.*;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.Supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PurchaseInvoice {
    private Long id;
    private String invoiceNumber;
    private Long supplierId;
    private Long branchId;
    private Long createdBy;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

//    Not in database
    private Supplier supplier;
    private Branch branch;
    private Employee creator;
    private List<PurchaseInvoiceItem> items;
}
