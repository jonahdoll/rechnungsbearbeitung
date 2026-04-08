package com.example.grpc.service;

import com.example.grpc.RechnungsMetadata;
import com.example.grpc.entity.Rechnungsmetadaten;
import com.example.grpc.entity.RechnungsmetadatenBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

class RechnungsmetadatenMapper {
    public RechnungsmetadatenMapper() {}

    public Rechnungsmetadaten mapToRechnungsmetadaten(RechnungsMetadata.MetadatenSpeichernRequest request) {
        return new RechnungsmetadatenBuilder()
                .setRechnungsnummer(request.getRechnungsnummer())
                .setRechnungsdatum(request.hasRechnungsdatum()
                        ? LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(
                                request.getRechnungsdatum().getSeconds(),
                                request.getRechnungsdatum().getNanos()),
                        ZoneId.systemDefault())
                        : null)
                .setBestellnummer(request.getBestellnummer())
                .setRechnungsausteller(request.getRechnungsausteller())
                .setRechnungsempfaenger(request.getRechnungsempfaenger())
                .setIban(request.getIban())
                .setBic(request.getBic())
                .build();
    }
}
