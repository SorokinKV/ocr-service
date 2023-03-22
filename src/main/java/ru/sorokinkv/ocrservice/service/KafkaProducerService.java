package ru.sorokinkv.ocrservice.service;

import ru.sorokinkv.ocrservice.model.dto.ObjectDto;

import java.util.List;

/**
 * KafkaProducerService interface.
 */
public interface KafkaProducerService {
    void send(List<ObjectDto> dto);
}
