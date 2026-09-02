## Why doesn't the @Cached annotation work when calling another method in the same class?

JetCache's annotation feature is implemented using Spring AOP, and Spring implements AOP based on proxies.

Beans obtained from the Spring Context, as well as those injected via `@Autowired`, are proxy-enhanced, so cache-related logic can be woven in. However, calling another method within the same class via `this` bypasses the proxy, so neither JetCache's caching logic nor any other Spring AOP aspects will take effect.

JetCache does not currently support AspectJ.

A workaround is to inject the bean into itself via `@Autowired`, and then use the injected instance instead of `this` to make the call.

## Cache not working after using parameter names in @Cached key/condition expressions

The javac compiler requires the `-parameters` flag to write parameter name information into the bytecode, which can then be read via reflection. By default, this flag is not specified.

To specify it in your pom:

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.7.0</version>
        <configuration>
            <source>1.8</source>
            <target>1.8</target>
            <compilerArgument>-parameters</compilerArgument>
        </configuration>
    </plugin>
</plugins>
```
If you are running your application in an IDE, specifying it in the pom alone is not enough.

Setting it in IntelliJ IDEA:
![Set javac -parameters in IntelliJ IDEA](../images/faq_1.png)

Setting it in Eclipse:
![Set javac -parameters in Eclipse](../images/faq_2.png)

## How to customize your own serializer

The `serialPolicy` attribute on `@Cached` and `@CreateCache` can reference a Spring Bean.

Create a class that implements the `SerialPolicy` interface, register it as a bean in the Spring Context (e.g., with the name `myBean`), and then set `serialPolicy = "bean:myBean"` on the `@Cached` or `@CreateCache` annotation.

To go a step further and make your custom serializer the default, implement an `EncoderParser` (you can extend `DefaultSpringEncoderParser` and modify it), then register it as a bean in the Spring Context.

## Deserialization errors after upgrading to 2.8 (DecodeFilterException / InvalidClassException)

JetCache 2.8.x enables a deserialization security filter by default, which only allows classes under `java.lang`, `java.util`, `java.time`, `java.math`, `java.net`, `com.alicp.jetcache`, and similar packages to be deserialized. If your cached values contain custom classes, you need to configure an allow list:

```yaml
jetcache:
  decodeFilterAllowPatterns:
    - com.yourcompany.  # Prefix match: allows all classes under this package and its sub-packages
```

Or configure it programmatically:
```java
DecodeFilter.getDefault().addAllowPatterns("com.yourcompany.");
```

If you don't want to deal with this right away, you can disable the filter by setting `jetcache.decodeFilterEnabled: false` (**not recommended in production environments**). See the "Deserialization Filter Configuration" section in the [Configuration document](Config.md) for details.

## I want a JSON serializer

Earlier versions of JetCache included two serializers: `java` and `kryo`. JetCache 2.7 added `kryo5`, `fastjson2`, and `jackson`.

The current default serializer is the Java serializer — the least performant, but the most compatible and familiar to most developers.

JSON is not a dedicated Java serialization library; it is primarily used for data exchange with front-end applications. Deserializing untrusted JSON strings from the front end without proper safeguards can cause extremely serious security issues. To mitigate these risks, all major JSON libraries have become very conservative — whenever the type cannot be identified through reflection (e.g., a field declared as `Object` that is actually `XxxBean`), the value is deserialized as a `JSONObject`, which makes deserialization compatibility quite poor.

For this reason, the `fastjson2` and `jackson` JSON serializers are not registered by default. If you want to use them, you need to handle the configuration and any necessary modifications yourself.

If you are unsure how to register them, it is recommended not to use a JSON serializer, as you will likely have trouble debugging JSON deserialization issues as well.
