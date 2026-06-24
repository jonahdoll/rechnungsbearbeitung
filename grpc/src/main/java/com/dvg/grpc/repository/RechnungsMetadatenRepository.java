package com.dvg.grpc.repository;

import com.dvg.grpc.entity.Rechnungsmetadaten;
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
      RETURNING id
      """;

  private static final String DELETE_RECHNUNG_BY_ID_SQL =
      "DELETE FROM rechnungsmetadaten WHERE id = ?";

  private static final String DELETE_RECHNUNG_BY_NUMMER_SQL =
      "DELETE FROM rechnungsmetadaten WHERE rechnungsnummer = ?";

  private static final Logger logger = LoggerFactory.getLogger(RechnungsMetadatenRepository.class);

  private final DataSource dataSource;

  public RechnungsMetadatenRepository(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /// Speichert eine Rechnung. Wenn eine ID übergeben wird, wird die bestehende Rechnung
  /// (inkl. Positionen via CASCADE) zuerst per ID gelöscht und dann neu eingefügt.
  /// Wenn keine ID übergeben wird, wird zusätzlich anhand der Rechnungsnummer geprüft,
  /// ob bereits ein Datensatz existiert. Falls ja, wird dieser auch gelöscht
  ///
  /// @param conn Bestehende Datenbankverbindung.
  /// @param rechnung Rechnungsmetadaten, die gespeichert werden sollen.
  /// @param isUpdate Gibt an, ob ein bestehendes Objekt anhand der ID ersetzt werden soll.
  /// @return Die generierte/beibehaltene Rechnungs-ID.
  /// @throws SQLException wenn ein Datenbankfehler auftritt.
  public UUID save(final Connection conn, final Rechnungsmetadaten rechnung, final boolean isUpdate)
      throws SQLException {
    logger.debug("rechnungsnummer={}, isUpdate={}", rechnung.rechnungsnummer(), isUpdate);
    if (isUpdate) {
      deleteRechnungById(conn, rechnung.id());
    }
    return insertRechnung(conn, rechnung);
  }

  private void deleteRechnungById(final Connection conn, final UUID id) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(DELETE_RECHNUNG_BY_ID_SQL)) {
      stmt.setObject(1, id);
      stmt.executeUpdate();
    }
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
