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

> **_NOTE:_** If it’s not possible to use annotated mode,
you can try a Weld-specific feature to conserve memory used:
Veto types without bean defining annotation.
A similar solution is going to be standardized in CDI 2.0 (see also CDI-420).
