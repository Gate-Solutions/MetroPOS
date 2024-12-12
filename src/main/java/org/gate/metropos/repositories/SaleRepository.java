package org.gate.metropos.repositories;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.PurchaseInvoice.PurchaseInvoiceFields;
import org.gate.metropos.enums.SaleFields;
import org.gate.metropos.enums.SaleItemFields;
import org.gate.metropos.models.BranchProduct;
import org.gate.metropos.models.Sale;
import org.gate.metropos.models.SaleItem;
import org.gate.metropos.utils.ServiceResponse;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
public class SaleRepository {
    private final DSLContext dsl;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchProductRepository branchProductRepository;

    public SaleRepository() {
        this.dsl = DatabaseConfig.getLocalDSL();
        this.productRepository = new ProductRepository();
        this.branchRepository = new BranchRepository();
        this.employeeRepository = new EmployeeRepository();
        this.branchProductRepository = new BranchProductRepository();
    }

    public Sale createSale(Sale sale) {

        dsl.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);
            Record saleRecord = ctx.insertInto(SaleFields.toTableField())
                    .set(SaleFields.INVOICE_NUMBER.toField(), sale.getInvoiceNumber())
                    .set(SaleFields.BRANCH_ID.toField(), sale.getBranchId())
                    .set(SaleFields.CREATED_BY.toField(), sale.getCreatedBy())
                    .set(SaleFields.INVOICE_DATE.toField(), sale.getInvoiceDate())
                    .set(SaleFields.TOTAL_AMOUNT.toField(), sale.getTotalAmount())
                    .set(SaleFields.DISCOUNT.toField(), sale.getDiscount())
                    .set(SaleFields.NET_AMOUNT.toField(), sale.getNetAmount())
                    .set(SaleFields.NOTES.toField(), sale.getNotes())
                    .returning(
                            SaleFields.ID.toField()
                    )
                    .fetchOne();

            if(saleRecord == null) {
                throw new Exception("Record not found");
            }

            Long invoiceId = saleRecord.get(SaleFields.ID.toField(), Long.class);
            sale.setId(invoiceId);

