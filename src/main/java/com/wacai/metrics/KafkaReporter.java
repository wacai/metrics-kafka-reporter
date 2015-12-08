package com.wacai.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class KafkaReporter extends ScheduledReporter {

    public static Builder forRegistry(MetricRegistry registry,
                                      String topic,
                                      String appname,
                                      String instance,
                                      Properties kafkaConfig) {
        return new Builder(registry, topic, appname, instance, kafkaConfig);
    }

    private static class Builder {

        private final MetricRegistry registry;
        private final String         appname;
        private final String         instance;
        private final Properties     properties;
        private final String         topic;

        private MetricFilter filter;
        private TimeUnit     rateUnit;
        private TimeUnit     durationUnit;
        private boolean      showSamples;
        private Callback     callback;

        public Builder(MetricRegistry registry, String topic, String appname, String instance, Properties properties) {
            this.registry = registry;
            this.topic = topic;
            this.appname = appname;
            this.properties = properties;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.showSamples = false;
            this.filter = MetricFilter.ALL;
            this.instance = instance;
            this.callback = (metadata, exception) -> { /* Ignore */ };
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder showSamples(boolean value) {
            this.showSamples = value;
            return this;
        }

        public Builder callback(Callback value) {
            this.callback = value;
            return this;
        }

        public KafkaReporter build() {
            return new KafkaReporter(registry,
                                     "kafka-reporter",
                                     filter,
                                     rateUnit,
                                     durationUnit,
                                     showSamples,
                                     topic, appname,
                                     instance,
                                     properties, callback);
        }

    }

    private final String                        appname;
    private final String                        instance;
    private final String                        topic;
    private final Callback                      callback;
    private final ObjectMapper                  mapper;
    private final KafkaProducer<String, String> producer;

    protected KafkaReporter(MetricRegistry registry,
                            String name,
                            MetricFilter filter,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            boolean showSamples,
                            String topic,
                            String appname,
                            String instance,
                            Properties properties,
                            Callback callback) {
        super(registry, name, filter, rateUnit, durationUnit);
        this.topic = topic;
        this.appname = appname;
        this.instance = instance;
        this.callback = callback;
        final MetricsModule module = new MetricsModule(rateUnit, durationUnit, showSamples, filter);
        mapper = new ObjectMapper().registerModule(module);
        producer = new KafkaProducer<>(properties, new StringSerializer(), new StringSerializer());
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final Message message = new Message(appname, instance, System.currentTimeMillis(),
                                            gauges, counters, histograms, meters, timers);
        try {
            producer.send(new ProducerRecord<>(topic, mapper.writeValueAsString(message)), callback);
        } catch (JsonProcessingException e) {
            callback.onCompletion(null, e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        producer.close();
    }

    private static class Message {
        private final String                       appname;
        private final String                       instance;
        private final long                         timestampMillis;
        private final SortedMap<String, Gauge>     gauges;
        private final SortedMap<String, Counter>   counters;
        private final SortedMap<String, Histogram> histograms;
        private final SortedMap<String, Meter>     meters;
        private final SortedMap<String, Timer>     timers;

        public Message(String appname, String instance, long timestampMillis, SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
            this.appname = appname;
            this.instance = instance;
            this.timestampMillis = timestampMillis;
            this.gauges = gauges;
            this.counters = counters;
            this.histograms = histograms;
            this.meters = meters;
            this.timers = timers;
        }

        public String getAppname() {
            return appname;
        }

        public String getInstance() {
            return instance;
        }

        public long getTimestampMillis() {
            return timestampMillis;
        }

        public SortedMap<String, Gauge> getGauges() {
            return gauges;
        }

        public SortedMap<String, Counter> getCounters() {
            return counters;
        }

        public SortedMap<String, Histogram> getHistograms() {
            return histograms;
        }

        public SortedMap<String, Meter> getMeters() {
            return meters;
        }

        public SortedMap<String, Timer> getTimers() {
            return timers;
        }
    }

}
