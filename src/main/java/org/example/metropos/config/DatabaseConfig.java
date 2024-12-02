package org.example.metropos.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {
    private static HikariDataSource localDataSource;
    private static HikariDataSource remoteDataSource;

    private static void initializeRemoteDataSources() {
        HikariConfig remoteConfig = new HikariConfig();
        remoteConfig.setJdbcUrl("jdbc:postgresql://remote-host:5432/remote_db");
        remoteConfig.setUsername("remote_user");
        remoteConfig.setPassword("remote_password");
        remoteDataSource = new HikariDataSource(remoteConfig);

    }

    private static void initializeLocalDataSource() {
        HikariConfig localConfig = new HikariConfig();
        localConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/local_db");
        localConfig.setUsername("local_user");
        localConfig.setPassword("local_password");
        localDataSource = new HikariDataSource(localConfig);
    }

    public static HikariDataSource getLocalContext() {
        if(localDataSource == null) {
            initializeLocalDataSource();
        }
        return localDataSource;
    }

    public static HikariDataSource getRemoteContext() {
        if(remoteDataSource == null) {
            initializeRemoteDataSources();
        }
        return remoteDataSource;
    }

}
