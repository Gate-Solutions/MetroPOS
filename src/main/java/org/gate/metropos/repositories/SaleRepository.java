package org.gate.metropos.repositories;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.SaleFields;
import org.gate.metropos.enums.SaleItemFields;
import org.gate.metropos.models.Sale;
import org.gate.metropos.models.SaleItem;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
public class SaleRepository {
    private final DSLContext dsl;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;

    public SaleRepository() {
        this.dsl = DatabaseConfig.getLocalDSL();
        this.productRepository = new ProductRepository();
        this.branchRepository = new BranchRepository();
        this.employeeRepository = new EmployeeRepository();
    }

    public Sale createSale(Sale sale) {

        Record saleRecord = dsl.insertInto(SaleFields.SaleTable.toTableField())
                .set(SaleFields.INVOICE_NUMBER.toField(), sale.getInvoiceNumber())
                .set(SaleFields.BRANCH_ID.toField(), sale.getBranchId())
                .set(SaleFields.CREATED_BY.toField(), sale.getCreatedBy())
                .set(SaleFields.INVOICE_DATE.toField(), sale.getInvoiceDate())
                .set(SaleFields.TOTAL_AMOUNT.toField(), sale.getTotalAmount())
                .set(SaleFields.DISCOUNT.toField(), sale.getDiscount())
                .set(SaleFields.NET_AMOUNT.toField(), sale.getNetAmount())
                .set(SaleFields.NOTES.toField(), sale.getNotes())
                .returning()
                .fetchOne();

        Sale newSale = mapToSale(saleRecord);

        // Insert sale items
        for (SaleItem item : sale.getItems()) {
            dsl.insertInto(SaleItemFields.SaleItemTable.toTableField())
                    .set(SaleItemFields.SALE_ID.toField(), newSale.getId())
                    .set(SaleItemFields.PRODUCT_ID.toField(), item.getProductId())
                    .set(SaleItemFields.QUANTITY.toField(), item.getQuantity())
                    .set(SaleItemFields.UNIT_PRICE.toField(), item.getUnitPrice())
                    .set(SaleItemFields.TOTAL_PRICE.toField(), item.getTotalPrice())
                    .execute();
        }

        return findById(newSale.getId());
    }

    public Sale findById(Long id) {
        Record record = dsl.select()
                .from(SaleFields.SaleTable.toTableField())
                .where(SaleFields.ID.toField().eq(id))
                .fetchOne();

        if (record == null) return null;

        Sale sale = mapToSale(record);
        sale.setItems(getSaleItems(id));
        return sale;
    }

    private List<SaleItem> getSaleItems(Long saleId) {
        Result<Record> records = dsl.select()
                .from(SaleItemFields.SaleItemTable.toTableField())
                .where(SaleItemFields.SALE_ID.toField().eq(saleId))
                .fetch();

        return records.map(this::mapToSaleItem);
    }

    private Sale mapToSale(Record record) {
        if (record == null) return null;

        Long branchId = record.get(SaleFields.BRANCH_ID.toField(), Long.class);
        Long createdBy = record.get(SaleFields.CREATED_BY.toField(), Long.class);

        return Sale.builder()
                .id(record.get(SaleFields.ID.toField(), Long.class))
                .invoiceNumber(record.get(SaleFields.INVOICE_NUMBER.toField(), String.class))
                .branchId(branchId)
                .createdBy(createdBy)
                .invoiceDate(record.get(SaleFields.INVOICE_DATE.toField(), LocalDate.class))
                .totalAmount(record.get(SaleFields.TOTAL_AMOUNT.toField(), BigDecimal.class))
                .discount(record.get(SaleFields.DISCOUNT.toField(), BigDecimal.class))
                .netAmount(record.get(SaleFields.NET_AMOUNT.toField(), BigDecimal.class))
                .notes(record.get(SaleFields.NOTES.toField(), String.class))
                .createdAt(record.get(SaleFields.CREATED_AT.toField(), LocalDateTime.class))
                .updatedAt(record.get(SaleFields.UPDATED_AT.toField(), LocalDateTime.class))
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
                .from(SaleFields.SaleTable.toTableField())
                .where(SaleFields.BRANCH_ID.toField().eq(branchId))
                .fetch();
        return records.map(record -> {
            Sale sale = mapToSale(record);
            sale.setItems(getSaleItems(sale.getId()));
            return sale;
        });
    }

    public void deleteSale(Long id) {

        dsl.deleteFrom(SaleItemFields.SaleItemTable.toTableField())
                .where(SaleItemFields.SALE_ID.toField().eq(id))
                .execute();

        dsl.deleteFrom(SaleFields.SaleTable.toTableField())
                .where(SaleFields.ID.toField().eq(id))
                .execute();
    }

    public Sale updateSale(Sale sale) {

        dsl.update(SaleFields.SaleTable.toTableField())
                .set(SaleFields.TOTAL_AMOUNT.toField(), sale.getTotalAmount())
                .set(SaleFields.DISCOUNT.toField(), sale.getDiscount())
                .set(SaleFields.NET_AMOUNT.toField(), sale.getNetAmount())
                .set(SaleFields.NOTES.toField(), sale.getNotes())
                .set(SaleFields.UPDATED_AT.toField(), LocalDateTime.now())
                .where(SaleFields.ID.toField().eq(sale.getId()))
                .execute();

        // Delete existing items
        dsl.deleteFrom(SaleItemFields.SaleItemTable.toTableField())
                .where(SaleItemFields.SALE_ID.toField().eq(sale.getId()))
                .execute();

        // Insert new items
        for (SaleItem item : sale.getItems()) {
            dsl.insertInto(SaleItemFields.SaleItemTable.toTableField())
                    .set(SaleItemFields.SALE_ID.toField(), sale.getId())
                    .set(SaleItemFields.PRODUCT_ID.toField(), item.getProductId())
                    .set(SaleItemFields.QUANTITY.toField(), item.getQuantity())
                    .set(SaleItemFields.UNIT_PRICE.toField(), item.getUnitPrice())
                    .set(SaleItemFields.TOTAL_PRICE.toField(), item.getTotalPrice())
                    .execute();
        }

        return findById(sale.getId());
    }




}
