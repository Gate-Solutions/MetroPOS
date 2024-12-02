package org.example.metropos.enums;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public enum SyncTrackingFields {
    SyncTrackingFieldsTable("sync_tracking"),
    ID("id"),
    TABLE_NAME("table_name"),
    RECORD_ID("record_id"),
    OPERATION("operation"),
    SYNC_STATUS("sync_status"),
    CREATED_AT("created_at");

    private final String columnName;

    SyncTrackingFields(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field<Object> toField() {
        return DSL.field(columnName);
    }

    public Table<?> toTableField() {
        return DSL.table(SyncTrackingFieldsTable.getColumnName());
    }
}
