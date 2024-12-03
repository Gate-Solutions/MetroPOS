package org.gate.metropos.services;

import org.gate.metropos.models.Branch;
import org.gate.metropos.repositories.BranchRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    private Branch mockBranch;
    private final String mockBranchCode = "BR001";
    private final String mockName = "Test Branch";
    private final String mockCity = "Test City";
    private final String mockAddress = "Test Address";
    private final String mockPhone = "1234567890";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockBranch = Branch.builder()
                .id(1L)
                .branchCode(mockBranchCode)
                .name(mockName)
                .city(mockCity)
                .address(mockAddress)
                .phone(mockPhone)
                .isActive(true)
                .numberOfEmployees(0)
                .build();

        when(branchRepository.findById(1L)).thenReturn(mockBranch);
        when(branchRepository.findByBranchCode(mockBranchCode)).thenReturn(mockBranch);
        when(branchRepository.createBranch(any(Branch.class))).thenReturn(mockBranch);
        when(branchRepository.updateBranch(any(Branch.class))).thenReturn(mockBranch);
        when(branchRepository.getAllBranches()).thenReturn(Arrays.asList(mockBranch));
    }

    @Test
    void createBranchSuccessful() {
        when(branchRepository.findByBranchCode(any())).thenReturn(null);
        when(branchRepository.createBranch(
                mockBranchCode, mockName, mockCity, mockAddress, mockPhone))
                .thenReturn(mockBranch);

        ServiceResponse<Branch> response = branchService.createBranch(
                mockBranchCode, mockName, mockCity, mockAddress, mockPhone);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void createBranchWithExistingCode() {
        ServiceResponse<Branch> response = branchService.createBranch(
                mockBranchCode, mockName, mockCity, mockAddress, mockPhone);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Branch code already exists", response.getMessage());
    }

    @Test
    void updateBranchSuccessful() {
        when(branchRepository.findByBranchCode(mockBranchCode)).thenReturn(null);

        ServiceResponse<Branch> response = branchService.updateBranch(mockBranch);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void updateNonExistentBranch() {
        when(branchRepository.findById(any())).thenReturn(null);

        ServiceResponse<Branch> response = branchService.updateBranch(mockBranch);

        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertEquals("Branch not found", response.getMessage());
    }

    @Test
    void updateBranchWithActiveEmployees() {
        mockBranch.setNumberOfEmployees(5);
        mockBranch.setActive(false);

        ServiceResponse<Branch> response = branchService.updateBranch(mockBranch);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Cannot deactivate branch with active employees", response.getMessage());
    }

    @Test
    void getBranchSuccessful() {
        ServiceResponse<Branch> response = branchService.getBranch(1L);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    void getBranchNotFound() {
        when(branchRepository.findById(999L)).thenReturn(null);

        ServiceResponse<Branch> response = branchService.getBranch(999L);

        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertEquals("Branch not found", response.getMessage());
    }

    @Test
    void getAllBranchesSuccessful() {
        ServiceResponse<List<Branch>> response = branchService.getAllBranches();

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void setBranchStatusSuccessful() {
        ServiceResponse<Void> response = branchService.setBranchStatus(1L, false);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        verify(branchRepository).setBranchStatus(1L, false);
    }

    @Test
    void setBranchStatusWithActiveEmployees() {
        mockBranch.setNumberOfEmployees(5);

        ServiceResponse<Void> response = branchService.setBranchStatus(1L, false);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Cannot deactivate branch with active employees", response.getMessage());
    }

    @Test
    void validateBranchDataWithInvalidPhone() {
        mockBranch.setPhone("invalid");

        when(branchRepository.findByBranchCode(mockBranch.getBranchCode())).thenReturn(null);
        ServiceResponse<Branch> response = branchService.createBranch(mockBranch);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Invalid phone number format", response.getMessage());
    }

    @Test
    void validateBranchDataWithEmptyFields() {
        mockBranch.setName("");

        when(branchRepository.findByBranchCode(mockBranch.getBranchCode())).thenReturn(null);

        ServiceResponse<Branch> response = branchService.createBranch(mockBranch);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Branch name cannot be empty", response.getMessage());
    }
}
