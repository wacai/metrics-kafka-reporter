package com.wacai.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wacai.metrics.KafkaReporter.MetricMessage;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageTest {

    @Test
    public void demo_message_json() throws Exception {
        final Gauge<Double> gauge = mock(RatioGauge.class);
        when(gauge.getValue()).thenReturn(0.9);

        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        final MetricMessage message = new MetricMessage("app", "10.0.0.1", System.currentTimeMillis(),
                                                        singleSortedMap("cpu", gauge),
                                                        singleSortedMap("cnt", counter),
                                                        null,
                                                        null,
                                                        null);

        final ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .registerModule(new MetricsModule(SECONDS, MILLISECONDS, false, MetricFilter.ALL));

        System.out.println(mapper.writeValueAsString(message));
    }

    private <K, V> SortedMap<K, V> singleSortedMap(K key, V value) {
        SortedMap<K, V> map = new TreeMap<>();
        map.put(key, value);
        return map;
    }
}