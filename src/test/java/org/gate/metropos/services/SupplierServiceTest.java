package org.gate.metropos.services;

import org.gate.metropos.models.Supplier;
import org.gate.metropos.repositories.SupplierRepository;
import org.gate.metropos.utils.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SupplierServiceTest {
    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier mockSupplier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockSupplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .email("supplier@test.com")
                .phone("1234567890")
                .ntnNumber("NTN123")
                .isActive(true)
                .build();

        when(supplierRepository.findById(1L)).thenReturn(mockSupplier);
        when(supplierRepository.createSupplier(any(Supplier.class))).thenReturn(mockSupplier);
        when(supplierRepository.updateSupplier(any(Supplier.class))).thenReturn(mockSupplier);
    }

    @Test
    void createSupplierSuccessful() {
        when(supplierRepository.findByEmail(any())).thenReturn(null);

        ServiceResponse<Supplier> response = supplierService.createSupplier(mockSupplier);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void createSupplierWithExistingEmail() {
        when(supplierRepository.findByEmail(mockSupplier.getEmail())).thenReturn(mockSupplier);

        ServiceResponse<Supplier> response = supplierService.createSupplier(mockSupplier);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Email already exists", response.getMessage());
    }

    @Test
    void createSupplierWithInvalidData() {
        mockSupplier.setPhone("invalid");

        ServiceResponse<Supplier> response = supplierService.createSupplier(mockSupplier);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Invalid phone number format", response.getMessage());
    }

    @Test
    void updateSupplierSuccessful() {
        when(supplierRepository.findByEmail(any())).thenReturn(null);

        ServiceResponse<Supplier> response = supplierService.updateSupplier(mockSupplier);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void updateNonExistentSupplier() {
        when(supplierRepository.findById(999L)).thenReturn(null);
        mockSupplier.setId(999L);

        ServiceResponse<Supplier> response = supplierService.updateSupplier(mockSupplier);

        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
    }

    @Test
    void getAllSuppliersSuccessful() {
        when(supplierRepository.getAllSuppliers()).thenReturn(Arrays.asList(mockSupplier));

        ServiceResponse<List<Supplier>> response = supplierService.getAllSuppliers();

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
    }

    @Test
    void getSupplierSuccessful() {
        ServiceResponse<Supplier> response = supplierService.getSupplier(1L);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void getSupplierNotFound() {
        when(supplierRepository.findById(999L)).thenReturn(null);

        ServiceResponse<Supplier> response = supplierService.getSupplier(999L);

        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
    }

    @Test
    void setSupplierStatusSuccessful() {
        ServiceResponse<Void> response = supplierService.setSupplierStatus(1L, false);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        verify(supplierRepository).setSupplierStatus(1L, false);
    }

    @Test
    void validateSupplierWithInvalidEmail() {
        mockSupplier.setEmail("invalid-email");

        ServiceResponse<Supplier> response = supplierService.createSupplier(mockSupplier);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Invalid email format", response.getMessage());
    }
}
