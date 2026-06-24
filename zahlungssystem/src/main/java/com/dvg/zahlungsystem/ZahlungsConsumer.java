package com.dvg.zahlungsystem;

import com.dvg.zahlungsystem.config.DatabaseConfig;
import com.dvg.zahlungsystem.config.DatabaseMigration;
import com.dvg.zahlungsystem.entity.Zahlungsauftrag;
import com.dvg.zahlungsystem.repository.ZahlungsStatusType;
import com.dvg.zahlungsystem.repository.ZahlungsauftragRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZahlungsConsumer {
  private static final Logger logger = LoggerFactory.getLogger(ZahlungsConsumer.class);
  private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

  private static final JsonMapper MAPPER =
      JsonMapper.builder().addModule(new JavaTimeModule()).build();

  private final ZahlungsauftragRepository repository;
  private final CountDownLatch shutdownLatch = new CountDownLatch(1);

  public ZahlungsConsumer() {
    this.repository = new ZahlungsauftragRepository();
  }

  public void start() throws IOException, TimeoutException {
    var factory = new ConnectionFactory();
    factory.setHost(dotenv.get("RABBITMQ_HOST"));
    factory.setPort(Integer.parseInt(dotenv.get("RABBITMQ_PORT")));
    factory.setUsername(dotenv.get("RABBITMQ_USERNAME"));
    factory.setPassword(dotenv.get("RABBITMQ_PASSWORD"));
    factory.setAutomaticRecoveryEnabled(true);
    factory.setTopologyRecoveryEnabled(true);

    try (var connection = factory.newConnection();
        var channel = connection.createChannel()) {

      String queue = dotenv.get("RABBITMQ_QUEUE_NAME");
      channel.exchangeDeclare(queue + ".dlx", "direct", true);
      channel.queueDeclare(queue + ".dlq", true, false, false, null);
      channel.queueBind(queue + ".dlq", queue + ".dlx", queue);

      Map<String, Object> args = Map.of("x-dead-letter-exchange", queue + ".dlx");
      try {
        channel.queueDeclare(queue, true, false, false, args);
      } catch (IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("inequivalent")) {
          logger.warn(
              "Queue '{}' existiert mit abweichenden Argumenten — lösche und erstelle neu.", queue);
          channel.queueDelete(queue);
          channel.queueDeclare(queue, true, false, false, args);
        } else {
          throw e;
        }
      }

      // nur 1 Nachricht gleichzeitig
      channel.basicQos(1);

      logger.info("ZahlungsConsumer bereit. Warte auf Nachrichten in '{}'...", queue);

      DeliverCallback deliverCallback =
          (_, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            processMessage(channel, delivery.getEnvelope().getDeliveryTag(), message);
          };

      channel.basicConsume(queue, false, deliverCallback, _ -> {});
      shutdownLatch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.info("RabbitMQ-Consumer sauber beendet.");
    }
  }

  private void processMessage(Channel channel, long tag, String message) {
    String ref = "Unbekannt";
    boolean dbCommitted = false;
    boolean transientError = true;
    try {
      Zahlungsauftrag auftrag = MAPPER.readValue(message, Zahlungsauftrag.class);
      ref = auftrag.zahlungsReferenz();

      try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
        conn.setAutoCommit(false);

        var currentStatus = repository.findStatus(conn, ref);
        if (currentStatus.isPresent() && currentStatus.get().isTerminal()) {
          logger.info(
              "Zahlung {} bereits abgeschlossen (Status: {}). Überspringe.",
              ref,
              currentStatus.get());
          commitTransaction(conn);
          dbCommitted = true;
        } else {
          if (currentStatus.isEmpty()) {
            repository.save(conn, auftrag);
            logger.info(
                "Zahlungsauftrag in Datenbank registriert. Referenz: {}, Status: AUSSTEHEND", ref);
          } else {
            logger.info("Zahlung {} wiederaufgenommen (Status: {}).", ref, currentStatus.get());
          }

          repository.updateStatus(conn, ref, ZahlungsStatusType.IN_BEARBEITUNG);
          logger.info("Zahlvorgang wird eingeleitet. Referenz: {}, Status: IN_BEARBEITUNG", ref);

          boolean erfolg = Math.random() > 0.1;
          ZahlungsStatusType endStatus =
              erfolg ? ZahlungsStatusType.ABGESCHLOSSEN : ZahlungsStatusType.FEHLGESCHLAGEN;

          repository.updateStatus(conn, ref, endStatus);
          if (erfolg) {
            logger.info(
                "Zahlvorgang erfolgreich abgeschlossen. Referenz: {}, Status: ABGESCHLOSSEN", ref);
          } else {
            logger.warn("Zahlvorgang abgebrochen. Referenz: {}, Status: FEHLGESCHLAGEN", ref);
          }

          commitTransaction(conn);
          dbCommitted = true;
        }
      }
    } catch (SQLException e) {
      logger.error("Datenbankfehler bei Referenz {}: {}", ref, e.toString());
    } catch (JsonProcessingException | IllegalArgumentException e) {
      transientError = false;
      logger.error("Fehlerhafte Nachricht, wird verworfen: {}", e.toString());
    } catch (Exception e) {
      logger.error("Transienter Fehler bei Referenz {}: {}", ref, e.toString());
    }

    if (dbCommitted) {
      safeAck(channel, tag, ref);
    } else if (!transientError) {
      safeReject(channel, tag, ref);
    } else {
      safeNack(channel, tag, ref);
    }
  }

  private static void commitTransaction(Connection conn) throws SQLException {
    try {
      conn.commit();
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException rollbackEx) {
        logger.error("Rollback fehlgeschlagen: {}", rollbackEx.getMessage());
      }
      throw e;
    }
  }

  private void safeAck(Channel channel, long tag, String ref) {
    try {
      channel.basicAck(tag, false);
      logger.debug("ACK gesendet für Referenz: {}", ref);
    } catch (Exception e) {
      logger.error(
          "ACK fehlgeschlagen für Referenz {} — DB ist committed, "
              + "Redelivery wird durch Idempotenz abgefangen.",
          ref,
          e);
    }
  }

  private void safeNack(Channel channel, long tag, String ref) {
    try {
      channel.basicNack(tag, false, true);
      logger.info("NACK gesendet — Zahlung {} wird zurückgestellt.", ref);
    } catch (Exception e) {
      logger.error("NACK fehlgeschlagen für Referenz {}: {}", ref, e.getMessage());
    }
  }

  private void safeReject(Channel channel, long tag, String ref) {
    try {
      channel.basicReject(tag, false);
      logger.warn("REJECT gesendet — Zahlung {} in Dead Letter Queue verschoben.", ref);
    } catch (Exception e) {
      logger.error("Reject fehlgeschlagen für Referenz {}: {}", ref, e.getMessage());
    }
  }

  public void shutdown() {
    logger.info("Shutdown-Signal empfangen.");
    shutdownLatch.countDown();
  }

  static void main() {
    try {
      DatabaseMigration.migrate();

      ZahlungsConsumer consumer = new ZahlungsConsumer();
      Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(consumer::shutdown));

      consumer.start();
    } catch (Exception e) {
      logger.error("Systemfehler: {} — {}", e.getClass().getSimpleName(), e.getMessage());
      System.exit(1);
    } finally {
      logger.info("Schließe Datenbankverbindungspool...");
      DatabaseConfig.shutdown();
    }
  }
}
