package org.gate.metropos.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Getter
@AllArgsConstructor
public enum EmployeeFields {
    EmployeeTable("employees"),
    NAME("name"),
    EMPLOYEE_NO("employee_no"),
    IS_ACTIVE("is_active"),
    IS_FIRST_TIME("is_first_time"),
    SALARY("salary"),
    BRANCH_ID("branch_id");


    private final String columnName;

    public Field<Object> toField() {
        return DSL.field(columnName);
    }

    public static Table<?> toTableField() {
        return DSL.table(EmployeeTable.getColumnName());
    }
}
