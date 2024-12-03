package org.gate.metropos.database;

import org.gate.metropos.enums.*;
import org.gate.metropos.models.SuperAdmin;
import org.jooq.DSLContext;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.*;

// TODO: Modify inherited table queries to add non inherited constraints
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
//        Adding All the tables
        createSyncTrackingTable();
        createUserTable();
        createEmployeeTable();
        createSuperAdminTable();
        createBranchTable();
    }

//    Adding functions to create tables
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

    public void createEmployeeTable() {
        String sql = """
    CREATE TABLE IF NOT EXISTS employees (
        id BIGINT GENERATED ALWAYS AS IDENTITY,
        name VARCHAR(255) NOT NULL,
        employee_no VARCHAR(50) NOT NULL,
        is_active BOOLEAN DEFAULT true,
        is_first_time BOOLEAN DEFAULT true,
        salary DECIMAL(10,2),
        branch_id BIGINT,
        CONSTRAINT employee_no_unique UNIQUE (employee_no),
        CONSTRAINT salary_check CHECK (salary > 0)
--        CONSTRAINT branch_fk FOREIGN KEY (branch_id) REFERENCES branches(id)
    ) INHERITS (users);
    """;
        dsl.execute(sql);
    }

    public void createBranchTable() {
        dsl.createTableIfNotExists(BranchFields.BranchTable.getColumnName())
                .column(BranchFields.ID.getColumnName(), BIGINT.identity(true).notNull())
                .column(BranchFields.BRANCH_CODE.getColumnName(), VARCHAR(50).notNull())
                .column(BranchFields.NAME.getColumnName(), VARCHAR(255).notNull())
                .column(BranchFields.CITY.getColumnName(), VARCHAR(100).notNull())
                .column(BranchFields.ADDRESS.getColumnName(), VARCHAR(500).notNull())
                .column(BranchFields.PHONE.getColumnName(), VARCHAR(20).notNull())
                .column(BranchFields.IS_ACTIVE.getColumnName(), BOOLEAN.defaultValue(true).notNull())
                .column(BranchFields.NUMBER_OF_EMPLOYEES.getColumnName(), INTEGER.defaultValue(0).notNull())
                .column(BranchFields.CREATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()).notNull())
                .column(BranchFields.UPDATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()).notNull())
                .constraints(
                        primaryKey(BranchFields.ID.getColumnName()),
                        unique(BranchFields.BRANCH_CODE.getColumnName()),
                        check(field(BranchFields.NUMBER_OF_EMPLOYEES.getColumnName()).greaterOrEqual(0))
                )
                .execute();
    }


}
