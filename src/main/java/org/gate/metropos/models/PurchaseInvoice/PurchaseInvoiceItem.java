package org.gate.metropos.models.PurchaseInvoice;

import lombok.*;
import org.gate.metropos.models.Product;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PurchaseInvoiceItem {
    private Long id;
    private Long invoiceId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal cartonPrice;
    private BigDecimal totalPrice;
//    Not in database
    private Product product;
}

