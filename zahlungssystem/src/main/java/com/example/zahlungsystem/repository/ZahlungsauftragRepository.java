package com.example.zahlungsystem.repository;

import com.example.zahlungsystem.entity.Zahlungsauftrag;
import java.sql.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Repository für die Verwaltung von Zahlungsaufträgen.
public class ZahlungsauftragRepository {

  private static final Logger logger = LoggerFactory.getLogger(ZahlungsauftragRepository.class);

  private static final String INSERT_ZAHLUNG_SQL =
      """
        INSERT INTO zahlungsauftraege (
            zahlungs_referenz, betrag, iban, faelligkeitsdatum, status
        ) VALUES (?, ?, ?, ?, ?::zahlungs_status_type)
        """;

  private static final String UPDATE_STATUS_SQL =
      "UPDATE zahlungsauftraege SET status = ?::zahlungs_status_type WHERE zahlungs_referenz = ?";

  private final DataSource dataSource;

  public ZahlungsauftragRepository(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /// Speichert einen neuen Zahlungsauftrag.
  ///
  /// @param conn Die Datenbankverbindung.
  /// @param auftrag Der zu speichernde Zahlungsauftrag.
  /// @throws SQLException Falls ein Datenbankfehler auftritt.
  public void save(final Connection conn, final Zahlungsauftrag auftrag) throws SQLException {
    logger.debug("Speichere Zahlungsauftrag: Referenz={}", auftrag.zahlungsReferenz());
    insertZahlungsauftrag(conn, auftrag);
  }

  private void insertZahlungsauftrag(final Connection conn, final Zahlungsauftrag auftrag)
      throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(INSERT_ZAHLUNG_SQL)) {
      stmt.setString(1, auftrag.zahlungsReferenz());
      stmt.setBigDecimal(2, auftrag.betrag());
      stmt.setString(3, auftrag.iban());
      stmt.setTimestamp(4, Timestamp.valueOf(auftrag.faelligkeitsdatum()));
      stmt.setString(5, ZahlungsStatusType.AUSSTEHEND.toString());

      int affectedRows = stmt.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Speichern des Zahlungsauftrags fehlgeschlagen.");
      }

      logger.info("Zahlungsauftrag erfolgreich gespeichert: {}", auftrag.zahlungsReferenz());
    } catch (SQLException e) {
      logger.error("Fehler beim Insert des Zahlungsauftrags: {}", e.getMessage());
      throw e;
    }
  }

  public void updateStatus(java.sql.Connection conn, String ref, ZahlungsStatusType status)
      throws SQLException {
    try (var stmt = conn.prepareStatement(UPDATE_STATUS_SQL)) {
      stmt.setString(1, String.valueOf(status));
      stmt.setString(2, ref);
      stmt.executeUpdate();
    }
  }
}
