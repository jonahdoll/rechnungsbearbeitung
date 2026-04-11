package com.example.grpc.entity;

import com.example.grpc.RechnungsMetadata;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record Rechnungsposition(
        @NotNull
        UUID id,
        @NotBlank
        String artikelnummer,
        @Positive
        @NotNull
        BigDecimal menge,
        @Positive
        @NotNull
        BigDecimal einzelpreisBetrag,
        @NotBlank
        @Pattern(regexp = "[A-Z]{3}")
        String waehrung
) {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public Rechnungsposition {
        var violations = VALIDATOR.validate(this);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
    }

    public static Rechnungsposition fromProto(RechnungsMetadata.Rechnungsposition proto) {
        return new Rechnungsposition(
                UUID.randomUUID(),
                proto.getArtikelnummer(),
                BigDecimal.valueOf(proto.getMenge()),
                BigDecimal.valueOf(proto.getEinzelpreis().getBetrag()),
                proto.getEinzelpreis().getWaehrungsCode()
        );
    }
}