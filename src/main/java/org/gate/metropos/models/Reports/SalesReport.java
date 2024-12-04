package org.gate.metropos.models.Reports;

import lombok.*;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoice;
import org.gate.metropos.models.Sale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalesReport {
    private Branch branch;
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDiscounts;
    private BigDecimal netAmount;
    private BigDecimal totalSales;
    private List<Sale> details;
    private List<PurchaseInvoice> purchaseInvoices;
}
