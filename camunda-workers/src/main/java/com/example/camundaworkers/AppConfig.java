package com.example.camundaworkers;

import io.github.cdimascio.dotenv.Dotenv;

public record AppConfig(
    String rabbitmqHost,
    int rabbitmqPort,
    String rabbitmqUser,
    String rabbitmqPassword,
    String rabbitmqQueue,
    String grpcHost,
    int grpcPort,
    String camundaRESTApi,
    String camundaClientId,
    String camundaClientSecret,
    String camundaAudience) {
  public static AppConfig load() {
    Dotenv dotenv = Dotenv.load();
    return new AppConfig(
        dotenv.get("RABBITMQ_HOST"),
        Integer.parseInt(dotenv.get("RABBITMQ_PORT")),
        dotenv.get("RABBITMQ_USERNAME"),
        dotenv.get("RABBITMQ_PASSWORD"),
        dotenv.get("RABBITMQ_QUEUE_NAME"),
        dotenv.get("GRPC_HOST"),
        Integer.parseInt(dotenv.get("GRPC_PORT")),
        dotenv.get("CAMUNDA_REST_API"),
        dotenv.get("CAMUNDA_CLIENT_ID"),
        dotenv.get("CAMUNDA_CLIENT_SECRET"),
        dotenv.get("CAMUNDA_AUDIENCE"));
  }
}
