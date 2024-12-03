package org.gate.metropos.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String code;
    private Long categoryId;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private BigDecimal priceOfCarton;
    private boolean isActive;
}