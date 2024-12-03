package org.gate.metropos.database;

import org.gate.metropos.enums.*;
import org.gate.metropos.enums.PurchaseInvoice.PurchaseInvoiceFields;
import org.gate.metropos.enums.PurchaseInvoice.PurchaseInvoiceItemFields;
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
//        Add All the tables
        createSyncTrackingTable();
        createUserTable();
        createEmployeeTable();
        createSuperAdminTable();
        createBranchTable();
        createSupplierTable();
        createProductTables();
        createPurchaseInvoiceTables();
        createSalesTables();
    }

//   functions to create tables
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
        id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
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

    public void createSupplierTable() {
        dsl.createTableIfNotExists(SupplierFields.SupplierTable.getColumnName())
                .column(SupplierFields.ID.getColumnName(), BIGINT.identity(true))
                .column(SupplierFields.NAME.getColumnName(), VARCHAR(255).notNull())

                .column(SupplierFields.EMAIL.getColumnName(), VARCHAR(255))
                .column(SupplierFields.PHONE.getColumnName(), VARCHAR(20).notNull())

                .column(SupplierFields.NTN_NUMBER.getColumnName(), VARCHAR(50))
                .column(SupplierFields.IS_ACTIVE.getColumnName(), BOOLEAN.defaultValue(true))
                .column(SupplierFields.CREATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()))
                .column(SupplierFields.UPDATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()))
                .constraints(
                        primaryKey(SupplierFields.ID.getColumnName()),
                        unique(SupplierFields.NTN_NUMBER.getColumnName()),
                        unique(SupplierFields.EMAIL.getColumnName())
                )
                .execute();
    }

    public void createProductTables() {
        createCategoryTable();

        dsl.createTableIfNotExists("products")
                .column("id", BIGINT.identity(true))
                .column("name", VARCHAR(255).notNull())
                .column("code", VARCHAR(50).notNull())
                .column("category_id", BIGINT)
                .column("original_price", DECIMAL(10, 2).notNull())
                .column("sale_price", DECIMAL(10, 2).notNull())
                .column("is_active", BOOLEAN.defaultValue(true))
                .column("price_of_carton", DECIMAL(10, 2).notNull())
                .constraints(
                        primaryKey("id"),
                        unique("code"),
                        foreignKey("category_id").references("categories", "id"),
                        check(field("sale_price").greaterThan(field("original_price")))
                )
                .execute();

        dsl.createTableIfNotExists("branch_products")
                .column("id", BIGINT.identity(true))
                .column("branch_id", BIGINT.notNull())
                .column("product_id", BIGINT.notNull())
                .column("quantity", INTEGER.notNull().defaultValue(0))
                .constraints(
                        primaryKey("id"),
                        unique("branch_id", "product_id"),
                        foreignKey("branch_id").references("branches", "id"),
                        foreignKey("product_id").references("products", "id"),
                        check(field("quantity").greaterOrEqual(0))
                )
                .execute();
    }

    public void createCategoryTable() {
        dsl.createTableIfNotExists("categories")
                .column("id", BIGINT.identity(true))
                .column("name", VARCHAR(100).notNull())
                .constraints(
                        primaryKey("id"),
                        unique("name")
                )
                .execute();
    }

    public void createPurchaseInvoiceTables() {
        dsl.createTableIfNotExists(PurchaseInvoiceFields.PurchaseInvoiceTable.getColumnName())
                .column(PurchaseInvoiceFields.ID.getColumnName(), BIGINT.identity(true))
                .column(PurchaseInvoiceFields.INVOICE_NUMBER.getColumnName(), VARCHAR(50).notNull())
                .column(PurchaseInvoiceFields.SUPPLIER_ID.getColumnName(), BIGINT.notNull())
                .column(PurchaseInvoiceFields.BRANCH_ID.getColumnName(), BIGINT.notNull())
                .column(PurchaseInvoiceFields.CREATED_BY.getColumnName(), BIGINT.notNull())
                .column(PurchaseInvoiceFields.INVOICE_DATE.getColumnName(), DATE.notNull())
                .column(PurchaseInvoiceFields.TOTAL_AMOUNT.getColumnName(), DECIMAL(10, 2).notNull())
                .column(PurchaseInvoiceFields.NOTES.getColumnName(), VARCHAR(500))
                .column(PurchaseInvoiceFields.CREATED_AT.getColumnName(), TIMESTAMPWITHTIMEZONE.defaultValue(currentOffsetDateTime()))
                .column(PurchaseInvoiceFields.UPDATED_AT.getColumnName(), TIMESTAMPWITHTIMEZONE.defaultValue(currentOffsetDateTime()))
                .constraints(
                        primaryKey(PurchaseInvoiceFields.ID.getColumnName()),
                        unique(PurchaseInvoiceFields.INVOICE_NUMBER.getColumnName()),
                        foreignKey(PurchaseInvoiceFields.SUPPLIER_ID.getColumnName())
                                .references(SupplierFields.SupplierTable.getColumnName(), SupplierFields.ID.getColumnName()),
                        foreignKey(PurchaseInvoiceFields.BRANCH_ID.getColumnName())
                                .references(BranchFields.BranchTable.getColumnName(), BranchFields.ID.getColumnName()),
                        foreignKey(PurchaseInvoiceFields.CREATED_BY.getColumnName())
                                .references(UserFields.UserTable.getColumnName(), UserFields.ID.getColumnName())
                )
                .execute();

        dsl.createTableIfNotExists(PurchaseInvoiceItemFields.PurchaseInvoiceItemTable.getColumnName())
                .column(PurchaseInvoiceItemFields.ID.getColumnName(), BIGINT.identity(true))
                .column(PurchaseInvoiceItemFields.INVOICE_ID.getColumnName(), BIGINT.notNull())
                .column(PurchaseInvoiceItemFields.PRODUCT_ID.getColumnName(), BIGINT.notNull())
                .column(PurchaseInvoiceItemFields.QUANTITY.getColumnName(), INTEGER.notNull())
                .column(PurchaseInvoiceItemFields.UNIT_PRICE.getColumnName(), DECIMAL(10, 2).notNull())
                .column(PurchaseInvoiceItemFields.CARTON_PRICE.getColumnName(), DECIMAL(10, 2).notNull())
                .column(PurchaseInvoiceItemFields.TOTAL_PRICE.getColumnName(), DECIMAL(10, 2).notNull())
                .constraints(
                        primaryKey(PurchaseInvoiceItemFields.ID.getColumnName()),
                        foreignKey(PurchaseInvoiceItemFields.INVOICE_ID.getColumnName())
                                .references(PurchaseInvoiceFields.PurchaseInvoiceTable.getColumnName(), PurchaseInvoiceFields.ID.getColumnName()),
                        foreignKey(PurchaseInvoiceItemFields.PRODUCT_ID.getColumnName())
                                .references(ProductFields.ProductTable.getColumnName(), ProductFields.ID.getColumnName()),
                        check(field(PurchaseInvoiceItemFields.QUANTITY.getColumnName()).greaterThan(0)),
                        check(field(PurchaseInvoiceItemFields.UNIT_PRICE.getColumnName()).greaterThan(0)),
                        check(field(PurchaseInvoiceItemFields.CARTON_PRICE.getColumnName()).greaterThan(0))
                )
                .execute();
    }


    public void createSalesTables() {
        // Create Sales table
        dsl.createTableIfNotExists(SaleFields.SaleTable.getColumnName())
                .column(SaleFields.ID.getColumnName(), BIGINT.identity(true).notNull())
                .column(SaleFields.INVOICE_NUMBER.getColumnName(), VARCHAR(50).notNull())
                .column(SaleFields.BRANCH_ID.getColumnName(), BIGINT.notNull())
                .column(SaleFields.CREATED_BY.getColumnName(), BIGINT.notNull())
                .column(SaleFields.INVOICE_DATE.getColumnName(), DATE.notNull())
                .column(SaleFields.TOTAL_AMOUNT.getColumnName(), DECIMAL(10, 2).notNull())
                .column(SaleFields.DISCOUNT.getColumnName(), DECIMAL(10, 2).notNull())
                .column(SaleFields.NET_AMOUNT.getColumnName(), DECIMAL(10, 2).notNull())
                .column(SaleFields.NOTES.getColumnName(), VARCHAR(500))
                .column(SaleFields.CREATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()).notNull())
                .column(SaleFields.UPDATED_AT.getColumnName(), TIMESTAMP.defaultValue(currentTimestamp()).notNull())
                .constraints(
                        primaryKey(SaleFields.ID.getColumnName()),
                        unique(SaleFields.INVOICE_NUMBER.getColumnName()),
                        foreignKey(SaleFields.BRANCH_ID.getColumnName())
                                .references("branches", "id"),
                        foreignKey(SaleFields.CREATED_BY.getColumnName())
                                .references(EmployeeFields.EmployeeTable.getColumnName(), "id")
                )
                .execute();

        // Create Sale Items table
        dsl.createTableIfNotExists(SaleItemFields.SaleItemTable.getColumnName())
                .column(SaleItemFields.ID.getColumnName(), BIGINT.identity(true).notNull())
                .column(SaleItemFields.SALE_ID.getColumnName(), BIGINT.notNull())
                .column(SaleItemFields.PRODUCT_ID.getColumnName(), BIGINT.notNull())
                .column(SaleItemFields.QUANTITY.getColumnName(), INTEGER.notNull())
                .column(SaleItemFields.UNIT_PRICE.getColumnName(), DECIMAL(10, 2).notNull())
                .column(SaleItemFields.TOTAL_PRICE.getColumnName(), DECIMAL(10, 2).notNull())
                .constraints(
                        primaryKey(SaleItemFields.ID.getColumnName()),
                        foreignKey(SaleItemFields.SALE_ID.getColumnName())
                                .references(SaleFields.SaleTable.getColumnName(), SaleFields.ID.getColumnName()),
                        foreignKey(SaleItemFields.PRODUCT_ID.getColumnName())
                                .references("products", "id")
                )
                .execute();
    }


}