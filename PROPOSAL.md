# Proposal: plain-project

Ich möchte eine Webapplikation mit Spring Boot erstellen. Das Backend soll in Java geschrieben sein. Das Frontend mit plain JavaScript, CSS und HTML 5.
Als Datenbank soll PostgreSQL verwendet werden. Datenzugriff über Spring Data JDBC.

## Zweck

Das Projekt ist ein **Template** für neue Projekte. Es enthält genau ein Beispiel-Aggregat, die **Note**, damit alle Schichten einmal
durchgestochen sind. Die Fachbegriffe stehen in [CONTEXT.md](CONTEXT.md).

## Fachlich

- Eine Note hat `title` (Pflicht, max. 200 Zeichen), `content` (optional, Klartext beliebiger Länge), `createdAt` und `updatedAt`.
- Voll-CRUD: auflisten, anzeigen, anlegen, bearbeiten, löschen. Löschen ist endgültig.
- Liste sortiert nach `updatedAt` absteigend, paginiert (Standard-Seitengröße 20), keine Suche.
- Gleichzeitiges Bearbeiten: Optimistic Locking. Wer eine inzwischen geänderte Note speichert, erhält **409 Conflict**.

## Backend

- Java 17, Spring Boot 4.x, Maven mit Maven Wrapper (`mvnw`).
- Koordinaten: groupId `com.kah`, artifactId `plain-project`, Basis-Package `com.kah.plainproject`.
- Package-Struktur nach Feature (`note/`).
- Spring Data JDBC; IDs als `Long` (`BIGINT GENERATED ALWAYS AS IDENTITY`); `@Version` für Optimistic Locking;
  Zeitstempel über Spring Data JDBC Auditing als `Instant` / `timestamptz`.
- Schema über Flyway (versionierte SQL-Migrationen).
- REST-API unter `/api/notes`, JSON, Request-/Response-DTOs als Java-Records, Bean Validation,
  Fehler als RFC 9457 `ProblemDetail`: 400 (mit Feldfehlern), 404, 409.
- Spring Boot Actuator, nur `/actuator/health`.
- Konfiguration über Umgebungsvariablen (`SPRING_DATASOURCE_URL` usw.), keine Profile.
- Container-Image über Buildpacks (`./mvnw spring-boot:build-image`), kein eigenes Dockerfile.
- Keine Security (später als eigener Schritt).

## Frontend

- Statische Dateien unter `src/main/resources/static`, ausgeliefert von Spring Boot.
- Native ES-Module, kein npm, kein Bundler, kein Build-Schritt.
- Bootstrap 5 (CSS + JS-Bundle) als WebJar.
- Eine Seite, zweispaltig: links Liste, rechts Formular für Anlegen/Anzeigen/Bearbeiten; auf schmalen Bildschirmen untereinander.
- Löschen mit Bestätigung als Bootstrap-Modal.
- Dark Mode automatisch nach Betriebssystem (`prefers-color-scheme`), kein Umschalter.
- Fehleranzeige: 400 direkt am Feld, 409 als Hinweis mit „Neu laden“, sonst Toast.
- Text-Buttons, keine Icons.
- Zeitstempel in lokaler Zeit des Browsers.

## Infrastruktur und Tests

- Lokale Entwicklung: PostgreSQL über Spring Boot Docker Compose Support (`compose.yaml`, startet automatisch).
- Tests: Integrationstests mit `@SpringBootTest` und Testcontainers-PostgreSQL gegen `/api/notes`
  (inkl. 400/404/409). Keine Frontend-Tests.
- Git-Repository mit `.gitignore` für Maven und IntelliJ.
- README auf Deutsch: Voraussetzungen, Starten, Testen, Template für ein neues Projekt umbenennen.
