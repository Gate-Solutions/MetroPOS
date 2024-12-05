package org.gate.metropos.database;

import org.gate.metropos.enums.SuperAdminFields;
import org.gate.metropos.enums.SyncTrackingFields;
import org.gate.metropos.enums.UserFields;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.SuperAdmin;
import org.gate.metropos.models.User;
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

    public void initialize() {
        createTables();
        initializeSuperAdmin();
    }

    public void createTables() {
//        Add All the tables
        createSyncTrackingTable();
        createUserTable();
        createSuperAdminTable();
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
                .column(SyncTrackingFields.FIELD_VALUES.getColumnName(), JSONB)
                .column(SyncTrackingFields.SYNC_STATUS.getColumnName(), BOOLEAN.defaultValue(false))
                .column(SyncTrackingFields.CREATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()))
                .primaryKey(SyncTrackingFields.ID.getColumnName())
                .execute();
    }

    public void createUserTable() {
        dsl.createTableIfNotExists(UserFields.UserTable.getColumnName())
                .column(UserFields.ID.getColumnName(), BIGINT.identity(true))
                .column(UserFields.USERNAME.getColumnName(), VARCHAR(255))
                .column(UserFields.EMAIL.getColumnName(), VARCHAR(255))
                .column(UserFields.PASSWORD.getColumnName(), VARCHAR(255))
                .column(UserFields.ROLE.getColumnName(), VARCHAR(20))
                .column(UserFields.CREATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()))
                .column(UserFields.UPDATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()))
                .constraints(
                        primaryKey(UserFields.ID.getColumnName()),
                        unique(UserFields.EMAIL.getColumnName()),
                        unique(UserFields.USERNAME.getColumnName()),
                        check(field(UserFields.ROLE.getColumnName()).in("SUPER_ADMIN", "BRANCH_MANAGER", "CASHIER", "DATA_ENTRY_OPERATOR"))
                )
                .execute();
    }

    public void createSuperAdminTable() {
        String sql = """
    CREATE TABLE IF NOT EXISTS super_admin (
        id BIGINT GENERATED ALWAYS AS IDENTITY
    ) INHERITS (users);
""";
        dsl.execute(sql);
    }


    public void initializeSuperAdmin() {
        boolean superAdminExists = dsl.fetchCount(SuperAdminFields.SuperAdminTable.toTableField()) > 0;

        if (!superAdminExists) {
            SuperAdmin defaultAdmin = SuperAdmin.builder()
                    .username("admin")
                    .email("admin@metro.com")
                    .password("admin") // Use your password encoder to hash the password
                    .role(UserRole.SUPER_ADMIN)
                    .build();


            dsl.insertInto(SuperAdminFields.SuperAdminTable.toTableField())
                    .set(UserFields.USERNAME.toField(), defaultAdmin.getUsername())
                    .set(UserFields.EMAIL.toField(), defaultAdmin.getEmail())
                    .set(UserFields.PASSWORD.toField(), defaultAdmin.getPassword())
                    .set(UserFields.ROLE.toField(), defaultAdmin.getRole().toString())
                    .execute();
        }
    }


}
