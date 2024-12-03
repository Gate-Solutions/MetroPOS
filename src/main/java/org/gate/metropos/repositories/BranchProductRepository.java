package org.gate.metropos.repositories;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.BranchProductFields;
import org.gate.metropos.models.BranchProduct;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

@AllArgsConstructor
public class BranchProductRepository {
    private final DSLContext dsl;

    public BranchProductRepository() {
        dsl = DatabaseConfig.getLocalDSL();
    }

    public BranchProduct addProductToBranch(Long branchId, Long productId, Integer quantity) {
        Record record = dsl.insertInto(BranchProductFields.BranchProductTable.toTableField())
                .set(BranchProductFields.BRANCH_ID.toField(), branchId)
                .set(BranchProductFields.PRODUCT_ID.toField(), productId)
                .set(BranchProductFields.QUANTITY.toField(), quantity)
                .returning()
                .fetchOne();

        return mapToBranchProduct(record);
    }

    public void updateQuantity(Long branchId, Long productId, Integer quantity) {
        dsl.update(BranchProductFields.BranchProductTable.toTableField())
                .set(BranchProductFields.QUANTITY.toField(), quantity)
                .where(BranchProductFields.BRANCH_ID.toField().eq(branchId))
                .and(BranchProductFields.PRODUCT_ID.toField().eq(productId))
                .execute();
    }

    public List<BranchProduct> getProductsByBranch(Long branchId) {
        Result<Record> records = dsl.select()
                .from(BranchProductFields.BranchProductTable.toTableField())
                .where(BranchProductFields.BRANCH_ID.toField().eq(branchId))
                .fetch();
        return records.map(this::mapToBranchProduct);
    }

    private BranchProduct mapToBranchProduct(Record record) {
        if (record == null) return null;
        return BranchProduct.builder()
                .id(record.get(BranchProductFields.ID.toField(), Long.class))
                .branchId(record.get(BranchProductFields.BRANCH_ID.toField(), Long.class))
                .productId(record.get(BranchProductFields.PRODUCT_ID.toField(), Long.class))
                .quantity(record.get(BranchProductFields.QUANTITY.toField(), Integer.class))
                .build();
    }
}
