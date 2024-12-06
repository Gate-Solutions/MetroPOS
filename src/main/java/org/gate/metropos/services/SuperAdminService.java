package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gate.metropos.models.SuperAdmin;
import org.gate.metropos.repositories.SuperAdminRepository;

//@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminService {
    SuperAdminRepository superAdminRepository ;

    @Getter
    SuperAdmin superAdmin = null;

    public SuperAdminService(){
        superAdminRepository = new SuperAdminRepository();
    }
    public SuperAdmin login(String emailOrUsername, String password) {
        boolean isEmail = emailOrUsername.contains("@");

        if(isEmail) {
            SuperAdmin admin = superAdminRepository.findByEmail(emailOrUsername.toLowerCase());
            if(admin != null && admin.getPassword().equals(password)) {
                this.superAdmin = admin;
            }
        }
        else {
            SuperAdmin admin = superAdminRepository.findByUsername(emailOrUsername.toLowerCase());
            if(admin != null && admin.getPassword().equals(password)) {
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

}
