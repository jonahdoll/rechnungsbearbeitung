package com.example.grpc;

import com.example.grpc.config.DatabaseConfig;
import com.example.grpc.config.DatabaseMigration;
import com.example.grpc.service.RechnungsmetadatenService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/// gRPC-Server für den Rechnungsservice.
public class GrpcServer {
    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);
    private static final int PORT = 50051;

    public static void main(String[] args) throws IOException, InterruptedException {
        DatabaseMigration.migrate();
        logger.info("Server wird gestartet...");

        Server server = ServerBuilder.forPort(PORT)
                .addService(new RechnungsmetadatenService())
                .build();

        server.start();
        logger.info("gRPC-Server gestartet auf Port {}", PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Server wird heruntergefahren...");
            server.shutdown();
        }));

        server.awaitTermination();
    }
}