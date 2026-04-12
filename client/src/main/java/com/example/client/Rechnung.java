package com.example.client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Rechnung {
    @NotBlank
    private String rechnungsnummer;

    @NotBlank
    private String aussteller;

    @NotBlank
    private String empfaenger;

    private String steuernummer;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$")
    private String iban;

    @NotBlank
    @Size(max = 11)
    private String bic;

    @NotNull
    @Positive
    private BigDecimal gesamtBetrag;

    @NotNull
    private LocalDateTime faelligkeitsdatum;

    @NotNull
    private LocalDateTime rechnungsdatum;

    @NotEmpty
    @JacksonXmlElementWrapper(localName = "positionen")
    @JacksonXmlProperty(localName = "position")
    private List<Position> positionen;

    public Rechnung() {}

    public Rechnung(String rechnungsnummer, String aussteller, String empfaenger, String steuernummer,
                    String iban, String bic, BigDecimal gesamtBetrag, LocalDateTime faelligkeitsdatum,
                    LocalDateTime rechnungsdatum, List<Position> positionen) {
        this.rechnungsnummer = rechnungsnummer;
        this.aussteller = aussteller;
        this.empfaenger = empfaenger;
        this.steuernummer = steuernummer;
        this.iban = iban;
        this.bic = bic;
        this.gesamtBetrag = gesamtBetrag;
        this.faelligkeitsdatum = faelligkeitsdatum;
        this.rechnungsdatum = rechnungsdatum;
        this.positionen = positionen;

        BigDecimal summe = positionen == null ? BigDecimal.ZERO : positionen.stream()
                .map(p -> p.getEinzelpreis().multiply(BigDecimal.valueOf(p.getMenge())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (summe.compareTo(gesamtBetrag) != 0) {
            throw new IllegalArgumentException("Summe falsch. Einzelene Positionen: " + summe + " Gesamtbetrag: " + gesamtBetrag);
        }
    }

    public String getRechnungsnummer() { return rechnungsnummer; }

    public void setRechnungsnummer(String rechnungsnummer) { this.rechnungsnummer = rechnungsnummer; }

    public String getAussteller() { return aussteller; }

    public void setAussteller(String aussteller) { this.aussteller = aussteller; }

    public String getEmpfaenger() { return empfaenger; }

    public void setEmpfaenger(String empfaenger) { this.empfaenger = empfaenger; }

    public String getSteuernummer() { return steuernummer; }

    public void setSteuernummer(String steuernummer) { this.steuernummer = steuernummer; }

    public String getIban() { return iban; }

    public void setIban(String iban) { this.iban = iban; }

    public String getBic() { return bic; }

    public void setBic(String bic) { this.bic = bic; }

    public BigDecimal getGesamtBetrag() { return gesamtBetrag; }

    public void setGesamtBetrag(BigDecimal gesamtBetrag) { this.gesamtBetrag = gesamtBetrag; }

    public LocalDateTime getFaelligkeitsdatum() { return faelligkeitsdatum; }

    public void setFaelligkeitsdatum(LocalDateTime faelligkeitsdatum) { this.faelligkeitsdatum = faelligkeitsdatum; }

    public LocalDateTime getRechnungsdatum() { return rechnungsdatum; }

    public void setRechnungsdatum(LocalDateTime rechnungsdatum) { this.rechnungsdatum = rechnungsdatum; }

    public List<Position> getPositionen() { return positionen; }

    public void setPositionen(List<Position> positionen) { this.positionen = positionen; }
}

class Position {
    @NotBlank
    private String artikelnummer;

    @Positive
    private double menge;

    @NotNull
    @Positive
    private BigDecimal einzelpreis;

    public Position() {}

    public Position(String artikelnummer, double menge, BigDecimal einzelpreis) {
        this.artikelnummer = artikelnummer;
        this.menge = menge;
        this.einzelpreis = einzelpreis;
    }

    public String getArtikelnummer() { return artikelnummer; }

    public void setArtikelnummer(String artikelnummer) { this.artikelnummer = artikelnummer; }

    public double getMenge() { return menge; }

    public void setMenge(double menge) { this.menge = menge; }

    public BigDecimal getEinzelpreis() { return einzelpreis; }

    public void setEinzelpreis(BigDecimal einzelpreis) { this.einzelpreis = einzelpreis; }
}
