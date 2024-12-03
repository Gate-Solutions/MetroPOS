package org.gate.metropos.repositories.Reports;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.BranchProductFields;
import org.gate.metropos.enums.ProductFields;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Reports.ReportCriteria;
import org.gate.metropos.models.Reports.StockReport;
import org.gate.metropos.models.Reports.StockReportItem;
import org.gate.metropos.repositories.BranchProductRepository;
import org.gate.metropos.repositories.BranchRepository;
import org.gate.metropos.repositories.ProductRepository;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.util.List;

@AllArgsConstructor
public class StockReportRepository {
    private DSLContext dsl;
    private BranchRepository branchRepository;
    private ProductRepository productRepository;

    public StockReportRepository() {
        dsl = DatabaseConfig.getLocalDSL();
        branchRepository = new BranchRepository();
        productRepository = new ProductRepository();
    }

    public StockReport getStockReport(ReportCriteria criteria) {
        SelectConditionStep<Record> query = dsl
                .select()
                .from(BranchProductFields.toTableField())
                .leftJoin(ProductFields.toTableField())
                .on(BranchProductFields.PRODUCT_ID.toField()
                        .eq(ProductFields.toTableField().field(ProductFields.ID.toField())))
                .where(DSL.noCondition());

        if (criteria.getBranchId() != null) {
            query.and(BranchProductFields.BRANCH_ID.toField().eq(criteria.getBranchId()));
        }

        Result<Record> results = query.fetch();

        List<StockReportItem> stockItems = results.map(record -> {
            Long productId = record.get(BranchProductFields.PRODUCT_ID.toField(), Long.class);
            Integer quantity = record.get(BranchProductFields.QUANTITY.toField(), Integer.class);

            return StockReportItem.builder()
                    .product(productRepository.findById(productId))
                    .quantity(quantity != null ? quantity : 0)
                    .build();
        });

        Branch branch = null;
        if (criteria.getBranchId() != null) {
            branch = branchRepository.findById(criteria.getBranchId());
        }

        return StockReport.builder()
                .branch(branch)
                .stockDetails(stockItems)
                .build();
    }
}
