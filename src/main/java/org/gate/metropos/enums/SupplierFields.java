package org.gate.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum SupplierFields {
    SupplierTable("suppliers"),
    ID("id"),
    NAME("name"),
    EMAIL("email"),
    PHONE("phone"),
    NTN_NUMBER("ntn_number"),
    IS_ACTIVE("is_active"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");

    private final String columnName;

    SupplierFields(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field<Object> toField() {
        return DSL.field(this.getColumnName());
    }

    public static Table<?> toTableField() {
        return DSL.table(SupplierTable.getColumnName());
    }
}
