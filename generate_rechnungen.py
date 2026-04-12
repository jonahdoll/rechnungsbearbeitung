#!/usr/bin/env python3
"""
Mithilfe von Gemini generiert um realistische Testdaten für die Rechnungsbearbeitung zu erstellen.
Generator für 100 XML-Rechnungen für den Rechnungsbearbeitung-Client.
Berücksichtigt alle Client-Validierungen:
- artikelnummer darf nicht leer sein
- menge muss > 0 sein
- einzelpreis darf nicht null sein
"""

import random
from datetime import datetime, timedelta
from decimal import Decimal

# Aussteller-Pool (Unternehmen)
AUSSTELLER = [
    "Tech Software GmbH",
    "IT Solutions AG",
    "Software Werke GmbH & Co. KG",
    "Digital Services Deutschland AG",
    "Cloud Computing GmbH",
    "Enterprise IT Solutions GmbH",
    "Software Engineering AG",
    "Tech Innovations GmbH",
    "Digital Business Solutions AG",
    "Future Tech Systems GmbH",
    "Data Processing Center AG",
    "Smart Software GmbH",
    "Business Application Services AG",
    "Modern IT Systems GmbH",
    "Professional Software GmbH",
]

# Empfänger-Pool (Personen und Firmen)
EMPFAENGER = [
    "Max Mustermann",
    "Anna Schmidt",
    "Peter Müller",
    "Lisa Weber",
    "Thomas Fischer",
    "Klaus Bauer",
    "Sandra Klein",
    "Michael Richter",
    "Claudia Wolf",
    "Stefan Schröder",
    "Müller & Co. GmbH",
    "Schmidt Engineering AG",
    "Bauer & Söhne OHG",
    "Weber Solutions GmbH",
    "Klein IT Services GmbH",
    "Fischer Consulting KG",
    "Richter Logistik AG",
    "Wolf & Partner GmbH",
    "Schroeder Industries AG",
    "Tobias Neumann",
    "Katharina Berger",
    "Frank Hoffmann",
    "Nina Lehmann",
    "Ulrich Krause",
    "Buschmann GmbH",
    "Eckhardt AG",
    "Schulz Systems GmbH",
    "Werner Digital GmbH",
    "Kraus & Co. KG",
    "Peters Software AG",
]

# Deutsche BICs
BICS = [
    "GENODEF1XXX",
    "DEUTDEFFXXX",
    "HYVEDEMMXXX",
    "COBADEFFXXX",
    "DRESDEFFXXX",
    "KFWIDEFFXXX",
    "PBNKDEFFXXX",
    "NWBKDEFFXXX",
    "SOLADESTXXX",
    "FRSPDE66XXX",
]

# Artikeldaten
ARTIKEL = [
    ("LIZENZ-01", "Software Lizenz Basis", (99.00, 499.00)),
    ("LIZENZ-02", "Software Lizenz Professional", (299.00, 999.00)),
    ("LIZENZ-03", "Software Lizenz Enterprise", (999.00, 4999.00)),
    ("SUPPORT-01", "Support Vertrag Basis", (49.90, 149.90)),
    ("SUPPORT-02", "Support Vertrag Premium", (199.00, 499.00)),
    ("SUPPORT-03", "Support Vertrag 24/7", (499.00, 1999.00)),
    ("HARDWARE-01", "Server Hardware Basis", (999.00, 2999.00)),
    ("HARDWARE-02", "Server Hardware Premium", (2999.00, 7999.00)),
    ("HARDWARE-03", "Workstation Professional", (799.00, 2499.00)),
    ("CLOUD-01", "Cloud Storage 1TB", (9.99, 49.99)),
    ("CLOUD-02", "Cloud Storage 10TB", (49.99, 199.99)),
    ("BACKUP-01", "Backup Lösung Basis", (29.99, 99.99)),
    ("BACKUP-02", "Backup Lösung Enterprise", (199.00, 599.00)),
    ("CONSULT-01", "Beratung Standard", (95.00, 195.00)),
    ("CONSULT-02", "Beratung Senior", (195.00, 395.00)),
]


