package org.gate.metropos.repositories;

import org.gate.metropos.enums.UserFields;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.SuperAdmin;
import org.jooq.Record;
import org.jooq.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;


class SuperAdminRepositoryTest {

    @Mock
    private DSLContext dsl;

    @Mock
    private Record record;

    @Mock
    private SelectSelectStep<Record> selectStep;

    @Mock
    private SelectJoinStep<Record> fromStep;

    @Mock
    private SelectConditionStep<Record> conditionStep;

    @InjectMocks
    private SuperAdminRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // Mock the query chain
        when(dsl.select()).thenReturn(selectStep);
        when(selectStep.from((Table<?>) any())).thenReturn(fromStep);
        when(fromStep.where(any(Condition.class))).thenReturn(conditionStep);
        when(conditionStep.fetchOne()).thenReturn(record);
        // Mock record values
        when(record.get(UserFields.ID.toField(), Long.class)).thenReturn(1L);
        when(record.get(UserFields.USERNAME.toField(), String.class)).thenReturn("admin");
        when(record.get(UserFields.EMAIL.toField(), String.class)).thenReturn("admin@test.com");
        when(record.get(UserFields.PASSWORD.toField(), String.class)).thenReturn("test_password");
        when(record.get(UserFields.ROLE.toField(), String.class)).thenReturn(UserRole.SUPER_ADMIN.toString());
    }

    @Test
    void findByEmailWhenRecordExists() {
        String email = "admin@test.com";

        SuperAdmin result = repository.findByEmail(email);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(email, result.getEmail());
        assertEquals("test_password", result.getPassword());
        assertEquals(UserRole.SUPER_ADMIN, result.getRole());
    }


    @Test
    void findByEmailWhenRecordDoesNotExist() {
        when(conditionStep.fetchOne()).thenReturn(null);

        String email = "nonexistent@test.com";
        SuperAdmin result = repository.findByEmail(email);

        assertNull(result);
    }

    @Test
    void findByUsernameWhenRecordExists() {
        String username = "admin";
        SuperAdmin result = repository.findByUsername(username);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(username, result.getUsername());
        assertEquals("test_password", result.getPassword());
        assertEquals(UserRole.SUPER_ADMIN, result.getRole());
    }

    @Test
    void findByUsernameWhenRecordDoesNotExist() {
        when(conditionStep.fetchOne()).thenReturn(null);

        String username = "nonexistent";
        SuperAdmin result = repository.findByUsername(username);

        assertNull(result);
    }

}
