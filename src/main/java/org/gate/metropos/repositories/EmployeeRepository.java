package org.gate.metropos.repositories;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.EmployeeFields;
import org.gate.metropos.enums.UserFields;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
public class EmployeeRepository {
    private DSLContext dsl;

    public EmployeeRepository () {
        dsl = DatabaseConfig.getLocalDSL();
    }
    public Employee findByEmail(String email) {
        Record record = dsl.select()
                .from(EmployeeFields.EmployeeTable.toTableField())
                .where(UserFields.EMAIL.toField().eq(email))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public Employee findByUsername(String username) {
        Record record = dsl.select()
                .from(EmployeeFields.EmployeeTable.toTableField())
                .where(UserFields.USERNAME.toField().eq(username))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public Employee findByEmployeeNo(String employeeNo) {
        Record record = dsl.select()
                .from(EmployeeFields.EmployeeTable.toTableField())
                .where(EmployeeFields.EMPLOYEE_NO.toField().eq(employeeNo))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public Employee addEmployee(Employee employee) {
        Record record = dsl.insertInto(EmployeeFields.EmployeeTable.toTableField())
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
                .returning()
                .fetchOne();

        return mapToEmployee(record);
    }

    public Employee getEmployee(Long id) {
        Record record = dsl.select()
                .from(EmployeeFields.EmployeeTable.toTableField())
                .where(UserFields.ID.toField().eq(id))
                .fetchOne();

        if(record == null) return null;
        return mapToEmployee(record);
    }

    public List<Employee> getAllEmployeesByRole(UserRole role) {
        Result<Record> records = dsl.select()
                .from(EmployeeFields.EmployeeTable.toTableField())
                .where(UserFields.ROLE.toField().eq(role.toString()))
                .fetch();

        return records.map(this::mapToEmployee);
    }

    public List<Employee> getAllEmployees() {
        Result<Record> records = dsl.select()
                .from(EmployeeFields.EmployeeTable.toTableField())
                .fetch();

        return records.map(this::mapToEmployee);
    }

    public void setEmployeeStatus(Long employeeId, boolean isActive) {
        dsl.update(EmployeeFields.EmployeeTable.toTableField())
                .set(EmployeeFields.IS_ACTIVE.toField(), isActive)
                .set(UserFields.UPDATED_AT.toField(), LocalDateTime.now())
                .where(UserFields.ID.toField().eq(employeeId))
                .execute();
    }

    public void updatePassword(Long employeeId, String newPassword) {
        dsl.update(EmployeeFields.EmployeeTable.toTableField())
                .set(UserFields.PASSWORD.toField(), newPassword)
                .set(EmployeeFields.IS_FIRST_TIME.toField(), false)
                .set(UserFields.UPDATED_AT.toField(), LocalDateTime.now())
                .where(UserFields.ID.toField().eq(employeeId))
                .execute();
    }

    public Employee updateEmployee(Employee employee) {
        Record record = dsl.update(EmployeeFields.EmployeeTable.toTableField())
                .set(UserFields.USERNAME.toField(), employee.getUsername())
                .set(UserFields.EMAIL.toField(), employee.getEmail())
                .set(UserFields.ROLE.toField(), employee.getRole().toString())
                .set(UserFields.UPDATED_AT.toField(), LocalDateTime.now())
                .set(EmployeeFields.NAME.toField(), employee.getName())
                .set(EmployeeFields.EMPLOYEE_NO.toField(), employee.getEmployeeNo())
                .set(EmployeeFields.SALARY.toField(), employee.getSalary())
                .set(EmployeeFields.BRANCH_ID.toField(), employee.getBranchId())
                .where(UserFields.ID.toField().eq(employee.getId()))
                .returning()
                .fetchOne();

        if(record == null) return null;
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
