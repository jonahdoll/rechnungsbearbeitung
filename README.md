# Rechnungsbearbeitung

Ein unternehmensweites verteiltes System zur automatisierten Rechnungsbearbeitung, Zahlungsverarbeitung und Workflow-Orchestrierung basierend auf Java, gRPC, RabbitMQ, PostgreSQL und Camunda BPMN.

## Projektübersicht

Das System ist eine Kombination aus mehreren Microservices und einer zentralen Workflow-Engine zur Verwaltung von Geschäftsprozessen rund um die Rechnungsabwicklung. Es bietet eine vollständige Lösung für die Erfassung, Validierung, Verarbeitung und Zahlung von Rechnungen.

Das System besteht aus 4 Code Projekten, der Camunda Workflow Engine und einem ERP-System.

1. **gRPC-Server**: Validiert und speichert Rechnungsmetadaten mit PostgreSQL als Backend
2. **Zahlungssystem**: Asynchrone Verarbeitung von Zahlungsaufträgen über RabbitMQ Message Queue
3. **Client**: XML-basiertes Rechnungsimport-Tool mit gRPC-Integration
4. **Camunda-Workers**: Eigenständiger Service, der die Prozess-Engine kontinuierlich nach anstehenden automatisierten Aufgaben abfragt und dann per gRPC die Metadaten von Rechnungen speichert oder Zahlungen auf die rabbitmq legt. Er liefert das Ergebnis anschließend an die Engine zürück (auch wenn Service nicht verfügbar ist)
5. **Camunda Workflow Engine**: BPMN-basierte Orchestrierung aller Geschäftsprozesse, inklusive User Tasks, E-Mail-Integration, EDI-Integration und Portal für Sachbearbeiter, Manager und Finance-Personal
6. **ERP-System**: Wird von den Arbeitern zum Eintragen der Rechnungsdaten genutzt

## Systemarchitektur

Das System folgt einer Microservices-Architektur mit ereignisgetriebener Verarbeitung:
![Systemarchitektur](DVG-Orchestrierung.drawio.svg)
*Tipp: Klicke auf das Bild, um es interaktiv in draw.io zu öffnen.*

## Voraussetzungen

- **Java 25+** (oder Ihre unterstützte JDK-Version)
- **Maven 3.8+**
- **Docker & Docker Compose**
- **Git**

## Schnellstart

### 1. Repository klonen

```bash
git clone <repository-url>
cd rechnungsbearbeitung
```

### 2. Umgebungsvariablen konfigurieren

```bash
cp example.env .env
```

Die `.env`-Datei enthält bereits die richtigen Standard-Konfigurationen:
- PostgreSQL für gRPC auf Port 5432
- PostgreSQL für Zahlungssystem auf Port 5433
- RabbitMQ auf Port 5672
- Camunda auf Port 8080

### 3. Docker-Services starten

Starten Sie alle notwendigen Services (PostgreSQL-Datenbanken, RabbitMQ und Camunda):

```bash
docker-compose -f extras/compose/backend/docker-compose.yml up -d
```

**Warten Sie ca. 30-45 Sekunden**, bis alle Services vollständig gestartet sind.

Überprüfen Sie den Status mit:
```bash
docker-compose -f extras/compose/backend/docker-compose.yml ps
```

### 4. Projekt bauen

```bash
mvn clean install
```

Dies kompiliert alle vier Module (grpc, zahlungssystem, client, camunda-workers).

### 5. Services starten

Öffnen Sie **vier bis fünf separate Terminal-Fenster** und starten Sie die Services nacheinander:

#### Terminal 1: gRPC-Server starten

```bash
mvn -pl grpc exec:java -Dexec.mainClass="com.example.grpc.GrpcServer"
```

Sie sollten eine Meldung sehen:
```
gRPC-Server gestartet auf Port 50051
```

#### Terminal 2: Zahlungssystem-Consumer starten

```bash
mvn -pl zahlungssystem exec:java -Dexec.mainClass="com.example.zahlungsystem.ZahlungsConsumer"
```

Sie sollten sehen:
```
ZahlungsConsumer bereit. Warte auf Nachrichten in 'zahlungsauftraege'...
```

#### Terminal 3: Client ausführen

```bash
mvn -pl client exec:java -Dexec.mainClass="com.example.client.ClientApplication"
```

Der Client liest Rechnungen aus `client/src/main/resources/rechnungen.xml`, sendet diese an den gRPC-Server und erzeugt Zahlungsaufträge.

#### Terminal 4 (optional): Camunda Workers starten

