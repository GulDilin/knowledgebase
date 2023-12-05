## Java SE App with CDI

In CDI 2.0 there is a feature to implement CDI with Java SE application.

[CDI Specs](https://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#part_2)

[Full source code example](./examples/java-se-app-with-cdi)

## Introducing

In Java SE, the CDI container must be explicitly bootstrapped by the user.
This is performed by the `SeContainerInitializer` abstract class and its static method \`newInstance()\`\`.

`SeContainerInitializer` is a `ServiceProvider` of the service `javax.enterprise.inject.se.SeContainerInitializer`
declared in `META-INF/services`. This class allows a user to configure the CDI container before it is bootstrapped.
The `SeContainerInitializer.initialize()` method bootstraps the container and returns a SeContainer instance.

## Example

Implementation CDI app with Java SE (For examle standalone server without application server).

- Weld https://weld.cdi-spec.org/
- Jakarta EE https://jakarta.ee/

To see full example just explore code in same directory.

### Part of pom.xml

```xml
<dependencies>
    <!-- https://mvnrepository.com/artifact/org.jboss.weld.se/weld-se-core -->
    <!-- CDI 2.0 with SE context loader (for executable jar packaging) -->
    <!-- implementation by Weld-->
    <dependency>
        <groupId>org.jboss.weld.se</groupId>
        <artifactId>weld-se-core</artifactId>
        <version>5.1.2.Final</version>
    </dependency>
    <!-- /CDI 2.0 -->

    <!-- Other dependencies -->
</dependencies>

<build>
    <plugins>
        <!-- Static Indexer for beans -->
        <plugin>
            <groupId>org.jboss.jandex</groupId>
            <artifactId>jandex-maven-plugin</artifactId>
            <version>1.2.3</version>
        </plugin>
    </plugins>
</build>
```

### com/example/ExampleService.java

```java
/**
 * This class describes an example repository. It is Bean.
 */
@ApplicationScoped
public class ExampleRepository {
    /**
     * Calculates the some value.
     *
     * @return     Some value.
     */
    public Integer computeSomeValue() {
        return 2;
    }
}
```

### com/example/ExampleService.java

```java
/**
 * This class describes an example service. It is Bean.
 */
@ApplicationScoped
public class ExampleService {

    /**
     * Example repository that will be injected.
     */
    @Inject
    private ExampleRepository repository;

    /**
     * Calculates the another value.
     *
     * @return     Another value.
     */
    public Integer computeAnotherValue() {
        this.repository.computeSomeValue() + 2;
    }
}
```

### com/example/Main.java

```java
public final class Main {
    /**
     * Default empty constructor.
     */
    private Main() {
        // empty constructor
    }

    /**
     * Main method for standalone app.
     *
     * @param argv CLI args
     */
    public static void main(final String[] argv) {
        Weld initializer = new Weld();
        // Initialize CDI container
        WeldContainer container = initializer.initialize();
        // Select bean from container. All injected dependencies will be initialized automatically.
        ExampleService service = container.select(ExampleService.class).get();
        // Invoke method from selected service
        System.out.println(service.computeAnotherValue()); // Prints 4
    }
}
```

### resources/META-INF/beans.xml

```xml
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_2_0.xsd"
       bean-discovery-mode="annotated" version="2.0">
</beans>

```
