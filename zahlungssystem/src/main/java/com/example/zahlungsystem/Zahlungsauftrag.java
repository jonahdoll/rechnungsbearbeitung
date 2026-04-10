package com.example.zahlungsystem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Zahlungsauftrag {
    private UUID rechnungId;
    private BigDecimal betrag;
    private String waehrung;
    private String zahlungsReferenz;
    private String empfaengerIban;
    private String empfaengerBic;
    private String empfaengerName;
    private LocalDate faelligkeitsDatum;
    private ZahlungsStatusType status;

    public Zahlungsauftrag(final UUID rechnungId, final BigDecimal betrag, final String waehrung, final String zahlungsReferenz, final String empfaengerIban, final String empfaengerBic, final String empfaengerName, final LocalDate faelligkeitsDatum, final ZahlungsStatusType status) {
        this.rechnungId = rechnungId;
        this.betrag = betrag;
        this.waehrung = waehrung;
        this.zahlungsReferenz = zahlungsReferenz;
        this.empfaengerIban = empfaengerIban;
        this.empfaengerBic = empfaengerBic;
        this.empfaengerName = empfaengerName;
        this.faelligkeitsDatum = faelligkeitsDatum;
        this.status = status;
    }

    public UUID getRechnungId() {
        return rechnungId;
    }

    public void setRechnungId(final UUID rechnungId) {
        this.rechnungId = rechnungId;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }

    public void setBetrag(final BigDecimal betrag) {
        this.betrag = betrag;
    }

    public String getWaehrung() {
        return waehrung;
    }

    public void setWaehrung(final String waehrung) {
        this.waehrung = waehrung;
    }

    public String getZahlungsReferenz() {
        return zahlungsReferenz;
    }

    public void setZahlungsReferenz(final String zahlungsReferenz) {
        this.zahlungsReferenz = zahlungsReferenz;
    }

    public String getEmpfaengerIban() {
        return empfaengerIban;
    }

    public void setEmpfaengerIban(final String empfaengerIban) {
        this.empfaengerIban = empfaengerIban;
    }

    public String getEmpfaengerBic() {
        return empfaengerBic;
    }

    public void setEmpfaengerBic(final String empfaengerBic) {
        this.empfaengerBic = empfaengerBic;
    }

    public String getEmpfaengerName() {
        return empfaengerName;
    }

    public void setEmpfaengerName(final String empfaengerName) {
        this.empfaengerName = empfaengerName;
    }

    public LocalDate getFaelligkeitsDatum() {
        return faelligkeitsDatum;
    }

    public void setFaelligkeitsDatum(final LocalDate faelligkeitsDatum) {
        this.faelligkeitsDatum = faelligkeitsDatum;
    }

    public ZahlungsStatusType getStatus() {
        return status;
    }

    public void setStatus(final ZahlungsStatusType status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Zahlungsauftrag that)) return false;
        return Objects.equals(rechnungId, that.rechnungId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rechnungId);
    }

    @Override
    public String toString() {
        return "Zahlungsauftrag{" +
                "rechnungId=" + rechnungId +
                ", betrag=" + betrag +
                ", waehrung='" + waehrung + '\'' +
                ", zahlungsReferenz='" + zahlungsReferenz + '\'' +
                ", empfaengerIban='" + empfaengerIban + '\'' +
                ", empfaengerBic='" + empfaengerBic + '\'' +
                ", empfaengerName='" + empfaengerName + '\'' +
                ", faelligkeitsDatum=" + faelligkeitsDatum +
                ", status=" + status +
                '}';
    }
}