```bash
mvn -pl camunda-workers exec:java -Dexec.mainClass="com.example.camunda.CamundaWorker"
```

#### Terminal 5: Camunda UI öffnen

Öffnen Sie in Ihrem Browser:
```
http://localhost:8080/camunda/
```

Standardanmeldedaten:
- **Benutzer**: demo
- **Passwort**: demo

## Verarbeitung im Detail

### Workflow einer Rechnungsverarbeitung:

1. **Client-Input**: Rechnungen werden aus XML-Dateien durch den Client Importer gelesen
2. **gRPC-Validierung**: Rechnungsmetadaten werden an den gRPC-Server zur Validierung gesendet
3. **Speicherung in gRPC-DB**: Der gRPC-Server speichert validierte Daten in seiner PostgreSQL-Datenbank
4. **Workflow-Auslöser**: Nach erfolgreicher Speicherung wird ein Zahlungsauftrag in RabbitMQ Queue publiziert
5. **Workflow-Orchestrierung**: Camunda Workflow Engine empfängt das Event und initiiert den entsprechenden BPMN-Prozess
6. **Asynchrone Verarbeitung**: Der Zahlungssystem-Consumer verarbeitet die Zahlungsaufträge aus der Queue
7. **Status-Tracking**: Zahlungsaufträge werden mit Status aktualisiert (AUSSTEHEND → IN_BEARBEITUNG → ABGESCHLOSSEN/FEHLGESCHLAGEN)
8. **Benutzer-Interaktion**: Sachbearbeiter, Manager und Finance-Personal interagieren über die Camunda UI / Portal
9. **ERP-Integration**: Abgeschlossene Transaktionen werden ins ERP-System synchronisiert

## Services und Ports

### gRPC-Server
- **Port**: 50051
- **Funktion**: Validierung und Speicherung von Rechnungsmetadaten
- **Protokoll**: gRPC
- **Datenbank**: PostgreSQL auf Port 5432

### Zahlungssystem
- **Queue**: zahlungsauftraege (RabbitMQ)
- **Funktion**: Verarbeitung von Zahlungsaufträgen
- **Datenbank**: PostgreSQL auf Port 5433

### Camunda Workflow Engine
- **Port**: 8080 (Standard)
- **UI-Port**: 8080/camunda
- **Funktion**: BPMN-basierte Prozessorchestrierung
- **Features**: User Task Management, Email Integration, EDI Integration, Portal

### RabbitMQ Message Broker
- **AMQP-Port**: 5672
- **Management-UI**: 15672
- **Queue**: zahlungsauftraege
- **Persistierung**: Aktiviert

## Aufräumen

### Services stoppen und entfernen

```bash
docker-compose -f extras/compose/backend/docker-compose.yml down
```

### Zusätzlich Volumes löschen (um Datenbankdaten zu entfernen)

```bash
docker-compose -f extras/compose/backend/docker-compose.yml down -v
```

## Entwicklung

### Code formatieren

Das Projekt verwendet Google Java Format:

```bash
mvn spotless:apply
```

Code-Stil überprüfen:

```bash
mvn spotless:check
```

### Nur ein Modul bauen

```bash
mvn -pl grpc clean install
mvn -pl zahlungssystem clean install
mvn -pl client clean install
```

### Logs anzeigen

Die Logs werden in die Konsole geschrieben. Für gRPC und Consumer verwenden Sie die Standard-Log-Ausgabe.

## Projektstruktur

```
rechnungsbearbeitung/
├── grpc/                          # gRPC-Server Modul
│   └── src/main/java/com/example/grpc/
│       ├── GrpcServer.java        # Main-Klasse
│       ├── config/                # Datenbank-Config
│       ├── entity/                # JPA-Entities
│       └── service/               # Business Logic
│
├── zahlungssystem/                # Zahlungssystem Modul
│   └── src/main/java/com/example/zahlungsystem/
│       ├── ZahlungsConsumer.java  # Main-Klasse
│       ├── ZahlungsProducer.java  # RabbitMQ Producer
│       ├── config/                # Datenbank-Config
│       ├── entity/                # Zahlungsauftrag
│       └── repository/            # Datenzugriff
│
├── client/                        # Client Modul
│   ├── src/main/java/com/example/client/
│   │   ├── ClientApplication.java # Main-Klasse
│   │   └── ...
│   └── src/main/resources/
│       └── rechnungen.xml         # Eingabe-Rechnungen
│
├── extras/
│   └── compose/                   # Docker Compose Konfigurationen
│       ├── backend/
│       ├── grpc/
│       └── zahlungssystem/
│
├── example.env                    # Umgebungsvariablen Template
├── pom.xml                        # Parent Maven POM
└── README.md                      # Diese Datei
```

