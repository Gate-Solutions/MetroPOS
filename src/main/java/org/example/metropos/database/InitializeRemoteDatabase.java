package org.example.metropos.database;

import org.example.metropos.config.DatabaseConfig;
import org.jooq.DSLContext;

public class InitializeRemoteDatabase {
    public static void main(String[] args) {
        DSLContext remoteDSL = DatabaseConfig.getRemoteDSL();
        if(remoteDSL == null) {
            return;
        }
        DatabaseInitializer initializer = new DatabaseInitializer(remoteDSL);
        initializer.createTables();
    }
}
