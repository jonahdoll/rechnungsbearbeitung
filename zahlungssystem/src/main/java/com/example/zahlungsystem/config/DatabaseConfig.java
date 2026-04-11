package com.example.zahlungsystem.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static final HikariDataSource dataSource;

    static {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dotenv.get("ZAHLUNGSSYSTEM_DB_URL"));
        config.setUsername(dotenv.get("ZAHLUNGSSYSTEM_DB_USERNAME"));
        config.setPassword(dotenv.get("ZAHLUNGSSYSTEM_DB_PASSWORD"));
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);
    }

    public static DataSource getInstance() {
        return dataSource;
    }
}
