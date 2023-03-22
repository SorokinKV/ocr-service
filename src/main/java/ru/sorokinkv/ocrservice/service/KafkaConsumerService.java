package ru.sorokinkv.ocrservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * KafkaConsumerService interface.
 */
public interface KafkaConsumerService {
    void receive(ConsumerRecord<Long, String> data) throws JsonProcessingException;

    //    @KafkaListener(topics = "${kafka.listen.topic}")
//    void receive(ConsumerRecord<Long, String> data, KafkaProducerService kafkaProducerService) throws JsonProcessingException;
}
