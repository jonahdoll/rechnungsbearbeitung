package com.example.zahlungsystem;

import com.example.zahlungsystem.entity.Zahlungsauftrag;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZahlungsProducer implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(ZahlungsProducer.class);
  private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

  private static final JsonMapper MAPPER =
      JsonMapper.builder().addModule(new JavaTimeModule()).build();

  private final Connection connection;
  private final String queueName;

  public ZahlungsProducer() throws Exception {
    this.queueName = dotenv.get("RABBITMQ_QUEUE_NAME");

    var factory = new ConnectionFactory();
    factory.setHost(dotenv.get("RABBITMQ_HOST"));
    factory.setPort(Integer.parseInt(dotenv.get("RABBITMQ_PORT")));
    factory.setUsername(dotenv.get("RABBITMQ_USERNAME"));
    factory.setPassword(dotenv.get("RABBITMQ_PASSWORD"));

    factory.setThreadFactory(Thread.ofVirtual().factory());

    this.connection = factory.newConnection();
  }

  public void sendeZahlungsauftrag(Zahlungsauftrag auftrag) {
    try (var channel = connection.createChannel()) {
      channel.queueDeclare(queueName, true, false, false, null);

      byte[] body = MAPPER.writeValueAsBytes(auftrag);
      channel.basicPublish("", queueName, null, body);

      logger.info("Erfolgreich an RabbitMQ gesendet: {}", auftrag.zahlungsReferenz());
    } catch (Exception e) {
      logger.error("Fehler beim Senden: {}", e.getMessage());
    }
  }

  @Override
  public void close() throws Exception {
    if (connection != null) connection.close();
  }
}