def generate_iban():
    """Generiert eine gültige deutsche IBAN."""
    kontonr = ''.join([str(random.randint(0, 9)) for _ in range(10)])
    blz = random.choice([
        "50010517",  # ING
        "10070024",  # Deutsche Bank
        "50010060",  # BHF-Bank
        "20030000",  # UniCredit
        "37020500",  # Oldenburgische Landesbank
        "43050001",  # Sparkasse Bochum
        "60050101",  # Sparkasse Karlsruhe
        "70022200",  # MKB Mittelstandskreditbank
        "76010085",  # Postbank
        "80053772",  # Bundesbank
        "85020500",  # Landesbank Baden-Württemberg
        "90040060",  # Commerzbank München
    ])
    # Prüfziffer berechnen (vereinfacht für DE)
    iban_prefix = "DE00{}{}".format(blz, kontonr)
    # Konvertiere in numerisch: DE=1314, A=10...
    iban_numeric = iban_prefix[4:] + "131400"
    for char in iban_prefix[:4]:
        if char.isalpha():
            iban_numeric += str(ord(char) - ord('A') + 10)
        else:
            iban_numeric += char
    pruef = 98 - (int(iban_numeric) % 97)
    pruef_str = str(pruef).zfill(2)
    return f"DE{pruef_str}{blz}{kontonr}"


def generate_steuernummer():
    """Generiert eine deutsche Steuernummer im Format XXX/XXX/XXXX."""
    return "{}/{}/{}".format(
        random.randint(100, 999),
        random.randint(100, 999),
        random.randint(1000, 9999)
    )


def generate_position():
    """Generiert eine Rechnungsposition mit gültigen Werten."""
    artikel_nr, _, preis_range = random.choice(ARTIKEL)
    menge = round(random.uniform(1, 10), 1) if random.random() > 0.8 else float(random.randint(1, 5))
    einzelpreis = round(random.uniform(preis_range[0], preis_range[1]), 2)
    return {
        "artikelnummer": artikel_nr,
        "menge": menge,
        "einzelpreis": einzelpreis,
    }


def generate_rechnung(nr):
    """Generiert eine einzelne Rechnung."""
    aussteller = random.choice(AUSSTELLER)
    empfaenger = random.choice(EMPFAENGER)
    steuernummer = generate_steuernummer()
    iban = generate_iban()
    bic = random.choice(BICS)

    # 1-5 Positionen pro Rechnung
    anzahl_positionen = random.choices([1, 2, 3, 4, 5], weights=[30, 30, 20, 15, 5])[0]
    positionen = [generate_position() for _ in range(anzahl_positionen)]

    # Gesamtbetrag berechnen
    gesamt = sum(pos["menge"] * pos["einzelpreis"] for pos in positionen)

    # Rechnungsdatum: zufällig in den letzten 90 Tagen
    rechnungsdatum = datetime.now() - timedelta(days=random.randint(1, 90))
    # Faelligkeitsdatum: 14-30 Tage nach Rechnungsdatum
    faelligkeitsdatum = rechnungsdatum + timedelta(days=random.randint(14, 30))

    return {
        "rechnungsnummer": f"RE-2026-{1000 + nr:04d}",
        "aussteller": aussteller,
        "empfaenger": empfaenger,
        "steuernummer": steuernummer,
        "iban": iban,
        "bic": bic,
        "gesamtBetrag": round(gesamt, 2),
        "rechnungsdatum": rechnungsdatum,
        "faelligkeitsdatum": faelligkeitsdatum,
        "positionen": positionen,
    }


def escape_xml(text):
    """Escapet XML-Sonderzeichen."""
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")


def rechnung_to_xml(rechnung):
    """Konvertiert eine Rechnung in XML-String."""
    xml = []
    xml.append("  <rechnung>")
    xml.append(f"    <rechnungsnummer>{escape_xml(rechnung['rechnungsnummer'])}</rechnungsnummer>")
    xml.append(f"    <aussteller>{escape_xml(rechnung['aussteller'])}</aussteller>")
    xml.append(f"    <empfaenger>{escape_xml(rechnung['empfaenger'])}</empfaenger>")
    xml.append(f"    <steuernummer>{escape_xml(rechnung['steuernummer'])}</steuernummer>")
    xml.append(f"    <iban>{rechnung['iban']}</iban>")
    xml.append(f"    <bic>{rechnung['bic']}</bic>")
    xml.append(f"    <gesamtBetrag>{rechnung['gesamtBetrag']}</gesamtBetrag>")
    xml.append(f"    <faelligkeitsdatum>{rechnung['faelligkeitsdatum'].isoformat()}</faelligkeitsdatum>")
    xml.append("    <positionen>")

    for pos in rechnung["positionen"]:
        xml.append("      <position>")
        xml.append(f"        <artikelnummer>{escape_xml(pos['artikelnummer'])}</artikelnummer>")
        # Menge als Ganzzahl wenn .0, sonst mit Dezimal
        menge = int(pos["menge"]) if pos["menge"] == int(pos["menge"]) else pos["menge"]
        xml.append(f"        <menge>{menge}.0</menge>" if isinstance(menge, int) else f"        <menge>{menge}</menge>")
        xml.append(f"        <einzelpreis>{pos['einzelpreis']}</einzelpreis>")
        xml.append("      </position>")

    xml.append("    </positionen>")
    xml.append("  </rechnung>")
    return "\n".join(xml)


