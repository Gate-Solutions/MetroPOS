package org.gate.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum UserFields {
    UserTable("users"),
    ID("id"),
    USERNAME("username"),
    EMAIL("email"),
    PASSWORD("password"),
    ROLE("role", "VARCHAR(20)",
            String.format("CHECK (%s in ('SUPER_ADMIN', 'BRANCH_MANAGER', 'CASHIER', 'DATA_ENTRY_OPERATOR'))", "role")),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");

    private final String columnName;
    private final String dataType;
    private final String constraint;

    UserFields(String columnName) {
        this(columnName, null, null);
    }

    UserFields(String columnName, String dataType, String constraint) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.constraint = constraint;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public String getConstraint() {
        return constraint;
    }

    public Field<Object> toField() {
        return DSL.field(columnName);
    }

    public Table<?> toTableField() {
        return DSL.table(UserTable.getColumnName());
    }
}
