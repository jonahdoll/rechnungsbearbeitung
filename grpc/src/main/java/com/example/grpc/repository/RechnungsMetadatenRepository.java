package com.example.grpc.repository;

import com.example.grpc.entity.Rechnungsmetadaten;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/// Repository für Rechnungen.
public class RechnungsMetadatenRepository {
  private static final String INSERT_RECHNUNG_SQL = """
      INSERT INTO rechnungsmetadaten (
          rechnungsnummer, rechnungsdatum, bestellnummer, faelligkeitsdatum,
          rechnungsausteller, rechnungsempfaenger, steuernummeraussteller, iban, bic
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      RETURNING id
      """;

  private static final Logger logger = LoggerFactory.getLogger(RechnungsMetadatenRepository.class);

  private final DataSource dataSource;

  public RechnungsMetadatenRepository(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /// Speichert eine Rechnung.
  ///
  /// @param conn Bestehende Datenbankverbindung.
  /// @param rechnung Rechnungsmetadaten, die gespeichert werden sollen.
  /// @return Die generierte Rechnungs-ID.
  /// @throws SQLException wenn ein Datenbankfehler auftritt.
  public UUID save(final Connection conn, final Rechnungsmetadaten rechnung) throws SQLException {
    logger.debug("rechnungsnummer={}", rechnung.getRechnungsnummer());
    return insertRechnung(conn, rechnung);
  }

  private UUID insertRechnung(final Connection conn, final Rechnungsmetadaten rechnung) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(INSERT_RECHNUNG_SQL)) {
      stmt.setString(1, rechnung.getRechnungsnummer());
      stmt.setTimestamp(2, Timestamp.valueOf(rechnung.getRechnungsdatum()));
      stmt.setString(3, rechnung.getBestellnummer());
      stmt.setTimestamp(4, rechnung.getFaelligkeitsdatum() != null
          ? Timestamp.valueOf(rechnung.getFaelligkeitsdatum()) : null);
      stmt.setString(5, rechnung.getRechnungsausteller());
      stmt.setString(6, rechnung.getRechnungsempfaenger());
      stmt.setString(7, rechnung.getSteuernummeraussteller());
      stmt.setString(8, rechnung.getIban());
      stmt.setString(9, rechnung.getBic());

      try (ResultSet result = stmt.executeQuery()) {
        if (result.next()) {
          return result.getObject("id", UUID.class);
        }
        throw new SQLException("Failed to retrieve inserted rechnungsId");
      }
    }
  }
}
