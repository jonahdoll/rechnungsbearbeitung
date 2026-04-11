CREATE TYPE zahlungs_status_type AS ENUM ('AUSSTEHEND', 'IN_BEARBEITUNG', 'ABGESCHLOSSEN', 'FEHLGESCHLAGEN');

CREATE TABLE zahlungsauftraege (
    zahlungs_referenz VARCHAR(255) PRIMARY KEY,
    betrag DECIMAL(15, 2) NOT NULL CHECK (betrag > 0),
    iban VARCHAR(34) NOT NULL,
    faelligkeitsdatum TIMESTAMP NOT NULL,
    status zahlungs_status_type NOT NULL
);

CREATE INDEX idx_auftraege_zeitstempel ON zahlungsauftraege(faelligkeitsdatum);
CREATE INDEX idx_auftraege_status ON zahlungsauftraege(status);