package com.example.zahlungsystem;

import com.example.zahlungsystem.entity.Zahlungsauftrag;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamundaRabbitMqWorker {
  private static final Logger logger = LoggerFactory.getLogger(CamundaRabbitMqWorker.class);
  private static final String JOB_TYPE = "rabbitmq-zahlungsauftrag";

  public static void main(String[] args) {
    try (var camunda = CamundaClientFactory.create();
        var publisher = new RabbitMqZahlungsauftragPublisher();
        var worker = camunda.newWorker().jobType(JOB_TYPE).handler(new Handler(publisher)).open()) {

      camunda.newTopologyRequest().execute();
      logger.info("Camunda-Worker fuer Job-Type '{}' gestartet.", JOB_TYPE);
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.info("Camunda-Worker beendet.");
    } catch (Exception e) {
      logger.error("Camunda-Worker konnte nicht gestartet werden: {}", e.getMessage(), e);
      System.exit(1);
    }
  }

  private static final class Handler implements JobHandler {
    private final RabbitMqZahlungsauftragPublisher publisher;

    private Handler(RabbitMqZahlungsauftragPublisher publisher) {
      this.publisher = publisher;
    }

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
      Zahlungsauftrag auftrag = toZahlungsauftrag(job.getVariablesAsMap());
      publisher.publish(auftrag);

      client
          .newCompleteCommand(job.getKey())
          .variables(Map.of("rabbitMqGesendet", true))
          .send()
          .join();
    }

    private Zahlungsauftrag toZahlungsauftrag(Map<String, Object> variables) {
      return new Zahlungsauftrag(
          stringValue(variables, "zahlungsReferenz"),
          bigDecimalValue(variables, "betrag"),
          stringValue(variables, "iban"),
          LocalDateTime.parse(stringValue(variables, "faelligkeitsdatum")));
    }

    private String stringValue(Map<String, Object> variables, String key) {
      Object value = variables.get(key);
      if (value == null) {
        throw new IllegalArgumentException("Camunda-Variable fehlt: " + key);
      }
      return value.toString();
    }

    private BigDecimal bigDecimalValue(Map<String, Object> variables, String key) {
      Object value = variables.get(key);
      if (value == null) {
        throw new IllegalArgumentException("Camunda-Variable fehlt: " + key);
      }
      return new BigDecimal(value.toString());
    }
  }
}
