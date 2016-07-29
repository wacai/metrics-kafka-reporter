## Install

```
> mvn clean install
```

```xml

<dependency>
    <groupId>com.wacai</groupId>
    <artifactId>metrics-kafka-reporter</artifactId>
    <version>${version}</version>
</dependency>

```

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

###
> mvn clean install
```Metrics Counter Help

<http://metrics.dropwizard.io/3.1.0/manual/core/#counters>

## Message JSON Example

```json
{
  "appname" : "app",
  "instance" : "10.0.0.1-1449632521805",
  "timestampMillis" : 1449632523805,
  "gauges" : {
    "cpu" : {
      "value" : 0.9
    }
  },
  "counters" : {
    "cnt" : {
      "count" : 100
    }
  }
}
```

> Gauge 和 Counter 已基本满足 Dashboard 对基本数据的需求

> 如需使用更多的 Metric, 请在[MessageTest.java](./src/test/java/com/wacai/metrics/MessageTest.java)中获取更多 JSON 示例.
 
## Change Log

### 0.0.2

* Append start time to instance.
 
### 0.0.1
 
* Send metrics message to kafka. 
