package ru.sorokinkv.ocrservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * FileGlobalInfoDto class.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileGlobalInfoDto {
    DataDto data;
}
