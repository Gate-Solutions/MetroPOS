package org.gate.metropos.utils;

import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.User;

public class SessionManager {
    private static Employee currentEmployee;
    private static User currentSuperAdmin;
    private static UserRole currentUserRole;

    public static void initEmployeeSession(Employee employee) {
        currentEmployee = employee;
        currentUserRole = employee.getRole();
        currentSuperAdmin = null;
    }

    public static void initSuperAdminSession(User superAdmin) {
        currentSuperAdmin = superAdmin;
        currentUserRole = UserRole.SUPER_ADMIN;
        currentEmployee = null;
    }

    public static Employee getCurrentEmployee() {
        if (currentEmployee == null) {
            throw new IllegalStateException("No employee session found");
        }
        return currentEmployee;
    }

    public static User getCurrentSuperAdmin() {
        if (currentSuperAdmin == null) {
            throw new IllegalStateException("No super admin session found");
        }
        return currentSuperAdmin;
    }

    public static UserRole getCurrentUserRole() {
        if (currentUserRole == null) {
            throw new IllegalStateException("No active session found");
        }
        return currentUserRole;
    }

    public static void clearSession() {
        currentEmployee = null;
        currentSuperAdmin = null;
        currentUserRole = null;
    }
    public static boolean isSuperAdmin() {
        return currentUserRole == UserRole.SUPER_ADMIN;
    }

    public static boolean hasActiveSession() {
        return currentEmployee != null || currentSuperAdmin != null;
    }
}
