package com.example.grpc.entity;

import java.math.BigDecimal;
import java.util.UUID;

/// Builder-Klasse für {@link Rechnungsposition}.
public class RechnungspositionBuilder {
    private UUID id;
    private UUID rechnungsId;
    private String artikelnummer;
    private BigDecimal menge;
    private BigDecimal einzelpreisBetrag;
    private String einzelpreisWaehrung;
    private BigDecimal steuersatz;

    public RechnungspositionBuilder setId(final UUID id) {
        this.id = id;
        return this;
    }

    public RechnungspositionBuilder setRechnungId(final UUID rechnungsId) {
        this.rechnungsId = rechnungsId;
        return this;
    }

    public RechnungspositionBuilder setArtikelnummer(final String artikelnummer) {
        this.artikelnummer = artikelnummer;
        return this;
    }

    public RechnungspositionBuilder setMenge(final BigDecimal menge) {
        this.menge = menge;
        return this;
    }

    public RechnungspositionBuilder setEinzelpreisBetrag(final BigDecimal einzelpreisBetrag) {
        this.einzelpreisBetrag = einzelpreisBetrag;
        return this;
    }

    public RechnungspositionBuilder setEinzelpreisWaehrung(final String einzelpreisWaehrung) {
        this.einzelpreisWaehrung = einzelpreisWaehrung;
        return this;
    }

    public RechnungspositionBuilder setSteuersatz(final BigDecimal steuersatz) {
        this.steuersatz = steuersatz;
        return this;
    }

    public Rechnungsposition build() {
        return new Rechnungsposition(
                id,
                rechnungsId,
                artikelnummer,
                menge,
                einzelpreisBetrag,
                einzelpreisWaehrung,
                steuersatz
        );
    }
}
