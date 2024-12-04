package org.gate.metropos.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class DatabaseConfig {
    private static Dotenv dotenv;
    private static HikariDataSource localDataSource;
    private static HikariDataSource remoteDataSource;
    private static DSLContext localDsl;
    private static DSLContext remoteDsl;

    private static void initializeRemoteDataSources() {
        dotenv = Dotenv.load();
        try {
            HikariConfig remoteConfig = new HikariConfig();
            remoteConfig.setJdbcUrl(dotenv.get("REMOTE_DB_URL"));
            remoteConfig.setUsername(dotenv.get("REMOTE_DB_USERNAME"));
            remoteConfig.setPassword(dotenv.get("REMOTE_DB_PASSWORD"));
            remoteDataSource = new HikariDataSource(remoteConfig);
            remoteDsl = DSL.using(remoteDataSource, SQLDialect.POSTGRES);
        } catch (Exception e) {
            System.out.println("Couldn't connect to remote database");
        }

    }

    private static void initializeLocalDataSource() {
        dotenv = Dotenv.load();
        HikariConfig localConfig = new HikariConfig();
        localConfig.setJdbcUrl(dotenv.get("LOCAL_DB_URL"));
        localConfig.setUsername(dotenv.get("LOCAL_DB_USERNAME"));
        localConfig.setPassword(dotenv.get("LOCAL_DB_PASSWORD"));
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

    public static boolean isRemoteAvailable() {
        return remoteDsl != null;
    }

    public static void shutdown() {
        if (localDataSource != null) {
            localDataSource.close();
        }
        if (remoteDataSource != null) {
            remoteDataSource.close();
        }
    }

}
