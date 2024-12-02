package org.example.metropos.services;

import org.example.metropos.config.DatabaseConfig;
import org.example.metropos.enums.SyncTrackingFields;
import org.example.metropos.utils.InternetConnectivityCheckerUtil;
import org.jooq.DSLContext;


public class SyncService {
    private final DSLContext localContext = DatabaseConfig.getLocalDSL();
    private final DSLContext remoteContext = DatabaseConfig.getRemoteDSL();


    public void trackChange(String tableName, Integer recordId, String operation) {
        localContext.insertInto(SyncTrackingFields.SyncTrackingFieldsTable.toTableField())
                .set(SyncTrackingFields.TABLE_NAME.toField(), tableName)
                .set(SyncTrackingFields.RECORD_ID.toField(), recordId)
                .set(SyncTrackingFields.OPERATION.toField(), operation)
                .execute();
    }

    public synchronized void syncWithRemote() {
        if (!InternetConnectivityCheckerUtil.isInternetAvailable()) {
            return;
        }
//        Sync Data
        System.out.println("Internet Available");

    }

}
