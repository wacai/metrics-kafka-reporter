## Install

```xml

<dependency>
    <groupId>com.wacai</groupId>
    <artifactId>metrics-kafka-reporter</artifactId>
    <version>${version}</version>
</dependency>

```

> ${version} see [tags](../../tags)

## Usage

```java
MetricRegistry registry = new MetricRegistry();
Counter invokeCounter = registry.counter(name(Xxxx.class, "invoke")); 
KafkaReporter reporter = KafkaReporter.forRegistry(registry,"topic","appname","10.0.0.1", loadKafkaConfig()).build();
report.start(1, TimeUnit.SECONDS);

...

invokeCounter.inc();
...

report.stop();
```

### Metrics Counter Help

<http://metrics.dropwizard.io/3.1.0/manual/core/#counters>