package org.gate.metropos.models;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    private Long id;
    private String name;
    private String code;

    private Category category;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private BigDecimal priceOfCarton;
    private boolean isActive;
}