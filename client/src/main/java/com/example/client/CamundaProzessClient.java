package com.example.client;

import com.example.zahlungsystem.entity.Zahlungsauftrag;
import io.camunda.client.CamundaClient;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamundaProzessClient implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(CamundaProzessClient.class);
  private static final String BPMN_PROCESS_ID = "rechnungsbearbeitung";

  private final CamundaClient client;

  public CamundaProzessClient() {
    this.client = CamundaClientFactory.create();
  }

  public void pruefeVerbindung() {
    client.newTopologyRequest().execute();
    logger.info("Verbindung zu Camunda 8 hergestellt.");
  }

  public void deployeProzess() {
    client
        .newDeployResourceCommand()
        .addResourceFromClasspath("rechnungsbearbeitung.bpmn")
        .execute();
    logger.info("Camunda-Prozess '{}' deployt.", BPMN_PROCESS_ID);
  }

  public void starteZahlungsprozess(Zahlungsauftrag auftrag) {
    var variables =
        Map.of(
            "zahlungsReferenz", auftrag.zahlungsReferenz(),
            "betrag", auftrag.betrag(),
            "iban", auftrag.iban(),
            "faelligkeitsdatum", auftrag.faelligkeitsdatum().toString());

    client
        .newCreateInstanceCommand()
        .bpmnProcessId(BPMN_PROCESS_ID)
        .latestVersion()
        .variables(variables)
        .execute();

    logger.info("Camunda-Prozessinstanz fuer Zahlung {} gestartet.", auftrag.zahlungsReferenz());
  }

  @Override
  public void close() {
    client.close();
  }
}
