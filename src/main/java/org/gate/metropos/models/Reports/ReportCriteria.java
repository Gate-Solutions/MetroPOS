package org.gate.metropos.models.Reports;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReportCriteria {
    private Long branchId;
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
}