package org.gate.metropos.services;

import org.gate.metropos.models.SuperAdmin;
import org.gate.metropos.repositories.SuperAdminRepository;

public class SuperAdminService {
    SuperAdminRepository superAdminRepository = new SuperAdminRepository();
    SuperAdmin superAdmin = null;

    public SuperAdmin login(String emailOrUsername, String password) {
        Boolean isEmail = emailOrUsername.contains("@");

        if(isEmail) {
            SuperAdmin admin = superAdminRepository.findByEmail(emailOrUsername.toLowerCase());
            if(admin != null && admin.getPassword().equals(password)) {
                this.superAdmin = admin;
            }
        }
        else {
            SuperAdmin admin = superAdminRepository.findByUsername(emailOrUsername.toLowerCase());
            if(admin.getPassword().equals(password)) {
                this.superAdmin =  admin;
            }
        }
        return this.superAdmin;
    }

    public boolean isLoggedIn() {
        return this.superAdmin != null;
    }

    public void logout() {
        this.superAdmin = null;
    }

    public SuperAdmin getSuperAdmin() {
        return this.superAdmin;
    }

}
