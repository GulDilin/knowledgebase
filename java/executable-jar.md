# How to Build an executable jar

## Use maven-assembly-plugin

This plugin with `jar-with-dependencies` descriptor pack all classes (including dependencies in jar root).

Change `com.example.Main` to your main class

> **_NOTE_** If you choose non-jar packaging or build `war` + `jar`
> dont change execution phase to `package`. It won't include your project classes for unknown reasons

Part of `pom.xml`

```xml
<project ...>
    <!-- ... -->

    <build>
        <plugins>
            <!-- Package to JAR-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <configuration>
                    <appendAssemblyId>false</appendAssemblyId>
                    <archive>
                        <manifest>
                            <mainClass>com.example.Main</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <!-- Do not change to package, it won't include project classes for unknown reason-->
                        <phase>compile</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <!-- /Package to JAR -->

            <!-- ... -->
        </plugins>
    </build>

    <!-- ... -->
</project>
```

> **_NOTE_** If you are using CDI for java SE app it can be possible problems with Ambiguous dependencies
> caused by multiple injection candidates from dependencies classes

## Solution for Ambiguous beans from dependencies classes

Usually you dont want to index bean provides from dependencies and want to index only your application beans.
To solve problem with Ambiguous beans from dependencies you can configure jandex.

Jandex https://smallrye.io/jandex/jandex/3.1.6/maven/shading.html

The Jandex Maven plugin has an additional goal jandex-jar that can be used to create an index inside an existing JAR. This goal is not bound to any phase by default, so you have to configure that manually.

It is useful together with shading,
where the Maven Shade plugin creates a JAR from multiple previously existing JARs.
A shaded JAR may already contain a Jandex index,
if at least one of the constituent JARs contains one,
but that index is most likely not what you want. First,
it is an unmodified index originating in one of the constituent JARs

Part of `pom.xml`

```xml
<project ...>
    <!-- ... -->

    <build>
        <plugins>
            <plugin>
                <!-- https://github.com/wildfly/jandex-maven-plugin -->
                <groupId>io.smallrye</groupId>
                <artifactId>jandex-maven-plugin</artifactId>
                <executions>
                    <!--
                        This configuration will index all .class files in your target/classes directory,
                        and write the index to target/classes/META-INF/jandex.idx.
                    -->
                    <execution>
                        <id>make-index</id>
                        <goals>
                            <!-- phase is 'process-classes by default' -->
                            <goal>jandex</goal>
                        </goals>
                    </execution>

                    <!-- This execution will index classes from project to result uber jar  -->
                    <execution>
                        <id>uberjar-index</id>
                        <phase>package</phase>
                        <goals>
                            <goal>jandex-jar</goal>
                        </goals>
                        <configuration>
                            <jar>${project.build.directory}/${project.build.finalName}.jar</jar>
                            <includes>
                                <include>com/example/**/*.class</include>
                            </includes>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- ... -->
        </plugins>
    </build>

    <!-- ... -->
</project>
```
