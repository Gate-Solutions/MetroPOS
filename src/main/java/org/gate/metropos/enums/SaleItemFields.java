package org.gate.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum SaleItemFields {
    SaleItemTable("sale_items"),
    ID("id"),
    SALE_ID("sale_id"),
    PRODUCT_ID("product_id"),
    QUANTITY("quantity"),
    UNIT_PRICE("unit_price"),
    TOTAL_PRICE("total_price");

    private final String columnName;

    SaleItemFields(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field<Object> toField() {
        return DSL.field(this.getColumnName());
    }

    public static Table<?> toTableField() {
        return DSL.table(SaleItemTable.getColumnName());
    }
}
