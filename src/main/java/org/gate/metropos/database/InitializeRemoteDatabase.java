package org.gate.metropos.database;

import org.gate.metropos.config.DatabaseConfig;
import org.jooq.DSLContext;

public class InitializeRemoteDatabase {
    public static void main(String[] args) {
        DSLContext remoteDSL = DatabaseConfig.getRemoteDSL();
        if(remoteDSL == null) {
            return;
        }
        DatabaseInitializer initializer = new DatabaseInitializer(remoteDSL, false);
        initializer.createTables();
    }
}