def generate_large_rechnung(nr):
    """Generiert eine einzelne Rechnung für die große Datei."""
    aussteller = random.choice(AUSSTELLER)
    empfaenger = random.choice(EMPFAENGER)
    steuernummer = generate_steuernummer()
    iban = generate_iban()
    bic = random.choice(BICS)

    # 1-5 Positionen pro Rechnung
    anzahl_positionen = random.choices([1, 2, 3, 4, 5], weights=[30, 30, 20, 15, 5])[0]
    positionen = [generate_position() for _ in range(anzahl_positionen)]

    # Gesamtbetrag berechnen
    gesamt = sum(pos["menge"] * pos["einzelpreis"] for pos in positionen)

    # Faelligkeitsdatum: 14-30 Tage zukünftig
    faelligkeitsdatum = datetime.now() + timedelta(days=random.randint(14, 30))

    return {
        "rechnungsnummer": f"RE-2026-{nr:08d}",
        "aussteller": aussteller,
        "empfaenger": empfaenger,
        "steuernummer": steuernummer,
        "iban": iban,
        "bic": bic,
        "gesamtBetrag": round(gesamt, 2),
        "faelligkeitsdatum": faelligkeitsdatum,
        "positionen": positionen,
    }


def generate_small():
    """Generiert 100 Rechnungen in XML-Datei."""
    random.seed(42)  # Reproduzierbare Ergebnisse

    rechnungen = [generate_rechnung(i) for i in range(1, 101)]

    xml_output = []
    xml_output.append('<?xml version="1.0" encoding="UTF-8"?>')
    xml_output.append("<rechnungen>")

    for rechnung in rechnungen:
        xml_output.append(rechnung_to_xml(rechnung))

    xml_output.append("</rechnungen>")

    output_path = "client/src/main/resources/rechnungen.xml"
    with open(output_path, "w", encoding="UTF-8") as f:
        f.write("\n".join(xml_output))

    print(f"✅ {len(rechnungen)} Rechnungen generiert und gespeichert in:")
    print(f"   {output_path}")

    # Statistik
    gesamtvertraege = sum(len(r["positionen"]) for r in rechnungen)
    gesamtwert = sum(r["gesamtBetrag"] for r in rechnungen)
    print(f"\n📊 Statistik:")
    print(f"   - Rechnungen: {len(rechnungen)}")
    print(f"   - Positionen gesamt: {gesamtvertraege}")
    print(f"   - Durchschnitt Positionen/Rechnung: {gesamtvertraege/len(rechnungen):.1f}")
    print(f"   - Gesamtwert: {gesamtwert:,.2f} EUR")

    return len(rechnungen), gesamtvertraege, gesamtwert


def generate_large(count=100000):
    """Generiert 100.000 Rechnungen streaming in separate XML-Datei."""
    random.seed(43)  # Anderer Seed für Varianz

    output_path = "client/src/main/resources/rechnungen/rechnungen_large.xml"

    gesamtvertraege = 0
    gesamtwert = 0.0

    with open(output_path, "w", encoding="UTF-8") as f:
        f.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        f.write("<rechnungen>\n")

        # Fortschrittsanzeige alle 10.000
        for i in range(1, count + 1):
            rechnung = generate_large_rechnung(i)
            f.write(rechnung_to_xml(rechnung) + "\n")
            gesamtvertraege += len(rechnung["positionen"])
            gesamtwert += rechnung["gesamtBetrag"]

            if i % 10000 == 0:
                print(f"   ... {i:,} Rechnungen generiert")

        f.write("</rechnungen>\n")

    print(f"\n✅ {count:,} Rechnungen generiert und gespeichert in:")
    print(f"   {output_path}")
    print(f"\n📊 Statistik:")
    print(f"   - Rechnungen: {count:,}")
    print(f"   - Positionen gesamt: {gesamtvertraege:,}")
    print(f"   - Durchschnitt Positionen/Rechnung: {gesamtvertraege/count:.1f}")
    print(f"   - Gesamtwert: {gesamtwert:,.2f} EUR")

    return count, gesamtvertraege, gesamtwert


def main():
    """Generiert beide Dateien: 100 und 100.000 Rechnungen."""
    print("=" * 60)
    print("Generiere kleine Datei (100 Rechnungen)...")
    print("=" * 60)
    generate_small()

    print("\n")
    print("=" * 60)
    print("Generiere große Datei (100.000 Rechnungen)...")
    print("=" * 60)
    generate_large(100000)


if __name__ == "__main__":
    main()
