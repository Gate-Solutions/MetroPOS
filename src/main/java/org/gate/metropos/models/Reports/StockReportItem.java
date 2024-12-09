package org.gate.metropos.models.Reports;

import lombok.*;
import org.gate.metropos.models.Product;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StockReportItem {
    private Product product;
    private Integer quantity;
}