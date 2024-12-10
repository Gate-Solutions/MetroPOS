package org.gate.metropos.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Getter
@AllArgsConstructor
public enum BranchFields {
    BranchTable("branches"),
    ID("id"),
    BRANCH_CODE("branch_code"),
    NAME("name"),
    CITY("city"),
    ADDRESS("address"),
    PHONE("phone"),
    IS_ACTIVE("is_active"),
    NUMBER_OF_EMPLOYEES("number_of_employees"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");

    private final String columnName;

    public Field<Object> toField() {
        return DSL.field(columnName);
    }

    public static Table<?> toTableField() {
        return DSL.table(BranchTable.getColumnName());
    }
}

