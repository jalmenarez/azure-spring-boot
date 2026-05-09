# Spring Boot 3.x Migration Plan

**Target:** Spring Boot 3.3.0+ with Spring Cloud Azure 5.x  
**Current:** Spring Boot 2.7.18 with Spring Cloud Azure 4.20.0  
**Estimated Duration:** 10–12 working days  
**Branch:** `feature/spring-boot-3x-jakarta-migration`

---

## Overview

This document provides a step-by-step plan to migrate azure-spring-boot from Spring Boot 2.7.18 to Spring Boot 3.3.0, including the critical namespace migration from `javax.*` to `jakarta.*`.

### Key Changes Required

1. **Namespace migration**: `javax.*` → `jakarta.*` (required by Spring Boot 3.x)
2. **Spring Cloud Azure**: 4.20.0 → 5.x
3. **Dependencies**: Remove javax-* libraries, add jakarta-* equivalents
4. **Java**: Already at 17+ (compatible)

---

## Phase 1: Preparation & Analysis (Days 1–2)

### 1.1 Create Feature Branch
```bash
git checkout -b feature/spring-boot-3x-jakarta-migration
```

### 1.2 Impact Assessment

**Files to modify:**
- **POM files**: 14 (root, BOM, parent, core, 11 starters)
- **Java sources**: ~150 files total
- **Test files**: ~100+ files
- **Files with `javax.*` imports**: ~27 files requiring manual review

**Namespace changes:**
| Old (javax) | New (jakarta) |
|---|---|
| `javax.servlet.*` | `jakarta.servlet.*` |
| `javax.annotation.*` | `jakarta.annotation.*` |
| `javax.validation.*` | `jakarta.validation.*` |
| `javax.sql.*` | `jakarta.sql.*` |
| `javax.naming.*` | `jakarta.naming.*` |

### 1.3 Risk Assessment

- **Low risk**: Dependency updates, POM changes
- **Medium risk**: Servlet filter/interceptor updates, validation annotation migrations
- **High risk**: Spring Cloud Azure 5.x API compatibility
- **Contingency**: Maintain `migrate/spring-boot-3x-base` branch for rollback

---

## Phase 2: Dependency & Build Configuration (Days 3–4)

### 2.1 Update `azure-spring-boot-bom/pom.xml`

**Change line 50-ish (spring.cloud.azure.version):**
```xml
<!-- FROM -->
<spring.cloud.azure.version>4.20.0</spring.cloud.azure.version>

<!-- TO -->
<spring.cloud.azure.version>5.11.0</spring.cloud.azure.version>
```

**Rationale**: Spring Cloud Azure 5.x requires Spring Boot 3.x

### 2.2 Update `azure-spring-boot-parent/pom.xml`

**Change spring-boot version (line ~31):**
```xml
<!-- FROM -->
<spring.boot.version>2.7.18</spring.boot.version>

<!-- TO -->
<spring.boot.version>3.3.0</spring.boot.version>
```

**Update Spring Cloud Azure version (line ~32):**
```xml
<!-- FROM -->
<spring.cloud.azure.version>4.20.0</spring.cloud.azure.version>

<!-- TO -->
<spring.cloud.azure.version>5.11.0</spring.cloud.azure.version>
```

**Update Hibernate Validator (line ~42):**
```xml
<!-- FROM -->
<hibernate.validator.version>6.2.5.Final</hibernate.validator.version>

<!-- TO -->
<hibernate.validator.version>8.0.1.Final</hibernate.validator.version>
```

**Update Micrometer (line ~37):**
```xml
<!-- FROM -->
<micrometer.core.version>1.9.17</micrometer.core.version>

<!-- TO -->
<micrometer.core.version>1.12.0</micrometer.core.version>
```

**Verify Java version (line 26–28):**
```xml
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>
<maven.compiler.release>17</maven.compiler.release>
```
(Already correct—no changes needed)

