package com.example.zahlungsystem;

import io.camunda.client.CamundaClient;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;

final class CamundaClientFactory {
  private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

  private CamundaClientFactory() {}

  static CamundaClient create() {
    return CamundaClient.newClientBuilder()
        .grpcAddress(URI.create(value("CAMUNDA_GRPC_ADDRESS", "http://localhost:26500")))
        .restAddress(URI.create(value("CAMUNDA_REST_ADDRESS", "http://localhost:8080")))
        .build();
  }

  private static String value(String key, String fallback) {
    String value = DOTENV.get(key);
    if (value == null || value.isBlank()) {
      value = System.getenv(key);
    }
    return value == null || value.isBlank() ? fallback : value;
  }
}
