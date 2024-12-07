package org.gate.metropos.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Category {
    private Long id;
    private String name;
}
