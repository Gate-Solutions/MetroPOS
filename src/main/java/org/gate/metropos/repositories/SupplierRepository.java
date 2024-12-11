package org.gate.metropos.repositories;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.SupplierFields;
import org.gate.metropos.models.Supplier;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
public class SupplierRepository {
    private final DSLContext dsl;

    public SupplierRepository() {
        dsl = DatabaseConfig.getLocalDSL();
    }

    public Supplier findById(Long id) {
        Record record = dsl.select()
                .from(SupplierFields.SupplierTable.toTableField())
                .where(SupplierFields.ID.toField().eq(id))
                .fetchOne();
        return mapToSupplier(record);
    }

    public Supplier findByEmail(String email) {
        Record record = dsl.select()
                .from(SupplierFields.SupplierTable.toTableField())
                .where(SupplierFields.EMAIL.toField().eq(email))
                .fetchOne();
        return mapToSupplier(record);
    }

    public Supplier createSupplier(Supplier supplier) {
        Record record = dsl.insertInto(SupplierFields.toTableField())
                .set(SupplierFields.NAME.toField(), supplier.getName())
                .set(SupplierFields.EMAIL.toField(), supplier.getEmail())
                .set(SupplierFields.PHONE.toField(), supplier.getPhone())
                .set(SupplierFields.NTN_NUMBER.toField(), supplier.getNtnNumber())
                .set(SupplierFields.COMPANY_NAME.toField(), supplier.getCompanyName())
                .set(SupplierFields.IS_ACTIVE.toField(), true)
                .set(SupplierFields.COMPANY_NAME.toField(), supplier.getCompanyName())

                .returning(
                        SupplierFields.ID.toField(),
                        SupplierFields.NAME.toField(),
                        SupplierFields.EMAIL.toField(),
                        SupplierFields.PHONE.toField(),
                        SupplierFields.NTN_NUMBER.toField(),
                        SupplierFields.IS_ACTIVE.toField(),
                        SupplierFields.COMPANY_NAME.toField()
                )
                .fetchOne();

        return mapToSupplier(record);
    }


    public Supplier updateSupplier(Supplier supplier) {
        Record record = dsl.update(SupplierFields.toTableField())
                .set(SupplierFields.NAME.toField(), supplier.getName())

                .set(SupplierFields.EMAIL.toField(), supplier.getEmail())
                .set(SupplierFields.PHONE.toField(), supplier.getPhone())
                .set(SupplierFields.COMPANY_NAME.toField(), supplier.getCompanyName())
                .set(SupplierFields.NTN_NUMBER.toField(), supplier.getNtnNumber())
                .set(SupplierFields.IS_ACTIVE.toField(), supplier.isActive())
                .where(SupplierFields.ID.toField().eq(supplier.getId()))
                .returning(SupplierFields.ID.toField(),
                        SupplierFields.NAME.toField(),
                        SupplierFields.EMAIL.toField(),
                        SupplierFields.PHONE.toField(),
                        SupplierFields.NTN_NUMBER.toField(),
                        SupplierFields.COMPANY_NAME.toField(),
                        SupplierFields.IS_ACTIVE.toField())
                .fetchOne();

        return mapToSupplier(record);
    }

    public List<Supplier> getAllSuppliers() {
        Result<Record> records = dsl.select()
                .from(SupplierFields.toTableField())
                .fetch();

        return records.map(this::mapToSupplier);
    }

    public void setSupplierStatus(Long id, boolean isActive) {
        dsl.update(SupplierFields.toTableField())
                .set(SupplierFields.IS_ACTIVE.toField(), isActive)
                .where(SupplierFields.ID.toField().eq(id))
                .execute();
    }


    private Supplier mapToSupplier(Record record) {
        if (record == null) return null;
        return Supplier.builder()
                .id(record.get(SupplierFields.ID.toField(), Long.class))
                .name(record.get(SupplierFields.NAME.toField(), String.class))

                .email(record.get(SupplierFields.EMAIL.toField(), String.class))
                .phone(record.get(SupplierFields.PHONE.toField(), String.class))

                .ntnNumber(record.get(SupplierFields.NTN_NUMBER.toField(), String.class))
                .isActive(record.get(SupplierFields.IS_ACTIVE.toField(), Boolean.class))
                .companyName(record.get(SupplierFields.COMPANY_NAME.toField(), String.class))


                .build();
    }
}
