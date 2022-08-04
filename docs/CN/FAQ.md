## 为什么调用同一个类的另一个方法，```@Cached```注解没有生效？
JetCache的注解功能是使用Spring AOP来实现的，而Spring基于Proxy来实现AOP。
从Spring Context中获得的bean，以及通过```@Autowired```注解都是代理增强过的，所以可以织入缓存相关的逻辑，同一个类中通过this调用另一个方法，不经过代理，所以JetCache的缓存逻辑以及Spring的其它AOP切面都不会生效。

JetCache暂未支持AspectJ。

一个替代方法是，在bean中通过```@Autowired```注入它自己，然后在用注入的实例代替this来调用。

## @Cached的key、condition等表达式中使用参数名以后缓存没有生效
javac编译器需要指定-parameters参数以后才会把参数名信息写入到字节码中，然后才能被反射机制读取，默认情况下这个参数是没有指定的。

pom中的指定方式：
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
如果你在IDE中运行程序，光在pom中指定还不够。

在IntelliJ IDEA中设置：
![Set javac -parameters in IntelliJ IDEA](../images/faq_1.png)

在Eclipse中设置：
![Set javac -parameters in Eclipse](../images/faq_2.png)

## 如何定制自己的序列化器
在Cached和CreateCache上的serialPolicy可以指向一个Spring Bean。
做一个类实现SerialPolicy接口，在Spring Context中创建该类的实例（假设名字为myBean），在Cached和CreateCache注解上设置serialPolicy="bean:myBean"即可。

更进一步，如果想把自定义的序列化器设置为默认的，可以声明一个SpringConfigProvider，然后覆盖其parseValueEncoder和parseValueDecoder方法：
```java
@Bean
public SpringConfigProvider springConfigProvider() {
    return new SpringConfigProvider() {
        @Override
        public Function<Object, byte[]> parseValueEncoder(String valueEncoder) {
              if(valueEncoder.equals("xxx")){
                   return MyEncoder();
              }else{
                   return super.parseValueEncoder(valueEncoder);
              }
        };
        @Override
        public Function<byte[], Object> parseValueDecoder(String valueDecoder) {
               .........
        }
}
```

## 我想要JSON序列化器
jetcache老版本中是有三个序列化器的：java、kryo、fastjson。
但是fastjson做序列化兼容性不是特别好，并且某次升级以后单元测试就无法通过了，怕大家用了以后觉得有坑，就把它废弃了。
现在默认的序列化器是性能最差，但是兼容性最好，大家也最熟悉的java序列化器。

需要JSON序列化器的，自己动手吧，可以参考这里，还是挺简单的：

https://github.com/alibaba/jetcache/blob/master/samples/simple-samples/src/main/java/FastjsonValueEncoder.java

https://github.com/alibaba/jetcache/blob/master/samples/simple-samples/src/main/java/FastjsonValueDecoder.java

useIdentityNumber属性用于修改serialPolicy以后的值兼容（比如serialPolicy改成新的了，redis里面还是旧的）。
