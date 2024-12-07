package org.gate.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum ProductFields {
    ProductTable("products"),
    ID("id"),
    NAME("name"),
    CODE("code"),
    CATEGORY_ID("category_id"),
    ORIGINAL_PRICE("original_price"),
    SALE_PRICE("sale_price"),
    IS_ACTIVE("is_active"),
    PRICE_OF_CARTON("price_of_carton");
    private final String columnName;

    ProductFields(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field<Object> toField() {
        return DSL.field(this.getColumnName());
    }

    public static Table<?> toTableField() {
        return DSL.table(ProductTable.getColumnName());
    }
}
