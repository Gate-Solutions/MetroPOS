package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import org.gate.metropos.models.Supplier;
import org.gate.metropos.repositories.SupplierRepository;
import org.gate.metropos.utils.ServiceResponse;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService() {
        this.supplierRepository = new SupplierRepository();
    }

    public ServiceResponse<Supplier> createSupplier(Supplier supplier) {
        if (supplier.getEmail() != null && supplierRepository.findByEmail(supplier.getEmail()) != null) {
            return new ServiceResponse<>(false, 400, "Email already exists", null);
        }

        try {
            validateSupplierData(supplier);
        } catch (IllegalArgumentException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        }

        Supplier newSupplier = supplierRepository.createSupplier(supplier);
        return new ServiceResponse<>(true, 200, "Supplier created successfully", newSupplier);
    }

    public ServiceResponse<Supplier> updateSupplier(Supplier supplier) {
        Supplier existingSupplier = supplierRepository.findById(supplier.getId());
        if (existingSupplier == null) {
            return new ServiceResponse<>(false, 404, "Supplier not found", null);
        }

        if (supplier.getEmail() != null && !supplier.getEmail().equals(existingSupplier.getEmail())
                && supplierRepository.findByEmail(supplier.getEmail()) != null) {
            return new ServiceResponse<>(false, 400, "Email already exists", null);
        }

        try {
            validateSupplierData(supplier);
        } catch (IllegalArgumentException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        }

        Supplier updatedSupplier = supplierRepository.updateSupplier(supplier);
        return new ServiceResponse<>(true, 200, "Supplier updated successfully", updatedSupplier);
    }

    public ServiceResponse<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.getAllSuppliers();
        return new ServiceResponse<>(true, 200, "Suppliers retrieved successfully", suppliers);
    }

    public ServiceResponse<Supplier> getSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id);
        if (supplier == null) {
            return new ServiceResponse<>(false, 404, "Supplier not found", null);
        }
        return new ServiceResponse<>(true, 200, "Supplier retrieved successfully", supplier);
    }

    public ServiceResponse<Void> setSupplierStatus(Long id, boolean isActive) {
        Supplier supplier = supplierRepository.findById(id);
        if (supplier == null) {
            return new ServiceResponse<>(false, 404, "Supplier not found", null);
        }

        supplierRepository.setSupplierStatus(id, isActive);
        String status = isActive ? "activated" : "deactivated";
        return new ServiceResponse<>(true, 200, "Supplier " + status + " successfully", null);
    }

    private void validateSupplierData(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be empty");
        }
        if (supplier.getPhone() == null || !supplier.getPhone().matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (supplier.getEmail() != null && !supplier.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }




    }





