package org.gate.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum SaleFields {
    SaleTable("sales"),
    ID("id"),
    INVOICE_NUMBER("invoice_number"),
    BRANCH_ID("branch_id"),
    CREATED_BY("created_by"),
    INVOICE_DATE("invoice_date"),
    TOTAL_AMOUNT("total_amount"),
    DISCOUNT("discount"),
    NET_AMOUNT("net_amount"),
    NOTES("notes"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");

    private final String columnName;

    SaleFields(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field<Object> toField() {
        return DSL.field(this.getColumnName());
    }

    public static Table<?> toTableField() {
        return DSL.table(SaleTable.getColumnName());
    }
}
