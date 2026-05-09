# Análisis: Actualización de Azure Spring Boot BOM

## Situación Actual

**Versión Actual**: `com.microsoft.azure:azure-spring-boot-bom:2.1.7-SNAPSHOT`  
**Versión disponible**: `com.azure.spring:azure-spring-boot-bom:3.14.0` (EOL) o `4.0.0+`

## Problemas Identificados

### 1. Artifact Relocation (2020)
El artifact ha sido relocado de:
- `com.microsoft.azure:azure-spring-boot-bom` → `com.azure.spring:azure-spring-boot-bom`

### 2. Dependencias Obsoletas
El proyecto usa starters y SDKs que ya no existen en los BOMs modernos:
- `com.microsoft.azure:spring-data-cosmosdb` - Reemplazado por `com.azure:azure-cosmos`
- `com.microsoft.spring.data.gremlin:spring-data-gremlin` - Deprecado
- `com.microsoft.azure:azure-documentdb` - Reemplazado por `com.azure:azure-cosmos`
- `com.microsoft.azure:azure-media` - Deprecado
- `com.microsoft.azure:azure-servicebus` - Reemplazado por `com.azure:azure-messaging-servicebus`
- `com.microsoft.azure:azure-storage-blob` - Reemplazado por `com.azure:azure-storage-blob`
- `com.microsoft.azure:adal4j` - Reemplazado por `com.azure:azure-identity`
- `com.microsoft.azure:azure-client-authentication` - Deprecado

### 3. Maven Central Availability
La mayoría de estas dependencias antiguas **no están disponibles** en Maven Central 2.1.x o superior.

## Estrategia de Actualización Recomendada

### Opción A: Actualización Completa (Recomendada)
**Scope**: Migración completa de Azure SDK antiguo a SDK moderno
**Effort**: Alto
**Beneficio**: Compatible con Spring Boot 3.x+, mejores APIs, mejor soporte

**Pasos**:
1. Actualizar a `com.azure.spring:azure-spring-boot-bom:4.0.0+`
2. Reemplazar starters antiguos con nuevos:
   - `spring-cloud-azure-starter-cosmos` para CosmosDB
   - `spring-cloud-azure-starter-storage-blob` para Storage
   - `spring-cloud-azure-starter-service-bus` para Service Bus
   - `spring-cloud-azure-starter-keyvault-secrets` para Key Vault
   - `spring-cloud-azure-starter-identity` para autenticación
3. Actualizar código que use APIs deprecadas
4. Migrar javax → jakarta (si se usa Spring Boot 3.x)
5. Ejecutar tests completos

### Opción B: Actualización Parcial (No Recomendada)
**Scope**: Mantener BOM antiguo pero actualizar versiones
**Effort**: Bajo
**Beneficio**: Cambios mínimos inmediatos

**Limitación**: No resuelve el problema de fondo - las versiones siguen siendo antiguas

### Opción C: Mantener Estado Actual (No Recomendado)
**Scope**: No hacer cambios
**Beneficio**: Sin cambios
**Riesgo**: Vulnerabilidades de seguridad sin parches

## Versiones Modernas Disponibles

| Componente | Versión Antigua | Componente Moderno | Versión Moderna |
|---|---|---|---|
| azure-spring-boot-bom | 2.1.7 | azure-spring-boot-bom | 4.0.0+ |
| spring-data-cosmosdb | 2.1.x | spring-cloud-azure-starter-cosmos | 4.0.0+ |
| azure-storage-blob | 2.1.x | spring-cloud-azure-starter-storage-blob | 4.0.0+ |
| azure-servicebus | 2.1.x | spring-cloud-azure-starter-service-bus | 4.0.0+ |
| adal4j | 1.6.x | spring-cloud-azure-starter-identity | 4.0.0+ |

## Compatibilidad con Spring Boot

- **Spring Boot 2.7.15** (actual): Compatible con Azure Spring Boot BOM 4.x hasta 4.20.0
- **Spring Boot 3.x**: Compatible con Azure Spring Boot BOM 4.x / Spring Cloud Azure 7.x

## Recomendación Final

**Se recomienda:**
1. ✅ Implementar **Opción A** - Actualización completa
2. ✅ Realizar en fases (ver DEPENDENCY_UPDATES.md)
3. ✅ Mantener Sprint Boot 2.7.15 inicialmente
4. ✅ Planificar migración a Spring Boot 3.x como fase futura

## Próximos Pasos

1. Documentar lista completa de cambios de API necesarios
2. Actualizar cada starter uno por uno
3. Ejecutar tests después de cada cambio
4. Crear PRs para revisión gradual

## Referencias

- [Azure Spring Boot Migration Guide](https://github.com/Azure/azure-sdk-for-java)
- [Spring Cloud Azure Documentation](https://learn.microsoft.com/en-us/azure/developer/java/spring-framework/)
- [Spring Boot Version Support Matrix](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Versions-Mapping)

---

**Autor**: Análisis de actualización de dependencias Azure  
**Fecha**: 2026-05-09  
**Estado**: Análisis completo - Esperando aprobación para implementación