**Remove deprecated javax dependencies (lines ~229–234):**
```xml
<!-- REMOVE: javax.annotation-api - provided by Spring Boot 3.x BOM -->
<dependency>
    <groupId>com.google.code.findbugs</groupId>
    <artifactId>jsr305</artifactId>
    <version>3.0.2</version>
</dependency>
```

**Add jakarta APIs after line 147 (dependencyManagement section):**
```xml
<!-- Jakarta APIs (Spring Boot 3.x requirement) -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.0.0</version>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <version>3.0.2</version>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
    <version>2.1.1</version>
    <optional>true</optional>
</dependency>
```

### 2.3 Update `azure-spring-boot/pom.xml`

**Remove lines ~49–52 (javax.validation-api):**
```xml
<!-- REMOVE -->
<dependency>
    <groupId>javax.validation</groupId>
    <artifactId>validation-api</artifactId>
    <optional>true</optional>
</dependency>
```

**Remove lines ~115–118 (javax.servlet-api):**
```xml
<!-- REMOVE -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <optional>true</optional>
</dependency>
```

**Add after spring-web dependency (line ~34):**
```xml
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <optional>true</optional>
</dependency>
```

### 2.4 Verify Starter POMs

All starters in `azure-spring-boot-starters/` inherit from parent—no direct changes needed if they properly reference parent properties.

**Verify each starter has:**
```xml
<parent>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-spring-boot-parent</artifactId>
    <version>2.1.7-SNAPSHOT</version>
</parent>
```

### 2.5 Test POM Changes

**Build and validate POMs compile:**
```bash
mvn clean install -f azure-spring-boot-bom/pom.xml
mvn clean install -f azure-spring-boot-parent/pom.xml
mvn clean install -f azure-spring-boot/pom.xml
```

---

## Phase 3: Source Code Migration (Days 5–7)

### 3.1 Automated Namespace Migration

Use this script to replace all `javax.*` imports with `jakarta.*`:

```bash
find ./azure-spring-boot -type f -name "*.java" -path "*/src/*" ! -path "*/target/*" | \
  xargs sed -i 's/import javax\./import jakarta./g' && \
  xargs sed -i 's/import javax\./import jakarta./g'
```

### 3.2 Manual Review — Priority 1 (Servlet Handling — 4 files)

These files handle HTTP requests and must be carefully reviewed:

#### File 1: `azure-spring-boot/src/main/java/.../b2c/AADB2CAuthorizationRequestResolver.java`
- **Change**: Line 6 `import javax.servlet.http.HttpServletRequest;` → `jakarta.servlet.http.HttpServletRequest;`
- **Verify**: `OncePerRequestFilter` logic still works (it does in Spring 6.x)

#### File 2: `azure-spring-boot/src/main/java/.../aad/AADAuthenticationFilter.java`
- **Change**: Lines 19–23 servlet imports from `javax.servlet.*` to `jakarta.servlet.*`
- **Verify**: Filter chain logic, `doFilterInternal()` method signature

#### File 3: Test file `AADAuthenticationFilterTest.java`
- **Change**: Servlet mock imports

#### File 4: Test file `AADB2CAuthorizationRequestResolverTest.java`
- **Change**: Servlet mock imports

### 3.3 Manual Review — Priority 2 (Annotations — 8 files)

#### Files with `@PostConstruct`:
- `azure-spring-boot/src/main/java/.../servicebus/ServiceBusAutoConfiguration.java`
- `azure-spring-boot/src/main/java/.../sqlserver/KeyVaultProperties.java`
- `azure-spring-boot/src/main/java/.../mediaservices/MediaServicesAutoConfiguration.java`
- `azure-spring-boot/src/main/java/.../storage/StorageAutoConfiguration.java`
- `azure-spring-boot/src/main/java/.../aad/AADAuthenticationFilterAutoConfiguration.java`

**Change**: `import javax.annotation.PostConstruct;` → `import jakarta.annotation.PostConstruct;`

