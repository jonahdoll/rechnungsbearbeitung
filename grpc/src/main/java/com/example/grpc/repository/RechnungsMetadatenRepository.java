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
          rechnungsnummer, rechnungsdatum, faelligkeitsdatum,
          rechnungsausteller, rechnungsempfaenger, iban, bic
      ) VALUES (?, ?, ?, ?, ?, ?, ?)
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
    logger.debug("rechnungsnummer={}", rechnung.rechnungsnummer());
    return insertRechnung(conn, rechnung);
  }

  private UUID insertRechnung(final Connection conn, final Rechnungsmetadaten rechnung) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(INSERT_RECHNUNG_SQL)) {
      stmt.setString(1, rechnung.rechnungsnummer());
      stmt.setTimestamp(2, Timestamp.valueOf(rechnung.rechnungsdatum()));
      stmt.setTimestamp(3, rechnung.faelligkeitsdatum() != null
          ? Timestamp.valueOf(rechnung.faelligkeitsdatum()) : null);
      stmt.setString(4, rechnung.rechnungsausteller());
      stmt.setString(5, rechnung.rechnungsempfaenger());
      stmt.setString(6, rechnung.iban());
      stmt.setString(7, rechnung.bic());

      try (ResultSet result = stmt.executeQuery()) {
        if (result.next()) {
          return result.getObject("id", UUID.class);
        }
        throw new SQLException("Failed to retrieve inserted rechnungsId");
      }
    }
  }
}
