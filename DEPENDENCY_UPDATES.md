# Dependency Updates Available

This document outlines all available dependency updates for the Azure Spring Boot project.

## Summary

The project is currently using Spring Boot 2.7.15 (latest 2.x LTS) with various supporting libraries. Below is a comprehensive list of available updates with recommendations.

## Critical Updates Recommended

### Security Updates

| Dependency | Current | Available | Type | Priority |
|---|---|---|---|---|
| nimbus-jose-jwt | 9.16.1 | 9.25+ | Security | **HIGH** |
| commons-io | 2.11.0 | 2.13.0+ | Bug Fixes | **HIGH** |
| micrometer-core | 1.9.5 | 1.11.4+ | Bug Fixes | **HIGH** |

### Feature Updates

| Dependency | Current | Available | Type | Notes |
|---|---|---|---|---|
| mockito-core | 4.11.0 | 5.2.0+ | Feature | API improvements |
| findbugs-annotations | 2.0.1 | 3.0.1 | Feature | New annotations |
| maven-javadoc-plugin | 3.5.0 | 3.6.0+ | Feature | Better support |

## Major Version Upgrades (Requires Code Changes)

The following dependencies have major version upgrades available but require significant code changes:

### Spring Boot 3.x Upgrade Path

To upgrade to Spring Boot 3.x, the following changes would be required:

1. **javax → jakarta namespace migration**
   - Change: `javax.validation` → `jakarta.validation`
   - Change: `javax.servlet` → `jakarta.servlet`
   - Change: `javax.annotation` → `jakarta.annotation`

2. **Azure Libraries Major Versions**
   - `azure-active-directory-spring-boot-starter`: 2.1.7 → 3.0.0
   - `azure-storage-spring-boot-starter`: 2.1.7 → 3.0.0
   - `azure-cosmosdb-spring-boot-starter`: 2.1.7 → 3.0.0
   - `azure-keyvault-secrets-spring-boot-starter`: 2.1.7 → 3.0.0

3. **Azure Service SDKs**
   - `azure-storage-blob`: 10.1.0 → 12.x
   - `azure-servicebus`: 1.2.8 → 3.6.7+
   - `azure-keyvault`: 1.0.0 → 1.2.6

## Recommended Update Strategy

### Phase 1: Minor Version Updates (Low Risk)
- [ ] Update mockito-core to 5.2.0+
- [ ] Update findbugs-annotations to 3.0.1
- [ ] Update maven-javadoc-plugin to 3.6.0
- [ ] Update commons-io to 2.13.0
- [ ] Update nimbus-jose-jwt to 9.25.2
- [ ] Update micrometer-core to 1.11.4

**Impact**: Minimal code changes, mostly backward compatible

### Phase 2: Spring Boot 2.7.x Stability Updates
- [ ] Keep Spring Boot at 2.7.15 (latest LTS 2.x)
- [ ] Update other Spring libraries to 2.7.x compatible versions
- [ ] Update azure-spring-boot related libraries to latest 2.x compatible versions

**Impact**: Bug fixes and security patches within 2.x series

### Phase 3: Spring Boot 3.x Migration (Future)
- [ ] Migrate all javax imports to jakarta
- [ ] Update Spring Boot to 3.3.0 or later
- [ ] Update Azure SDK libraries to 3.x versions
- [ ] Update dependent libraries to 3.x compatible versions

**Impact**: Major refactoring required, breaking changes possible

## Compatibility Matrix

### Current Configuration (Safe)
- Java: 11+ ✅
- Spring Boot: 2.7.15 ✅
- Spring Framework: 5.3.x ✅
- Maven: 3.0+ ✅

### Planned Configuration (Phase 1)
- Java: 11+ ✅
- Spring Boot: 2.7.15 ✅
- Selected dependency updates compatible with 2.7.x

### Future Configuration (Phase 3)
- Java: 17+ (minimum for Spring Boot 3.x)
- Spring Boot: 3.3.0+
- Spring Framework: 6.x
- jakarta instead of javax

## Testing Checklist

Before updating any dependencies:

- [ ] Run full test suite
- [ ] Run code coverage analysis
- [ ] Verify checkstyle compliance
- [ ] Test against various Java versions
- [ ] Test against various Spring Boot supported versions
- [ ] Update documentation if APIs change
- [ ] Update samples with new patterns

## Resources

- [Spring Boot Release Notes](https://spring.io/projects/spring-boot)
- [Spring Boot 3.x Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)

## Next Steps

1. Review this document in team meetings
2. Plan implementation of Phase 1 updates
3. Create separate PRs for each dependency update (for easier review)
4. Update tests and documentation
5. Plan Spring Boot 3.x migration for a future release

## Questions?

For questions about dependency updates or migration planning, please:
- Open an issue on GitHub
- Discuss in the project's Gitter chat
- Contact the maintainers
