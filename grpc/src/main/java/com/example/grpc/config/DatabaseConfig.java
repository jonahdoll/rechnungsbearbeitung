package com.example.grpc.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static final HikariDataSource dataSource;

    static {
        Dotenv dotenv = Dotenv.load();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dotenv.get("DB_URL"));
        config.setUsername(dotenv.get("DB_USERNAME"));
        config.setPassword(dotenv.get("DB_PASSWORD"));

        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();
    }

    public static DataSource getInstance() {
        return dataSource;
    }
}