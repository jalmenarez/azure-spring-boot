# Project Guidelines

A concise set of workspace-wide guidance for contributors and the assistant.

## Code Style
- JDK: 1.8+. Use the Maven wrapper: `./mvnw` (Unix) or `mvnw.cmd` (Windows).
- Checkstyle rules: [config/checkstyle.xml](config/checkstyle.xml).
- FindBugs exclusions: [config/findbugs-exclude.xml](config/findbugs-exclude.xml).

## Architecture
- `azure-spring-boot-bom/` — Bill of Materials for consistent dependency versions.
- `azure-spring-boot-parent/` — Parent POM with shared plugin/configuration (checkstyle, cobertura, findbugs).
- `azure-spring-boot/` — Core library and shared runtime code.
- `azure-spring-boot-starters/` — Individual Spring Boot starters for Azure services.
- `azure-spring-boot-samples/` — Sample applications demonstrating each starter.

## Build and Test
- Build whole repo: `./mvnw clean install` (Windows: `mvnw.cmd clean install`).
- Quick rebuild (skip tests): `./mvnw clean install -DskipTests`.
- Build a single starter/sample: `./mvnw clean install -pl azure-spring-boot-starters/<starter-dir>`.
- Run tests: `./mvnw test`.
- Validate linters and static checks: `./mvnw validate`.
- Run a sample locally: go to the sample directory, configure `src/main/resources/application.properties`, then `./mvnw spring-boot:run` or `./mvnw package && java -jar target/<artifact>.jar`.

## Conventions & Tooling
- Checkstyle and FindBugs run during the build; fix violations before committing.
- Code coverage is enforced (Cobertura branch-rate policy); keep new code covered.
- Prefer the included Maven wrapper to ensure consistent Maven versions.
- Many samples require Azure resources; follow each sample's README for prerequisites.

## Quick Links
- Main README: [README.md](README.md)
- Contribution guide: [HowToContribute.md](HowToContribute.md)
- Checkstyle config: [config/checkstyle.xml](config/checkstyle.xml)
- Samples: [azure-spring-boot-samples/](azure-spring-boot-samples/)

## Core Principles
1. Minimal by default — include only items that apply workspace-wide.
2. Concise and actionable — prefer short commands and links to docs.
3. Link, don't embed — reference detailed docs (sample READMEs, HowToContribute.md).

## Anti-patterns
- Do not duplicate docs; link to existing READMEs instead.
- Do not mix area-specific instructions here; use nested AGENTS.md if needed.
- Avoid long, exhaustive command lists — provide a few canonical examples.

## Suggested Assistant Prompts
- "Build project" → runs `./mvnw clean install` (add `-DskipTests` for faster iteration).
- "Run sample <sample-name>" → open the sample README and run `./mvnw spring-boot:run`.
- "Check code style" → runs `./mvnw validate`.

---

For more granular, area-specific defaults (for example: only samples or only starters), add an `AGENTS.md` in the relevant subfolder.
