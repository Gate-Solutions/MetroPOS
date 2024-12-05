package org.gate.metropos.services;

import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.SuperAdmin;
import org.gate.metropos.repositories.SuperAdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SuperAdminServiceTest {

    @Mock
    private SuperAdminRepository superAdminRepository;
    @InjectMocks
    private SuperAdminService superAdminService;

    private final String mockEmail = "admin@test.com";
    private final String mockUsername = "admin";
    private final String mockPassword = "password";
    private final String wrongEmail = "wrong@test.com";
    private final String wrongUsername = "wrong";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SuperAdmin mockAdmin = SuperAdmin.builder()
                .email(mockEmail)
                .password(mockPassword)
                .role(UserRole.SUPER_ADMIN)
                .username(mockUsername)
                .build();

        when(superAdminRepository.findByEmail(mockEmail)).thenReturn(mockAdmin);
        when(superAdminRepository.findByUsername(mockUsername)).thenReturn(mockAdmin);
        when(superAdminRepository.findByEmail(wrongEmail)).thenReturn(null);
        when(superAdminRepository.findByUsername(wrongUsername)).thenReturn(null);
    }

    @Test
    void loginWithEmailSuccessful() {
        String email = mockEmail;

        SuperAdmin result = superAdminService.login(email, mockPassword);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertTrue(superAdminService.isLoggedIn());
    }


    @Test
    void loginWithEmailIncorrectPassword() {
        String email = mockEmail;
        String wrongPassword = "wrongPassword";

        SuperAdmin result = superAdminService.login(email, wrongPassword);

        assertNull(result);
        assertFalse(superAdminService.isLoggedIn());
    }

    @Test
    void loginWithUsernameSuccessful() {
        String username = mockUsername;

        SuperAdmin result = superAdminService.login(username, mockPassword);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertTrue(superAdminService.isLoggedIn());
    }

    @Test
    void loginWithUsernameIncorrectPassword() {
        String username = mockUsername;
        String wrongPassword = "wrongPassword";

        SuperAdmin result = superAdminService.login(username, wrongPassword);

        assertNull(result);
        assertFalse(superAdminService.isLoggedIn());
    }

    @Test
    void loginWithNonExistentEmail() {

        SuperAdmin result = superAdminService.login(wrongEmail, mockPassword);

        assertNull(result);
        assertFalse(superAdminService.isLoggedIn());
    }

    @Test
    void loginWithNonExistentUsername() {

        SuperAdmin result = superAdminService.login(wrongUsername, mockPassword);

        assertNull(result);
        assertFalse(superAdminService.isLoggedIn());
    }

    @Test
    void logoutSuccessful() {
//        First Login
        superAdminService.login(mockUsername, mockPassword);
        assertTrue(superAdminService.isLoggedIn());
//        Then test
        superAdminService.logout();
        assertFalse(superAdminService.isLoggedIn());
        assertNull(superAdminService.getSuperAdmin());
    }
}