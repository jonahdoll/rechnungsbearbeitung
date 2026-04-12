package com.example.zahlungsystem;

import com.example.zahlungsystem.config.DatabaseConfig;
import com.example.zahlungsystem.config.DatabaseMigration;
import com.example.zahlungsystem.entity.Zahlungsauftrag;
import com.example.zahlungsystem.repository.ZahlungsStatusType;
import com.example.zahlungsystem.repository.ZahlungsauftragRepository;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZahlungsConsumer {
  private static final Logger logger = LoggerFactory.getLogger(ZahlungsConsumer.class);
  private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

  private static final JsonMapper MAPPER =
      JsonMapper.builder().addModule(new JavaTimeModule()).build();

  private final ZahlungsauftragRepository repository;

  public ZahlungsConsumer() {
    this.repository = new ZahlungsauftragRepository(DatabaseConfig.getInstance());
  }

  public void start() throws IOException, TimeoutException {
    var factory = new ConnectionFactory();
    factory.setHost(dotenv.get("RABBITMQ_HOST"));
    factory.setPort(Integer.parseInt(dotenv.get("RABBITMQ_PORT")));
    factory.setUsername(dotenv.get("RABBITMQ_USERNAME"));
    factory.setPassword(dotenv.get("RABBITMQ_PASSWORD"));

    factory.setThreadFactory(Thread.ofVirtual().factory());

    try (var connection = factory.newConnection();
        var channel = connection.createChannel()) {

      String queue = dotenv.get("RABBITMQ_QUEUE_NAME");
      channel.queueDeclare(queue, true, false, false, null);
      channel.basicQos(10);

      logger.info("ZahlungsConsumer bereit. Warte auf Nachrichten in '{}'...", queue);

      DeliverCallback deliverCallback =
          (_, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            processMessage(channel, delivery.getEnvelope().getDeliveryTag(), message);
          };

      channel.basicConsume(queue, false, deliverCallback, _ -> {});

      Thread.currentThread().join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Consumer unterbrochen.");
    }
  }

  private void processMessage(Channel channel, long tag, String message) {
    String ref = "Unbekannt";
    try {
      Zahlungsauftrag auftrag = MAPPER.readValue(message, Zahlungsauftrag.class);
      ref = auftrag.zahlungsReferenz();

      try (java.sql.Connection conn = DatabaseConfig.getInstance().getConnection()) {
        repository.save(conn, auftrag);
        logger.info(
            "Zahlungsauftrag in Datenbank registriert. Referenz: {}, Status: AUSSTEHEND", ref);

        repository.updateStatus(conn, ref, ZahlungsStatusType.IN_BEARBEITUNG);
        logger.info("Zahlvorgang wird eingeleitet. Referenz: {}, Status: IN_BEARBEITUNG", ref);

        Thread.sleep(1000);

        boolean erfolg = Math.random() > 0.1;

        if (erfolg) {
          repository.updateStatus(conn, ref, ZahlungsStatusType.ABGESCHLOSSEN);
          logger.info(
              "Zahlvorgang erfolgreich abgeschlossen. Referenz: {}, Status: ABGESCHLOSSEN", ref);
        } else {
          repository.updateStatus(conn, ref, ZahlungsStatusType.FEHLGESCHLAGEN);
          logger.warn("Zahlvorgang abgebrochen. Referenz: {}, Status: FEHLGESCHLAGEN", ref);
        }

        channel.basicAck(tag, false);
        logger.debug("RabbitMQ-Bestatigung gesendet für Referenz: {}", ref);
      }
    } catch (Exception e) {
      logger.error("Fehler bei der Verarbeitung der Referenz {}: {}", ref, e.getMessage());
      try {
        channel.basicNack(tag, false, true);
        logger.info("Zahlung {} aufgrund eines technischen Fehlers zurückgestellt.", ref);
      } catch (Exception nackEx) {
        logger.error("Nack konnte nicht gesendet werden: {}", nackEx.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    try {
      DatabaseMigration.migrate();

      new ZahlungsConsumer().start();
    } catch (Exception e) {
      logger.error("Systemfehler: {}", e.getMessage());
      System.exit(1);
    }
  }
}
