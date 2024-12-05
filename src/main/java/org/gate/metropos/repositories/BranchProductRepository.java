package org.gate.metropos.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.BranchProductFields;
import org.gate.metropos.models.BranchProduct;
import org.gate.metropos.services.SyncService;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class BranchProductRepository {
    private final DSLContext dsl;
    private final SyncService syncService;

    public BranchProductRepository() {
        dsl = DatabaseConfig.getLocalDSL();
        syncService = new SyncService();
    }

    public BranchProduct addProductToBranch(Long branchId, Long productId, Integer quantity) {
        return addProductToBranch(dsl, branchId, productId, quantity);
    }

    public BranchProduct addProductToBranch(DSLContext ctx, Long branchId, Long productId, Integer quantity) {
        Record record = ctx.insertInto(BranchProductFields.toTableField())
                .set(BranchProductFields.BRANCH_ID.toField(), branchId)
                .set(BranchProductFields.PRODUCT_ID.toField(), productId)
                .set(BranchProductFields.QUANTITY.toField(), quantity)
                .returning()
                .fetchOne();

        if(record == null) return null;
        int id = record.get(BranchProductFields.BRANCH_ID.toField(), Integer.class);

        try {
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put("branch_id", branchId);
            fieldValues.put("product_id", productId);
            fieldValues.put("quantity", quantity);

            syncService.trackChange(
                    ctx,
                    "branch_products",
                    id,
                    "insert",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }

        return mapToBranchProduct(record);
    }

    public void updateQuantity(Long branchId, Long productId, Integer quantity) {
        updateQuantity(dsl, branchId, productId, quantity);
    }

    public void updateQuantity(DSLContext ctx, Long branchId, Long productId, Integer quantity) {
        ctx.update(BranchProductFields.toTableField())
                .set(BranchProductFields.QUANTITY.toField(), quantity)
                .where(BranchProductFields.BRANCH_ID.toField().eq(branchId))
                .and(BranchProductFields.PRODUCT_ID.toField().eq(productId))
                .execute();

        try {
            BranchProduct branchProduct = getProductById(ctx, branchId, productId);
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put("quantity", quantity);

            syncService.trackChange(
                    ctx,
                    "branch_products",
                    branchProduct.getId().intValue(),
                    "update",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }
    }

    public List<BranchProduct> getProductsByBranch(Long branchId) {
        Result<Record> records = dsl.select()
                .from(BranchProductFields.toTableField())
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

    public BranchProduct getProductById(DSLContext ctx, Long branchId, Long productId) {
        Record record = ctx.select()
                .from(BranchProductFields.toTableField())
                .where(BranchProductFields.BRANCH_ID.toField().eq(branchId))
                .and(BranchProductFields.PRODUCT_ID.toField().eq(productId))
                .fetchOne();

        return mapToBranchProduct(record);
    }

    public BranchProduct getProductById(Long branchId, Long productId) {
        return getProductById(dsl, branchId, productId);
    }
}
