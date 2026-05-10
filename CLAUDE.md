# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Azure Spring Boot is a collection of Spring Boot starters and autoconfiguration for Microsoft Azure services. The project provides integration with Key Vault, Cosmos DB, Service Bus, Storage, Active Directory, and other Azure services.

**Recent Status:** The project has been migrated to **Spring Boot 3.3.0 and Spring Cloud Azure 5.11.0** with full Jakarta EE namespace migration. All core module tests pass (16/16), and all starters build successfully (12/12).

## Project Structure

The repository is organized as a Maven multi-module project with the following key modules:

- **azure-spring-boot-bom** — Bill of Materials defining all versions and dependencies
- **azure-spring-boot-parent** — Parent POM with common build configuration, Java 17+ settings, and property management
- **azure-spring-boot** — Core module containing autoconfiguration classes for all Azure services
- **azure-spring-boot-starters** — Production-ready Spring Boot starters (12 modules):
  - Active Directory / AD B2C
  - Cosmos DB, Service Bus, Storage, Key Vault
  - Media Services, Gremlin, SQL Server, Metrics
- **azure-spring-boot-samples** — Example applications demonstrating starter usage

## Key Versions (Post-Migration)

- **Java**: 17 (minimum); CI tests on Java 17 and 21
- **Spring Boot**: 3.3.0
- **Spring Cloud Azure**: 5.11.0
- **Spring Security**: 6.x
- **Jakarta EE** namespace (not javax)
- **Validation**: Hibernate Validator 8.0.1.Final
- **Metrics**: Micrometer 1.12.0

## Build & Test Commands

### Full Build
```bash
mvn clean verify                              # Full build including tests
mvn clean install                             # Build and install to local Maven repository
mvn clean install -DskipTests                 # Build without tests (faster)
```

### Running Tests
```bash
mvn clean test                                # Run all tests
mvn test -Dtest=TestClassName                 # Run specific test class
mvn test -Dtest=Telemetry*                    # Run tests matching pattern
mvn clean test -Dspotbugs.skip=true           # Skip SpotBugs static analysis
mvn clean test -Dcheckstyle.skip=true         # Skip checkstyle validation
```

### Static Analysis
```bash
mvn spotbugs:check -pl azure-spring-boot      # SpotBugs analysis on core module only
mvn compile spotbugs:check                    # SpotBugs on all modules
```

### Building Samples
```bash
# Build parent hierarchy first, then samples
mvn install -DskipTests -pl azure-spring-boot-bom -pl azure-spring-boot-parent -pl azure-spring-boot -am
mvn package -DskipTests -pl azure-spring-boot-samples -am

# Or as single command (auto-builds dependencies)
mvn package -DskipTests -pl azure-spring-boot-samples -am
```

### Module-Specific Builds
```bash
mvn clean test -f azure-spring-boot/pom.xml                    # Test core module
mvn clean test -f azure-spring-boot-starters/pom.xml           # Test all starters
mvn clean test -f azure-spring-boot-samples/pom.xml -DskipTests # Build (but don't test) samples
```

## Architecture & Design Patterns

### Autoconfiguration Pattern
Each Azure service module follows the Spring Boot autoconfiguration pattern:
- **AutoConfiguration class**: Annotated with `@Configuration`, conditionally enables beans based on properties
- **Properties class**: `@ConfigurationProperties` with prefix (e.g., `azure.keyvault.vault-uri`)
- **Environment post-processor**: Some modules (e.g., KeyVault) hook into application startup via `EnvironmentPostProcessor`
- **Conditional beans**: Use `@ConditionalOnProperty`, `@ConditionalOnMissingBean`, `@ConditionalOnClass`

### Spring Security Integration (Post-3.x)
The project has been updated to Spring Security 6.x:
- **WebSecurityConfigurerAdapter is removed** — Use `SecurityFilterChain` bean pattern instead
- **@EnableGlobalMethodSecurity → @EnableMethodSecurity** (with `prePostEnabled = true`)
- **authorizeRequests() → authorizeHttpRequests()** with lambda-based configuration
- **Test imports**: Use `jakarta.servlet.*` (not `javax.servlet.*`)

