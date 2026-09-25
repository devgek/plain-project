# plain-project

## This project is used as a starting point for new projects working with claude in intellij.

Template für Webapplikationen mit Spring Boot 4, Spring Data JDBC und PostgreSQL. Das Frontend besteht aus plain
JavaScript (ES-Module), HTML 5 und Bootstrap 5, ohne npm und ohne Build-Schritt. Das Beispiel-Aggregat ist die **Note**
(siehe [CONTEXT.md](CONTEXT.md)); die Entscheidungen stehen in [PROPOSAL.md](PROPOSAL.md).

## Voraussetzungen

- JDK 17 oder neuer
- Docker (für die lokale Datenbank und die Tests)

Maven muss nicht installiert sein, der Maven Wrapper (`mvnw`) lädt es selbst.

## Starten

```shell
./mvnw spring-boot:run
```

Spring Boot startet PostgreSQL aus `compose.yaml` automatisch, Flyway legt das Schema an.
Die Anwendung läuft unter <http://localhost:8080>, der Health-Check unter <http://localhost:8080/actuator/health>.

In anderen Umgebungen wird die Datenbank über Umgebungsvariablen konfiguriert:
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

## Testen

```shell
./mvnw test
```

Die Integrationstests starten PostgreSQL über Testcontainers.

## Container-Image

```shell
./mvnw spring-boot:build-image
```

Baut über Cloud Native Buildpacks das Image `plain-project:0.0.1-SNAPSHOT`, ein Dockerfile ist nicht nötig.

## Aufbau

| Pfad                                         | Inhalt                                                      |
|----------------------------------------------|-------------------------------------------------------------|
| `src/main/java/com/kah/plainproject/note/`   | Feature `note`: Entity, Repository, Service, REST-Controller |
| `src/main/java/com/kah/plainproject/`        | Anwendung und zentrale Fehlerbehandlung (`ProblemDetail`)   |
| `src/main/resources/db/migration/`           | Flyway-Migrationen                                          |
| `src/main/resources/static/`                 | Frontend (`index.html`, `js/`, `css/`)                      |
| `compose.yaml`                               | PostgreSQL für die lokale Entwicklung                       |

REST-API: `GET/POST /api/notes`, `GET/PUT/DELETE /api/notes/{id}`. Beim Ändern muss die zuletzt gelesene `version`
mitgeschickt werden, sonst antwortet die API mit 409 Conflict.

## Template für ein neues Projekt verwenden

In Claude Code mit `/gek-plain <artifactId> [groupId] [Elternverzeichnis]` (Skill unter `~/.claude/skills/gek-plain/`).
Der Skill übernimmt den committeten Stand dieses Repos, Änderungen am Template also vorher committen.

Von Hand:

1. Verzeichnis kopieren (ohne `target/` und `.git/`), dann `git init`.
2. In `pom.xml` `groupId`, `artifactId`, `name` und `description` anpassen.
3. Package `com.kah.plainproject` umbenennen (in IntelliJ: *Refactor → Rename*), auch unter `src/test`.
4. `PlainProjectApplication` umbenennen.
5. In `application.properties` `spring.application.name` anpassen.
6. In `compose.yaml` Datenbankname, Benutzer und Passwort anpassen.
7. Das Feature `note` als Vorlage nutzen oder entfernen (Java-Package, Migration `V1__create_note.sql`, Frontend,
   Test) und `CONTEXT.md` sowie `PROPOSAL.md` für die neue Domäne neu schreiben.
