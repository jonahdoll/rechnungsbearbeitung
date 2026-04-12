package com.example.grpc.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseMigration {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

  public static void migrate() {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    boolean shouldClean = Boolean.parseBoolean(dotenv.get("GRPC_DB_CLEAN", "false"));

    logger.info("Starte Datenbank Migration...");

    Flyway flyway =
        Flyway.configure()
            .dataSource(DatabaseConfig.getInstance())
            .cleanDisabled(false)
            .locations("classpath:db/migration")
            .load();

    if (shouldClean) {
      logger.info("Die Datenbank wird zurückgesetzt.");
      flyway.clean();
    }

    flyway.migrate();
    logger.info("Datenbank Migration erfolgreich abgeschlossen.");
  }
}
