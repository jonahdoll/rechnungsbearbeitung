package com.example.zahlungsystem.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public record Zahlungsauftrag(
    @NotBlank String zahlungsReferenz,
    @Positive @NotNull BigDecimal betrag,
    @NotBlank @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$") String iban,
    @NotNull LocalDateTime faelligkeitsdatum) {
  private static final Validator VALIDATOR =
      Validation.buildDefaultValidatorFactory().getValidator();

  public Zahlungsauftrag(
      String zahlungsReferenz, BigDecimal betrag, String iban, LocalDateTime faelligkeitsdatum) {
    this.zahlungsReferenz = zahlungsReferenz;
    this.betrag = betrag;
    this.iban = iban;
    this.faelligkeitsdatum = faelligkeitsdatum;

    var violations = VALIDATOR.validate(this);
    if (!violations.isEmpty()) {
      String fehler =
          violations.stream()
              .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
              .collect(Collectors.joining(", "));

      throw new IllegalArgumentException("Ungueltiger Zahlungsauftrag: " + fehler);
    }
  }
}
