package org.example.metropos.models;


import lombok.*;

import java.sql.Timestamp;
import java.util.Map;

@Builder(builderClassName = "SyncTrackingBuilder", access = AccessLevel.PUBLIC)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SyncTracking {
    private Long id;
    private String tableName;
    private Integer recordId;
    private String operation;
    private Map<String, Object> fieldValues;
    private Boolean syncStatus;
    private Timestamp createdAt;
}
