# How to Build and Contribute
Below are guidelines for building and code contribution.

## Prerequisites
- JDK 1.8 and above
- [Maven](http://maven.apache.org/) 3.0 and above

## Build from source
To build the project, run maven commands.

```bash
git clone https://github.com/Microsoft/azure-spring-boot.git 
cd azure-spring-boot
mvn clean install
```
*Cloning the git repository on Windows*

Some files in the git repository may exceed the Windows maximum file path (260 characters), depending on where you clone the repository. If you get `Filename too long` errors, set the `core.longPaths=true` git option:
```
git clone -c core.longPaths=true https://github.com/Microsoft/azure-spring-boot.git
cd azure-spring-boot
mvn clean install
```

## Test

### Running Tests

- Run all tests including unit and integration tests
```bash
mvn clean install
```

- Run specific test class
```bash
mvn test -Dtest=TelemetrySenderTest
```

- Run tests matching a pattern
```bash
mvn test -Dtest=Telemetry*
```

- Skip test execution
```bash
mvn clean install -DskipTests
```

- Skip checkstyle validation (useful during development)
```bash
mvn clean test -Dcheckstyle.skip=true
```

### Writing Unit Tests

When contributing new code or features, please include comprehensive unit tests:

- **Test Structure**: Follow the existing pattern using JUnit 4 and Mockito
- **Naming Convention**: Use `*Test.java` for test classes
- **Location**: Place tests in `src/test/java` under the same package structure as the code being tested
- **Coverage**: Aim for code coverage >= 65% for new code
- **Test Cases**: Include:
  - Happy path / success scenarios
  - Error/exception handling
  - Edge cases (null values, empty collections, boundary values)
  - Integration with dependencies (using mocks/stubs)

Example test structure:
```java
@RunWith(MockitoJUnitRunner.class)
public class MyServiceTest {
    
    @Before
    public void setUp() {
        // Initialize test fixtures
    }
    
    @Test
    public void testSuccessfulOperation() {
        // Arrange
        // Act
        // Assert
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInput() {
        // Test error handling
    }
}
```

### Continuous Integration

- Code coverage is automatically checked (minimum 65%)
- Tests must pass before code can be merged
- Both unit and integration tests run on CI

## Version management
Developing version naming convention is like `2.0.0-SNAPSHOT`. Release version naming convention is like `2.0.0`. Please don't update version if no release plan. 

## CI
Both [Travis](https://travis-ci.org/Microsoft/azure-spring-boot) and [Appveyor](https://ci.appveyor.com/project/yungez/azure-spring-boot) CI is enabled.

## Contribution
Code contribution is welcome. To contribute to existing code or add a new starter, please make sure below check list is checked.
- [ ] Build pass. Checkstyle and findbugs is enabled by default. Please check [checkstyle.xml](config/checkstyle.xml) to learn detailed checkstyle configuration.
- [ ] Documents are updated to align with code.
- [ ] New starter must have sample folder containing sample code and corresponding readme file.
- [ ] Code coverage for new code >= 65%. Code coverage check is enabled with 65% bar.