#### Files with `@NotEmpty`, `@NotBlank`, `@Valid`:
- `azure-spring-boot/src/main/java/.../aad/AADAuthenticationProperties.java`
- `azure-spring-boot/src/main/java/.../b2c/AADB2CProperties.java`
- `azure-spring-boot/src/main/java/.../storage/StorageProperties.java`

**Change**: `import javax.validation.constraints.*` → `import jakarta.validation.constraints.*`

### 3.4 Manual Review — Priority 3 (Data/SQL — 2 files)

#### File: `azure-spring-boot/src/main/java/.../sqlserver/AlwaysEncryptedAutoConfiguration.java`

**Change**: `import javax.sql.DataSource;` → `import jakarta.sql.DataSource;`

### 3.5 Manual Review — Priority 4 (JNDI — 1 file)

#### File: `azure-spring-boot/src/main/java/.../aad/AADAuthenticationFilter.java`

**Change**: `import javax.naming.ServiceUnavailableException;` → `import jakarta.naming.ServiceUnavailableException;`

### 3.6 Update Test Files

**Run automated migration on all test files:**
```bash
find ./azure-spring-boot/src/test -type f -name "*.java" | \
  xargs sed -i 's/import javax\./import jakarta./g'
```

**Manually verify:**
- Test servlet mocks
- Test validation setup
- Test resource files

---

## Phase 4: Spring Cloud Azure Alignment (Days 8–9)

### 4.1 Verify AutoConfiguration

Check `/home/user/azure-spring-boot/azure-spring-boot/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

Verify all listed autoconfiguration classes compile:
- `AzureAuthenticationFilterAutoConfiguration`
- `AzureADAutoConfiguration`
- `AzureADB2CAutoConfiguration`
- `AzureKeyVaultAutoConfiguration`
- `AzureServiceBusAutoConfiguration`
- `AzureStorageAutoConfiguration`
- `AzureCosmosDBAutoConfiguration`
- `AzureMediaServicesAutoConfiguration`
- `AzureSQLServerAutoConfiguration`
- `AzureGremlinAutoConfiguration`

### 4.2 Test Spring Cloud Azure Integration

```bash
mvn clean compile -pl azure-spring-boot
mvn clean test -pl azure-spring-boot
```

**Expected outcome**: All tests pass with Spring Cloud Azure 5.x

---

## Phase 5: Testing Strategy (Days 10–12)

### 5.1 Unit Test Validation

**Command:**
```bash
mvn clean test -pl azure-spring-boot
```

**Expected**: All tests pass with:
- Jakarta imports
- Spring Boot 3.3.0 API
- Spring Cloud Azure 5.x components

### 5.2 Module-by-Module Build

```bash
# 1. Core module
mvn clean verify -pl azure-spring-boot

# 2. All starters
mvn clean verify -pl azure-spring-boot-starters

# 3. All samples
mvn clean verify -pl azure-spring-boot-samples -am

# 4. Full build
mvn clean verify
```

### 5.3 Sample Application Testing

For each sample, verify it:
1. **Compiles** without errors
2. **Starts** Spring Boot context
3. **Connects** to Azure services (if credentials available)

Key samples to test:
- `azure-storage-spring-boot-sample` — blob operations
- `azure-servicebus-spring-boot-sample` — messaging
- `azure-cosmosdb-spring-boot-sample` — database operations
- `azure-active-directory-spring-boot-backend-sample` — OAuth2 auth
- `azure-active-directory-b2c-oidc-spring-boot-sample` — B2C auth

### 5.4 SpotBugs & Checkstyle

```bash
mvn spotbugs:check -pl azure-spring-boot
mvn checkstyle:check
```

**Expected**: No violations (same as Spring Boot 2.7.18)

---

## Phase 6: Validation & Verification (Day 13)

### 6.1 Final Compilation Check

```bash
mvn clean verify --no-transfer-progress
```

### 6.2 Compatibility Verification Checklist

- [ ] All 150+ Java files compile without warnings
- [ ] All 100+ test files pass
- [ ] SpotBugs reports no new issues
- [ ] Checkstyle compliance maintained
- [ ] No `javax.*` imports remaining (grep to verify)
- [ ] All starters properly resolve Spring Cloud Azure 5.x beans
- [ ] All samples start without context initialization errors

### 6.3 Breaking Changes Documentation

Check and document any API changes:
- Spring Security 6.x authentication filter changes
- Spring Data REST API changes
- Spring Cloud Azure 5.x property names

---

## Phase 7: Release & Documentation (Days 14–15)

### 7.1 Update README

**File:** `README.md`

```markdown
<!-- Change line 12 -->
- FROM: Spring Boot 2.7.x
- TO: Spring Boot 3.3.x

