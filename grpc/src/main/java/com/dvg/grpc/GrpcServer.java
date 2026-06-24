package com.dvg.grpc;

import com.dvg.grpc.config.DatabaseConfig;
import com.dvg.grpc.config.DatabaseMigration;
import com.dvg.grpc.service.RechnungsmetadatenService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// gRPC-Server für den Rechnungsservice.
public class GrpcServer {
  private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);
  private static final int PORT = 50051;

  static void main() throws IOException, InterruptedException {
    DatabaseMigration.migrate();
    logger.info("Server wird gestartet...");

    Server server =
        ServerBuilder.forPort(PORT)
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .addService(new RechnungsmetadatenService())
            .build();

    server.start();
    logger.info("gRPC-Server gestartet auf Port {}", PORT);

    Runtime.getRuntime()
        .addShutdownHook(
            Thread.ofVirtual()
                .unstarted(
                    () -> {
                      logger.info("gRPC-Server wird heruntergefahren...");
                      server.shutdown();
                      try {
                        server.awaitTermination();
                      } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                      }
                      logger.info("Schließe Datenbankverbindungspool...");
                      DatabaseConfig.shutdown();
                    }));

    server.awaitTermination();
  }
}
