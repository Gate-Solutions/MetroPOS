package org.gate.metropos.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@ToString
@Data
public class Employee extends User {
    private String name;
    private String employeeNo;
    private boolean isActive;
    private boolean isFirstTime;
    private BigDecimal salary;
    private Long branchId;
}
