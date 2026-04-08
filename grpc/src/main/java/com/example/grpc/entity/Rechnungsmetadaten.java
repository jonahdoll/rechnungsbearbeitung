package com.example.grpc.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/// Entity-Klasse für die Rechnungsmetadaten.
public class Rechnungsmetadaten {
    private UUID id;
    private String rechnungsnummer;
    private LocalDateTime rechnungsdatum;
    private String bestellnummer;
    /// Das Datum an dem die Rechnung bezahlt sein muss.
    private LocalDateTime faelligkeitsdatum;
    private String rechnungsausteller;
    private String rechnungsempfaenger;
    private String steuernummeraussteller;
    private String iban;
    private String bic;
    private LocalDateTime erstelltAm;
    /// Die zugehörigen Rechnungspositionen.
    private List<Rechnungsposition> positionen;

    public Rechnungsmetadaten(final UUID id, final String rechnungsnummer, final LocalDateTime rechnungsdatum, final String bestellnummer, final LocalDateTime faelligkeitsdatum, final String rechnungsausteller, final String rechnungsempfaenger, final String steuernummeraussteller, final String iban, final String bic, final LocalDateTime erstelltAm, final List<Rechnungsposition> positionen) {
        this.id = id;
        this.rechnungsnummer = rechnungsnummer;
        this.rechnungsdatum = rechnungsdatum;
        this.bestellnummer = bestellnummer;
        this.faelligkeitsdatum = faelligkeitsdatum;
        this.rechnungsausteller = rechnungsausteller;
        this.rechnungsempfaenger = rechnungsempfaenger;
        this.steuernummeraussteller = steuernummeraussteller;
        this.iban = iban;
        this.bic = bic;
        this.erstelltAm = erstelltAm;
        this.positionen = positionen;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getRechnungsnummer() {
        return rechnungsnummer;
    }

    public void setRechnungsnummer(final String rechnungsnummer) {
        this.rechnungsnummer = rechnungsnummer;
    }

    public LocalDateTime getRechnungsdatum() {
        return rechnungsdatum;
    }

    public void setRechnungsdatum(final LocalDateTime rechnungsdatum) {
        this.rechnungsdatum = rechnungsdatum;
    }

    public String getBestellnummer() {
        return bestellnummer;
    }

    public void setBestellnummer(final String bestellnummer) {
        this.bestellnummer = bestellnummer;
    }

    public LocalDateTime getFaelligkeitsdatum() {
        return faelligkeitsdatum;
    }

    public void setFaelligkeitsdatum(final LocalDateTime faelligkeitsdatum) {
        this.faelligkeitsdatum = faelligkeitsdatum;
    }

    public String getRechnungsausteller() {
        return rechnungsausteller;
    }

    public void setRechnungsausteller(final String rechnungsausteller) {
        this.rechnungsausteller = rechnungsausteller;
    }

    public String getRechnungsempfaenger() {
        return rechnungsempfaenger;
    }

    public void setRechnungsempfaenger(final String rechnungsempfaenger) {
        this.rechnungsempfaenger = rechnungsempfaenger;
    }

    public String getSteuernummeraussteller() {
        return steuernummeraussteller;
    }

    public void setSteuernummeraussteller(final String steuernummeraussteller) {
        this.steuernummeraussteller = steuernummeraussteller;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(final String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(final String bic) {
        this.bic = bic;
    }

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }

    public void setErstelltAm(final LocalDateTime erstelltAm) {
        this.erstelltAm = erstelltAm;
    }

    public List<Rechnungsposition> getPositionen() {
        return positionen;
    }

    public void setPositionen(final List<Rechnungsposition> positionen) {
        this.positionen = positionen;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Rechnungsmetadaten that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RechnungsMetadaten{" +
                "id=" + id +
                ", bic='" + bic + '\'' +
                ", rechnungsnummer='" + rechnungsnummer + '\'' +
                ", bestellnummer='" + bestellnummer + '\'' +
                ", rechnungsdatum=" + rechnungsdatum +
                ", faelligkeitsdatum=" + faelligkeitsdatum +
                ", rechnungsausteller='" + rechnungsausteller + '\'' +
                ", rechnungsempfaenger='" + rechnungsempfaenger + '\'' +
                ", steuernummeraussteller='" + steuernummeraussteller + '\'' +
                ", iban='" + iban + '\'' +
                '}';
    }
}
