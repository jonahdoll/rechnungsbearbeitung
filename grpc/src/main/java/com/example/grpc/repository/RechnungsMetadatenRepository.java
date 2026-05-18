package com.example.grpc.repository;

import com.example.grpc.entity.Rechnungsmetadaten;
import java.sql.*;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Repository für Rechnungen.
public class RechnungsMetadatenRepository {
  private static final String INSERT_RECHNUNG_SQL =
      """
      INSERT INTO rechnungsmetadaten (
          id, rechnungsnummer, rechnungsdatum, faelligkeitsdatum,
          rechnungsausteller, rechnungsempfaenger, iban, bic
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT (id) DO UPDATE SET
          rechnungsnummer = EXCLUDED.rechnungsnummer,
          rechnungsdatum = EXCLUDED.rechnungsdatum,
          faelligkeitsdatum = EXCLUDED.faelligkeitsdatum,
          rechnungsausteller = EXCLUDED.rechnungsausteller,
          rechnungsempfaenger = EXCLUDED.rechnungsempfaenger,
          iban = EXCLUDED.iban,
          bic = EXCLUDED.bic
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

  private UUID insertRechnung(final Connection conn, final Rechnungsmetadaten rechnung)
      throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(INSERT_RECHNUNG_SQL)) {
      stmt.setObject(1, rechnung.id());
      stmt.setString(2, rechnung.rechnungsnummer());
      stmt.setTimestamp(3, Timestamp.valueOf(rechnung.rechnungsdatum()));
      stmt.setTimestamp(
          4,
          rechnung.faelligkeitsdatum() != null
              ? Timestamp.valueOf(rechnung.faelligkeitsdatum())
              : null);
      stmt.setString(5, rechnung.rechnungsausteller());
      stmt.setString(6, rechnung.rechnungsempfaenger());
      stmt.setString(7, rechnung.iban());
      stmt.setString(8, rechnung.bic());

      try (ResultSet result = stmt.executeQuery()) {
        if (result.next()) {
          return result.getObject("id", UUID.class);
        }
        throw new SQLException("Failed to retrieve inserted rechnungsId");
      }
    }
  }
}
