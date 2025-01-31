package org.gate.metropos.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.SyncTrackingFields;
import org.gate.metropos.models.SyncTracking;
import org.gate.metropos.utils.InternetConnectivityCheckerUtil;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SyncService {
    private final DSLContext localContext = DatabaseConfig.getLocalDSL();
    private final DSLContext remoteContext = DatabaseConfig.getRemoteDSL();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public void trackChange(String tableName, Integer recordId, String operation, String fieldNames ) {
        localContext.insertInto(SyncTrackingFields.SyncTrackingFieldsTable.toTableField())
                .set(SyncTrackingFields.TABLE_NAME.toField(), tableName)
                .set(SyncTrackingFields.RECORD_ID.toField(), recordId)
                .set(SyncTrackingFields.FIELD_VALUES.toField(), DSL.field("?::jsonb", String.class, fieldNames))
                .set(SyncTrackingFields.OPERATION.toField(), operation)
                .execute();
    }
    public void trackChange(DSLContext ctx, String tableName, Integer recordId, String operation, String fieldNames ) {
        ctx.insertInto(SyncTrackingFields.SyncTrackingFieldsTable.toTableField())
                .set(SyncTrackingFields.TABLE_NAME.toField(), tableName)
                .set(SyncTrackingFields.RECORD_ID.toField(), recordId)
                .set(SyncTrackingFields.FIELD_VALUES.toField(), DSL.field("?::jsonb", String.class, fieldNames))
                .set(SyncTrackingFields.OPERATION.toField(), operation)
                .execute();
    }

    public synchronized void syncWithRemote() {
        System.out.println("[Starting Sync Session]");
        if (!InternetConnectivityCheckerUtil.isInternetAvailable()) {
            System.out.println("[Syncing Abort] Internet not available");
            return;
        }


//        Sync Data
        System.out.println("[Syncing] Internet Available");
        try{
             List<SyncTracking> syncTrackingList = localContext
                     .select()
                     .from(SyncTrackingFields.SyncTrackingFieldsTable.getColumnName())
                     .orderBy(DSL.field(SyncTrackingFields.CREATED_AT.getColumnName()).asc())
                     .fetch().map(this::mapToSyncTracking);

//            System.out.println("Data from table:");
            for(SyncTracking syncTracking : syncTrackingList) {
                syncRecord(syncTracking);
                System.out.println(syncTracking.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        System.out.println("[Ending Sync Session]");
    }

    private void syncRecord(SyncTracking record) {
        try {
            switch (record.getOperation().toLowerCase()) {
                case "insert" -> handleInsert(record);
                case "update" -> handleUpdate(record);
                case "delete" -> handleDelete(record);
            }

            localContext
                    .deleteFrom(DSL.table(SyncTrackingFields.SyncTrackingFieldsTable.getColumnName()))
                    .where(DSL.field("id").eq(record.getId()))
                    .execute();
        } catch (Exception e) {
            System.out.println("Error in syncing: " + e.getMessage());
        }
    }

    private void handleInsert(SyncTracking syncRecord) {
        InsertSetStep<?> insert = remoteContext
                .insertInto(DSL.table(syncRecord.getTableName()));
        Map<Field<?>, Object> insertMap = new HashMap<>();
        Map<String, Object> fieldValues = syncRecord.getFieldValues();

        for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
            Object value = entry.getValue();
            if (entry.getKey().equals("invoice_date")) {
                value = Date.valueOf(value.toString());
            }
            insertMap.put(DSL.field(entry.getKey()), value);
        }

        insert.set(insertMap).execute();
    }

    private void handleUpdate(SyncTracking syncRecord) {
        Map<String, Object> fieldValues = syncRecord.getFieldValues();

        UpdateConditionStep<?> update = remoteContext
                .update(DSL.table(syncRecord.getTableName()))
                .set(fieldValues) // Set all fields at once
                .where(DSL.field("id").eq(syncRecord.getRecordId())); // Add the WHERE condition

        update.execute();
    }

    private void handleDelete(SyncTracking syncRecord) {
        remoteContext
                .deleteFrom(DSL.table(syncRecord.getTableName()))
                .where(DSL.field("id").eq(syncRecord.getRecordId()))
                .execute();
    }

    private SyncTracking mapToSyncTracking(Record record) {
        Map<String, Object> fieldValuesMap = null;

        String fieldValuesJson = record.get(SyncTrackingFields.FIELD_VALUES.toField(), String.class);
        if (fieldValuesJson != null) {
            try {
                fieldValuesMap = objectMapper.readValue(fieldValuesJson, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

            return SyncTracking.builder()
                    .id(Long.parseLong(record.get(SyncTrackingFields.ID.toField()).toString()))
                    .syncStatus(Boolean.parseBoolean(record.get(SyncTrackingFields.SYNC_STATUS.toField()).toString()))
                    .operation(record.get(SyncTrackingFields.OPERATION.toField()).toString())
                    .recordId(Integer.parseInt(record.get(SyncTrackingFields.RECORD_ID.toField()).toString()))
                    .createdAt(record.get(SyncTrackingFields.CREATED_AT.toField(), Timestamp.class))
                    .fieldValues(fieldValuesMap)
                    .tableName(record.get(SyncTrackingFields.TABLE_NAME.toField(), String.class))
                    .build();
    }



}
