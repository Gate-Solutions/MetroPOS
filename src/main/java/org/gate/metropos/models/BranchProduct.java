package org.gate.metropos.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchProduct {
    private Long id;
    private Long branchId;
    private Long productId;
    private Integer quantity;
}