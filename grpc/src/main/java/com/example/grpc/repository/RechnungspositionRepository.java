package com.example.grpc.repository;

import com.example.grpc.entity.Rechnungsposition;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/// Repository für Rechnungspositionen.
public class RechnungspositionRepository {
  private static final String INSERT_POSITION_SQL =
      """
      INSERT INTO rechnungspositionen (
          rechnung_id, artikelnummer, menge, einzelpreis_betrag,
          waehrung
      ) VALUES (?, ?, ?, ?, ?)
      """;

  public RechnungspositionRepository() {}

  /// Speichert Rechnungspositionen.
  ///
  /// @param conn Bestehende Datenbankverbindung.
  /// @param positionen Rechnungspositionen, die gespeichert werden sollen.
  /// @param rechnungsId Die ID der zugehörigen Rechnung.
  /// @throws SQLException SQLException.
  public void saveAll(
      final Connection conn, final List<Rechnungsposition> positionen, final UUID rechnungsId)
      throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(INSERT_POSITION_SQL)) {
      for (final Rechnungsposition position : positionen) {
        insertPosition(stmt, rechnungsId, position);
      }
      stmt.executeBatch();
    }
  }

  private void insertPosition(
      final PreparedStatement stmt, final UUID rechnungsId, final Rechnungsposition position)
      throws SQLException {
    stmt.setObject(1, rechnungsId);
    stmt.setString(2, position.artikelnummer());
    stmt.setBigDecimal(3, position.menge());
    stmt.setBigDecimal(4, position.einzelpreisBetrag());
    stmt.setString(5, position.waehrung());
    stmt.addBatch();
  }
}
