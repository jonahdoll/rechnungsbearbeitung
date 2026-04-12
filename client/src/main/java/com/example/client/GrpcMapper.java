package com.example.client;

import com.example.grpc.RechnungsMetadata;

public class GrpcMapper {
    public RechnungsMetadata.Rechnungsposition mapToGrpc(Position position) {
        return RechnungsMetadata.Rechnungsposition.newBuilder()
                .setArtikelnummer(position.getArtikelnummer())
                .setMenge(position.getMenge())
                .setEinzelpreis(RechnungsMetadata.Geld.newBuilder()
                        .setBetrag(position.getEinzelpreis().doubleValue())
                        .setWaehrungsCode("EUR").build())
                .build();
    }
}