Example (Spring Security 6.x):
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated())
        .oauth2Login();
    return http.build();
}
```

### Test Organization
- **Unit tests**: Located in `src/test/java` parallel to source structure
- **Test naming**: `*Test.java` for JUnit 5 tests
- **Mocking**: Uses Mockito for dependency mocking
- **Coverage requirement**: ≥ 65% for new code (enforced by CI)
- **JUnit 5**: Use `@Test` annotation (some legacy code may still use JUnit 4)

## Common Development Tasks

### Adding a New Property or Configuration
1. Add property to `*Properties.java` class with `@ConfigurationProperties` annotation
2. Update `*AutoConfiguration.java` to wire the property into beans
3. Add test in corresponding `*Test.java` verifying the property is loaded
4. Run: `mvn clean test -Dtest=YourTest`

### Updating Spring Security Configuration
When modifying security settings in samples or starters:
1. Remember all samples now use Spring Security 6.x
2. Use `SecurityFilterChain` bean pattern, not `WebSecurityConfigurerAdapter`
3. Use lambda-based `authorizeHttpRequests()` instead of `authorizeRequests()`
4. Ensure imports use `jakarta.servlet.*` not `javax.servlet.*`
5. Test: `mvn clean test -f azure-spring-boot-samples/pom.xml -Dspotbugs.skip=true`

### Running a Single Sample
```bash
cd azure-spring-boot-samples/[sample-directory]
mvn spring-boot:run
# Visit http://localhost:8080 (or configured port)
```

### Debugging Build Issues

| Problem | Solution |
|---------|----------|
| Test fails with "cannot find symbol" | Ensure Jakarta imports: `jakarta.servlet.*`, `jakarta.annotation.*`, etc. |
| `OutOfMemoryError` during build | Run tests in smaller batches; increase heap: `export MAVEN_OPTS="-Xmx2g"` |
| Spotbugs warnings on `@PostConstruct` methods | Expected—use `-Dspotbugs.skip=true` if not blocking; Spring invokes these automatically |
| Missing dependency in samples | Ensure parent samples pom has Spring Boot 3.3.0 and required OAuth2/Security dependencies |
| "Spring Security oauth2 class not found" | Add `spring-security-oauth2-client` dependency to sample pom |

## Git & Branching

- **Default branch**: `feature/spring-boot-3x-jakarta-migration` (development branch for migration work)
- **Main branch**: `master` (stable; PR merges require review)
- **CI checks**: Builds on Java 17 & 21; SpotBugs analysis; samples compilation
- **All tests must pass** before merging to master

## Key Files to Know

- `azure-spring-boot-parent/pom.xml` — Version management (Spring Boot, Cloud Azure, libraries)
- `azure-spring-boot/pom.xml` — Core module dependencies
- `azure-spring-boot-starters/pom.xml` — Aggregator for all starters
- `.github/workflows/ci.yml` — CI/CD pipeline definition
- `config/checkstyle.xml` — Code style rules
- `HowToContribute.md` — Contribution guidelines

## Important Notes for Contributors

1. **Jakarta EE Migration Complete** — All imports use `jakarta.*` (not `javax.*`)
2. **Spring Boot 3.3.0** — Not 2.7.x; uses Spring Security 6.x
3. **Test Coverage** — Aim for ≥ 65% for new code; CI enforces this
4. **Checkstyle & SpotBugs** — Enabled by default; use `-Dcheckstyle.skip=true` only during development
5. **Samples** — All sample security configurations updated to Spring Security 6.x patterns
6. **Maven Central** — Starters published under `com.azure.spring` groupId (Spring Cloud Azure equivalents)

## Troubleshooting Spring Boot 3.x Migration Issues

- **`javax.servlet` import errors** → Change to `jakarta.servlet`
- **`javax.validation` import errors** → Change to `jakarta.validation`
- **`javax.annotation` import errors** → Change to `jakarta.annotation`
- **WebSecurityConfigurerAdapter not found** → Use `@Bean SecurityFilterChain` pattern
- **@EnableGlobalMethodSecurity deprecated** → Use `@EnableMethodSecurity`
- **SpotBugs UPM_UNCALLED_PRIVATE_METHOD warnings on @PostConstruct** → Expected; Spring framework invokes these at runtime
