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
    this.positionRepo = new RechnungspositionRepository();
  }

  @Override
  public void speicherMetadaten(
      final RechnungsMetadata.MetadatenSpeichernRequest request,
      final StreamObserver<RechnungsMetadata.APIResponse> responseObserver) {

    final String rechnungsnummer = request.getRechnungsnummer();

    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      final UUID rechnungsId = persistiere(connection, request);
      connection.commit();

      logger.info("Rechnung {} erfolgreich gespeichert. ID: {}", rechnungsnummer, rechnungsId);
      sendResponse(responseObserver, 200, "Erfolgreich gespeichert", rechnungsId.toString());

    } catch (ConstraintViolationException e) {
      String fehlerDetails =
          e.getConstraintViolations().stream()
              .map(v -> v.getPropertyPath() + ": " + v.getMessage())
              .collect(Collectors.joining(", "));
      logger.warn("Validierungsfehler: Rechnungsnummer {}: {}", rechnungsnummer, fehlerDetails);
      sendResponse(responseObserver, 400, "Validierungsfehler: " + fehlerDetails, null);

    } catch (SQLException e) {
      handleSqlException(e, rechnungsnummer, responseObserver);

    } catch (Exception e) {
      logger.error("Interner Fehler: Rechnungsnummer: {}", rechnungsnummer, e);
      sendResponse(responseObserver, 500, "Interner Fehler: " + e.getMessage(), null);
    }
  }

  private UUID persistiere(final Connection connection,
      final RechnungsMetadata.MetadatenSpeichernRequest request) throws SQLException {
    try {
      final boolean isUpdate = request.hasId() && !request.getId().isBlank();
      final Rechnungsmetadaten metadaten = Rechnungsmetadaten.fromProto(request);
      final UUID rechnungsId = rechnungsRepo.save(connection, metadaten, isUpdate);
      positionRepo.saveAll(connection, metadaten.positionen(), rechnungsId);
      return rechnungsId;
    } catch (SQLException | RuntimeException e) {
      safeRollback(connection);
      throw e;
    }
  }

  private void handleSqlException(final SQLException e, final String rechnungsnummer,
      final StreamObserver<RechnungsMetadata.APIResponse> responseObserver) {
    if ("23505".equals(e.getSQLState())) {
      logger.warn("Duplikat: Rechnungsnummer {} existiert bereits.", rechnungsnummer);
      sendResponse(responseObserver, 409,
          "Rechnungsnummer " + rechnungsnummer + " existiert bereits.", null);
    } else {
      logger.error("Datenbankfehler: Rechnungsnummer: {}", rechnungsnummer, e);
      sendResponse(responseObserver, 500, "Datenbankfehler: " + e.getMessage(), null);
    }
  }

  private static void safeRollback(final Connection connection) {
    try {
      if (connection != null) {
        connection.rollback();
      }
    } catch (SQLException e) {
      logger.error("Rollback fehlgeschlagen", e);
    }
  }

  private void sendResponse(
      final StreamObserver<RechnungsMetadata.APIResponse> responseObserver,
      final int responseCode,
      final String message,
      final String generatedId) {
    final RechnungsMetadata.APIResponse response =
        RechnungsMetadata.APIResponse.newBuilder()
            .setResponseCode(responseCode)
            .setResponsemessage(message)
            .setGeneratedId(generatedId != null ? generatedId : "")
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
