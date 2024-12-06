package org.gate.metropos.services;

import static org.junit.jupiter.api.Assertions.*;



import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.gate.metropos.repositories.EmployeeRepository;
import org.gate.metropos.utils.PasswordEncoder;
import org.gate.metropos.utils.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee mockEmployee;
    private final String mockEmail = "employee@test.com";
    private final String mockUsername = "employee1";
    private final String mockPassword = "password123";
    private final String mockEmployeeNo = "EMP001";
    private final List<Employee> employeeList = Arrays.asList(mockEmployee);
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockEmployee = Employee.builder()
                .id(1L)
                .email(mockEmail)
                .username(mockUsername)
                .password(mockPassword)
                .employeeNo(mockEmployeeNo)
                .role(UserRole.CASHIER)
                .name("Test Employee")
                .salary(new BigDecimal("5000"))
                .branchId(1L)
                .isActive(true)
                .isFirstTime(true)
                .build();

        when(employeeRepository.findByEmail(mockEmail)).thenReturn(mockEmployee);
        when(employeeRepository.getEmployee(1L)).thenReturn(mockEmployee);
        when(employeeRepository.findByUsername(mockUsername)).thenReturn(mockEmployee);
        when(employeeRepository.findByEmployeeNo(mockEmployeeNo)).thenReturn(mockEmployee);
        when(passwordEncoder.matches(mockPassword, mockPassword)).thenReturn(true);
        when(passwordEncoder.encode(mockPassword)).thenReturn(mockPassword);
        when(employeeRepository.getAllEmployees()).thenReturn(employeeList);
        when(employeeRepository.addEmployee(mockEmployee)).thenReturn(mockEmployee);

    }

    @Test
    void createEmployeeSuccessful() {


        when(employeeRepository.findByEmail(any())).thenReturn(null);
        when(employeeRepository.findByUsername(any())).thenReturn(null);
        when(employeeRepository.findByEmployeeNo(any())).thenReturn(null);
        when(employeeRepository.addEmployee(any())).thenReturn(mockEmployee);

        ServiceResponse<Employee> response = employeeService.createEmployee(mockEmployee);


        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void createEmployeeWithExistingEmail() {
        ServiceResponse<Employee> response = employeeService.createEmployee(mockEmployee);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Email already exists", response.getMessage());
    }

    @Test
    void loginWithEmailSuccessful() {
        ServiceResponse<Employee> response = employeeService.login(mockEmail, mockPassword);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(mockEmail, response.getData().getEmail());
    }

    @Test
    void loginWithUsernameSuccessful() {
        ServiceResponse<Employee> response = employeeService.login(mockUsername, mockPassword);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(mockUsername, response.getData().getUsername());
    }

    @Test
    void loginWithInactiveAccount() {
        mockEmployee.setActive(false);
        ServiceResponse<Employee> response = employeeService.login(mockEmail, mockPassword);

        assertFalse(response.isSuccess());
        assertEquals(403, response.getCode());
        assertEquals("Account is inactive", response.getMessage());
    }

    @Test
    void getAllEmployeesSuccessful() {



        ServiceResponse<List<Employee>> response = employeeService.getAllEmployees();

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void updatePasswordSuccessful() {


        ServiceResponse<Void> response = employeeService.updatePassword(1L, mockPassword, "newPassword");

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        verify(employeeRepository).updatePassword(eq(1L), any());
    }

    @Test
    void logoutSuccessful() {
        // First login
        employeeService.login(mockEmail, mockPassword);
        assertNotNull(employeeService.getLoggedInEmployee());

        // Then logout
        ServiceResponse<Void> response = employeeService.logout();

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNull(employeeService.getLoggedInEmployee());
    }

    @Test
    void setEmployeeStatusSuccessful() {


        ServiceResponse<Void> response = employeeService.setEmployeeStatus(1L, false);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        verify(employeeRepository).setEmployeeStatus(1L, false);
    }

    @Test
    void isFirstTimeLoginWhenLoggedIn() {
        employeeService.login(mockEmail, mockPassword);

        ServiceResponse<Boolean> response = employeeService.isFirstTimeLogin();

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }

    @Test
    void isFirstTimeLoginWhenNotLoggedIn() {
        ServiceResponse<Boolean> response = employeeService.isFirstTimeLogin();

        assertFalse(response.isSuccess());
        assertEquals(401, response.getCode());
        assertEquals("No employee logged in", response.getMessage());
    }
}
