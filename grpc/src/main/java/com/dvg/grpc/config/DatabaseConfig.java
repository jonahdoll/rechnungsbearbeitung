package com.dvg.grpc.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import javax.sql.DataSource;

public class DatabaseConfig {
  private static final HikariDataSource dataSource;

  static {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(dotenv.get("GRPC_DB_URL"));
    config.setUsername(dotenv.get("GRPC_DB_USERNAME"));
    config.setPassword(dotenv.get("GRPC_DB_PASSWORD"));
    config.setMaximumPoolSize(10);
    config.setLeakDetectionThreshold(10_000);

    dataSource = new HikariDataSource(config);
  }

  public static DataSource getInstance() {
    return dataSource;
  }

  public static void shutdown() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
    }
  }
}
