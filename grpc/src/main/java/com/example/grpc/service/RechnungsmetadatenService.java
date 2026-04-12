package com.example.grpc.service;

import com.example.grpc.RechnungsMetadata;
import com.example.grpc.RechnungsmetadatenServiceGrpc;
import com.example.grpc.config.DatabaseConfig;
import com.example.grpc.entity.Rechnungsmetadaten;
import com.example.grpc.repository.RechnungsMetadatenRepository;
import com.example.grpc.repository.RechnungspositionRepository;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RechnungsmetadatenService
    extends RechnungsmetadatenServiceGrpc.RechnungsmetadatenServiceImplBase {
  private static final Logger logger = LoggerFactory.getLogger(RechnungsmetadatenService.class);

  private final RechnungsMetadatenRepository rechnungsRepo;
  private final RechnungspositionRepository positionRepo;
  private final DataSource dataSource;

  public RechnungsmetadatenService() {
    this.dataSource = DatabaseConfig.getInstance();
    this.rechnungsRepo = new RechnungsMetadatenRepository(dataSource);
    this.positionRepo = new RechnungspositionRepository(dataSource);
  }

  @Override
  public void speicherMetadaten(
      final RechnungsMetadata.MetadatenSpeichernRequest request,
      final StreamObserver<RechnungsMetadata.APIResponse> responseObserver) {

    final String rechnungsnummer = request.getRechnungsnummer();

    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);

      try {
        final Rechnungsmetadaten metadaten = Rechnungsmetadaten.fromProto(request);

        final UUID rechnungsId = rechnungsRepo.save(connection, metadaten);
        positionRepo.saveAll(connection, metadaten.positionen(), rechnungsId);

        connection.commit();

        logger.info("Rechnung {} erfolgreich gespeichert. ID: {}", rechnungsnummer, rechnungsId);
        sendResponse(responseObserver, 200, "Erfolgreich gespeichert. ID: " + rechnungsId);

      } catch (ConstraintViolationException e) {
        connection.rollback();
        String fehlerDetails =
            e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        logger.warn("Validierungsfehler: Rechnungsnummer {}: {}", rechnungsnummer, fehlerDetails);
        sendResponse(responseObserver, 400, "Validierungsfehler: " + fehlerDetails);

      } catch (Exception e) {
        connection.rollback();
        logger.error("Interner Fehler: Rechnungsnummer: {}", rechnungsnummer, e);
        sendResponse(responseObserver, 500, "Interner Fehler: " + e.getMessage());
      }

    } catch (SQLException e) {
      logger.error("Datenbankfehler: Rechnungsnummer {}", rechnungsnummer, e);
      sendResponse(responseObserver, 500, "Datenbankfehler");
    }
  }

  private void sendResponse(
      final StreamObserver<RechnungsMetadata.APIResponse> responseObserver,
      final int responseCode,
      final String message) {
    final RechnungsMetadata.APIResponse response =
        RechnungsMetadata.APIResponse.newBuilder()
            .setResponseCode(responseCode)
            .setResponsemessage(message)
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
