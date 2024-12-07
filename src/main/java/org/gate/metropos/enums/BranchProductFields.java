package org.gate.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum BranchProductFields {
    BranchProductTable("branch_products"),
    ID("id"),
    BRANCH_ID("branch_id"),
    PRODUCT_ID("product_id"),
    QUANTITY("quantity");

    private final String columnName;

    BranchProductFields(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field<Object> toField() {
        return DSL.field(this.getColumnName());
    }

    public static Table<?> toTableField() {
        return DSL.table(BranchProductTable.getColumnName());
    }
}
