package org.gate.metropos.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BranchProduct {
    private Long id;
    private Long branchId;
    private Long productId;
    private Integer quantity;
}