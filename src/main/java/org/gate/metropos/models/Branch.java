package org.gate.metropos.models;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Branch {
    private Long id;
    private String branchCode;
    private String name;
    private String city;
    private String address;
    private String phone;
    private boolean isActive;
    private int numberOfEmployees;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

