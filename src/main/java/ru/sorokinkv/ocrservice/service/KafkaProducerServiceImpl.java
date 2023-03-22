package ru.sorokinkv.ocrservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import ru.sorokinkv.ocrservice.aop.annotations.LogExecutionTime;
import ru.sorokinkv.ocrservice.model.dto.ObjectDto;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * KafkaProducerServiceImpl class.
 */
@Service
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    @Autowired
//    @Qualifier("KafkaTemplateInfo")
    private final KafkaTemplate<Long, List<ObjectDto>> kafkaTemplate;


    @Autowired
    public KafkaProducerServiceImpl(KafkaTemplate<Long, List<ObjectDto>> kafkaTemplate) {
        log.info("Initialising KafkaProducerServiceImpl");
        this.kafkaTemplate = kafkaTemplate;
        log.info("Producer DefaultTopic= "+kafkaTemplate.getDefaultTopic());
        log.info("Initialising OK\n");
    }

    @LogExecutionTime
    @Override
    public void send(List<ObjectDto> dto) {
        log.info("[KafkaProducerServiceImpl][send] topic=" + kafkaTemplate.getDefaultTopic() +
                " \n data: " + dto );
        ListenableFuture<SendResult<Long, List<ObjectDto>>> send = kafkaTemplate.send(kafkaTemplate.getDefaultTopic(), dto);
        try {
            log.info("[KafkaProducerServiceImpl][send] response meta:");
            log.info("topic: " + send.get().getRecordMetadata().topic());
            log.info("offset: "+send.get().getRecordMetadata().offset());
            log.info("partition: "+send.get().getRecordMetadata().partition());
            log.info("timestamp: " + send.get().getRecordMetadata().timestamp());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        kafkaTemplate.flush();
    }

}

