package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.repositories.BranchRepository;
import org.gate.metropos.repositories.EmployeeRepository;
import org.gate.metropos.utils.PasswordEncoder;
import org.gate.metropos.utils.ServiceResponse;

import java.math.BigDecimal;
import java.util.List;


@AllArgsConstructor
public class EmployeeService {
    private EmployeeRepository employeeRepository;
    private BranchService branchService;
    private PasswordEncoder passwordEncoder;
    @Getter
    private static Employee loggedInEmployee = null;


    public EmployeeService() {
        employeeRepository = new EmployeeRepository();
        branchService = new BranchService();
        passwordEncoder = new PasswordEncoder();
    }

    public ServiceResponse<Employee> createEmployee(Employee employee) {
        if (employeeRepository.findByEmail(employee.getEmail()) != null) {
            return new ServiceResponse<>(false, 400, "Email already exists", null);
        }
        if (employeeRepository.findByUsername(employee.getUsername()) != null) {
            return new ServiceResponse<>(false, 400, "Username already exists", null);
        }
        if (employeeRepository.findByEmployeeNo(employee.getEmployeeNo()) != null) {
            return new ServiceResponse<>(false, 400, "Employee number already exists", null);
        }
        if (employee.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            return new ServiceResponse<>(false, 400, "Salary must be greater than zero", null);
        }
        if(employee.getBranchId() == null) {
            return new ServiceResponse<>(false, 400, "Branch id is required", null);
        }
        ServiceResponse<Branch> resp = branchService.getBranch(employee.getBranchId());
        if(!resp.isSuccess()) {
            return new ServiceResponse<>(false, 400, "Branch not found", null);
        }
        employee.setPassword(passwordEncoder.encode("password"));
        employee.setFirstTime(true);
        employee.setActive(true);
        Employee savedEmployee = employeeRepository.addEmployee(employee);
        branchService.incrementBranchEmployeeCount(employee.getBranchId());
        return new ServiceResponse<>(true, 200, "Employee created successfully", savedEmployee);
    }

    public ServiceResponse<Employee> createEmployee(
            String username,
            String email,
            String password,
            UserRole role,
            String name,
            String employeeNo,
            BigDecimal salary,
            Long branchId
    ) {
        Employee newEmployee = Employee.builder()
                .username(username)
                .email(email.toLowerCase())
                .password(password)
                .role(role)
                .name(name)
                .employeeNo(employeeNo)
                .salary(salary)
                .branchId(branchId)
                .build();

        return createEmployee(newEmployee);
    }



    public ServiceResponse<Employee> updateEmployee(Employee employee) {
        Employee existingEmployee = employeeRepository.getEmployee(employee.getId());

        if (!existingEmployee.getEmail().equals(employee.getEmail())
                && employeeRepository.findByEmail(employee.getEmail()) != null) {
            return new ServiceResponse<>(false, 400, "Email already exists", null);
        }
        if (!existingEmployee.getUsername().equals(employee.getUsername())
                && employeeRepository.findByUsername(employee.getUsername()) != null) {
            return new ServiceResponse<>(false, 400, "Username already exists", null);
        }
        if (!existingEmployee.getEmployeeNo().equals(employee.getEmployeeNo())
                && employeeRepository.findByEmployeeNo(employee.getEmployeeNo()) != null) {
            return new ServiceResponse<>(false, 400, "Employee number already exists", null);
        }
        boolean isExistingEmployeeActive = existingEmployee.isActive();
//        if employee role is manager and branch already has a manager than do not activate employee

        if(employee.getRole() == UserRole.BRANCH_MANAGER && isExistingEmployeeActive != employee.isActive()) {
            if(employee.isActive()) {
                List<Employee> employees = employeeRepository.getEmployeesByBranchAndRole(employee.getBranchId(), UserRole.BRANCH_MANAGER);
                if (!employees.isEmpty())
                    return new ServiceResponse<>(false, 400, "Manager already exists for this branch", null);
            }
        }

        Employee updatedEmployee = employeeRepository.updateEmployee(employee);
        if (isExistingEmployeeActive != employee.isActive()) {
            if(employee.isActive())
                branchService.incrementBranchEmployeeCount(employee.getBranchId());
            else
                branchService.decrementBranchEmployeeCount(employee.getBranchId());
        }
        return new ServiceResponse<>(true, 200, "Employee updated successfully", updatedEmployee);
    }

