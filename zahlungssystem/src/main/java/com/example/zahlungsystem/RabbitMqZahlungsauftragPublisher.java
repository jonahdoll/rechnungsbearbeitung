package com.example.zahlungsystem;

import com.example.zahlungsystem.entity.Zahlungsauftrag;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMqZahlungsauftragPublisher implements AutoCloseable {
  private static final Logger logger =
      LoggerFactory.getLogger(RabbitMqZahlungsauftragPublisher.class);
  private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();
  private static final JsonMapper MAPPER =
      JsonMapper.builder().addModule(new JavaTimeModule()).build();

  private final Connection connection;
  private final String queueName;

  public RabbitMqZahlungsauftragPublisher() throws Exception {
    this.queueName = value("RABBITMQ_QUEUE_NAME");

    var factory = new ConnectionFactory();
    factory.setHost(value("RABBITMQ_HOST"));
    factory.setPort(Integer.parseInt(value("RABBITMQ_PORT")));
    factory.setUsername(value("RABBITMQ_USERNAME"));
    factory.setPassword(value("RABBITMQ_PASSWORD"));
    factory.setThreadFactory(Thread.ofVirtual().factory());

    this.connection = factory.newConnection();
  }

  public void publish(Zahlungsauftrag auftrag) throws Exception {
    try (var channel = connection.createChannel()) {
      channel.queueDeclare(queueName, true, false, false, null);
      channel.basicPublish("", queueName, null, MAPPER.writeValueAsBytes(auftrag));
      logger.info("Zahlungsauftrag {} an RabbitMQ gesendet.", auftrag.zahlungsReferenz());
    }
  }

  @Override
  public void close() throws Exception {
    connection.close();
  }

  private static String value(String key) {
    String value = DOTENV.get(key);
    if (value == null || value.isBlank()) {
      value = System.getenv(key);
    }
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Umgebungsvariable fehlt: " + key);
    }
    return value;
  }
}
