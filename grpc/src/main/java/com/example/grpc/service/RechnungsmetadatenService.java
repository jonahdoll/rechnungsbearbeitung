package com.example.grpc.service;

import com.example.grpc.RechnungsMetadata;
import com.example.grpc.RechnungsmetadatenServiceGrpc;
import com.example.grpc.config.DatabaseConfig;
import com.example.grpc.entity.Rechnungsmetadaten;
import com.example.grpc.entity.Rechnungsposition;
import com.example.grpc.repository.RechnungspositionRepository;
import com.example.grpc.repository.RechnungsMetadatenRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/// gRPC-Service für das Speichern von Rechnungsmetadaten.
public class RechnungsmetadatenService extends RechnungsmetadatenServiceGrpc.RechnungsmetadatenServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(RechnungsmetadatenService.class);

    private final RechnungsMetadatenRepository rechnungsRepo;
    private final RechnungspositionRepository positionRepo;
    private final RequestValidator requestValidator;
    private final RechnungsmetadatenMapper rechnungsmetadatenMapper;
    private final RechnungspositionsMapper rechnungspositionsMapper;
    private final DataSource dataSource;

    public RechnungsmetadatenService() {
        this.dataSource = DatabaseConfig.getInstance();
        this.rechnungsRepo = new RechnungsMetadatenRepository(dataSource);
        this.positionRepo = new RechnungspositionRepository(dataSource);
        this.rechnungsmetadatenMapper = new RechnungsmetadatenMapper();
        this.rechnungspositionsMapper = new RechnungspositionsMapper();
        this.requestValidator = new RequestValidator();
    }

    @Override
    public void speicherMetadaten(
            final RechnungsMetadata.MetadatenSpeichernRequest request,
            final StreamObserver<RechnungsMetadata.APIResponse> responseObserver) {
        final String rechnungsnummer = request.getRechnungsnummer();
        logger.debug("Rechnungsnummer: {}", rechnungsnummer);
        logger.debug("Anzahl Positionen: {}", request.getPositionenList().size());

        final List<String> fehler = requestValidator.validiere(request);
        if (!fehler.isEmpty()) {
            logger.warn("Validierung fehlgeschlagen für Rechnung {}: {}", rechnungsnummer, fehler);
            sendResponse(responseObserver, 400, "Die Rechnungsmetadaten sind nicht vollständig: " + String.join(", ", fehler));
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                final Rechnungsmetadaten metadaten = rechnungsmetadatenMapper.mapToRechnungsmetadaten(request);
                final UUID rechnungsId = rechnungsRepo.save(connection, metadaten);

                final List<Rechnungsposition> positionen = rechnungspositionsMapper.mapToRechnungspositionen(
                        request.getPositionenList());
                positionRepo.saveAll(connection, positionen, rechnungsId);

                connection.commit();
                logger.debug("Rechnung gespeichert mit ID: {} und {} Rechnungspositionen", rechnungsId, positionen.size());

                sendResponse(responseObserver, 200, "Erfolgreich gespeichert. ID: " + rechnungsId);

            } catch (final Exception e) {
                connection.rollback();
                logger.error("Fehler bei der Verarbeitung der Rechnung {}", rechnungsnummer, e);
                sendResponse(responseObserver, 500, "Interner Verarbeitungsfehler: " + e.getMessage());
            }

        } catch (final SQLException e) {
            logger.error("Datenbankverbindungsfehler bei Rechnung {}", rechnungsnummer, e);
            sendResponse(responseObserver, 500, "Datenbank Verbindungsfehler");
        }
    }

    private void sendResponse(final StreamObserver<RechnungsMetadata.APIResponse> responseObserver,
                              final int responseCode,
                              final String message) {
        final RechnungsMetadata.APIResponse response = RechnungsMetadata.APIResponse.newBuilder()
                .setResponseCode(responseCode)
                .setResponsemessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
