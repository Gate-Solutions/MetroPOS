package org.gate.metropos.models;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Sale {
    private Long id;
    private String invoiceNumber;
    private Long branchId;
    private Long createdBy;  // userId of the cashier/employee
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
    private BigDecimal discount;    
    private BigDecimal netAmount;   // Total after discount
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Not in database
    private Branch branch;
    private Employee creator;
    private List<SaleItem> items;
}
