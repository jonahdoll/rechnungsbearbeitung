package com.example.grpc.entity;

import com.example.grpc.RechnungsMetadata;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public record Rechnungsmetadaten(
        @NotNull
        UUID id,
        @NotBlank
        String rechnungsnummer,
        @NotNull
        LocalDateTime rechnungsdatum,
        LocalDateTime faelligkeitsdatum,
        @NotBlank
        String rechnungsausteller,
        @NotBlank
        String rechnungsempfaenger,
        @NotEmpty
        @Valid
        List<Rechnungsposition> positionen,
        @NotBlank
        @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$")
        String iban,
        @NotBlank
        @Size(max = 11)
        String bic
) {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public Rechnungsmetadaten(UUID id, String rechnungsnummer, LocalDateTime rechnungsdatum,
                              LocalDateTime faelligkeitsdatum, String rechnungsausteller,
                              String rechnungsempfaenger, List<Rechnungsposition> positionen,
                              String iban, String bic) {
        this.id = id;
        this.rechnungsnummer = rechnungsnummer;
        this.rechnungsdatum = rechnungsdatum;
        this.faelligkeitsdatum = faelligkeitsdatum;
        this.rechnungsausteller = rechnungsausteller;
        this.rechnungsempfaenger = rechnungsempfaenger;
        this.positionen = positionen;
        this.iban = iban;
        this.bic = bic;

        var violations = VALIDATOR.validate(this);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
    }

    public static Rechnungsmetadaten fromProto(RechnungsMetadata.MetadatenSpeichernRequest req) {
        return new Rechnungsmetadaten(
                UUID.randomUUID(),
                req.getRechnungsnummer(),
                convertTimestamp(req.getRechnungsdatum()),
                req.hasFaelligkeitsdatum() ? convertTimestamp(req.getFaelligkeitsdatum()) : null,
                req.getRechnungsausteller(),
                req.getRechnungsempfaenger(),
                req.getPositionenList().stream().map(Rechnungsposition::fromProto).toList(),
                req.getIban(),
                req.getBic()
        );
    }

    private static LocalDateTime convertTimestamp(com.google.protobuf.Timestamp ts) {
        if (ts == null || (ts.getSeconds() == 0 && ts.getNanos() == 0)) return LocalDateTime.now();
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()), ZoneId.systemDefault());
    }
}