    public ServiceResponse<Employee> getEmployee(Long id) {
        Employee employee = employeeRepository.getEmployee(id);
        if (employee == null) {
            return new ServiceResponse<>(false, 404, "Employee not found", null);
        }
        return new ServiceResponse<>(true, 200, "Employee retrieved successfully", employee);
    }

    public ServiceResponse<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeRepository.getAllEmployees();
        return new ServiceResponse<>(true, 200, "Employees retrieved successfully", employees);
    }

    public ServiceResponse<List<Employee>> getEmployeesByRole(UserRole role) {
        List<Employee> employees = employeeRepository.getAllEmployeesByRole(role);


        return new ServiceResponse<>(true, 200, "Employees retrieved successfully", employees);
    }


    public ServiceResponse<Void> setEmployeeStatus(Long id, boolean isActive) {
        Employee employee = employeeRepository.getEmployee(id);
        if (employee == null) {
            return new ServiceResponse<>(false, 404, "Employee not found", null);
        }
        employeeRepository.setEmployeeStatus(id, isActive);
        if(isActive != employee.isActive()) {
            if(isActive)
                branchService.incrementBranchEmployeeCount(employee.getBranchId());
            else
                branchService.decrementBranchEmployeeCount(employee.getBranchId());
        }
        String status = isActive ? "activated" : "deactivated";
        return new ServiceResponse<>(true, 200, "Employee " + status + " successfully", null);
    }

    public ServiceResponse<Void> updatePassword(Long id, String oldPassword, String newPassword) {
        Employee employee = employeeRepository.getEmployee(id);
        if (employee == null) {
            return new ServiceResponse<>(false, 404, "Employee not found", null);
        }
        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            return new ServiceResponse<>(false, 400, "Current password is incorrect", null);
        }
        employeeRepository.updatePassword(id, passwordEncoder.encode(newPassword));
        return new ServiceResponse<>(true, 200, "Password updated successfully", null);
    }

    public ServiceResponse<Employee> login(String emailOrUsername, String password) {
        Employee employee;
        boolean isEmail = emailOrUsername.contains("@");

        if(isEmail) {
            employee = employeeRepository.findByEmail(emailOrUsername.toLowerCase());
        } else {
            employee = employeeRepository.findByUsername(emailOrUsername.toLowerCase());
        }

        if(employee == null) {
            return new ServiceResponse<>(false, 404, "Employee not found", null);
        }

        if(!employee.isActive()) {
            return new ServiceResponse<>(false, 403, "Account is inactive", null);
        }

        if(!passwordEncoder.matches(password, employee.getPassword())) {
            System.out.println("Req pass: " + password);
            System.out.println("Act pass: " + employee.getPassword());
            return new ServiceResponse<>(false, 401, "Invalid credentials", null);
        }

        this.loggedInEmployee = employee;
        return new ServiceResponse<>(true, 200, "Login successful", employee);
    }

    public ServiceResponse<Void> logout() {
        this.loggedInEmployee = null;
        return new ServiceResponse<>(true, 200, "Logout successful", null);
    }

    public ServiceResponse<Boolean> isFirstTimeLogin() {
        if (loggedInEmployee == null) {
            return new ServiceResponse<>(false, 401, "No employee logged in", null);
        }
        return new ServiceResponse<>(true, 200, "First time status retrieved", loggedInEmployee.isFirstTime());
    }


}