package org.gate.metropos.repositories;

import org.gate.metropos.enums.*;
import org.gate.metropos.models.Employee;
import org.jooq.Record;
import org.jooq.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmployeeRepositoryTest {

    @Mock
    private DSLContext dsl;
    @Mock
    private Record record;
    @Mock
    private Result<Record> records;
    @Mock
    private SelectSelectStep<Record> selectStep;
    @Mock
    private SelectJoinStep<Record> fromStep;
    @Mock
    private SelectConditionStep<Record> conditionStep;
    @Mock
    private InsertSetStep<Record> insertStep;
    @Mock
    private InsertSetMoreStep<Record> insertSetMoreStep;
    @Mock
    private InsertResultStep<Record> insertResultStep;
    @Mock
    private UpdateSetFirstStep<Record> updateSetFirstStep;
    @Mock
    private UpdateSetMoreStep<Record> updateSetMoreStep;
    @Mock
    private UpdateConditionStep<Record> updateConditionStep;
    @Mock
    private UpdateResultStep<Record> updateResultStep;
    @InjectMocks
    private EmployeeRepository repository;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

//        Mock Select Query Chain
        when(dsl.select()).thenReturn(selectStep);
        when(selectStep.from((Table<?>) any())).thenReturn(fromStep);
        when(fromStep.where(any(Condition.class))).thenReturn(conditionStep);
        when(conditionStep.fetchOne()).thenReturn(record);

//        Mock Insertion
        when(dsl.insertInto(any(Table.class))).thenReturn(insertStep);
        when(insertStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.returning()).thenReturn(insertResultStep);
        when(insertResultStep.fetchOne()).thenReturn(record);

//        Mock Updation
        when(dsl.update(any(Table.class))).thenReturn(updateSetFirstStep);
        when(updateSetFirstStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(updateSetMoreStep);
        when(updateSetMoreStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(updateSetMoreStep);
        when(updateSetMoreStep.where(any(Condition.class))).thenReturn(updateConditionStep);
        when(updateConditionStep.returning()).thenReturn(updateResultStep);
        when(updateResultStep.fetchOne()).thenReturn(record);

        initializeMockEmployee();

//        Mock Record Values
        when(record.get(UserFields.ID.toField(), Long.class)).thenReturn(mockEmployee.getId());
        when(record.get(UserFields.USERNAME.toField(), String.class)).thenReturn(mockEmployee.getUsername());
        when(record.get(UserFields.EMAIL.toField(), String.class)).thenReturn(mockEmployee.getEmail());
        when(record.get(UserFields.PASSWORD.toField(), String.class)).thenReturn(mockEmployee.getPassword());
        when(record.get(UserFields.ROLE.toField(), String.class)).thenReturn(mockEmployee.getRole().name());
        when(record.get(EmployeeFields.NAME.toField(), String.class)).thenReturn(mockEmployee.getName());
        when(record.get(EmployeeFields.EMPLOYEE_NO.toField(), String.class)).thenReturn(mockEmployee.getEmployeeNo());
        when(record.get(EmployeeFields.IS_ACTIVE.toField(), Boolean.class)).thenReturn(mockEmployee.isActive());
        when(record.get(EmployeeFields.IS_FIRST_TIME.toField(), Boolean.class)).thenReturn(mockEmployee.isFirstTime());
        when(record.get(EmployeeFields.SALARY.toField(), BigDecimal.class)).thenReturn(mockEmployee.getSalary());
        when(record.get(EmployeeFields.BRANCH_ID.toField(), Long.class)).thenReturn(mockEmployee.getBranchId());
    }

    @Test
    void findByEmailWhenRecordExists() {
        String email = mockEmployee.getEmail();
        String name = mockEmployee.getName();
        String employeeNo = mockEmployee.getEmployeeNo();

        Employee result = repository.findByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getName());
        assertEquals(employeeNo, result.getEmployeeNo());
        assertEquals(mockEmployee.getRole(), result.getRole());
        assertNotEquals(UserRole.SUPER_ADMIN, result.getRole());
    }

    @Test
    void findByEmailWhenRecordDoesNotExist() {
        when(conditionStep.fetchOne()).thenReturn(null);

        Employee result = repository.findByEmail("nonexistent@test.com");

        assertNull(result);
    }

    @Test
    void findByEmployeeNoWhenRecordExists() {
        String employeeNo = mockEmployee.getEmployeeNo();
        String employeeName = mockEmployee.getName();

        Employee result = repository.findByEmployeeNo(employeeNo);

        assertNotNull(result);
        assertEquals(employeeNo, result.getEmployeeNo());
        assertEquals(employeeName, result.getName());
        assertTrue(result.isActive());
    }

    @Test
    void getAllEmployeesByRole() {
        when(conditionStep.fetch()).thenReturn(records);
        when(records.map(any())).thenReturn(Arrays.asList(
                createTestEmployee(1L, "emp1"),
                createTestEmployee(2L, "emp2")
        ));

        List<Employee> results = repository.getAllEmployeesByRole(UserRole.CASHIER);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void addEmployee() {
        Employee employeeToAdd = createTestEmployee(1L, "john");

        Employee result = repository.addEmployee(employeeToAdd);

        assertNotNull(result);
        assertEquals(employeeToAdd.getEmployeeNo(), result.getEmployeeNo());
    }

    @Test
    void updateEmployee() {
        Employee employeeToUpdate = createTestEmployee(1L, "updatedEmp");

        when(record.get(UserFields.USERNAME.toField(), String.class)).thenReturn("updatedEmp_username");
        Employee result = repository.updateEmployee(employeeToUpdate);

        assertNotNull(result);
        assertEquals(employeeToUpdate.getId(), result.getId());
        assertEquals(employeeToUpdate.getUsername(), result.getUsername());
    }

    private Employee createTestEmployee(Long id, String prefix) {
        return Employee.builder()
                .id(id)
                .username(prefix + "_username")
                .email(prefix + "@test.com")
                .password("password")
                .role(UserRole.CASHIER)
                .name(prefix + " Name")
                .employeeNo("EMP00" + id)
                .isActive(true)
                .isFirstTime(true)
                .salary(new BigDecimal("50000.00"))
                .branchId(1L)
                .build();
    }

    private void initializeMockEmployee() {
        this.mockEmployee = createTestEmployee(1L, "john");
    }
}
