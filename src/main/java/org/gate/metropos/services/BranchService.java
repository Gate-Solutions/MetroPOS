package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.repositories.BranchRepository;
import org.gate.metropos.utils.ServiceResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor

public class BranchService {
    private final BranchRepository branchRepository;

    public BranchService() {
        this.branchRepository = new BranchRepository();
    }
    public ServiceResponse<Branch> createBranch(String branchCode, String name, String city, String address, String phone) {

        Branch branch = Branch.builder()
                .branchCode(branchCode)
                .name(name)
                .city(city)
                .address(address)
                .phone(phone)
                .build();

        return this.createBranch(branch);
    }

    public ServiceResponse<Branch> updateBranch(Branch branch) {

        Branch existingBranch = branchRepository.findById(branch.getId());
        if (existingBranch == null) {
            return new ServiceResponse<>(false, 404, "Branch not found", null);
        }

        if (!existingBranch.getBranchCode().equals(branch.getBranchCode())
                && branchRepository.findByBranchCode(branch.getBranchCode()) != null) {
            return new ServiceResponse<>(false, 400, "Branch code already exists", null);
        }

        try {
            validateBranchData(branch);
        } catch (IllegalArgumentException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        }

//        TODO: Employee deactivation automation
        if (!branch.isActive() && existingBranch.getNumberOfEmployees() > 0) {
            return new ServiceResponse<>(false, 400, "Cannot deactivate branch with active employees", null);
        }

        Branch updatedBranch = branchRepository.updateBranch(branch);
        return new ServiceResponse<>(true, 200, "Branch updated successfully", updatedBranch);
    }


    public ServiceResponse<Branch> getBranch(Long id) {
        Branch branch = branchRepository.findById(id);
        if (branch == null) {
            return new ServiceResponse<>(false, 404, "Branch not found", null);
        }
        return new ServiceResponse<>(true, 200, "Branch retrieved successfully", branch);
    }

    public ServiceResponse<List<Branch>> getAllBranches() {
        List<Branch> branches = branchRepository.getAllBranches();
        return new ServiceResponse<>(true, 200, "Branches retrieved successfully", branches);
    }

    public ServiceResponse<Branch> getBranchByCode(String branchCode) {
        Branch branch = branchRepository.findByBranchCode(branchCode);
        if (branch == null) {
            return new ServiceResponse<>(false, 404, "Branch not found", null);
        }
        return new ServiceResponse<>(true, 200, "Branch retrieved successfully", branch);
    }

    public ServiceResponse<Void> setBranchStatus(Long id, boolean isActive) {
        Branch branch = branchRepository.findById(id);
        if (branch == null) {
            return new ServiceResponse<>(false, 404, "Branch not found", null);
        }

        if (!isActive && branch.getNumberOfEmployees() > 0) {
            return new ServiceResponse<>(false, 400, "Cannot deactivate branch with active employees", null);
        }

        branchRepository.setBranchStatus(id, isActive);
        String status = isActive ? "activated" : "deactivated";
        return new ServiceResponse<>(true, 200, "Branch " + status + " successfully", null);
    }

    public ServiceResponse<Branch> createBranch(Branch branch) {
        if (branchRepository.findByBranchCode(branch.getBranchCode()) != null) {
            return new ServiceResponse<>(false, 400, "Branch code already exists", null);
        }

        try {
            validateBranchData(branch);
        } catch (Exception e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        }
        Branch newBranch = branchRepository.createBranch(branch);
        return new ServiceResponse<>(true, 200, "Branch created successfully", newBranch);
    }

    private void validateBranchData(Branch branch) {
        if (branch.getBranchCode() == null || branch.getBranchCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch code cannot be empty");
        }
        if (branch.getName() == null || branch.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch name cannot be empty");
        }
        if (branch.getCity() == null || branch.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be empty");
        }
        if (branch.getAddress() == null || branch.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        if (branch.getPhone() == null || !branch.getPhone().matches("\\d{11}")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

    }

    public ServiceResponse<List<Branch>> getBranchesWithoutActiveManagers() {
        return getBranchesWithoutActiveManagers(null);
    }

    public ServiceResponse<List<Branch>> getBranchesWithoutActiveManagers(Employee employee) {
        try {
            List<Branch> branches = branchRepository.getBranchesWithoutActiveManagers();
            if(employee != null) {
                Long branchId = employee.getBranchId();
                Branch branch = branchRepository.findById(branchId);

                boolean isAlreadyPresent = false;
                for (Branch br : branches) {
                    if (Objects.equals(br.getId(), branch.getId())) {
                        isAlreadyPresent = true;
                        break;
                    }
                }
                if (!isAlreadyPresent)
                    branches.add(branch);
            }


            return new ServiceResponse<>(
                    true,
                    200,
                    "Successfully retrieved branches without active managers",
                    branches
            );

        } catch (Exception e) {
            return new ServiceResponse<>(
                    false,
                    500,
                    "Error retrieving branches: " + e.getMessage(),
                    null
            );
        }
    }
    public ServiceResponse<String> getManagerName (Long branchID) {
        if (branchID == null ) {
            return new ServiceResponse<>  (false,404,"Branch id is null or empty", null);
        }
        return new ServiceResponse<>(true,200,"Manager got Successfully",branchRepository.getManagerName(branchID));
    }

}



