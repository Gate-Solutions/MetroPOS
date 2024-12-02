package org.example.metropos.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class DatabaseConfig {
    private static HikariDataSource localDataSource;
    private static HikariDataSource remoteDataSource;
    private static DSLContext localDsl;
    private static DSLContext remoteDsl;

    private static void initializeRemoteDataSources() {
        try {
            HikariConfig remoteConfig = new HikariConfig();
            remoteConfig.setJdbcUrl("jdbc:postgresql://remote-host:5432/remote_db");
            remoteConfig.setUsername("postgres");
            remoteConfig.setPassword("asad123");
            remoteDataSource = new HikariDataSource(remoteConfig);
            remoteDsl = DSL.using(remoteDataSource, SQLDialect.POSTGRES);
        } catch (Exception e) {
            System.out.println("Couldn't connect to remote database");
        }

    }

    private static void initializeLocalDataSource() {
        HikariConfig localConfig = new HikariConfig();
        localConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/local_db");
        localConfig.setUsername("postgres");
        localConfig.setPassword("asad123");
        localDataSource = new HikariDataSource(localConfig);
        localDsl = DSL.using(localDataSource, SQLDialect.POSTGRES);
    }

    public static DSLContext getLocalDSL() {
        if(localDsl == null) {
            initializeLocalDataSource();
        }
        return localDsl;
    }

    public static DSLContext getRemoteDSL() {
        if(remoteDsl == null) {
            initializeRemoteDataSources();
        }
        return remoteDsl;
    }

    public boolean isRemoteAvailable() {
        return remoteDsl != null;
    }

    public void shutdown() {
        if (localDataSource != null) {
            localDataSource.close();
        }
        if (remoteDataSource != null) {
            remoteDataSource.close();
        }
    }

}
