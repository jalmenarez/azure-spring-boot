[![Travis CI](https://travis-ci.org/Microsoft/azure-spring-boot.svg?branch=master)](https://travis-ci.org/Microsoft/azure-spring-boot)
[![AppVeyor](https://ci.appveyor.com/api/projects/status/af0qeprdv3g9ox07/branch/master?svg=true)](https://ci.appveyor.com/project/yungez/azure-spring-boot)
[![codecov](https://codecov.io/gh/Microsoft/azure-spring-boot/branch/master/graph/badge.svg)](https://codecov.io/gh/Microsoft/azure-spring-boot)
[![MIT License](http://img.shields.io/badge/license-MIT-green.svg) ](https://github.com/Microsoft/azure-spring-boot/blob/master/LICENSE)
[![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg)](https://gitter.im/Microsoft/spring-on-azure)

# Azure Spring Boot

### Introduction

This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

### Support Spring Boot
This repository supports Spring Boot 2.7.x and is built on top of [Spring Cloud Azure 4.20.0](https://learn.microsoft.com/azure/developer/java/spring-framework/spring-cloud-azure). Please read [Branch and release](https://github.com/Microsoft/azure-spring-boot/wiki/Branch-and-release) for branch mapping.

### Prerequisites
- JDK 17 and above
- [Maven](http://maven.apache.org/) 3.0 and above

### Recent Changes

The codebase was migrated from the deprecated `azure-dependencies-bom:2.1.0.M5` to the modern `spring-cloud-azure-dependencies:4.20.0`. Notable changes:

- **Spring Boot**: upgraded `spring-boot-starter-parent` from `2.1.0.RELEASE` to `2.7.18`
- **Java**: minimum version bumped from 1.8 to 17; Lombok updated to `1.18.30`
- **Static analysis**: replaced `findbugs-maven-plugin:3.0.5` (incompatible with Java 17 bytecode) with `spotbugs-maven-plugin:4.8.3.1`
- **Azure SDK migration**:
  - Key Vault: `azure-keyvault` → `com.azure:azure-security-keyvault-secrets`
  - Identity: ADAL4J → `com.azure:azure-identity` (`ClientSecretCredential`, `ManagedIdentityCredential`)
  - Storage: reactive RxJava `ServiceURL`/`ContainerURL`/`BlockBlobURL` → synchronous `BlobServiceClient`/`BlobContainerClient`/`BlobClient`
  - Service Bus: `QueueClient`/`TopicClient`/`SubscriptionClient` → named `ServiceBusSenderClient`/`ServiceBusReceiverClient` beans
  - Cosmos DB: `@Document`/`DocumentDbRepository` → `@Container`/`CosmosRepository` (`azure-spring-data-cosmos`)
- **Tests**: key tests migrated from JUnit 4 to JUnit 5; `KeyVaultOperation` refactored to use functional interfaces so it remains testable under the Java 17 module system

### Usage

The starters below are the current **Spring Cloud Azure 4.x** equivalents (groupId `com.azure.spring`).
You can find them in [Maven Central Repository](https://search.maven.org/).

Starter Name | Artifact (com.azure.spring) | Version
---|---|:---:
Active Directory | [spring-cloud-azure-starter-active-directory](azure-spring-boot-starters/azure-active-directory-spring-boot-starter/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.azure.spring/spring-cloud-azure-starter-active-directory.svg)](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-starter-active-directory)
Active Directory B2C | [spring-cloud-azure-starter-active-directory-b2c](azure-spring-boot-starters/azure-active-directory-b2c-spring-boot-starter/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.azure.spring/spring-cloud-azure-starter-active-directory-b2c.svg)](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-starter-active-directory-b2c)
Cosmos DB | [spring-cloud-azure-starter-data-cosmos](azure-spring-boot-starters/azure-cosmosdb-spring-boot-starter/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.azure.spring/spring-cloud-azure-starter-data-cosmos.svg)](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-starter-data-cosmos)
Key Vault Secrets | [spring-cloud-azure-starter-keyvault-secrets](azure-spring-boot-starters/azure-keyvault-secrets-spring-boot-starter/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.azure.spring/spring-cloud-azure-starter-keyvault-secrets.svg)](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-starter-keyvault-secrets)
Service Bus | [spring-cloud-azure-starter-servicebus](azure-spring-boot-starters/azure-servicebus-spring-boot-starter/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.azure.spring/spring-cloud-azure-starter-servicebus.svg)](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-starter-servicebus)
Storage Blob | [spring-cloud-azure-starter-storage-blob](azure-spring-boot-starters/azure-storage-spring-boot-starter/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.azure.spring/spring-cloud-azure-starter-storage-blob.svg)](https://search.maven.org/artifact/com.azure.spring/spring-cloud-azure-starter-storage-blob)

> **Deprecated starters** (no Spring Cloud Azure 4.x equivalent):
> - `azure-mediaservices-spring-boot-starter` — Azure Media Services v2 SDK reached end-of-life.
> - `spring-data-gremlin-boot-starter` — use `spring-cloud-azure-starter-data-cosmos` with the Gremlin API instead.
> - `azure-sqlserver-spring-boot-starter` — use standard Spring Data JPA with the Microsoft JDBC driver.
> - `azure-spring-boot-metrics-starter` — use the [Azure Monitor OpenTelemetry Distro](https://learn.microsoft.com/azure/azure-monitor/app/opentelemetry-enable?tabs=java) instead.

### Snapshots  
[![Nexus OSS](https://img.shields.io/nexus/snapshots/https/oss.sonatype.org/com.microsoft.azure/azure-spring-boot.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/microsoft/azure/azure-spring-boot/)

Snapshots built from `master` branch are available, add [maven repositories](https://maven.apache.org/settings.html#Repositories) configuration to your pom file as below. 
```xml
<repositories>
  <repository>
    <id>nexus-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>
```

### How to Build and Contribute
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](./HowToContribute.md) to build from source or contribute.

### Other articles
You could check below articles to learn more on usage of specific starters.

[How to use the Spring Boot Starter with Azure Cosmos DB SQL API](https://learn.microsoft.com/azure/cosmos-db/nosql/quickstart-java-spring-data)

### Filing Issues

If you encounter any bug, please file an issue [here](https://github.com/Microsoft/azure-spring-boot/issues/new).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg)](https://gitter.im/Microsoft/spring-on-azure)

### Pull Requests

Pull requests are welcome. To open your own pull request, click [here](https://github.com/Microsoft/azure-spring-boot/compare). When creating a pull request, make sure you are pointing to the fork and branch that your changes were made in.

### Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Data/Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy](https://privacy.microsoft.com/en-us/privacystatement) statement to learn more.
