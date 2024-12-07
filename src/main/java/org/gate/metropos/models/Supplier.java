package org.gate.metropos.models;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Supplier {
    private Long id;
    private String name;
    private String companyName;
    private String email;
    private String phone;
    private String ntnNumber;
    private boolean isActive;
}
