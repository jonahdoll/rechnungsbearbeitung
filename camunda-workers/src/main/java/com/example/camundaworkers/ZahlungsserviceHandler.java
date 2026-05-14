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

public class ZahlungsserviceHandler implements JobHandler {
  private final ZahlungsProducer producer;
  public static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  public ZahlungsserviceHandler(ZahlungsProducer producer) {
    this.producer = producer;
  }

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

      producer.sendeZahlungsauftrag(auftrag);

      client.newCompleteCommand(job.getKey()).send().join();

    } catch (Exception e) {
      client
          .newFailCommand(job.getKey())
          .retries(0)
          .errorMessage("Zahlungsfehler: " + e.getMessage())
          .send()
          .join();
    }
  }
}