## Konfiguration

Bearbeiten Sie die `.env`-Datei, um die Konfiguration anzupassen:

```env
# gRPC Datenbank
GRPC_DB_PORT=5432
GRPC_DB_USERNAME=kunde
GRPC_DB_PASSWORD=p
GRPC_DB_NAME=grpc

# gRPC Server
GRPC_HOST=localhost
GRPC_PORT=50051

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=kunde
RABBITMQ_PASSWORD=p
RABBITMQ_QUEUE_NAME=zahlungsauftraege

# Zahlungssystem Datenbank
ZAHLUNGSSYSTEM_DB_PORT=5433
ZAHLUNGSSYSTEM_DB_USERNAME=kunde
ZAHLUNGSSYSTEM_DB_PASSWORD=p
ZAHLUNGSSYSTEM_DB_NAME=zahlungssystem
```

## Debugging

### Logs in den Services überprüfen

Die Services geben Logs zur Konsole aus. Achten Sie auf:

- `gRPC-Server gestartet auf Port 50051` → gRPC läuft
- `ZahlungsConsumer bereit. Warte auf Nachrichten...` → Consumer läuft
- Fehler bei der Datenbankverbindung → Docker-Services nicht gestartet
- Connection refused auf Port 5672 → RabbitMQ nicht gestartet

### Datenbankinhalte überprüfen

```bash
# Rechnungsmetadaten anzeigen
psql -h localhost -U kunde -d grpc -c "SELECT * FROM rechnungsmetadaten;"

# Zahlungsaufträge anzeigen
psql -h localhost -U kunde -d zahlungssystem -c "SELECT * FROM zahlungsauftraege;"
```

### RabbitMQ Queue Status

```bash
# RabbitMQ Management UI
http://localhost:15672 (Benutzer: kunde, Passwort: p)
```

## Häufige Probleme

### Problem: "Connection refused" beim Start von gRPC oder Consumer

**Lösung**: Docker-Services sind nicht gestartet:
```bash
docker-compose -f extras/compose/backend/docker-compose.yml up -d
docker-compose -f extras/compose/backend/docker-compose.yml ps  # Status überprüfen
```

### Problem: "Port already in use"

**Lösung**: Ein Service läuft bereits. Stoppen Sie alle:
```bash
docker-compose -f extras/compose/backend/docker-compose.yml down
lsof -i :5432  # Prüfen, welcher Prozess den Port nutzt
```

### Problem: Client startet nicht / "Class not found"

**Lösung**: Projekt nicht gebaut:
```bash
mvn clean install
```

### Problem: Datenbank wird nicht automatisch migriert

**Lösung**: Stellen Sie sicher, dass `GRPC_DB_CLEAN=true` und `ZAHLUNGSSYSTEM_DB_CLEAN=true` in der `.env`-Datei gesetzt sind.

## Zusätzliche Ressourcen

- [gRPC Dokumentation](https://grpc.io/docs/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [PostgreSQL Dokumentation](https://www.postgresql.org/docs/)
- [Maven Dokumentation](https://maven.apache.org/)

## Lizenz

Siehe [LICENSE](LICENSE) Datei.

## Gruppe 1

Rechnungsbearbeitung System

---

**Letzte Aktualisierung**: April 2026 (1 Sprint)
```

Diese README bietet:

✅ **Übersicht** des Projekts und der Architektur  
✅ **Voraussetzungen** und Installationsschritte  
✅ **Schritt-für-Schritt Anleitung** zum Starten aller Services  
✅ **Detaillierte Erklärung** des Ablaufs  
✅ **Konfigurationsoptionen**  
✅ **Debugging-Tipps** und häufige Probleme  
✅ **Projektstruktur** übersichtlich dargestellt  

Du kannst diese README jetzt in dein Projekt kopieren und auf GitHub pushen!
```

---KI-Hinweis
Dieses Projekt und die zugehörige Dokumentation wurden durch die Unterstützung von Künstlicher Intelligenz (Gemini) optimiert und strukturiert. Die KI wurde gezielt eingesetzt, um:

Die Architektur-Dokumentation zu strukturieren.

Die Code-Formatierung (Spotless/Google Java Format) zu integrieren.

Die README.md für eine bessere Lesbarkeit und Wartbarkeit aufzubereiten.
