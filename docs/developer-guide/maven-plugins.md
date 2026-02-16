# Maven Plugins Configuration

Maven plugins used in the build.



## Plugins

| Plugin | Version | Purpose |
|--------|---------|---------|
| maven-compiler-plugin | 3.13.0 | Compiles Java 21 source with Lombok |
| maven-enforcer-plugin | 3.6.2 | Enforces Maven 3.9.0+ and Java 21+ |
| maven-surefire-plugin | 3.5.4 | Runs tests |
| jacoco-maven-plugin | 0.8.13 | Generates coverage reports |
| maven-shade-plugin | 3.6.1 | Creates uber-JAR |



## Test Directories

The build uses the standard Maven test directory structure:

- `src/test/java` - Test source code
- `src/test/resources` - Test resources



## Uber-JAR

The `maven-shade-plugin` creates a single JAR (`kete.jar`) containing all dependencies. The `ServicesResourceTransformer` merges `META-INF/services` files from dependencies.



## Coverage Reports

After running tests, coverage reports are generated at `target/site/jacoco/index.html`.



## Build Lifecycle

```mermaid
flowchart TD
    subgraph V["1. validate"]
        V1["maven-enforcer-plugin"]
    end

    subgraph C["2. compile"]
        C1["maven-compiler-plugin"]
    end

    subgraph T["3. test"]
        T1["jacoco:prepare-agent"]
        T2["maven-surefire-plugin"]
        T3["jacoco:report"]
        T1 --> T2 --> T3
    end

    subgraph P["4. package"]
        P1["maven-shade-plugin"]
    end

    V --> C --> T --> P
```