<!-- Change line 12 -->
- FROM: Spring Cloud Azure 4.20.0
- TO: Spring Cloud Azure 5.11.0
```

### 7.2 Create Migration Guide

**File:** `MIGRATION_GUIDE_SPRING_BOOT_3X.md`

Document for consumers:
- Breaking changes in starter APIs
- Configuration property changes
- Namespace migration (if they have custom code using Spring Boot starters)
- Dependency updates required

### 7.3 Update CLAUDE.md

**File:** `CLAUDE.md`

```markdown
## Quick Version Reference (updated)

| Component | Version |
|-----------|---------|
| Spring Boot | 3.3.0 |
| Spring Cloud Azure | 5.11.0 |
| Java | 17+ |
| Jakarta APIs | 2.1+ / 3.0+ |
```

### 7.4 Commit Strategy

**Single PR with 5 commits:**

```bash
# Commit 1: Update BOMs and parent POM
git add azure-spring-boot-bom/pom.xml azure-spring-boot-parent/pom.xml
git commit -m "deps: upgrade Spring Boot to 3.3.0 and Spring Cloud Azure to 5.x

- Update spring-boot-starter-parent from 2.7.18 to 3.3.0
- Update spring-cloud-azure-dependencies from 4.20.0 to 5.11.0
- Replace javax.* dependencies with jakarta.* equivalents
- Update hibernate-validator to 8.0.1.Final for Jakarta compatibility
"

