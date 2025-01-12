package org.gate.metropos.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.EmployeeFields;
import org.gate.metropos.enums.UserFields;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.gate.metropos.services.SyncService;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@AllArgsConstructor
public class EmployeeRepository {
    private DSLContext dsl;
    private final SyncService syncService;

    public EmployeeRepository () {
        dsl = DatabaseConfig.getLocalDSL();
        syncService = new SyncService();
    }

    public Employee findById(Long id) {
        Record record = dsl.select()
                .from(EmployeeFields.toTableField())
                .where(UserFields.ID.toField().eq(id))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public Employee findByEmail(String email) {
        Record record = dsl.select()
                .from(EmployeeFields.toTableField())
                .where(UserFields.EMAIL.toField().eq(email))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public Employee findByUsername(String username) {
        Record record = dsl.select()
                .from(EmployeeFields.toTableField())
                .where(UserFields.USERNAME.toField().eq(username))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public Employee findByEmployeeNo(String employeeNo) {
        Record record = dsl.select()
                .from(EmployeeFields.toTableField())
                .where(EmployeeFields.EMPLOYEE_NO.toField().eq(employeeNo))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public Employee addEmployee(Employee employee) {
        Record record = dsl.insertInto(EmployeeFields.toTableField())
                .set(UserFields.USERNAME.toField(), employee.getUsername())
                .set(UserFields.EMAIL.toField(), employee.getEmail())
                .set(UserFields.PASSWORD.toField(), employee.getPassword())
                .set(UserFields.ROLE.toField(), employee.getRole().toString())
                .set(EmployeeFields.NAME.toField(), employee.getName())
                .set(EmployeeFields.EMPLOYEE_NO.toField(), employee.getEmployeeNo())
                .set(EmployeeFields.IS_ACTIVE.toField(), employee.isActive())
                .set(EmployeeFields.IS_FIRST_TIME.toField(), employee.isFirstTime())
                .set(EmployeeFields.SALARY.toField(), employee.getSalary())
                .set(EmployeeFields.BRANCH_ID.toField(), employee.getBranchId())
                .returning(
                        UserFields.ID.toField()
                )
                .fetchOne();

        if(record == null) return null;
        Long id = record.get(UserFields.ID.toField(), Long.class);

        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("username", employee.getUsername());
        fieldValues.put("email", employee.getEmail());
        fieldValues.put("password", employee.getPassword());
        fieldValues.put("role", employee.getRole().toString());
        fieldValues.put("name", employee.getName());
        fieldValues.put("employee_no", employee.getEmployeeNo());
        fieldValues.put("is_active", employee.isActive());
        fieldValues.put("is_first_time", employee.isFirstTime());
        fieldValues.put("salary", employee.getSalary());
        fieldValues.put("branch_id", employee.getBranchId());

        try {
            syncService.trackChange(
                    "employees",
                    id.intValue(),
                    "insert",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }

        return findById(id);
    }

    public Employee getEmployee(Long id) {
        Record record = dsl.select()
                .from(EmployeeFields.toTableField())
                .where(UserFields.ID.toField().eq(id))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public List<Employee> getAllEmployeesByRole(UserRole role) {
        Result<Record> records = dsl.select()
                .from(EmployeeFields.toTableField())
                .where(UserFields.ROLE.toField().eq(role.toString()))
                .fetch();

        return records.map(this::mapToEmployee);
    }

    public List<Employee> getAllEmployees() {
        Result<Record> records = dsl.select()
                .from(EmployeeFields.toTableField())
                .fetch();

        return records.map(this::mapToEmployee);
    }

    public List<Employee> getEmployeesByBranchAndRole(Long branchId, UserRole role) {
        Result<Record> result = dsl.select()
                .from(EmployeeFields.toTableField())
                .where(EmployeeFields.BRANCH_ID.toField().eq(branchId))
                .and(UserFields.ROLE.toField().eq(role.name()))
                .and(EmployeeFields.IS_ACTIVE.toField().eq(true))
                .fetch();

        return result.map(this::mapToEmployee);
    }


    public void setEmployeeStatus(Long employeeId, boolean isActive) {
        dsl.update(EmployeeFields.toTableField())
                .set(EmployeeFields.IS_ACTIVE.toField(), isActive)
                .set(UserFields.UPDATED_AT.toField(), LocalDateTime.now())
                .where(UserFields.ID.toField().eq(employeeId))
                .execute();

        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("is_active", isActive);

        try {
            syncService.trackChange(
                    "employees",
                    employeeId.intValue(),
                    "update",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }
    }

    public void updatePassword(Long employeeId, String newPassword) {
        dsl.update(EmployeeFields.toTableField())
                .set(UserFields.PASSWORD.toField(), newPassword)
                .set(EmployeeFields.IS_FIRST_TIME.toField(), false)
                .set(UserFields.UPDATED_AT.toField(), LocalDateTime.now())
                .where(UserFields.ID.toField().eq(employeeId))
                .execute();
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("password", newPassword);
        fieldValues.put("is_first_time", false);

        try {
            syncService.trackChange(
                    "employees",
                    employeeId.intValue(),
                    "update",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }
    }

    public Employee updateEmployee(Employee employee) {
        Record record = dsl.update(EmployeeFields.toTableField())
                .set(UserFields.USERNAME.toField(), employee.getUsername())
                .set(UserFields.EMAIL.toField(), employee.getEmail())
                .set(UserFields.ROLE.toField(), employee.getRole().toString())
                .set(UserFields.UPDATED_AT.toField(), LocalDateTime.now())
                .set(EmployeeFields.NAME.toField(), employee.getName())
                .set(EmployeeFields.EMPLOYEE_NO.toField(), employee.getEmployeeNo())
                .set(EmployeeFields.SALARY.toField(), employee.getSalary())
                .set(EmployeeFields.BRANCH_ID.toField(), employee.getBranchId())
                .set(EmployeeFields.IS_ACTIVE.toField(), employee.isActive())
                .where(UserFields.ID.toField().eq(employee.getId()))
                .returning(
                        UserFields.ID.toField(),
                        UserFields.USERNAME.toField(),
                        UserFields.EMAIL.toField(),
                        UserFields.ROLE.toField(),
                        UserFields.PASSWORD.toField(),
                        UserFields.UPDATED_AT.toField(),
                        EmployeeFields.NAME.toField(),
                        EmployeeFields.EMPLOYEE_NO.toField(),
                        EmployeeFields.SALARY.toField(),
                        EmployeeFields.BRANCH_ID.toField(),
                        EmployeeFields.IS_ACTIVE.toField()
                )
                .fetchOne();

//        if(record == null) return null;

        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("username", employee.getUsername());
        fieldValues.put("email", employee.getEmail());
        fieldValues.put("role", employee.getRole().toString());
        fieldValues.put("name", employee.getName());
        fieldValues.put("employee_no", employee.getEmployeeNo());
        fieldValues.put("salary", employee.getSalary());
        fieldValues.put("branch_id", employee.getBranchId());
        fieldValues.put("is_active", employee.isActive());

        try {
            syncService.trackChange(
                    "employees",
                    employee.getId().intValue(),
                    "update",
                    new ObjectMapper().writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }

        return mapToEmployee(record);
    }

    private Employee mapToEmployee(Record record) {
        if(record == null) return null;

        return Employee.builder()
                .id(record.get(UserFields.ID.toField(), Long.class))
                .username(record.get(UserFields.USERNAME.toField(), String.class))
                .email(record.get(UserFields.EMAIL.toField(), String.class))
                .password(record.get(UserFields.PASSWORD.toField(), String.class))
                .role(UserRole.valueOf(record.get(UserFields.ROLE.toField(), String.class)))
                .name(record.get(EmployeeFields.NAME.toField(), String.class))
                .employeeNo(record.get(EmployeeFields.EMPLOYEE_NO.toField(), String.class))
                .isActive(record.get(EmployeeFields.IS_ACTIVE.toField(), Boolean.class))
                .isFirstTime(record.get(EmployeeFields.IS_FIRST_TIME.toField(), Boolean.class))
                .salary(record.get(EmployeeFields.SALARY.toField(), BigDecimal.class))
                .branchId(record.get(EmployeeFields.BRANCH_ID.toField(), Long.class))
//                .createdAt(record.get(UserFields.CREATED_AT.toField(), LocalDateTime.class))
//                .updatedAt(record.get(UserFields.UPDATED_AT.toField(), LocalDateTime.class))
                .build();
    }

}
