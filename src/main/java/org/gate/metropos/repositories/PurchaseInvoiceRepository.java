package org.gate.metropos.repositories;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.BranchProductFields;
import org.gate.metropos.enums.PurchaseInvoice.PurchaseInvoiceFields;
import org.gate.metropos.enums.PurchaseInvoice.PurchaseInvoiceItemFields;
import org.gate.metropos.enums.UserFields;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoice;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoiceItem;
import org.gate.metropos.services.SyncService;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PurchaseInvoiceRepository {
    private final DSLContext dsl;
    private final SupplierRepository supplierRepository;
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final BranchProductRepository branchProductRepository;
    private final SyncService syncService;
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public PurchaseInvoiceRepository() {
        this.dsl = DatabaseConfig.getLocalDSL();
        this.supplierRepository = new SupplierRepository();
        this.branchRepository = new BranchRepository();
        this.employeeRepository = new EmployeeRepository();
        this.productRepository = new ProductRepository();
        this.branchProductRepository = new BranchProductRepository();
        syncService = new SyncService();
    }

    public PurchaseInvoice findById(Long id) {
        Record record = dsl.select()
                .from(PurchaseInvoiceFields.toTableField())
                .where(PurchaseInvoiceFields.ID.toField().eq(id))
                .fetchOne();
        return mapToPurchaseInvoice(record, true);
    }

    public List<PurchaseInvoice> getInvoicesByBranch(Long branchId) {
        Result<Record> records = dsl.select()
                .from(PurchaseInvoiceFields.toTableField())
                .where(PurchaseInvoiceFields.BRANCH_ID.toField().eq(branchId))
                .orderBy(PurchaseInvoiceFields.CREATED_AT.toField().desc())
                .fetch();

        return records.map(e-> mapToPurchaseInvoice(e, true));
    }

    public List<PurchaseInvoiceItem> getInvoiceItems(Long invoiceId) {
        Result<Record> records = dsl.select()
                .from(PurchaseInvoiceItemFields.toTableField())
                .where(PurchaseInvoiceItemFields.INVOICE_ID.toField().eq(invoiceId))
                .fetch();
        return records.map(this::mapToInvoiceItem);
    }

    public Long createInvoice(PurchaseInvoice invoice) {

        return dsl.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);
            // Insert main invoice
            Record record = ctx.insertInto(PurchaseInvoiceFields.toTableField())
                    .set(PurchaseInvoiceFields.INVOICE_NUMBER.toField(), invoice.getInvoiceNumber())
                    .set(PurchaseInvoiceFields.SUPPLIER_ID.toField(), invoice.getSupplierId())
                    .set(PurchaseInvoiceFields.BRANCH_ID.toField(), invoice.getBranchId())
                    .set(PurchaseInvoiceFields.CREATED_BY.toField(), invoice.getCreatedBy())
                    .set(PurchaseInvoiceFields.INVOICE_DATE.toField(), invoice.getInvoiceDate())
                    .set(PurchaseInvoiceFields.TOTAL_AMOUNT.toField(), invoice.getTotalAmount())
                    .set(PurchaseInvoiceFields.NOTES.toField(), invoice.getNotes())
                    .returning(
                            PurchaseInvoiceFields.ID.toField()
                    )
                    .fetchOne();


            if(record == null) {
                throw new Exception("Record not found");
            }

            Long invoiceId = record.get(PurchaseInvoiceFields.ID.toField(), Long.class);

            try {
                Map<String, Object> fieldValues = new HashMap<>();
                fieldValues.put("invoice_number", invoice.getInvoiceNumber());
                fieldValues.put("supplier_id", invoice.getSupplierId());
                fieldValues.put("branch_id", invoice.getBranchId());
                fieldValues.put("created_by", invoice.getCreatedBy());
                fieldValues.put("invoice_date", invoice.getInvoiceDate());
                fieldValues.put("total_amount", invoice.getTotalAmount());
                fieldValues.put("notes", invoice.getNotes());

                syncService.trackChange(
                        ctx,
                        "purchase_invoices",
                        invoiceId.intValue(),
                        "insert",
                        objectMapper.writeValueAsString(fieldValues)
                );

                // Track invoice items
                for (PurchaseInvoiceItem item : invoice.getItems()) {
                    insertInvoiceItem(ctx, invoiceId, item);
                }
            } catch (JsonProcessingException e) {
                System.out.println(e);
            }

            for (PurchaseInvoiceItem item : invoice.getItems()) {
                insertInvoiceItem(ctx, invoiceId, item);
                updateBranchProductQuantity(ctx, invoice.getBranchId(), item.getProductId(), item.getQuantity());
            }

            return invoiceId;
        });
    }

    private void insertInvoiceItem(DSLContext ctx, Long invoiceId, PurchaseInvoiceItem item) {
        Record record = ctx.insertInto(PurchaseInvoiceItemFields.toTableField())
                .set(PurchaseInvoiceItemFields.INVOICE_ID.toField(), invoiceId)
                .set(PurchaseInvoiceItemFields.PRODUCT_ID.toField(), item.getProductId())
                .set(PurchaseInvoiceItemFields.QUANTITY.toField(), item.getQuantity())
                .set(PurchaseInvoiceItemFields.UNIT_PRICE.toField(), item.getUnitPrice())
                .set(PurchaseInvoiceItemFields.CARTON_PRICE.toField(), item.getCartonPrice())
                .set(PurchaseInvoiceItemFields.TOTAL_PRICE.toField(), item.getTotalPrice())
                .returning(PurchaseInvoiceItemFields.ID.toField())
                .fetchOne();

        if (record != null) {
            try {
                Map<String, Object> fieldValues = new HashMap<>();
                fieldValues.put("invoice_id", invoiceId);
                fieldValues.put("product_id", item.getProductId());
                fieldValues.put("quantity", item.getQuantity());
                fieldValues.put("unit_price", item.getUnitPrice());
                fieldValues.put("carton_price", item.getCartonPrice());
                fieldValues.put("total_price", item.getTotalPrice());

                syncService.trackChange(
                        "purchase_invoice_items",
                        record.get(PurchaseInvoiceItemFields.ID.toField(), Integer.class),
                        "insert",
                        objectMapper.writeValueAsString(fieldValues)
                );
            } catch (JsonProcessingException e) {
                System.out.println(e);
            }
        }
    }

    private void updateBranchProductQuantity(DSLContext ctx, Long branchId, Long productId, Integer quantityChange) {

        Field<Object> qtyField = BranchProductFields.QUANTITY.toField();
        Field<Object> branchIdField = BranchProductFields.BRANCH_ID.toField();
        Field<Object> productIdField = BranchProductFields.PRODUCT_ID.toField();
        Table<?> tableField = BranchProductFields.toTableField();

        Record record = ctx.select(qtyField)
                .from(tableField)
                .where(branchIdField.eq(branchId))
                .and(productIdField.eq(productId))
                .fetchOne();

        if (record == null) {
            if (quantityChange < 0) {
                throw new IllegalStateException("Quantity less than zero for product Id: " + productId);
            }

            branchProductRepository.addProductToBranch(ctx, branchId, productId, quantityChange);
        } else {
            // Update existing quantity
            int currentQuantity = record.get(BranchProductFields.QUANTITY.getColumnName(), Integer.class);
            int newQuantity = currentQuantity + quantityChange;
            if (newQuantity < 0) {
                throw new IllegalStateException("Insufficient stock for product ID: " + productId);
            }

            branchProductRepository.updateQuantity(ctx, branchId, productId, newQuantity);
        }
    }

    private PurchaseInvoice mapToPurchaseInvoice(Record record, Boolean addRelatedObjects) {
        if (record == null) return null;

        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .id(record.get(PurchaseInvoiceFields.ID.toField(), Long.class))
                .invoiceNumber(record.get(PurchaseInvoiceFields.INVOICE_NUMBER.toField(), String.class))
                .supplierId(record.get(PurchaseInvoiceFields.SUPPLIER_ID.toField(), Long.class))
                .branchId(record.get(PurchaseInvoiceFields.BRANCH_ID.toField(), Long.class))
                .createdBy(record.get(PurchaseInvoiceFields.CREATED_BY.toField(), Long.class))
                .invoiceDate(record.get(PurchaseInvoiceFields.INVOICE_DATE.toField(), Date.class).toLocalDate())
                .totalAmount(record.get(PurchaseInvoiceFields.TOTAL_AMOUNT.toField(), BigDecimal.class))
                .notes(record.get(PurchaseInvoiceFields.NOTES.toField(), String.class))
                .createdAt(record.get(PurchaseInvoiceFields.CREATED_AT.toField(), OffsetDateTime.class).toLocalDateTime())
                .updatedAt(record.get(PurchaseInvoiceFields.UPDATED_AT.toField(), OffsetDateTime.class).toLocalDateTime())
                .build();

        if(addRelatedObjects) {
            invoice.setSupplier(supplierRepository.findById(invoice.getSupplierId()));
            invoice.setBranch(branchRepository.findById(invoice.getBranchId()));
            invoice.setCreator(employeeRepository.findById(invoice.getCreatedBy()));
        }

        invoice.setItems(getInvoiceItems(invoice.getId()));
        return invoice;
    }

    private PurchaseInvoiceItem mapToInvoiceItem(Record record) {
        if (record == null) return null;

        PurchaseInvoiceItem item = PurchaseInvoiceItem.builder()
                .id(record.get(PurchaseInvoiceItemFields.ID.toField(), Long.class))
                .invoiceId(record.get(PurchaseInvoiceItemFields.INVOICE_ID.toField(), Long.class))
                .productId(record.get(PurchaseInvoiceItemFields.PRODUCT_ID.toField(), Long.class))
                .quantity(record.get(PurchaseInvoiceItemFields.QUANTITY.toField(), Integer.class))
                .unitPrice(record.get(PurchaseInvoiceItemFields.UNIT_PRICE.toField(), BigDecimal.class))
                .cartonPrice(record.get(PurchaseInvoiceItemFields.CARTON_PRICE.toField(), BigDecimal.class))
                .totalPrice(record.get(PurchaseInvoiceItemFields.TOTAL_PRICE.toField(), BigDecimal.class))
                .build();

        item.setProduct(productRepository.findById(item.getProductId()));

        return item;
    }

    public boolean deleteInvoice(Long invoiceId) {
        return dsl.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            PurchaseInvoice invoice = findById(invoiceId);
            if (invoice == null) {
                return false;
            }

            for (PurchaseInvoiceItem item : invoice.getItems()) {
                this.updateBranchProductQuantity(ctx, invoice.getBranchId(),
                        item.getProductId(), -item.getQuantity());
            }
            List<PurchaseInvoiceItem> items = getInvoiceItems(invoiceId);
            for (PurchaseInvoiceItem item : items) {
                try {
                    syncService.trackChange(
                            ctx,
                            "purchase_invoice_items",
                            item.getId().intValue(),
                            "delete",
                            objectMapper.writeValueAsString(new HashMap<>())
                    );
                } catch (JsonProcessingException e) {
                    System.out.println(e);
                }
            }

            ctx.deleteFrom(PurchaseInvoiceItemFields.toTableField())
                    .where(PurchaseInvoiceItemFields.INVOICE_ID.toField().eq(invoiceId))
                    .execute();

            int deleted = ctx.deleteFrom(PurchaseInvoiceFields.toTableField())
                    .where(PurchaseInvoiceFields.ID.toField().eq(invoiceId))
                    .execute();

            try {
                syncService.trackChange(
                        ctx,
                        "purchase_invoices",
                        invoiceId.intValue(),
                        "delete",
                        objectMapper.writeValueAsString(new HashMap<>())
                );
            } catch (JsonProcessingException e) {
                System.out.println(e);
            }

            return deleted > 0;
        });
    }

    public PurchaseInvoice updateInvoice(PurchaseInvoice invoice) {
        return dsl.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            List<PurchaseInvoiceItem> existingItems = getInvoiceItems(invoice.getId());

            Record record = ctx.update(PurchaseInvoiceFields.toTableField())
                    .set(PurchaseInvoiceFields.SUPPLIER_ID.toField(), invoice.getSupplierId())
                    .set(PurchaseInvoiceFields.TOTAL_AMOUNT.toField(), invoice.getTotalAmount())
                    .set(PurchaseInvoiceFields.NOTES.toField(), invoice.getNotes())
                    .set(PurchaseInvoiceFields.UPDATED_AT.toField(), LocalDateTime.now())
                    .where(PurchaseInvoiceFields.ID.toField().eq(invoice.getId()))
                    .returning()
                    .fetchOne();

            if (record == null) {
                throw new IllegalStateException("Invoice not found for update");
            }

            try {
                Map<String, Object> fieldValues = new HashMap<>();
                fieldValues.put("supplier_id", invoice.getSupplierId());
                fieldValues.put("total_amount", invoice.getTotalAmount());
                fieldValues.put("notes", invoice.getNotes());

                syncService.trackChange(
                        ctx,
                        "purchase_invoices",
                        invoice.getId().intValue(),
                        "update",
                        objectMapper.writeValueAsString(fieldValues)
                );
            } catch (JsonProcessingException e) {
                System.out.println(e);
            }
            List<PurchaseInvoiceItem> itemsToDelete = getInvoiceItems(invoice.getId());

            for (PurchaseInvoiceItem item : itemsToDelete) {
                try {
                    syncService.trackChange(
                            ctx,
                            "purchase_invoice_items",
                            item.getId().intValue(),
                            "delete",
                            objectMapper.writeValueAsString(new HashMap<>())
                    );
                } catch (JsonProcessingException e) {
                    System.out.println(e);
                }
            }
            ctx.deleteFrom(PurchaseInvoiceItemFields.toTableField())
                    .where(PurchaseInvoiceItemFields.INVOICE_ID.toField().eq(invoice.getId()))
                    .execute();

            for (PurchaseInvoiceItem newItem : invoice.getItems()) {
                PurchaseInvoiceItem existingItem = existingItems.stream()
                        .filter(e -> e.getProductId().equals(newItem.getProductId()))
                        .findFirst()
                        .orElse(null);

                int quantityDiff = newItem.getQuantity() -
                        (existingItem != null ? existingItem.getQuantity() : 0);

                insertInvoiceItem(ctx, invoice.getId(), newItem);

                if (quantityDiff != 0) {
                    updateBranchProductQuantity(ctx, invoice.getBranchId(),
                            newItem.getProductId(), quantityDiff);
                }
            }

            return mapToPurchaseInvoice(record, true);
        });
    }

    public boolean isInvoiceNumberExists(String invoiceNumber, Long excludeId) {
        return dsl.fetchCount(
                dsl.selectFrom(PurchaseInvoiceFields.toTableField())
                        .where(PurchaseInvoiceFields.INVOICE_NUMBER.toField().eq(invoiceNumber))
                        .and(PurchaseInvoiceFields.ID.toField().ne(excludeId))
        ) > 0;
    }

}