# Commit 2: Update core and starter POMs
git add azure-spring-boot/pom.xml azure-spring-boot-starters/*/pom.xml
git commit -m "deps: update azure-spring-boot modules for Spring Boot 3.x

- Remove javax.* dependencies from core module
- Update test dependencies to Spring Boot 3.3.0 compatible versions
"

# Commit 3: Migrate source code to Jakarta
git add azure-spring-boot/src/main/java
git commit -m "refactor: migrate javax imports to jakarta namespace

- Update all servlet imports: javax.servlet → jakarta.servlet
- Update all annotation imports: javax.annotation → jakarta.annotation
- Update all validation imports: javax.validation → jakarta.validation
- Update SQL imports: javax.sql → jakarta.sql
"

# Commit 4: Migrate test code
git add azure-spring-boot/src/test/java
git commit -m "test: migrate test code to Jakarta namespace

- Update all test servlet mocks and imports
- Update validation test setup
"

# Commit 5: Update documentation
git add README.md CLAUDE.md MIGRATION_GUIDE_SPRING_BOOT_3X.md
git commit -m "docs: update documentation for Spring Boot 3.x migration

- Update README with new version numbers
- Add migration guide for consumers
- Update CLAUDE.md with new Spring Boot 3.3.0 reference
"
```

### 7.5 Create Pull Request

```bash
git push -u origin feature/spring-boot-3x-jakarta-migration
# Create PR on GitHub with:
# - Title: "Migrate to Spring Boot 3.3.0 and Spring Cloud Azure 5.x"
# - Description: Links to this plan, testing checklist
# - Label: enhancement, major-version-upgrade
# - Reviewers: Spring Boot/Azure experts
```

---

## Rollback & Contingency Plan

### Rollback Trigger Conditions

Rollback if:
1. Spring Cloud Azure 5.x has critical incompatibilities not documented
2. 3+ sample applications fail to start
3. Security issue discovered in Spring Boot 3.3.0 or Jakarta APIs
4. Migration would take >20 working days

### Rollback Steps

```bash
# If PR not yet merged:
git reset --hard origin/master
git branch -D feature/spring-boot-3x-jakarta-migration

# If PR already merged:
git revert -m 1 <MERGE_COMMIT_SHA>
git push origin master
```

**Estimated time**: < 1 hour

### Contingency Alternatives

| Issue | Fallback |
|-------|----------|
| Spring Cloud Azure 5.x not ready | Keep latest 4.x version, defer SCZ upgrade |
| Breaking changes in samples | Deprecate old samples, create new Spring Boot 3.x variants |
| Servlet API incompatibilities | Isolate into adapter classes, maintain backward compatibility |

---

## Build Order (Non-Negotiable)

```
1. azure-spring-boot-bom
   ↓
2. azure-spring-boot-parent
   ↓
3. azure-spring-boot (core module)
   ↓
4. azure-spring-boot-starters/* (11 starters)
   ↓
5. azure-spring-boot-samples/* (11+ samples)
```

Each module inherits from the previous. If you skip a module, downstream modules will fail.

---

## Critical Files to Monitor

| File | Why | Change Type |
|------|-----|------------|
| `azure-spring-boot-bom/pom.xml` | Imports Spring Cloud Azure BOM | Version update |
| `azure-spring-boot-parent/pom.xml` | Defines Spring Boot & Java versions | Version + new deps |
| `azure-spring-boot/pom.xml` | Removes javax, adds jakarta | Dependency update |
| `AADAuthenticationFilter.java` | Servlet-based security | Namespace migration |
| `*Properties.java` (8 files) | Configuration properties | Validation annotation updates |
| `META-INF/spring/...AutoConfiguration.imports` | Boot auto-discovery | Verification |

---

## Time Estimates (Sequential Phases)

| Phase | Task | Days | Start | End |
|-------|------|------|-------|-----|
| 1 | Preparation & Analysis | 2 | Day 1 | Day 2 |
| 2 | Build Configuration | 2 | Day 3 | Day 4 |
| 3 | Source Code Migration | 3 | Day 5 | Day 7 |
| 4 | Spring Cloud Azure Alignment | 2 | Day 8 | Day 9 |
| 5 | Testing Strategy | 3 | Day 10 | Day 12 |
| 6 | Validation & Verification | 1 | Day 13 | Day 13 |
| 7 | Release & Documentation | 2 | Day 14 | Day 15 |
| **Total** | | **15 days** | Day 1 | Day 15 |

**With parallelization** (phases 3–4 overlap): **10–12 working days**

---

## Success Criteria

✅ Migration is complete when:

1. **All code compiles** without errors or warnings
2. **All tests pass** with Spring Boot 3.3.0 and Spring Cloud Azure 5.x
3. **No `javax.*` imports** remain in the codebase
4. **All samples start** without context errors
5. **Documentation updated** (README, migration guide)
6. **PR merged** to master and released as new major version

---

## References

- [Spring Boot 3.x Migration Guide](https://spring.io/projects/spring-boot#learn)
- [Spring Cloud Azure Documentation](https://learn.microsoft.com/en-us/java/api/overview/azure/spring-cloud-azure-runtime)
- [Jakarta EE Namespace Migration](https://jakarta.ee/learn/faq/)
- [Spring Security 6.x Changes](https://docs.spring.io/spring-security/reference/6.0/index.html)

---

## Next Steps

1. **Review this plan** in a team meeting
2. **Create feature branch**: `git checkout -b feature/spring-boot-3x-jakarta-migration`
3. **Start Phase 1** on Day 1
4. **Create PR** when Phase 3 complete
5. **Request reviews** from Spring Boot / Azure experts
6. **Merge to master** once all phases complete and tests pass

---

**Last Updated:** 2026-05-09  
**Status:** Planning  
**Owner:** Claude Code  
**Target Release:** Q3 2026
