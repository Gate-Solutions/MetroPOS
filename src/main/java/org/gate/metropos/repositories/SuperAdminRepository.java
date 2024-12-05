package org.gate.metropos.repositories;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.SuperAdminFields;
import org.gate.metropos.enums.UserFields;
import org.gate.metropos.enums.UserRole;
import org.gate.metropos.models.SuperAdmin;
import org.jooq.DSLContext;
import org.jooq.Record;

@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminRepository {
    private DSLContext dsl = DatabaseConfig.getLocalDSL();

    public SuperAdmin findByEmail(String email) {
        Record record = dsl.select()
                .from(SuperAdminFields.SuperAdminTable.toTableField())
                .where(UserFields.EMAIL.toField().eq(email))
                .fetchOne();

        if(record == null) return null;
        return mapToSuperAdmin(record);
    }

    public SuperAdmin findByUsername(String username) {
        Record record = dsl.select()
                .from(SuperAdminFields.SuperAdminTable.toTableField())
                .where(UserFields.USERNAME.toField().eq(username))
                .fetchOne();

        if(record == null) return null;
        return mapToSuperAdmin(record);
    }


    private SuperAdmin mapToSuperAdmin(Record record) {
        if(record == null) return null;
        SuperAdmin superAdmin = new SuperAdmin();
        superAdmin.setId(record.get(UserFields.ID.toField(), Long.class));
        superAdmin.setUsername(record.get(UserFields.USERNAME.toField(), String.class));
        superAdmin.setEmail(record.get(UserFields.EMAIL.toField(), String.class));
        superAdmin.setPassword(record.get(UserFields.PASSWORD.toField(), String.class));
        superAdmin.setRole(UserRole.valueOf(record.get(UserFields.ROLE.toField(), String.class)));
//        TODO: add createdAt and updatedAt in this object
        return superAdmin;
    }
}
