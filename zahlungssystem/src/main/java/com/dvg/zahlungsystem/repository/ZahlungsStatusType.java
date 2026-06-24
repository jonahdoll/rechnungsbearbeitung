package com.dvg.zahlungsystem.repository;

public enum ZahlungsStatusType {
  AUSSTEHEND,
  IN_BEARBEITUNG,
  ABGESCHLOSSEN,
  FEHLGESCHLAGEN;

  public boolean isTerminal() {
    return this == ABGESCHLOSSEN || this == FEHLGESCHLAGEN;
  }
}
