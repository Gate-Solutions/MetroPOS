package org.gate.metropos.models;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SaleItem {
    private Long id;
    private Long saleId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Not in database
    private Product product;
}
