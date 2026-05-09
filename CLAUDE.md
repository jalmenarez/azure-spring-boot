# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Full build + unit tests (matches CI's build-and-test job)
mvn clean verify

# Single module (faster — useful while iterating)
mvn clean verify -pl azure-spring-boot

# Single test class / method
mvn test -Dtest=TelemetrySenderTest -pl azure-spring-boot
mvn test -Dtest=TelemetrySenderTest#testRetry -pl azure-spring-boot

# Static analysis (SpotBugs runs in the `compile` phase, not `verify`)
mvn compile spotbugs:check -pl azure-spring-boot

# Build samples — parent + BOM + core MUST be installed first
mvn install -DskipTests -pl .,azure-spring-boot-bom,azure-spring-boot-parent,azure-spring-boot -am
mvn package -DskipTests -pl azure-spring-boot-samples -am

# Skip checkstyle while iterating on code
mvn clean test -Dcheckstyle.skip=true
```

`HowToContribute.md` is partially outdated (still references JDK 1.8 / JUnit 4 / Travis). The README, `.github/workflows/ci.yml`, and `azure-spring-boot-parent/pom.xml` are the source of truth — Java 17, JUnit 5 (for migrated tests), and GitHub Actions.

## Module Layout & Build Order

The reactor (`pom.xml`) has 5 modules with a strict build-order dependency:

```
azure-spring-boot-bom         ← imports spring-cloud-azure-dependencies:4.20.0
  └── azure-spring-boot-parent  ← plugin/dependency management, surefire JVM args
        └── azure-spring-boot   ← autoconfigure JAR (the core module)
              └── azure-spring-boot-starters/*  (one starter per Azure service)
                    └── azure-spring-boot-samples/*  (runnable Spring Boot apps)
```

When changing build configuration, the change usually belongs in `azure-spring-boot-parent/pom.xml` (plugins, versions) or `azure-spring-boot-bom/pom.xml` (imported BOMs). Starters and samples should not redeclare versions — they inherit from the BOM.

## Critical Migration Context

The codebase was recently migrated from `azure-dependencies-bom:2.1.0.M5` (Spring Boot 2.1, Java 1.8) to `spring-cloud-azure-dependencies:4.20.0` (Spring Boot 2.7.18, Java 17). Do not revert these without explicit instruction:

- **Group ID for new starters**: `com.azure.spring` (not legacy `com.microsoft.azure`). The build itself still uses `com.microsoft.azure` as the reactor groupId — both coexist intentionally.
- **Azure SDK clients**: use `com.azure.*` (e.g. `BlobServiceClient`, `SecretClient`, `ServiceBusSenderClient`, `CosmosRepository`). Legacy `azure-keyvault`, `azure-storage` reactive `ServiceURL`/`ContainerURL`, `QueueClient`/`TopicClient`, ADAL4J, `@Document`/`DocumentDbRepository` are all removed.
- **`KeyVaultOperation`** uses functional interfaces (`Consumer`/`Function`) instead of holding a `SecretClient` reference. This is intentional — `SecretClient` is `final` and sealed by the Java 17 module system, so it cannot be mocked directly. Tests inject lambdas instead.
- **Surefire `--add-opens`** flags in `azure-spring-boot-parent/pom.xml` are required for Java 17 reflection. Only the `java.base/java.lang` and `java.base/java.util` opens are valid — do not add opens for `com.azure.*` modules (they are not named JVM modules and produce `WARNING: Unknown module` at test time).
- **SpotBugs 4.8.3.1** replaced FindBugs 3.0.5 (incompatible with Java 17 bytecode). The `<plugin>` is bound to the `compile` phase, so `mvn compile spotbugs:check` is sufficient — `mvn verify` also runs it.
- **Deprecated starters with no 4.x equivalent**: `azure-mediaservices-spring-boot-starter`, `spring-data-gremlin-boot-starter`, `azure-sqlserver-spring-boot-starter`, `azure-spring-boot-metrics-starter`. Their source still compiles but they should not be promoted as solutions.

## CI

`.github/workflows/ci.yml` runs three parallel jobs on push/PR to `master`:
1. `build-and-test` — matrix on Java 17 & 21 (`mvn clean verify`)
2. `spotbugs` — `mvn compile spotbugs:check -pl azure-spring-boot`
3. `build-samples` — installs core, then packages `azure-spring-boot-samples`

The required status checks for `master` branch protection are: `build-and-test (17)`, `build-and-test (21)`, `spotbugs`, `build-samples`. `.travis.yml` and `appveyor.yml` are obsolete leftover files.

## Key Versions at a Glance

| Component | Version | Note |
|-----------|---------|------|
| Spring Boot | 2.7.18 | LTS for 2.x branch |
| Spring Cloud Azure | 4.20.0 | Imported in `azure-spring-boot-bom` |
| Java | 17+ | minimum; also tested on 21 in CI |
| Maven | 3.0+ | |
| SpotBugs | 4.8.3.1 | runs in `compile` phase |
| Lombok | 1.18.30 | required for model POJOs |
| Mockito | 4.11.0+ | JUnit 5 compatible |

## Configuration Properties Pattern

Every starter has a `*Properties` class using `@ConfigurationProperties`. Example: `azure-servicebus-spring-boot-starter/src/main/java/...ServiceBusProperties.java`. These are auto-wired by `*AutoConfiguration` classes which expose beans like `ServiceBusSenderClient`, `ServiceBusReceiverClient`, etc.

To see all available properties for a starter:
- Find the `*Properties.java` class in the starter's main source
- It documents all fields with `@Value` or direct field assignments
- Properties are prefixed (e.g., `spring.cloud.azure.servicebus.*`)
- Some properties have defaults; others are required and will fail the context if missing

## Running a Sample App Locally

Each sample in `azure-spring-boot-samples/` is a runnable Spring Boot app. To test changes:

```bash
# Build and install core + starters first
mvn install -DskipTests -pl .,azure-spring-boot-bom,azure-spring-boot-parent,azure-spring-boot -am

# Run a sample
cd azure-spring-boot-samples/azure-storage-spring-boot-sample
mvn spring-boot:run
# Visit http://localhost:8080
```

Use `spring-boot:run` to pick up property changes without rebuilding JAR. Credentials (e.g., `AZURE_STORAGE_ACCOUNT_NAME`, `AZURE_STORAGE_ACCOUNT_KEY`) are typically passed as environment variables or in `application.properties`.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `Filename too long` on Windows | Clone with `-c core.longPaths=true` flag |
| `OutOfMemoryError` during `mvn clean verify` | Increase Maven heap: `export MAVEN_OPTS="-Xmx2g"` |
| SpotBugs report not generated | Run `mvn compile spotbugs:check -pl azure-spring-boot`; report is in `target/spotbugsXml.xml` |
| Checkstyle fails with weird formatting errors | Run `mvn checkstyle:check` in verbose mode to see violations; use `-Dcheckstyle.skip=true` while iterating |
| A sample app won't start (context error) | Check if required Azure credentials are set (env vars or `application.properties`) |
| `WARNING: Unknown module` during tests | This is from invalid `--add-opens` in surefire. Don't add opens for `com.azure.*`; only `java.base/java.lang` and `java.base/java.util` are valid. |

## Pull Request Workflow

1. Create a feature branch from `master`: `git checkout -b feat/your-feature`
2. Make changes, test locally: `mvn clean verify`
3. Push to remote: `git push -u origin feat/your-feature`
4. Open a PR on GitHub
5. All three CI jobs must pass: `build-and-test (17)`, `build-and-test (21)`, `spotbugs`, `build-samples`
6. Get one approval, then merge

If you need to commit a doc-only fix directly to master and can't push (403 Forbidden), use the GitHub MCP `mcp__github__create_or_update_file` tool with `branch: "master"`.

## Conventions

- **Checkstyle** (`config/checkstyle.xml`) is enforced in the `validate` phase and fails the build on violation. Use `-Dcheckstyle.skip=true` while iterating, never to merge.
- **Test framework**: new tests should use **JUnit 5** + Mockito. JUnit 4 still exists in legacy modules — when touching a JUnit 4 test heavily, migrate it.
- **Versions**: keep `2.1.7-SNAPSHOT` (the build groupId is `com.microsoft.azure` for historical reasons). Don't bump unless releasing.
- **Master is branch-protected**. Direct `git push origin master` returns 403. Always work on a feature branch and open a PR; if a doc-only fix is urgent, the GitHub MCP `create_or_update_file` tool can commit through the API.
