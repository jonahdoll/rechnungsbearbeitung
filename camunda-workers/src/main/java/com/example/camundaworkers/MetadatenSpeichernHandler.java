package com.example.camundaworkers;

import com.example.grpc.RechnungsMetadata;
import com.example.grpc.RechnungsmetadatenServiceGrpc;
import com.google.protobuf.Timestamp;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobHandler;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadatenSpeichernHandler implements JobHandler {

  private static final Logger logger = LoggerFactory.getLogger(MetadatenSpeichernHandler.class);
  public static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  private final RechnungsmetadatenServiceGrpc.RechnungsmetadatenServiceBlockingStub stub;

  public MetadatenSpeichernHandler(GrpcClient grpcClient) {
    this.stub = grpcClient.getStub();
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) {
    Map<String, Object> vars = job.getVariablesAsMap();

    try {
      var requestBuilder = RechnungsMetadata.MetadatenSpeichernRequest.newBuilder();

      // Beim Erstellen von neuen Rechnungsmetadaten wird keine id mitgeschickt
      Object idObj = vars.get("id");
      String existingID = idObj != null ? idObj.toString() : null;
      if (existingID != null && !existingID.isBlank()) {
        logger.info("Verwende übergebene Rechnungs-ID für Update: {}", existingID);
        requestBuilder.setId(existingID);
      }
      requestBuilder.setRechnungsnummer((String) vars.get("rechnungsnummer"));
      requestBuilder.setRechnungsausteller((String) vars.get("rechnungsausteller"));
      requestBuilder.setRechnungsempfaenger((String) vars.get("rechnungsempfaenger"));
      requestBuilder.setIban((String) vars.get("iban"));
      requestBuilder.setBic((String) vars.get("bic"));

      if (vars.containsKey("rechnungsdatum")) {
        requestBuilder.setRechnungsdatum(toTimestamp((String) vars.get("rechnungsdatum")));
      }
      if (vars.containsKey("faelligkeitsdatum")) {
        requestBuilder.setFaelligkeitsdatum(toTimestamp((String) vars.get("faelligkeitsdatum")));
      }

      @SuppressWarnings("unchecked")
      var positionen = (List<Map<String, Object>>) vars.getOrDefault("positionen", List.of());
      positionen.stream()
          .map(MetadatenSpeichernHandler::toRechnungsposition)
          .forEach(requestBuilder::addPositionen);

      RechnungsMetadata.APIResponse response = stub.speicherMetadaten(requestBuilder.build());

      if (response.getResponseCode() == 200) {
        logger.info("Metadaten erfolgreich gespeichert. Id: {}", response.getGeneratedId());
        client
            .newCompleteCommand(job.getKey())
            .variable("gespeicherteRechnungsId", response.getGeneratedId())
            .send()
            .join();
      } else {
        logger.warn(
            "gRPC-Fehler: {} - {}", response.getResponseCode(), response.getResponsemessage());
        client
            .newFailCommand(job.getKey())
            .retries(0)
            .errorMessage("gRPC-Fehler: " + response.getResponsemessage())
            .send()
            .join();
      }

    } catch (Exception e) {
      logger.error("Fehler beim Speichern der Metadaten", e);
      client
          .newFailCommand(job.getKey())
          .retries(0)
          .errorMessage("Metadatenfehler: " + e.getMessage())
          .send()
          .join();
    }
  }

  private static RechnungsMetadata.Rechnungsposition toRechnungsposition(Map<String, Object> pos) {
    double betrag = Double.parseDouble(pos.getOrDefault("einzelpreisBetrag", "0").toString());
    String waehrung = (String) pos.getOrDefault("waehrung", "EUR");
    return RechnungsMetadata.Rechnungsposition.newBuilder()
        .setArtikelnummer((String) pos.get("artikelnummer"))
        .setMenge(Double.parseDouble(pos.getOrDefault("menge", "1").toString()))
        .setEinzelpreis(
            RechnungsMetadata.Geld.newBuilder()
                .setBetrag(betrag)
                .setWaehrungsCode(waehrung)
                .build())
        .build();
  }

  private static Timestamp toTimestamp(String dateTimeStr) {
    LocalDateTime ldt = LocalDateTime.parse(dateTimeStr, FORMATTER);
    Instant instant = ldt.atZone(ZoneId.of("UTC")).toInstant();
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }
}
