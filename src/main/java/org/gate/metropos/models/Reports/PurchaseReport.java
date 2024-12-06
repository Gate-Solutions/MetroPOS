package org.gate.metropos.models.Reports;

import lombok.*;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PurchaseReport {
    private Branch branch;
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPurchases;
    protected List<PurchaseInvoice> details;
}
