package com.example.camundaworkers;

import io.camunda.client.CamundaClient;
import io.camunda.client.impl.oauth.OAuthCredentialsProviderBuilder;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerOrchestrator {
  private static final Logger logger = LoggerFactory.getLogger(WorkerOrchestrator.class);

  public static void main(String[] args) {
    AppConfig config = AppConfig.load();

    try {
      runWorkers(config);
    } catch (Exception e) {
      logger.error("Kritischer Systemfehler: ", e);
    }
  }

  private static void runWorkers(AppConfig config) throws Exception {
    try (GrpcClient grpcClient = new GrpcClient();
        CamundaClient camundaClient = createCamundaClient(config);
        var zahlungsserviceWorker =
            camundaClient
                .newWorker()
                .jobType("zahlungsservice-g1")
                .handler(new ZahlungsserviceHandler())
                .open();
        var metadatenWorker =
            camundaClient
                .newWorker()
                .jobType("metadaten-speichern-g1")
                .handler(new MetadatenSpeichernHandler(grpcClient))
                .open()) {
      logger.info("Zahlungsservice-Worker und Metadaten-Worker aktiv. Warte auf Jobs...");
      new CountDownLatch(1).await();
    }
  }

  private static CamundaClient createCamundaClient(final AppConfig config) {
    String restUrl =
        "https://"
            + config.camundaClientCloudRegion()
            + ".zeebe.camunda.io/"
            + config.camundaCloudClusterId();

    var credentialsProvider =
        new OAuthCredentialsProviderBuilder()
            .clientId(config.camundaClientId())
            .clientSecret(config.camundaClientSecret())
            .audience("zeebe.camunda.io")
            .build();

    return CamundaClient.newClientBuilder()
        .restAddress(URI.create(restUrl))
        .credentialsProvider(credentialsProvider)
        .build();
  }
}
