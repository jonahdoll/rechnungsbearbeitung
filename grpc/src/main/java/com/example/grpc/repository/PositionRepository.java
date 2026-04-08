package com.example.grpc.repository;

import com.example.grpc.entity.Rechnungsposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/// Repository für Rechnungspositionen.
public class PositionRepository {
  private static final String INSERT_POSITION_SQL = """
      INSERT INTO rechnungspositionen (
          rechnung_id, artikelnummer, menge, einzelpreis_betrag,
          einzelpreis_waehrung, steuersatz
      ) VALUES (?, ?, ?, ?, ?, ?)
      """;

  private static final Logger logger = LoggerFactory.getLogger(PositionRepository.class);

  private final DataSource dataSource;

  public PositionRepository(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /// Speichert Rechnungspositionen.
  ///
  /// @param conn Bestehende Datenbankverbindung.
  /// @param positionen Rechnungspositionen, die gespeichert werden sollen.
  /// @param rechnungsId Die ID der zugehörigen Rechnung.
  /// @throws SQLException SQLException.
  public void saveAll(final Connection conn, final List<Rechnungsposition> positionen, final UUID rechnungsId) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(INSERT_POSITION_SQL)) {
      positionen.forEach(position -> {
            try {
                insertPosition(stmt, rechnungsId, position);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
      );
      stmt.executeBatch();
    }
  }

  private void insertPosition(final PreparedStatement stmt, final UUID rechnungsId, final Rechnungsposition position) throws SQLException {
    stmt.setObject(1, rechnungsId);
    stmt.setString(2, position.getArtikelnummer());
    stmt.setBigDecimal(3, position.getMenge());
    stmt.setBigDecimal(4, position.getEinzelpreisBetrag());
    stmt.setString(5, position.getEinzelpreisWaehrung());
    stmt.setBigDecimal(6, position.getSteuersatz());
    stmt.addBatch();
  }
}
