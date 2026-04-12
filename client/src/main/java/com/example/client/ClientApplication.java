package com.example.client;

import com.example.zahlungsystem.ZahlungsProducer;
import com.example.zahlungsystem.entity.Zahlungsauftrag;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientApplication {
  private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);
  private static final XmlMapper xmlMapper = createXmlMapper();

  private static XmlMapper createXmlMapper() {
    XmlMapper mapper = new XmlMapper();
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  public static void main(String[] args) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor();
        var client = new RechnungClient()) {

      Rechnungen rechnungen =
          xmlMapper.readValue(
              ClientApplication.class.getResourceAsStream("/rechnungen.xml"), Rechnungen.class);

      rechnungen
          .getRechnungen()
          .forEach(rechnung -> executor.submit(() -> verarbeite(client, rechnung)));

      executor.shutdown();
      if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
        logger.warn("Executor hat nicht rechtzeitig beendet. Shutdown...");
        executor.shutdownNow();
      }

    } catch (Exception e) {
      logger.error("Systemfehler", e);
    }
  }

  private static void verarbeite(RechnungClient client, Rechnung rechnung) {
    try {
      if (client.speichereRechnungsMetadaten(rechnung).getResponseCode() == 200) {
        sendeZahlungsauftrag(rechnung);
      }
    } catch (Exception e) {
      logger.error("Fehler bei Rechnung {}: {}", rechnung.getRechnungsnummer(), e.getMessage());
    }
  }

  private static void sendeZahlungsauftrag(Rechnung r) throws Exception {
    try (var producer = new ZahlungsProducer()) {
      producer.sendeZahlungsauftrag(
          new Zahlungsauftrag(
              r.getRechnungsnummer(),
              r.getGesamtBetrag(),
              r.getIban(),
              LocalDateTime.now().plusDays(30)));
    }
  }
}
