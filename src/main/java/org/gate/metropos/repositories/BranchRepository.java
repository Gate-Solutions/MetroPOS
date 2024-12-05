package org.gate.metropos.repositories;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.BranchFields;
import org.gate.metropos.enums.EmployeeFields;
import org.gate.metropos.enums.UserFields;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Branch;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BranchRepository {
    private final DSLContext dsl;


    public BranchRepository() {
        dsl = DatabaseConfig.getLocalDSL();
    }


    public Branch findById(Long id) {
        Record record = dsl.select()
                .from(BranchFields.toTableField())
                .where(BranchFields.ID.toField().eq(id))
                .fetchOne();
        return mapToBranch(record);
    }


    public Branch findByBranchCode(String branchCode) {
        Record record = dsl.select()
                .from(BranchFields.toTableField())
                .where(BranchFields.BRANCH_CODE.toField().eq(branchCode))
                .fetchOne();

        return mapToBranch(record);
    }


    public Branch createBranch(Branch branch) {
        Record record = dsl.insertInto(BranchFields.toTableField())
                .set(BranchFields.BRANCH_CODE.toField(), branch.getBranchCode())
                .set(BranchFields.NAME.toField(), branch.getName())
                .set(BranchFields.CITY.toField(), branch.getCity())
                .set(BranchFields.ADDRESS.toField(), branch.getAddress())
                .set(BranchFields.PHONE.toField(), branch.getPhone())
                .returning(
                        BranchFields.ID.toField()
                )
                .fetchOne();

        if (record == null) {return null;}

        Long branchId = record.get(BranchFields.ID.toField(), Long.class);

        return findById(branchId);
    }


    public Branch createBranch(String branchCode, String name, String city, String address, String phone) {
        Record record = dsl.insertInto(BranchFields.toTableField())
                .set(BranchFields.BRANCH_CODE.toField(), branchCode)
                .set(BranchFields.NAME.toField(), name)
                .set(BranchFields.CITY.toField(), city)
                .set(BranchFields.ADDRESS.toField(), address)
                .set(BranchFields.PHONE.toField(), phone)
                .set(BranchFields.IS_ACTIVE.toField(), true)
                .set(BranchFields.NUMBER_OF_EMPLOYEES.toField(), 0)
                .returning(
                        BranchFields.ID.toField(),
                        BranchFields.BRANCH_CODE.toField(),
                        BranchFields.NAME.toField(),
                        BranchFields.CITY.toField(),
                        BranchFields.ADDRESS.toField(),
                        BranchFields.PHONE.toField(),
                        BranchFields.IS_ACTIVE.toField(),
                        BranchFields.NUMBER_OF_EMPLOYEES.toField()
                        )
                .fetchOne();

        return mapToBranch(record);
    }

    public Branch incrementEmployeeCount(Long branchId) {
        Branch b = this.findById(branchId);
        int employeeCount = Math.max(0, b.getNumberOfEmployees()+1);

        Record record = dsl.update(BranchFields.toTableField())
                .set(BranchFields.NUMBER_OF_EMPLOYEES.toField(), employeeCount)
                .set(BranchFields.UPDATED_AT.toField(), LocalDateTime.now())
                .where(BranchFields.ID.toField().eq(branchId))
                .returning(
                        BranchFields.ID.toField(),
                        BranchFields.BRANCH_CODE.toField(),
                        BranchFields.NAME.toField(),
                        BranchFields.CITY.toField(),
                        BranchFields.ADDRESS.toField(),
                        BranchFields.PHONE.toField(),
                        BranchFields.IS_ACTIVE.toField(),
                        BranchFields.NUMBER_OF_EMPLOYEES.toField()
                )
                .fetchOne();

        return mapToBranch(record);
    }

    public Branch decrementEmployeeCount(Long branchId) {

        Branch b = this.findById(branchId);
        int employeeCount = Math.max(0, b.getNumberOfEmployees()-1);

        Record record = dsl.update(BranchFields.toTableField())
                .set(BranchFields.NUMBER_OF_EMPLOYEES.toField(),employeeCount)
                .set(BranchFields.UPDATED_AT.toField(), LocalDateTime.now())
                .where(BranchFields.ID.toField().eq(branchId))
                .returning(
                        BranchFields.ID.toField(),
                        BranchFields.BRANCH_CODE.toField(),
                        BranchFields.NAME.toField(),
                        BranchFields.CITY.toField(),
                        BranchFields.ADDRESS.toField(),
                        BranchFields.PHONE.toField(),
                        BranchFields.IS_ACTIVE.toField(),
                        BranchFields.NUMBER_OF_EMPLOYEES.toField()
                )
                .fetchOne();

        return mapToBranch(record);
    }

    public Branch updateBranch(Branch branch) {
        return dsl.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            if (!branch.isActive()) {
                ctx.update(EmployeeFields.toTableField())
                        .set(EmployeeFields.IS_ACTIVE.toField(), false)
                        .where(EmployeeFields.BRANCH_ID.toField().eq(branch.getId()))
                        .execute();
                branch.setNumberOfEmployees(0);
            }

            Record record = ctx.update(BranchFields.toTableField())
                    .set(BranchFields.NAME.toField(), branch.getName())
                    .set(BranchFields.CITY.toField(), branch.getCity())
                    .set(BranchFields.ADDRESS.toField(), branch.getAddress())
                    .set(BranchFields.PHONE.toField(), branch.getPhone())
                    .set(BranchFields.UPDATED_AT.toField(), LocalDateTime.now())
                    .set(BranchFields.IS_ACTIVE.toField(), branch.isActive())
                    .where(BranchFields.ID.toField().eq(branch.getId()))
                    .returning(
                            BranchFields.ID.toField(),
                            BranchFields.BRANCH_CODE.toField(),
                            BranchFields.NAME.toField(),
                            BranchFields.CITY.toField(),
                            BranchFields.ADDRESS.toField(),
                            BranchFields.PHONE.toField(),
                            BranchFields.IS_ACTIVE.toField(),
                            BranchFields.NUMBER_OF_EMPLOYEES.toField()
                    )
                    .fetchOne();

            return mapToBranch(record);
        });
    }


    public void setBranchStatus(Long branchId, boolean isActive) {
        dsl.update(BranchFields.toTableField())
                .set(BranchFields.IS_ACTIVE.toField(), isActive)
                .set(BranchFields.UPDATED_AT.toField(), LocalDateTime.now())
                .where(BranchFields.ID.toField().eq(branchId))
                .execute();
    }


    public List<Branch> getAllBranches() {
        Result<org.jooq.Record> records = dsl.select()
                .from(BranchFields.toTableField())
                .fetch();

        return records.map(this::mapToBranch);
    }


    private Branch mapToBranch(Record record) {
        if(record == null) return null;
        // TODO: Add createdAt and updatedAt fields
        return Branch.builder()
                .id(record.get(BranchFields.ID.toField(), Long.class))
                .branchCode(record.get(BranchFields.BRANCH_CODE.toField(), String.class))
                .name(record.get(BranchFields.NAME.toField(), String.class))
                .city(record.get(BranchFields.CITY.toField(), String.class))
                .address(record.get(BranchFields.ADDRESS.toField(), String.class))
                .phone(record.get(BranchFields.PHONE.toField(), String.class))
                .isActive(record.get(BranchFields.IS_ACTIVE.toField(), Boolean.class))
                .numberOfEmployees(record.get(BranchFields.NUMBER_OF_EMPLOYEES.toField(), Integer.class))

                .build();
    }


    public List<Branch> getBranchesWithoutActiveManagers() {
        return dsl.select()
                .from(BranchFields.BranchTable.toTableField())
                .where(
                        // Case 1: Branch has no managers at all
                        BranchFields.ID.toField().notIn(
                                        dsl.select(EmployeeFields.BRANCH_ID.toField())
                                                .from(EmployeeFields.toTableField())
                                                .where(UserFields.ROLE.toField().eq(UserRole.BRANCH_MANAGER.toString()))
                                )
                                .or(
                                        // Case 2: Branch has no active managers but might have inactive ones
                                        BranchFields.ID.toField().notIn(
                                                dsl.select(EmployeeFields.BRANCH_ID.toField())
                                                        .from(EmployeeFields.toTableField())
                                                        .where(UserFields.ROLE.toField().eq(UserRole.BRANCH_MANAGER.toString()))
                                                        .and(EmployeeFields.IS_ACTIVE.toField().eq(true))
                                        )
                                )
                )
                .fetchInto(Branch.class);
    }

    public String getManagerName(Long branchId) {
        Record record = dsl.select(
                        EmployeeFields.NAME.toField())
                .from(EmployeeFields.toTableField())
                .where(EmployeeFields.BRANCH_ID.toField().eq(branchId))
                .and(UserFields.ROLE.toField().eq(UserRole.BRANCH_MANAGER.toString()))
                .and(EmployeeFields.IS_ACTIVE.toField().eq(true))
                .fetchOne();

        if (record != null) {
            return record.get(EmployeeFields.NAME.toField(),String.class);
        }

        return "Not Assigned";
    }






}


