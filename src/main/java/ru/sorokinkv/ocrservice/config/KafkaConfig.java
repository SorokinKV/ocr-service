package ru.sorokinkv.ocrservice.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.sorokinkv.ocrservice.model.dto.ObjectDto;

/**
 * KafkaConfig class.
 */

@Slf4j
@EnableKafka
@Configuration
public class KafkaConfig {


    @Value("${kafka.server}")
    private String kafkaServer;

    @Value("${kafka.port}")
    private String kafkaPort;

    @Value("${kafka.producer.id}")
    private String kafkaProducerId;

    // @Value("${kafka.producer.idEs}")
    // private String kafkaProducerIdEs;

    @Value("${kafka.consumer.id}")
    private String kafkaConsumerId;

    @Value("${kafka.consumer.group.id}")
    private String kafkaConsumerGroupId;

    @Value("${kafka.write.topic}")
    private String kafkaProducerTopic;

    // @Value("${kafka.es.topic}")
    // private String kafkaProducerEsTopic;

    /**
     * producerConfigs bean.
     *
     * @return
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer + ":" + kafkaPort);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        final int maxRequestSize = 2147483640;
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize);
        final int bufferMemory = 2147483640;
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        //        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 10);
        //        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 300000);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProducerId);
        log.info("ProducerConfigs Props:");
        props.entrySet().stream().forEach(s-> log.info("\tKey="+ s.getKey()+ " value=" + s.getValue()));
        return props;
    }

    /**
     * producerConfigsEs bean.
     *
     * @return
     */
    // @Bean
    // public Map<String, Object> producerConfigsEs() {
    //     Map<String, Object> props = new HashMap<>();
    //     props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer + ":" + kafkaPort);
    //     props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    //     props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    //     final int maxRequestSize = 2147483640;
    //     props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize);
    //     final int bufferMemory = 2147483640;
    //     props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
    //     final int batchSize = 1;
    //     props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
    //     final int lingerMs = 5000;
    //     props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
    //     props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProducerIdEs);
    //     return props;
    // }

    /**
     * consumerConfigs bean.
     *
     * @return
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer + ":" + kafkaPort);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer + ":" + kafkaPort);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConsumerId);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroupId);

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
//        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "1200000");
//        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
//        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "1200000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "3");
//        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        return props;
    }

    @Bean
    public ProducerFactory<Long, List<ObjectDto>> producerStringFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    // @Bean
    // public ProducerFactory<Long, EsDto> producerStringFactoryEs() {
    //     return new DefaultKafkaProducerFactory<>(producerConfigsEs());
    // }

    @Bean
    public ConsumerFactory<Long, String> consumerStringFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * kafkaTemplate KafkaTemplateInfo bean.
     *
     * @return
     */
//    @Bean("KafkaTemplateInfo")
    @Bean
    public KafkaTemplate<Long, List<ObjectDto>> kafkaTemplate() {
        KafkaTemplate<Long, List<ObjectDto>> template = new KafkaTemplate<>(producerStringFactory());
        template.setDefaultTopic(kafkaProducerTopic);
        template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }

    /**
     * kafkaTemplateEs KafkaTemplateEs bean.
     *
     * @return
     */
    // @Bean("KafkaTemplateEs")
    // public KafkaTemplate<Long, EsDto> kafkaTemplateEs() {
    //     KafkaTemplate<Long, EsDto> template = new KafkaTemplate<>(producerStringFactoryEs());
    //     template.setDefaultTopic(kafkaProducerEsTopic);
    //     template.setMessageConverter(new StringJsonMessageConverter());
    //     return template;
    // }

    /**
     * kafkaListenerContainerFactory bean.
     *
     * @return
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        DefaultKafkaConsumerFactory defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfigs());
        factory.setConsumerFactory(defaultKafkaConsumerFactory);
//        factory.getContainerProperties().setPollTimeout(1000);
        return factory;
    }

    @Override
    public String toString() {
        return "KafkaConfig{" + "\n\t"+
                "kafkaServer='" + kafkaServer + '\'' + ",\n\t"+
                "kafkaPort='" + kafkaPort + '\'' + ",\n\t"+
                "kafkaProducerId='" + kafkaProducerId + '\'' + ",\n\t"+
                "kafkaConsumerId='" + kafkaConsumerId + '\'' + ",\n\t"+
                "kafkaConsumerGroupId='" + kafkaConsumerGroupId + '\'' + ",\n\t"+
                "kafkaProducerTopic='" + kafkaProducerTopic + '\'' + "\n"+
                '}';
    }
}


