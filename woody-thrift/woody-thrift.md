# com.rbkmoney.woody.thrift

Реализация woody-api для Thrift, которая по возможности абстрагирует разработчика от деталей работы с Thrift библиотекой. 

### Подключение к библиотеки к проекту

В `pom.xml` добавить зависимость:

```xml
<dependency>
    <groupId>com.rbkmoney.woody</groupId>
    <artifactId>woody-thrift</artifactId>
    <version>[1.0.0,)</version>
</dependency>
```

Для генерации кода по Thrift IDL добавляем плагин:

```java
<plugin>
    <groupId>org.apache.thrift</groupId>
    <artifactId>thrift-maven-plugin</artifactId>
    <version>[0.9.3-1,)</version>
    <configuration>
        <thriftExecutable>/usr/local/bin/thrift</thriftExecutable>
    </configuration>
    <executions>
        <execution>
            <id>thrift-sources</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
        <execution>
            <id>thrift-test-sources</id>
            <phase>generate-test-sources</phase>
            <goals>
                <goal>testCompile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
``` 

### Создание клиента

```java
THClientBuilder clientBuilder = new THClientBuilder()
.withAddress(new URI(serviceURLString))
//Optional, you can set your own configured client if you want, if not HttpClient with default configuration will be used.
.withHttpClient(HttpClientBuilder.create().build())
// Optional, you can set your own id generation logic, TimeStampIdGenerator is used by default.
.withIdGenerator(idGenerator)
//Optional, you can omit setting eventListener and all generated events 'll be just skipped.
.withEventListener(eventListener); 
ThriftServiceSrv.Iface client = clientBuilder.build(ThriftServiceSrv.Iface.class);
```

Полученный объект `client` реализует интерфейс Thritf сервиса и готов к использованию.  
__Текущая версия клиента не thread-safe!__

### Создание сервиса

```java
THServiceBuilder serviceBuilder = new THServiceBuilder();
serviceBuilder.withEventListener(eventListener);
Servlet service = serviceBuilder.build(ThriftServiceSrv.Iface.class, handler);
```

Полученный объект `service` - это ничто иное, как javax.servlet.Servlet, готовый обрабатывать запросы к Thrift-сервису, `handler` - реализация `ThriftServiceSrv.Iface`, которая вызвается 


