# rechnungsbearbeitung

Ein verteiltes System zur automatisierten Rechnungsbearbeitung und Zahlungsverarbeitung, das auf Java, gRPC, RabbitMQ und PostgreSQL basiert.

## 📋 Projektübersicht

Das System besteht aus drei Hauptkomponenten:

1. **gRPC-Server**: Speichert Rechnungsmetadaten in PostgreSQL
2. **Zahlungssystem**: Verarbeitet Zahlungsaufträge über RabbitMQ und speichert sie in einer separaten PostgreSQL-Datenbank
3. **Client**: Liest Rechnungen aus XML-Dateien, sendet sie an den gRPC-Server und erzeugt Zahlungsaufträge

## 🏗️ Systemarchitektur

## ⚙️ Voraussetzungen

- **Java 25+** (oder Ihre unterstützte JDK-Version)
- **Maven 3.8+**
- **Docker & Docker Compose**
- **Git**

## 🚀 Schnellstart

### 1. Repository klonen

git clone <repository-url>
cd rechnungsbearbeitung

# 2. Umgebungsvariablen konfigurieren
cp example.env .env

Die .env-Datei enthält bereits die richtigen Standard-Konfigurationen:
PostgreSQL für gRPC auf Port 5432
PostgreSQL für Zahlungssystem auf Port 5433
RabbitMQ auf Port 5672

# 3. Docker-Services starten
docker-compose -f extras/compose/backend/docker-compose.yml up -d

