CREATE TABLE IF NOT EXISTS rechnungsmetadaten (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rechnungsnummer VARCHAR(255) NOT NULL UNIQUE,
    rechnungsdatum TIMESTAMP WITH TIME ZONE NOT NULL,
    bestellnummer VARCHAR(255),
    faelligkeitsdatum TIMESTAMP WITH TIME ZONE,
    rechnungsausteller VARCHAR(500) NOT NULL,
    rechnungsempfaenger VARCHAR(500) NOT NULL,
    steuernummeraussteller VARCHAR(100),
    iban VARCHAR(34),
    bic VARCHAR(11),
    erstellt_am TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS rechnungspositionen (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rechnung_id UUID NOT NULL REFERENCES rechnungsmetadaten(id) ON DELETE CASCADE,
    artikelnummer VARCHAR(255) NOT NULL,
    menge DECIMAL(15, 3) NOT NULL,
    einzelpreis_betrag DECIMAL(15, 2) NOT NULL,
    einzelpreis_waehrung VARCHAR(3) NOT NULL,
    steuersatz DECIMAL(5, 2) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_positionen_rechnung_id ON rechnungspositionen(rechnung_id);