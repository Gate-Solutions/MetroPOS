package org.gate.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum CategoryFields {
    CategoryTable("categories"),
    ID("id"),
    NAME("name");

    private final String columnName;

    CategoryFields(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field<Object> toField() {
        return DSL.field(this.getColumnName());
    }

    public static Table<?> toTableField() {
        return DSL.table(CategoryTable.getColumnName());
    }
}
