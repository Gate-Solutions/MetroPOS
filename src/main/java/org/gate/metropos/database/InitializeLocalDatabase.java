package org.gate.metropos.database;

import org.gate.metropos.config.DatabaseConfig;
import org.jooq.DSLContext;

public class InitializeLocalDatabase {

    public static void main(String[] args) {
        DSLContext localDSL = DatabaseConfig.getLocalDSL();
        DatabaseInitializer initializer = new DatabaseInitializer(localDSL);
        initializer.initialize();
    }
}
