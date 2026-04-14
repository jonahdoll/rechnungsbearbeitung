# Rechnungsbearbeitung

Ein verteiltes System zur automatisierten Rechnungsbearbeitung und Zahlungsverarbeitung, das auf Java, gRPC, RabbitMQ und PostgreSQL basiert.

## Projektübersicht

Das System besteht aus drei Hauptkomponenten:

1. **gRPC-Server**: Speichert Rechnungsmetadaten in PostgreSQL
2. **Zahlungssystem**: Verarbeitet Zahlungsaufträge über RabbitMQ und speichert sie in einer separaten PostgreSQL-Datenbank
3. **Client**: Liest Rechnungen aus XML-Dateien, sendet sie an den gRPC-Server und erzeugt Zahlungsaufträge

## Systemarchitektur

```
Client
↓ (gRPC)
gRPC-Server (Port 50051)
↓ (Speichert in PostgreSQL)
Database (gRPC) auf Port 5432

Client
↓ (RabbitMQ)
RabbitMQ Message Queue (Port 5672)
↓ (Konsumiert)
Zahlungssystem
↓ (Speichert in PostgreSQL)
Database (Zahlungssystem) auf Port 5433
```

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

### 3. Docker-Services starten

Starten Sie alle notwendigen Services (PostgreSQL-Datenbanken und RabbitMQ):

```bash
docker-compose -f extras/compose/backend/docker-compose.yml up -d
```

**Warten Sie ca. 10-15 Sekunden**, bis die Services vollständig gestartet sind.

### 4. Projekt bauen

```bash
mvn clean install
```

Dies kompiliert alle drei Module (grpc, zahlungssystem, client).

### 5. Alle Services starten

Öffnen Sie **vier separate Terminal-Fenster** und starten Sie die Services nacheinander:

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

## Verarbeitung im Detail

### Ablauf einer Rechnungsverarbeitung:

1. **Client liest XML**: Rechnungen werden aus `rechnungen.xml` gelesen
2. **gRPC-Anfrage**: Rechnungsmetadaten werden an den gRPC-Server gesendet
3. **Speicherung**: Der gRPC-Server speichert die Daten in seiner PostgreSQL-Datenbank
4. **RabbitMQ**: Bei erfolgreicher Speicherung wird ein Zahlungsauftrag in die RabbitMQ-Queue geschrieben
5. **Consumer**: Der Zahlungssystem-Consumer verarbeitet die Zahlungsaufträge
6. **Speicherung & Verarbeitung**: Zahlungsaufträge werden mit Status verfolgt (AUSSTEHEND → IN_BEARBEITUNG → ABGESCHLOSSEN/FEHLGESCHLAGEN)

## Datenbanken

### gRPC-Datenbank

- **Host**: localhost
- **Port**: 5432
- **Datenbank**: grpc
- **Benutzer**: kunde
- **Passwort**: p

Verbindung testen:
```bash
psql -h localhost -U kunde -d grpc
```

### Zahlungssystem-Datenbank

- **Host**: localhost
- **Port**: 5433
- **Datenbank**: zahlungssystem
- **Benutzer**: kunde
- **Passwort**: p

Verbindung testen:
```bash
psql -h localhost -U kunde -d zahlungssystem
```

## RabbitMQ

- **Host**: localhost
- **Port**: 5672 (AMQP)
- **Port**: 15672 (Management-UI)
- **Benutzer**: kunde
- **Passwort**: p
- **Queue**: zahlungsauftraege

Management-UI öffnen:
```
http://localhost:15672
```

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
