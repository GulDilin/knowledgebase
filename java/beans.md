# Beans

## CDI specs

- [CDI 1.2](https://docs.jboss.org/cdi/spec/1.2/cdi-spec.html)
- [CDI 2.0](https://docs.jboss.org/cdi/spec/2.0/cdi-spec.html)

## Which Java classes are managed beans?

[Source](https://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#what_classes_are_beans)

A top-level Java class is a managed bean if it is defined to be a managed bean by any other Java EE specification, or if it meets all of the following conditions:

- It is not a non-static inner class.
- It is a concrete class, or is annotated `@Decorator`.
- It is not annotated with an EJB component-defining annotation or declared as an EJB bean class in `ejb-jar.xml`.
- It does not implement `javax.enterprise.inject.spi.Extension`.
- It is not annotated `@Vetoed` or in a package annotated `@Vetoed`.
- It has an appropriate constructor - either:
    * the class has a constructor with no parameters, or
    * the class declares a constructor annotated `@Inject`.

All Java classes that meet these conditions are managed beans
and thus no special declaration is required to define a managed bean.

If packages annotated `@Vetoed` are split across classpath entries, non-portable behavior results.
An application can prevent packages being split across jars
by sealing the package as defined by the Extension Mechanism Architecture.


## Bean defining annotations

https://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#bean_defining_annotations

## Discovery mode

In CDI 1.0 there was only one "discovery mode". How does it work? Simply put: find all bean archives (containing `beans.xml`),
discover and process all the found classes (identify beans, etc.).
Needless to say, this might be a performance problem for large applications with thousands of classes.
In CDI 1.1+ we call this mode `all` and a bean archive with this mode is called **EXPLICIT**.
Since CDI 1.1+ a new discovery mode - `annotated` - can be used.
The difference is that if this mode is used only classes with a bean defining annotation are considered.
In other words, a component must be explicitly designated.
A bean archive with this mode is called **IMPLICIT**.
To make things a little bit more complicated, an implicit bean archive does not have to contain a `beans.xml` file at all.
One class with a bean defining annotation or a session bean is enough.


Implicit bean archive has pros and cons:

Pros:

+ saves a lot of memory if an archive contains a lot of classes which should NOT become beans
(the container does not have to store the metadata)
+ speeds up the bootstrap (the container does not have to process all the types, fire events like `ProcessBean`, etc.)

Cons:
- does not fire `ProcessAnnotatedType` for all types from the bean archive;
this breaks some extensions (e.g. - `MessageBundleExtension` from `DeltaSpike`)
- does not pick up `@javax.inject.Singleton` beans (it’s not a bean defining annotation)

**CONCLUSION**: If possible, use the annotated discovery mode.

> **_NOTE:_**  Most Weld-based runtimes allow to suppress implicit bean archives without beans.xml,
i.e. to require the `beans.xml` file in bean archives so that it’s not necessary
to scan all the parts of the application. See also FAQ.

> **_NOTE:_** If it is not possible to use annotated mode,
you can try a Weld-specific feature to conserve memory used:
Veto types without bean defining annotation.
A similar solution is going to be standardized in CDI 2.0 (see also CDI-420).

## Weld Tip 3 - Boost performance of Weld apps

https://weld.cdi-spec.org/news/2016/10/25/tip3-performance/

### Lazy initialization

Weld initializes bean instances of normal scoped beans lazily.
In other words, when injecting a normal scoped bean
(`@RequestScoped`, `@ApplicationScoped`, etc.) a new instance is not created until actually used.
Instead, a shared client proxy is injected.
This proxy invokes a method upon the correct bean instance (created if necessary).

> **_NOTE:_** Having many injection points resolving to normal scoped beans
does not necessarily mean additional overhead associated with bean instance creation.

In the following example, an `OrderProcessor` instance is not created until its `OrderProcess.process()` method is called:

```java
@ApplicationScoped
class OrderProcessor {
  @PostConstruct
  void init() {
    // Do some expensive initialization logic
  }
  void process() {
    // Business logic
  }
}

@RequestScoped
class OrderService {
  @Inject
  OrderProcessor processor; // A shared client proxy is injected

  void create(Order order) {
    if (order.isValid()) {
        // Processor is not initialized unless we have a valid order
        processor.process(order);
    }
  }
}
```

> **_NOTE:_**  Weld’s session context is also initilized lazily and doesn’t require an HTTP session
to actually exist until a bean instance must be written
(i.e. until a method of a `@SessionScoped` bean is invoked).

## Eager CDI beans (Startup initialization)


> **PROBLEM:_** default beans (`@SessionScoped`,`@RequestScoped`, `@ApplicationScoped`, etc.) are lazy initialized

There is no option you could select for eager initialization,
you have to choose some "initialization".
CDI does not define whether bean init should be lazy or eager
and since lazy makes more sense most of the time, Weld went that way.

> **_NOTE:_** Eager means that bean creates at start.


### JSF solution (deprecated)


```java
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.annotation.PostConstruct;

@ApplicationScoped
@ManagedBean(eager=true)
public class StartupBean{

    @PostConstruct
    public void init(){
        //Do all needed application initialization.
    }
    ...
```

`@ManagedBean` annotation was deprecated with JSF 2.2.
It is highly recommended to use CDI (context dependency injection) beans in JEE environment.


### Java EE EJB solution


```java
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

// OR javax (same thing, just old version)
// import javax.ejb.Singleton;
// import javax.ejb.Startup;

@Singleton
@Startup
public class StartupBean{

    @PostConstruct
    public void init(){
        //Do all needed application initialization.
    }
    ...
```

### Portable CDI Extensions

Source http://ovaraksin.blogspot.com/2013/02/eager-cdi-beans.html

Extensions https://docs.jboss.org/weld/reference/latest/en-US/html/extend.html


#### com/example/Eager.java
```java
package com.example

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Eager { }
```

#### com/example/EagerExtension.java
```java
package com.example

import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;

public class EagerExtension implements Extension {
    private List<Bean<?>> eagerBeansList = new ArrayList<Bean<?>>();

    public <T> void collect(@Observes ProcessBean<T> event) {
        if (event.getAnnotated().isAnnotationPresent(Eager.class)
            && event.getAnnotated().isAnnotationPresent(ApplicationScoped.class)) {
            eagerBeansList.add(event.getBean());
        }
    }

    public void load(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        for (Bean<?> bean : eagerBeansList) {
            // note: toString() is important to instantiate the bean
            beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)).toString();
        }
    }
}
```

#### Register extensions in a file

```
META-INF/services/javax.enterprise.inject.spi.Extension
```

The file has only one line with a fully qualified path to the EagerExtension class.

#### Example `META-INF/services/javax.enterprise.inject.spi.Extension` content

```
com.example.EagerExtension
```


#### com/example/StartupBean.java
```java
package com.example

@ApplicationScoped
@Eager
public class StartupBean {

    @PostConstruct
    public void init(){
        //Do all needed application initialization.
    }
    ...
```
```

