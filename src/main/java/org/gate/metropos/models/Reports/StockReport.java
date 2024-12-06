package org.gate.metropos.models.Reports;

import lombok.*;
import org.gate.metropos.models.Branch;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StockReport {
    private Branch branch;
    private List<StockReportItem> stockDetails;
}
