package com.example.grpc.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/// Builder-Klasse für {@link Rechnungsmetadaten}.
public class RechnungsmetadatenBuilder {
    private UUID id;
    private String rechnungsnummer;
    private LocalDateTime rechnungsdatum;
    private String bestellnummer;
    private LocalDateTime faelligkeitsdatum;
    private String rechnungsausteller;
    private String rechnungsempfaenger;
    private String steuernummeraussteller;
    private String iban;
    private String bic;
    private LocalDateTime erstelltAm;
    private List<Rechnungsposition> positionen = new ArrayList<>();

    public RechnungsmetadatenBuilder setId(final UUID id) {
        this.id = id;
        return this;
    }

    public RechnungsmetadatenBuilder setRechnungsnummer(final String rechnungsnummer) {
        this.rechnungsnummer = rechnungsnummer;
        return this;
    }

    public RechnungsmetadatenBuilder setRechnungsdatum(final LocalDateTime rechnungsdatum) {
        this.rechnungsdatum = rechnungsdatum;
        return this;
    }

    public RechnungsmetadatenBuilder setBestellnummer(final String bestellnummer) {
        this.bestellnummer = bestellnummer;
        return this;
    }

    public RechnungsmetadatenBuilder setFaelligkeitsdatum(final LocalDateTime faelligkeitsdatum) {
        this.faelligkeitsdatum = faelligkeitsdatum;
        return this;
    }

    public RechnungsmetadatenBuilder setRechnungsausteller(final String rechnungsausteller) {
        this.rechnungsausteller = rechnungsausteller;
        return this;
    }

    public RechnungsmetadatenBuilder setRechnungsempfaenger(final String rechnungsempfaenger) {
        this.rechnungsempfaenger = rechnungsempfaenger;
        return this;
    }

    public RechnungsmetadatenBuilder setSteuernummeraussteller(final String steuernummeraussteller) {
        this.steuernummeraussteller = steuernummeraussteller;
        return this;
    }

    public RechnungsmetadatenBuilder setIban(final String iban) {
        this.iban = iban;
        return this;
    }

    public RechnungsmetadatenBuilder setBic(final String bic) {
        this.bic = bic;
        return this;
    }

    public RechnungsmetadatenBuilder setErstelltAm(final LocalDateTime erstelltAm) {
        this.erstelltAm = erstelltAm;
        return this;
    }

    public RechnungsmetadatenBuilder setPositionen(final List<Rechnungsposition> positionen) {
        this.positionen = positionen != null ? new ArrayList<>(positionen) : new ArrayList<>();
        return this;
    }

    public Rechnungsmetadaten build() {
        return new Rechnungsmetadaten(
                id,
                rechnungsnummer,
                rechnungsdatum,
                bestellnummer,
                faelligkeitsdatum,
                rechnungsausteller,
                rechnungsempfaenger,
                steuernummeraussteller,
                iban,
                bic,
                erstelltAm,
                positionen
        );
    }
}