            // Insert sale items
            for (SaleItem item : sale.getItems()) {
                ctx.insertInto(SaleItemFields.toTableField())
                        .set(SaleItemFields.SALE_ID.toField(), invoiceId)
                        .set(SaleItemFields.PRODUCT_ID.toField(), item.getProductId())
                        .set(SaleItemFields.QUANTITY.toField(), item.getQuantity())
                        .set(SaleItemFields.UNIT_PRICE.toField(), item.getUnitPrice())
                        .set(SaleItemFields.TOTAL_PRICE.toField(), item.getTotalPrice())
                        .execute();
            }
            for (SaleItem item : sale.getItems()) {
                validateAndUpdateStock(
                        ctx,
                        sale.getBranchId(),
                        item.getProductId(),
                        item.getQuantity()
                );

            }
            return null;
        });
        return findById(sale.getId());
    }

    private void validateAndUpdateStock(DSLContext ctx, Long branchId, Long productId, int quantity) {
        BranchProduct branchProduct = branchProductRepository.getProductById(ctx, branchId, productId);

        if (branchProduct == null) {
            throw  new IllegalStateException("Product not available in this branch");
        }
        if (branchProduct.getQuantity() < quantity) {
            throw  new IllegalStateException("Insufficient stock. Available: " + branchProduct.getQuantity());
        }

        int newQuantity = branchProduct.getQuantity() - quantity;
        branchProductRepository.updateQuantity(ctx,branchId, productId, newQuantity);

    }

    public Sale findById(Long id) {
        Record record = dsl.select()
                .from(SaleFields.toTableField())
                .where(SaleFields.ID.toField().eq(id))
                .fetchOne();

        if (record == null) return null;

        Sale sale = mapToSale(record);
        sale.setItems(getSaleItems(id));
        return sale;
    }

    private List<SaleItem> getSaleItems(Long saleId) {
        Result<Record> records = dsl.select()
                .from(SaleItemFields.toTableField())
                .where(SaleItemFields.SALE_ID.toField().eq(saleId))
                .fetch();

        return records.map(this::mapToSaleItem);
    }

    private  Sale mapToSale(Record record) {
        if (record == null) return null;

        Long branchId = record.get(SaleFields.BRANCH_ID.toField(), Long.class);
        Long createdBy = record.get(SaleFields.CREATED_BY.toField(), Long.class);

        return Sale.builder()
                .id(record.get(SaleFields.ID.toField(), Long.class))
                .invoiceNumber(record.get(SaleFields.INVOICE_NUMBER.toField(), String.class))
                .branchId(branchId)
                .createdBy(createdBy)
                .invoiceDate(record.get(SaleFields.INVOICE_DATE.toField(), Date.class).toLocalDate())
                .totalAmount(record.get(SaleFields.TOTAL_AMOUNT.toField(), BigDecimal.class))
                .discount(record.get(SaleFields.DISCOUNT.toField(), BigDecimal.class))
                .netAmount(record.get(SaleFields.NET_AMOUNT.toField(), BigDecimal.class))
                .notes(record.get(SaleFields.NOTES.toField(), String.class))
                .createdAt(record.get(SaleFields.CREATED_AT.toField(), OffsetDateTime.class).toLocalDateTime())
                .updatedAt(record.get(SaleFields.UPDATED_AT.toField(), OffsetDateTime.class).toLocalDateTime())
                .branch(branchRepository.findById(branchId))
                .creator(employeeRepository.findById(createdBy))
                .build();
    }

    private SaleItem mapToSaleItem(Record record) {
        if (record == null) return null;

        Long productId = record.get(SaleItemFields.PRODUCT_ID.toField(), Long.class);

        return SaleItem.builder()
                .id(record.get(SaleItemFields.ID.toField(), Long.class))
                .saleId(record.get(SaleItemFields.SALE_ID.toField(), Long.class))
                .productId(productId)
                .quantity(record.get(SaleItemFields.QUANTITY.toField(), Integer.class))
                .unitPrice(record.get(SaleItemFields.UNIT_PRICE.toField(), BigDecimal.class))
                .totalPrice(record.get(SaleItemFields.TOTAL_PRICE.toField(), BigDecimal.class))
                .product(productRepository.findById(productId))
                .build();
    }

    public List<Sale> findByBranchId(Long branchId) {
        Result<Record> records = dsl.select()
                .from(SaleFields.toTableField())
                .where(SaleFields.BRANCH_ID.toField().eq(branchId))
                .fetch();
        return records.map(record -> {
            Sale sale = mapToSale(record);
            sale.setItems(getSaleItems(sale.getId()));
            return sale;
        });
    }

    public void deleteSale(Long id) {
        dsl.transaction(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            // First get the sale and items to restore stock
            Sale existingSale = findById(id);
            if (existingSale != null) {
                // Restore stock quantities
                for (SaleItem item : existingSale.getItems()) {
                    restoreStock(ctx, existingSale.getBranchId(), item.getProductId(), item.getQuantity());
                }

                // Delete sale items first (due to foreign key)
                ctx.deleteFrom(SaleItemFields.toTableField())
                        .where(SaleItemFields.SALE_ID.toField().eq(id))
                        .execute();

                // Delete the sale
                ctx.deleteFrom(SaleFields.toTableField())
                        .where(SaleFields.ID.toField().eq(id))
                        .execute();
            }
            throw new IllegalStateException("No Sale found by ID: " + id);
        });
    }

    public Sale updateSale(Sale sale) {
        dsl.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            // Get existing sale to handle stock differences
            Sale existingSale = findById(sale.getId());
            if (existingSale == null) {
                throw new IllegalStateException("Sale not found");
            }

            // First restore old quantities
            for (SaleItem oldItem : existingSale.getItems()) {
                restoreStock(ctx, existingSale.getBranchId(), oldItem.getProductId(), oldItem.getQuantity());
            }

            // Update main sale record
            ctx.update(SaleFields.toTableField())
                    .set(SaleFields.TOTAL_AMOUNT.toField(), sale.getTotalAmount())
                    .set(SaleFields.DISCOUNT.toField(), sale.getDiscount())
                    .set(SaleFields.NET_AMOUNT.toField(), sale.getNetAmount())
                    .set(SaleFields.NOTES.toField(), sale.getNotes())
                    .set(SaleFields.UPDATED_AT.toField(), LocalDateTime.now())
                    .where(SaleFields.ID.toField().eq(sale.getId()))
                    .execute();

            // Delete existing items
            ctx.deleteFrom(SaleItemFields.toTableField())
                    .where(SaleItemFields.SALE_ID.toField().eq(sale.getId()))
                    .execute();

            // Insert new items and update stock
            for (SaleItem item : sale.getItems()) {
                // Insert new item
                ctx.insertInto(SaleItemFields.toTableField())
                        .set(SaleItemFields.SALE_ID.toField(), sale.getId())
                        .set(SaleItemFields.PRODUCT_ID.toField(), item.getProductId())
                        .set(SaleItemFields.QUANTITY.toField(), item.getQuantity())
                        .set(SaleItemFields.UNIT_PRICE.toField(), item.getUnitPrice())
                        .set(SaleItemFields.TOTAL_PRICE.toField(), item.getTotalPrice())
                        .execute();

                // Validate and update new stock
                validateAndUpdateStock(ctx, sale.getBranchId(), item.getProductId(), item.getQuantity());
            }
            return null;
        });

        return findById(sale.getId());
    }

    private void restoreStock(DSLContext ctx, Long branchId, Long productId, int quantity) {
        BranchProduct branchProduct = branchProductRepository.getProductById(ctx, branchId, productId);

        if (branchProduct != null) {
            int newQuantity = branchProduct.getQuantity() + quantity;
            branchProductRepository.updateQuantity(ctx, branchId, productId, newQuantity);
        }
    }


}
