package org.gate.metropos.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.SupplierFields;
import org.gate.metropos.models.Supplier;
import org.gate.metropos.services.SyncService;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class SupplierRepository {
    private final DSLContext dsl;
    private final SyncService syncService;

    public SupplierRepository() {
        dsl = DatabaseConfig.getLocalDSL();
        syncService = new SyncService();
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

        if(record == null) return null;
        int id = record.get(SupplierFields.ID.toField(), Integer.class);

        try {
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put("name", supplier.getName());
            fieldValues.put("email", supplier.getEmail());
            fieldValues.put("phone", supplier.getPhone());
            fieldValues.put("ntn_number", supplier.getNtnNumber());
            fieldValues.put("company_name", supplier.getCompanyName());
            fieldValues.put("is_active", true);

            syncService.trackChange(
                    "suppliers",
                    id,
                    "insert",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }

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

        try {
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put("name", supplier.getName());
            fieldValues.put("email", supplier.getEmail());
            fieldValues.put("phone", supplier.getPhone());
            fieldValues.put("company_name", supplier.getCompanyName());
            fieldValues.put("ntn_number", supplier.getNtnNumber());
            fieldValues.put("is_active", supplier.isActive());

            syncService.trackChange(
                    "suppliers",
                    supplier.getId().intValue(),
                    "update",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }

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

        try {
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put("is_active", isActive);

            syncService.trackChange(
                    "suppliers",
                    id.intValue(),
                    "update",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }
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
