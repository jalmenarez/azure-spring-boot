# Testing Guidelines

This document provides guidelines for writing and running tests in the Azure Spring Boot project.

## Overview

Unit tests are essential for maintaining code quality and preventing regressions. This project follows industry best practices for test organization and execution.

## Test Framework and Tools

- **JUnit 4**: Unit testing framework
- **Mockito**: Mocking framework for isolating components under test
- **Maven Surefire Plugin**: Test execution and reporting

## Test Organization

### Directory Structure

Tests should be organized to mirror the source code structure:

```
src/
├── main/
│   └── java/
│       └── com/microsoft/azure/telemetry/
│           ├── TelemetrySender.java
│           ├── TelemetryEventData.java
│           └── ...
└── test/
    └── java/
        └── com/microsoft/azure/telemetry/
            ├── TelemetrySenderTest.java
            ├── TelemetryEventDataTest.java
            └── ...
```

### Naming Conventions

- Test classes: `*Test.java` (e.g., `TelemetrySenderTest.java`)
- Test methods: `test<FeatureName>` (e.g., `testSendWithValidEventData()`)
- Package name: Same as the class being tested

## Writing Unit Tests

### Test Structure (Arrange-Act-Assert)

Each test should follow the AAA pattern:

```java
@Test
public void testSendWithValidEventData() {
    // Arrange: Set up test data and mocks
    Map<String, String> properties = new HashMap<>();
    properties.put("key", "value");
    
    // Act: Execute the method under test
    telemetrySender.send("TestEvent", properties);
    
    // Assert: Verify the results
    assertTrue(properties.containsKey("installationId"));
}
```

### Best Practices

1. **One assertion focus**: Each test should verify one specific behavior
2. **Clear test names**: Names should describe what is being tested and the expected result
3. **Isolation**: Tests should not depend on each other or external systems
4. **Mocking**: Use Mockito to mock external dependencies
5. **No randomness**: Tests should be deterministic and repeatable

### Example: Testing Error Handling

```java
@Test(expected = IllegalArgumentException.class)
public void testSendWithNullProperties() {
    telemetrySender.send("TestEvent", null);
}

@Test
public void testSendRetryOnFailure() {
    // Mock to return failure
    when(mockRestTemplate.exchange(...))
        .thenReturn(new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR));
    
    telemetrySender.send("TestEvent", properties);
    
    // Verify retry was attempted (3 times)
    verify(mockRestTemplate, times(3)).exchange(...);
}
```

### Test Coverage Requirements

- Minimum 65% code coverage for new code
- Focus on:
  - Happy path scenarios
  - Error conditions and exception handling
  - Edge cases (null, empty, boundary values)
  - Integration points

## Running Tests

### Basic Commands

```bash
# Run all tests
mvn clean install

# Run specific test class
mvn test -Dtest=TelemetrySenderTest

# Run tests matching pattern
mvn test -Dtest=Telemetry*

# Skip tests
mvn clean install -DskipTests

# Run with code coverage report
mvn clean install jacoco:report
```

### IDE Integration

Most IDEs (IntelliJ IDEA, Eclipse, VS Code) provide built-in test runners:

- Right-click on test class → Run
- Right-click on test method → Run
- Use IDE shortcuts (Ctrl+Shift+F10 in IntelliJ)

## Mocking Best Practices

### Using Mockito

```java
@RunWith(MockitoJUnitRunner.class)
public class MyTest {
    
    @Mock
    private ExternalService mockService;
    
    @InjectMocks
    private MyClass myClass;
    
    @Test
    public void testWithMock() {
        // Setup mock behavior
        when(mockService.call()).thenReturn("result");
        
        // Test code
        String result = myClass.doSomething();
        
        // Verify mock was called
        verify(mockService).call();
    }
}
```

### When to Mock

- External service calls (HTTP, Database)
- Time-dependent operations
- Random number generation
- File I/O operations
- Any dependency you want to isolate

## Continuous Integration

All tests run automatically on:
- Pull requests (before merge)
- Commits to master branch
- Daily builds

Tests must pass before code can be merged into master.

## Common Issues and Solutions

### Issue: Tests fail locally but pass in CI

**Solution**: 
- Ensure JDK version matches (1.8+)
- Clear Maven cache: `mvn clean`
- Check for hardcoded paths or environment-specific code

### Issue: Flaky tests (intermittent failures)

**Solution**:
- Remove time-dependent operations
- Mock external services
- Avoid Thread.sleep() in tests
- Use proper synchronization for concurrent tests

### Issue: Slow tests

**Solution**:
- Mock external service calls
- Use small test data sets
- Avoid I/O operations
- Split integration tests from unit tests

## Resources

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)

## Questions?

For questions or suggestions about testing, please:
- Check the existing test examples in the codebase
- Open an issue on GitHub
- Contact the maintainers

Thank you for contributing quality tests!
