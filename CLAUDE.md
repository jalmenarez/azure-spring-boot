# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Quick Start](#quick-start)
3. [Project Structure](#project-structure)
4. [Architecture & Design](#architecture--design)
5. [Development Workflows](#development-workflows)
6. [Build & Test Guide](#build--test-guide)
7. [Troubleshooting](#troubleshooting)
8. [Spring Boot 3.x Migration Guide](#spring-boot-3x-migration-guide)

---

## Project Overview

### What is Azure Spring Boot?

**Azure Spring Boot** is a suite of Spring Boot starters that simplify the integration of Microsoft Azure services into Spring applications. Instead of managing Azure SDK configuration directly, developers use familiar Spring Boot patterns like `@ConfigurationProperties` and auto-configuration beans.

**Current Version:** 2.1.7-SNAPSHOT  
**Framework:** Spring Boot 3.3.0 + Spring Cloud Azure 5.11.0  
**Minimum Java:** 17  
**Status:** ✅ Production-ready with full Jakarta EE compliance

### Key Features

| Feature | Details |
|---------|----------|
| **14+ Service Integrations** | Key Vault, Cosmos DB, Service Bus, Storage, AAD, Media Services, Gremlin, Metrics, and more |
| **Zero-Config Setup** | Auto-discovery and conditional auto-configuration reduce boilerplate |
| **Type-Safe Properties** | `@ConfigurationProperties` classes with validation using Jakarta Bean Validation |
| **Spring Security 6.x** | Modern OAuth2/OIDC patterns for Azure AD and B2C authentication |
| **Comprehensive Testing** | 32 core unit tests, 12 validated starter modules |
| **Maven Central Published** | All starters available under `com.azure.spring` groupId |

**Migration Status:** ✅ Completed migration from Spring Boot 2.7.18 → 3.3.0 with full Jakarta EE namespace updates (javax → jakarta)

---

## Quick Start

### First-Time Setup

```bash
# Clone repository
git clone https://github.com/jalmenarez/azure-spring-boot.git
cd azure-spring-boot

# Install dependencies and run tests
mvn clean verify -Dspotbugs.skip=true

# Expected output
# BUILD SUCCESS (32/32 tests pass)
```

### Common Developer Commands

```bash
# Run core module tests
mvn clean test -f azure-spring-boot/pom.xml

# Run starters tests
mvn clean test -f azure-spring-boot-starters/pom.xml

# Build and run a sample locally
cd azure-spring-boot-samples/azure-storage-spring-boot-sample
mvn spring-boot:run
# Visit http://localhost:8080

# Run specific test
mvn test -Dtest=KeyVaultEnvironmentPostProcessorTest

# Skip static analysis during development
mvn clean test -Dcheckstyle.skip=true -Dspotbugs.skip=true
```

---

## Project Structure

### Module Organization

```
azure-spring-boot/
├── azure-spring-boot-bom/                    [Bill of Materials - version management]
├── azure-spring-boot-parent/                 [Parent POM - Java 17, plugins, dependencies]
├── azure-spring-boot/                        [Core module - 67 source files, 56 test files]
├── azure-spring-boot-starters/               [12 production starters]
│   ├── azure-active-directory-spring-boot-starter/
│   ├── azure-active-directory-b2c-spring-boot-starter/
│   ├── azure-cosmosdb-spring-boot-starter/
│   ├── azure-keyvault-secrets-spring-boot-starter/
│   ├── azure-servicebus-spring-boot-starter/
│   ├── azure-storage-spring-boot-starter/
│   ├── azure-mediaservices-spring-boot-starter/
│   ├── spring-data-gremlin-boot-starter/
│   └── ... 4 more starters
└── azure-spring-boot-samples/                [Example applications - 10 samples]
    ├── azure-active-directory-b2c-oidc-spring-boot-sample/
    ├── azure-active-directory-spring-boot-backend-sample/
    └── ... 8 more samples
```

### File Structure Within Each Module

**For each Azure Service:**

```
azure-spring-boot/src/main/java/
└── com/microsoft/azure/spring/autoconfigure/{service}/
    ├── {Service}AutoConfiguration.java        [Conditional auto-configuration bean]
    ├── {Service}Properties.java               [Type-safe configuration properties]
    ├── {Service}Environment.java              [Service-specific logic/facade]
    └── {Service}Exception.java                [Custom exceptions]

azure-spring-boot/src/test/java/
└── com/microsoft/azure/spring/autoconfigure/{service}/
    └── {Service}*Test.java                    [Unit tests with ≥65% coverage]
```

### Key Configuration Files

| File | Purpose |
|------|----------|
| `azure-spring-boot-parent/pom.xml` | Version management (Spring Boot 3.3.0, Cloud Azure 5.11.0) |
| `.github/workflows/ci.yml` | CI/CD pipeline (Java 17 & 21 builds) |
| `config/checkstyle.xml` | Code style rules (enforced on all commits) |
| `config/findbugs-exclude.xml` | SpotBugs exclusions |
| `HowToContribute.md` | Contribution guidelines |

---

## Architecture & Design

### Auto-Configuration Pattern

Every Azure service in this project follows the **Spring Boot Auto-Configuration** pattern:

```java
// 1. Properties class (Type-safe configuration)
@ConfigurationProperties(prefix = "azure.storage")
public class StorageProperties {
    private String account;
    private String key;
    
    @Validated
    @Valid
    // ... getters/setters
}

// 2. Auto-configuration class (Conditional bean creation)
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "azure.storage", name = "account")
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClient blobServiceClient(StorageProperties props) {
        // Create and configure Azure Storage client
        return new BlobServiceClientBuilder()
            .endpoint("https://" + props.getAccount() + ".blob.core.windows.net")
            .credential(new StorageSharedKeyCredential(props.getAccount(), props.getKey()))
            .buildClient();
    }
}

// 3. Usage in application
@SpringBootApplication
public class Application {
    @Autowired
    private BlobServiceClient blobServiceClient; // Auto-configured by framework
}
```

**Configuration in application.yml:**
```yaml
azure:
  storage:
    account: mystorageaccount
    key: myaccesskey
```

### Spring Security 6.x Patterns

The codebase was updated to Spring Security 6.x which removed the `WebSecurityConfigurerAdapter` base class. All security configurations now use the `SecurityFilterChain` bean pattern:

**Spring Security 6.x Pattern (Current):**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Lambda-style configuration (preferred)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/api/**").hasRole("USER")
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/dashboard"))
            .logout(logout -> logout
                .logoutSuccessUrl("/"));
        
        return http.build();
    }
}
```

**Key Changes from Spring Security 5.x:**
- `authorizeRequests()` → `authorizeHttpRequests()` 
- `.and()` chaining → Lambda functional configuration
- `WebSecurityConfigurerAdapter` → `SecurityFilterChain` bean
- `@EnableGlobalMethodSecurity` → `@EnableMethodSecurity`

### Jakarta EE Namespace

All `javax.*` imports have been migrated to `jakarta.*`:

```java
// ❌ OLD (Spring Boot 2.x)
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.annotation.PostConstruct;

// ✅ NEW (Spring Boot 3.x)
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.annotation.PostConstruct;
```

---

## Development Workflows

### Workflow 1: Adding a New Azure Service Integration

**Scenario:** Add support for Azure Service X

**Steps:**

1. **Create Properties Class**
   ```java
   // src/main/java/com/microsoft/azure/spring/autoconfigure/servicex/ServiceXProperties.java
   @ConfigurationProperties(prefix = "azure.servicex")
   public class ServiceXProperties {
       private String endpoint;
       private String credential;
       // Add validation with @NotEmpty, @NotNull, etc.
   }
   ```

2. **Create Auto-Configuration Class**
   ```java
   @Configuration
   @ConditionalOnProperty(prefix = "azure.servicex", name = "endpoint")
   @EnableConfigurationProperties(ServiceXProperties.class)
   public class ServiceXAutoConfiguration {
       @Bean
       @ConditionalOnMissingBean
       public ServiceXClient serviceXClient(ServiceXProperties props) {
           return new ServiceXClient(props.getEndpoint(), props.getCredential());
       }
   }
   ```

3. **Register Auto-Configuration**
   - Add to `spring.factories` (Spring Boot 2.x) or
   - Create `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

4. **Write Unit Tests** (≥65% coverage)
   ```java
   @RunWith(MockitoJUnitRunner.class)
   public class ServiceXAutoConfigurationTest {
       @Test
       public void testAutoConfigurationBean() {
           // Use ApplicationContextRunner to test conditional configuration
       }
   }
   ```

5. **Create Starter POM**
   ```xml
   <!-- azure-spring-boot-starters/azure-servicex-spring-boot-starter/pom.xml -->
   <dependency>
       <groupId>com.microsoft.azure</groupId>
       <artifactId>azure-spring-boot</artifactId>
   </dependency>
   ```

6. **Test Changes**
   ```bash
   mvn clean test -Dtest=ServiceX*
   ```

### Workflow 2: Fixing a Test Failure

**Scenario:** Test fails with "javax.servlet cannot be found"

**Root Cause:** Jakarta EE migration—file has old imports

**Fix:**
```bash
# 1. Find affected files
grep -r "import javax.servlet" azure-spring-boot/

# 2. Update imports
# OLD: import javax.servlet.http.HttpServletRequest;
# NEW: import jakarta.servlet.http.HttpServletRequest;

# 3. Run tests
mvn clean test -Dtest=YourTestClass
```

### Workflow 3: Running a Sample Application

**Scenario:** Test the Azure Storage starter locally

**Steps:**
```bash
# 1. Navigate to sample
cd azure-spring-boot-samples/azure-storage-spring-boot-sample

# 2. Set up Azure credentials (environment or application.yml)
export AZURE_STORAGE_ACCOUNT_NAME=mystorageaccount
export AZURE_STORAGE_ACCOUNT_KEY=mykey

# 3. Run the sample
mvn spring-boot:run

# 4. Test the application
curl http://localhost:8080/api/blobs

# 5. Stop with Ctrl+C
```

### Workflow 4: Writing Unit Tests

The project uses **JUnit 5** (`org.junit.jupiter.api`) with **AssertJ** for new tests. Some
legacy tests still use JUnit 4 (`org.junit`) — prefer JUnit 5 for new code.

**Pattern for Properties / POJO classes** (see `ServiceEndpointsPropertiesTest`,
`AADB2CPropertiesTest`, `ServiceEndpointsTest` for reference):

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MyPropertiesTest {

    private MyProperties properties;

    @BeforeEach
    public void setUp() {
        properties = new MyProperties();
    }

    @Test
    public void defaultValuesAreSet() {
        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    public void getterThrowsForInvalidInput() {
        assertThatThrownBy(() -> properties.lookup("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}
```

**Pattern for auto-configuration classes** — use Spring Boot's `ApplicationContextRunner`
to test conditional bean creation without a full application context (see
`StorageAutoConfigurationTest`):

```java
private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class));

@Test
public void beanIsCreatedWhenPropertyPresent() {
    contextRunner.withPropertyValues("azure.storage.account=myaccount")
            .run(context -> assertThat(context).hasSingleBean(BlobServiceClient.class));
}
```

**Conventions:**
- Place tests in `src/test/java` mirroring the source package
- Name test classes `{ClassUnderTest}Test.java`
- Cover happy path, error/exception handling, and edge cases (null, empty, defaults)
- Aim for ≥65% coverage on new code (CI-enforced)
- Run a single new test: `mvn test -f azure-spring-boot/pom.xml -Dtest=MyPropertiesTest`

---

## Build & Test Guide

### Full Build Verification

```bash
# ✅ Recommended: Full build with all checks
mvn clean verify

# ⏩ Fast: Build without tests (faster, less thorough)
mvn clean install -DskipTests

# 🧪 Tests only: Run all tests and checks
mvn clean test

# 📊 Specific module: Test only core module
mvn clean test -f azure-spring-boot/pom.xml
```

### Running Tests

```bash
# All tests
mvn clean test

# Specific test class
mvn test -Dtest=KeyVaultEnvironmentPostProcessorTest

# Test matching pattern
mvn test -Dtest=KeyVault*

# Single test method (Maven Surefire)
mvn test -Dtest=KeyVaultOperationUnitTest#testValidateCertificatePath

# Skip static analysis (development only)
mvn clean test -Dcheckstyle.skip=true -Dspotbugs.skip=true
```

### Static Analysis

```bash
# SpotBugs on core module
mvn spotbugs:check -pl azure-spring-boot

# SpotBugs on all modules
mvn compile spotbugs:check

# Checkstyle on all modules
mvn checkstyle:check
```

### Building Samples

```bash
# Build specific sample
mvn clean package -pl azure-spring-boot-samples/azure-storage-spring-boot-sample -DskipTests

# Build all samples
mvn clean package -pl azure-spring-boot-samples -DskipTests -am

# Note: -am flag builds all dependencies automatically
```

### Expected Test Results

| Component | Count | Status |
|-----------|-------|--------|
| Core Module Tests | 32 | ✅ All pass |
| Starter Modules | 12 | ✅ All build |
| Checkstyle Validations | N/A | ✅ Pass |
| SpotBugs Analysis | N/A | ✅ Clean |

---

## Troubleshooting

### Common Issues & Solutions

#### 1. Jakarta EE Import Errors

**Error:** `package javax.servlet does not exist`

**Solution:**
```bash
# Find and replace all javax imports
sed -i 's/import javax\./import jakarta\./g' src/**/*.java

# Specific replacements needed:
# javax.servlet.*     → jakarta.servlet.*
# javax.validation.*  → jakarta.validation.*
# javax.annotation.*  → jakarta.annotation.*
```

#### 2. Spring Security 6.x API Errors

**Error:** `WebSecurityConfigurerAdapter is deprecated and marked for removal`

**Solution:**
```java
// ❌ OLD (Spring Security 5.x)
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception { }
}

// ✅ NEW (Spring Security 6.x)
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { }
}
```

#### 3. OutOfMemory During Build

**Error:** `OutOfMemoryError: Java heap space`

**Solution:**
```bash
# Increase heap size
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"

# Then retry build
mvn clean verify
```

#### 4. SpotBugs @PostConstruct Warnings

**Error:** `UPM_UNCALLED_PRIVATE_METHOD on @PostConstruct methods`

**Context:** Spring framework invokes `@PostConstruct` methods via reflection at runtime, so static analysis tools can't detect the calls.

**Solution:**
```bash
# Skip SpotBugs if not blocking
mvn clean test -Dspotbugs.skip=true

# Or add @SuppressWarnings annotation (less preferred)
@PostConstruct
@SuppressWarnings("UPM_UNCALLED_PRIVATE_METHOD")
private void initialize() { }
```

#### 5. Missing Dependencies in Samples

**Error:** `Spring Security oauth2 class not found`

**Solution:** Ensure sample pom.xml includes required dependency:
```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-client</artifactId>
</dependency>
```

---

## Spring Boot 3.x Migration Guide

### What Changed

This codebase was migrated from **Spring Boot 2.7.18** to **Spring Boot 3.3.0**. This is a major upgrade requiring careful attention to several breaking changes.

### Key Migration Items

| Item | 2.7.x → 3.3.0 Change |
|------|----------------------|
| **Jakarta EE** | `javax.*` → `jakarta.*` |
| **Spring Security** | `WebSecurityConfigurerAdapter` → `SecurityFilterChain` bean |
| **Method Security** | `@EnableGlobalMethodSecurity` → `@EnableMethodSecurity` |
| **Authorization** | `authorizeRequests()` → `authorizeHttpRequests()` |
| **Java Version** | Minimum 11 → Minimum 17 |
| **Validation** | Hibernate Validator 6.x → 8.0.1.Final |

### Migration Checklist for New Features

- [ ] All imports use `jakarta.*` (not `javax.*`)
- [ ] Security configs use `SecurityFilterChain` bean pattern
- [ ] `@EnableMethodSecurity` used (not `@EnableGlobalMethodSecurity`)
- [ ] `authorizeHttpRequests()` used (not `authorizeRequests()`)
- [ ] No `WebSecurityConfigurerAdapter` base class
- [ ] Tests use `jakarta.servlet.*` imports
- [ ] Unit tests have ≥65% coverage
- [ ] Checkstyle passes: `mvn checkstyle:check`
- [ ] SpotBugs passes: `mvn spotbugs:check`

---

## Important Notes for Contributors

### Code Style & Standards

1. **Checkstyle**: Enforced by default. See `config/checkstyle.xml` for rules.
2. **SpotBugs**: Static analysis for bug detection. High-threshold mode active.
3. **Test Coverage**: Minimum 65% for new code (enforced by CI)
4. **Naming**: Follow existing patterns in the codebase
5. **Comments**: Minimal—code should be self-documenting

### Git Workflow

- **Feature branch**: `feature/spring-boot-3x-jakarta-migration`
- **Main branch**: `master` (stable, requires review)
- **CI checks**: Java 17 & 21, SpotBugs, Checkstyle, Sample builds
- **All tests must pass** before PR can merge

### Published Artifacts

Starters are published to [Maven Central](https://search.maven.org/) under:
- **GroupId**: `com.azure.spring`
- **Artifact IDs**: Follow Spring Cloud Azure naming (e.g., `spring-cloud-azure-starter-keyvault-secrets`)

---

## Additional Resources

- **README.md**: Project overview and dev container setup
- **HowToContribute.md**: Detailed contribution guidelines
- **MIGRATION_SPRING_BOOT_3X.md**: Complete migration plan and timeline
- **.github/workflows/ci.yml**: CI/CD pipeline definition
- [Spring Boot 3.x Migration Guide](https://spring.io/blog/2022/01/20/spring-boot-3-0-0-m1-is-now-available)
- [Spring Cloud Azure 5.x Documentation](https://learn.microsoft.com/en-us/azure/developer/java/spring-framework/spring-cloud-azure)

---

**Last Updated:** May 29, 2026  
**Migration Status:** ✅ Complete (Spring Boot 3.3.0 + Jakarta EE)
