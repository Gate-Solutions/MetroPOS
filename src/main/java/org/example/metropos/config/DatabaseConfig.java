package org.example.metropos.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class DatabaseConfig {
    private static HikariDataSource localDataSource;
    private static HikariDataSource cloudDataSource;

    public static DSLContext getLocalContext() {
        if (localDataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/local_db");
            config.setUsername("root");
            config.setPassword("");
            config.setMaximumPoolSize(10);

            localDataSource = new HikariDataSource(config);
        }
        return DSL.using(localDataSource, SQLDialect.MYSQL);
    }

    public static DSLContext getCloudContext() {
        if (cloudDataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/cloud_db");
            config.setUsername("root");
            config.setPassword("");
            config.setMaximumPoolSize(10);

            cloudDataSource = new HikariDataSource(config);
        }
        return DSL.using(cloudDataSource, SQLDialect.MYSQL);
    }

    public static void closeDataSources() {
        if (localDataSource != null) {
            System.out.println("Closing local");
            localDataSource.close();
        }
        if (cloudDataSource != null) {
            System.out.println("Closing cloud");
            cloudDataSource.close();
        }
    }
}