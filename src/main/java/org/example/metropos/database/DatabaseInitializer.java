package org.example.metropos.database;

import org.example.metropos.enums.SyncTrackingFields;
import org.jooq.DSLContext;
import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.*;


public class DatabaseInitializer {
    private final DSLContext dsl;
    boolean isLocal = true;

    public DatabaseInitializer(DSLContext dsl) {
        this.dsl = dsl;
    }

    public DatabaseInitializer(DSLContext dsl, boolean isLocal) {
        this.dsl = dsl;
        this.isLocal = isLocal;
    }

    public void createTables() {
//        Add All the tables
        createSyncTrackingTable();
    }

//    Add functions to create tables
    public void createSyncTrackingTable() {
        if(!isLocal)
            return;

        dsl.createTableIfNotExists(SyncTrackingFields.SyncTrackingFieldsTable.getColumnName())
                .column(SyncTrackingFields.ID.getColumnName(), BIGINT.identity(true))
                .column(SyncTrackingFields.TABLE_NAME.getColumnName(), VARCHAR(255))
                .column(SyncTrackingFields.RECORD_ID.getColumnName(), INTEGER)
                .column(SyncTrackingFields.OPERATION.getColumnName(), VARCHAR(50))
                .column(SyncTrackingFields.SYNC_STATUS.getColumnName(), BOOLEAN.defaultValue(false))
                .column(SyncTrackingFields.CREATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()))
                .primaryKey(SyncTrackingFields.ID.getColumnName())
                .execute();
    }


}
