package org.gate.metropos.enums.PurchaseInvoice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

@AllArgsConstructor
@Getter
public enum PurchaseInvoiceItemFields {
    PurchaseInvoiceItemTable("purchase_invoice_items"),
    ID("id"),
    INVOICE_ID("invoice_id"),
    PRODUCT_ID("product_id"),
    QUANTITY("quantity"),
    UNIT_PRICE("unit_price"),
    CARTON_PRICE("carton_price"),
    TOTAL_PRICE("total_price");

    private final String columnName;

    public Field<Object> toField() {
        return DSL.field(this.getColumnName());
    }

    public static Table<?> toTableField() {
        return DSL.table(PurchaseInvoiceItemTable.getColumnName());
    }
}