package org.gate.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum SuperAdminFields {
    SuperAdminTable("super_admin");

    private final String columnName;

    SuperAdminFields(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field<Object> toField() {
        return DSL.field(columnName);
    }

    public Table<?> toTableField() {
        return DSL.table(SuperAdminTable.getColumnName());
    }
}
