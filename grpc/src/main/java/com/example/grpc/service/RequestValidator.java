package com.example.grpc.service;

import com.example.grpc.RechnungsMetadata;

import java.util.ArrayList;
import java.util.List;

/// Validiert eingehende RechnungsmetadatenRequests.
class RequestValidator {
    public RequestValidator() {
    }

    /// Validiert einen {@link RechnungsMetadata.MetadatenSpeichernRequest}.
    ///
    /// @param request Der zu validierende Request
    /// @return Liste von Fehlermeldungen
    public List<String> validiere(final RechnungsMetadata.MetadatenSpeichernRequest request) {
        final List<String> fehler = new ArrayList<>();

        if (request.getRechnungsnummer().isBlank()) {
            fehler.add("Rechnungsnummer ist erforderlich");
        }
        if (!request.hasRechnungsdatum()) {
            fehler.add("Rechnungsdatum ist erforderlich");
        }
        if (!request.hasFaelligkeitsdatum()) {
            fehler.add("Fälligkeitsdatum ist erforderlich");
        }
        if (request.getRechnungsausteller().isBlank()) {
            fehler.add("Rechnungsausteller ist erforderlich");
        }
        if (request.getRechnungsempfaenger().isBlank()) {
            fehler.add("Rechnungsempfänger ist erforderlich");
        }
        if (request.getSteuernummeraussteller().isBlank()) {
            fehler.add("Steuernummer des Ausstellers ist erforderlich");
        }
        if (request.getIban().isBlank()) {
            fehler.add("IBAN ist erforderlich");
        }
        if (request.getBic().isBlank()) {
            fehler.add("BIC ist erforderlich");
        }
        if (request.getPositionenList().isEmpty()) {
            fehler.add("Mindestens eine Rechnungsposition erforderlich");
        }

        for (int i = 0; i < request.getPositionenList().size(); i++) {
            fehler.addAll(validierePosition(request.getPositionenList().get(i), i + 1));
        }

        return fehler;
    }

    private List<String> validierePosition(
            final RechnungsMetadata.Rechnungsposition pos,
            final int positionsnummer) {
        final List<String> positionsFehler = new ArrayList<>();

        if (pos.getArtikelnummer().isBlank()) {
            positionsFehler.add("Position " + positionsnummer + ": Artikelnummer fehlt");
        }
        if (pos.getMenge() <= 0) {
            positionsFehler.add("Position " + positionsnummer + ": Menge muss größer 0 sein");
        }
        if (!pos.hasEinzelpreis() || pos.getEinzelpreis().getBetrag() <= 0) {
            positionsFehler.add("Position " + positionsnummer + ": Einzelpreis fehlt oder ist ungültig");
        }

        return positionsFehler;
    }
}
