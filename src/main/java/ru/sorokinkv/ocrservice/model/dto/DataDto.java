package ru.sorokinkv.ocrservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DataDto class.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataDto {
    ObjectDto object;
}
