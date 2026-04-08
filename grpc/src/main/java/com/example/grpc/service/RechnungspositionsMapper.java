package com.example.grpc.service;

import com.example.grpc.RechnungsMetadata;
import com.example.grpc.entity.Rechnungsposition;
import com.example.grpc.entity.RechnungspositionBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

/// Mapper für die Konvertierung von Rechnungspositionen aus Proto zu Entity.
class RechnungspositionsMapper {

    /// Konvertiert eine Liste von Proto-Rechnungspositionen zu Entity-Objekten.
    ///
    /// @param protoPositionen Liste der Protobuf-Rechnungspositionen
    /// @return Liste der Entity-Rechnungspositionen
    List<Rechnungsposition> mapToRechnungspositionen(
            final List<RechnungsMetadata.Rechnungsposition> protoPositionen) {
        return IntStream.range(0, protoPositionen.size())
                .mapToObj(i -> mapPosition(protoPositionen.get(i), i + 1))
                .toList();
    }

    private Rechnungsposition mapPosition(
            final RechnungsMetadata.Rechnungsposition protoPos,
            final int positionsnummer) {
        return new RechnungspositionBuilder()
                .setArtikelnummer(protoPos.getArtikelnummer())
                .setMenge(BigDecimal.valueOf(protoPos.getMenge()))
                .setEinzelpreisBetrag(
                        protoPos.hasEinzelpreis()
                                ? BigDecimal.valueOf(protoPos.getEinzelpreis().getBetrag())
                                : null)
                .setEinzelpreisWaehrung(
                        protoPos.hasEinzelpreis()
                                ? protoPos.getEinzelpreis().getWaehrungsCode()
                                : null)
                .setSteuersatz(BigDecimal.valueOf(protoPos.getSteuersatz()))
                .build();
    }
}
