package ru.sorokinkv.ocrservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * FileDto class.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDto {
    String id;
    String name;
    String createdAt;
    String parentId;
    String groupId;
    String registered;
    boolean isDel;
}
