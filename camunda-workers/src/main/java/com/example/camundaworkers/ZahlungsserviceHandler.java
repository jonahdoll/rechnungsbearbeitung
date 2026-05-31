package com.example.camundaworkers;

import com.example.zahlungsystem.ZahlungsProducer;
import com.example.zahlungsystem.entity.Zahlungsauftrag;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZahlungsserviceHandler implements JobHandler {
  private static final Logger logger = LoggerFactory.getLogger(ZahlungsserviceHandler.class);
  public static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public ZahlungsserviceHandler() {}

  @Override
  public void handle(JobClient client, ActivatedJob job) {
    Map<String, Object> vars = job.getVariablesAsMap();

    try {
      Zahlungsauftrag auftrag =
          new Zahlungsauftrag(
              (String) vars.get("rechnungsId"),
              new BigDecimal(vars.getOrDefault("gesamtbetrag", "0").toString()),
              (String) vars.get("iban"),
              LocalDateTime.parse((String) vars.get("faelligkeitsdatum"), FORMATTER));

      try (ZahlungsProducer producer = new ZahlungsProducer()) {
        producer.sendeZahlungsauftrag(auftrag);
      }

      client.newCompleteCommand(job.getKey()).send().join();

    } catch (Exception e) {
      int remainingRetries = job.getRetries() - 1;
      logger.error("Fehler beim Senden an RabbitMQ. Verbleibende Retries: {}", remainingRetries, e);

      if (remainingRetries <= 0) {
        logger.warn("RabbitMQ dauerhaft nicht erreichbar. Melde SERVICE_UNAVAILABLE an Camunda.");
        client
            .newThrowErrorCommand(job.getKey())
            .errorCode("ZAHLUNGSSERVICE_SERVICE_UNAVAILABLE")
            .errorMessage("RabbitMQ-Broker ist nicht erreichbar: " + e.getMessage())
            .send()
            .join();
      } else {
        client
            .newFailCommand(job.getKey())
            .retries(remainingRetries)
            .retryBackoff(java.time.Duration.ofSeconds(5))
            .errorMessage("RabbitMQ temporär nicht erreichbar: " + e.getMessage())
            .send()
            .join();
      }
    }
  }
}
