package com.example.grpc.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/// Entity-Klasse für die einzelnen Rechnungspositionen.
public class Rechnungsposition {
    private UUID id;
    /// ID der zugehörigen Rechnung.
    private UUID rechnungsId;
    private String artikelnummer;
    /// Die Anzahl der berechneten Einheiten.
    private BigDecimal menge;
    /// Der Preis pro Einheit.
    private BigDecimal einzelpreisBetrag;
    /// ISO-Währungscode.
    private String einzelpreisWaehrung;
    /// Der Steuersatz in Prozent.
    private BigDecimal steuersatz;

    public Rechnungsposition(final UUID id, final UUID rechnungsId, final String artikelnummer, final BigDecimal menge, final BigDecimal einzelpreisBetrag, final String einzelpreisWaehrung, final BigDecimal steuersatz) {
        this.id = id;
        this.rechnungsId = rechnungsId;
        this.artikelnummer = artikelnummer;
        this.menge = menge;
        this.einzelpreisBetrag = einzelpreisBetrag;
        this.einzelpreisWaehrung = einzelpreisWaehrung;
        this.steuersatz = steuersatz;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getRechnungsId() {
        return rechnungsId;
    }

    public void setRechnungsId(final UUID rechnungsId) {
        this.rechnungsId = rechnungsId;
    }

    public String getArtikelnummer() {
        return artikelnummer;
    }

    public void setArtikelnummer(final String artikelnummer) {
        this.artikelnummer = artikelnummer;
    }

    public BigDecimal getMenge() {
        return menge;
    }

    public void setMenge(final BigDecimal menge) {
        this.menge = menge;
    }

    public BigDecimal getEinzelpreisBetrag() {
        return einzelpreisBetrag;
    }

    public void setEinzelpreisBetrag(final BigDecimal einzelpreisBetrag) {
        this.einzelpreisBetrag = einzelpreisBetrag;
    }

    public String getEinzelpreisWaehrung() {
        return einzelpreisWaehrung;
    }

    public void setEinzelpreisWaehrung(final String einzelpreisWaehrung) {
        this.einzelpreisWaehrung = einzelpreisWaehrung;
    }

    public BigDecimal getSteuersatz() {
        return steuersatz;
    }

    public void setSteuersatz(final BigDecimal steuersatz) {
        this.steuersatz = steuersatz;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Rechnungsposition that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Rechnungsposition{" +
                "id=" + id +
                ", rechnung_id=" + rechnungsId +
                ", artikelnummer='" + artikelnummer + '\'' +
                ", menge=" + menge +
                ", einzelpreisBetrag=" + einzelpreisBetrag +
                ", einzelpreisWaehrung='" + einzelpreisWaehrung + '\'' +
                ", steuersatz=" + steuersatz +
                '}';
    }
}
