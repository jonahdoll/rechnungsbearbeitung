package com.dvg.camundaworkers;

import io.github.cdimascio.dotenv.Dotenv;

public record AppConfig(
    String rabbitmqHost,
    int rabbitmqPort,
    String rabbitmqUser,
    String rabbitmqPassword,
    String rabbitmqQueue,
    String grpcHost,
    int grpcPort,
    String camundaClientMode,
    String camundaCloudClusterId,
    String camundaClientCloudRegion,
    String camundaClientId,
    String camundaClientSecret) {

  public static AppConfig load() {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    return new AppConfig(
        dotenv.get("RABBITMQ_HOST"),
        Integer.parseInt(dotenv.get("RABBITMQ_PORT")),
        dotenv.get("RABBITMQ_USERNAME"),
        dotenv.get("RABBITMQ_PASSWORD"),
        dotenv.get("RABBITMQ_QUEUE_NAME"),
        dotenv.get("GRPC_HOST"),
        Integer.parseInt(dotenv.get("GRPC_PORT")),
        dotenv.get("CAMUNDA_CLIENT_MODE"),
        dotenv.get("CAMUNDA_CLIENT_CLOUD_CLUSTER_ID"),
        dotenv.get("CAMUNDA_CLIENT_CLOUD_REGION"),
        dotenv.get("CAMUNDA_CLIENT_AUTH_CLIENT_ID"),
        dotenv.get("CAMUNDA_CLIENT_AUTH_CLIENT_SECRET"));
  }
}
