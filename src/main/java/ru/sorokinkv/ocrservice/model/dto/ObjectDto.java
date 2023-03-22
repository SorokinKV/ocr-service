package ru.sorokinkv.ocrservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * ObjectDto class.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectDto {
    String id;
    String name;
    String createdAt;
    String updatedAt;
    String deletedAt;
    @JsonProperty("parentID")
    String parentId;
    String nodePath;
    String nodePathNames;
    String storagePath;
    String storageFileName;
    String storagePathConverted;
    String storagePathOcr;
    String storageBucketName;
    String extension;
    String type;
    String groupId;
    String userId;
    List<String> nodePathArray;
    Long size;
    @JsonProperty("isDel")
    boolean isDel;

    @JsonProperty("isBin")
    boolean isBin;

    @JsonProperty("isConverted")
    boolean isConverted;

    @JsonProperty("isRecognized")
    boolean isRecognized;
    String registered;

    @JsonProperty("isSearchable")
    boolean isSearchable;

    public void setConverted(boolean converted) {
        isConverted = converted;
    }

    public void setRecognized(boolean recognized) {
        isRecognized = recognized;
    }

    public void setDel(boolean del) {
        isDel = del;
    }

    public void setBin(boolean bin) {
        isBin = bin;
    }

    public boolean isSearchable() {
        return isSearchable;
    }

    public void setSearchable(boolean searchable) {
        isSearchable = searchable;
    }

    @Override
    public String toString() {
        return "\n\t"+"ObjectDto{" + "\n\t"+
                "id='" + id + '\'' +"\n\t"+
                ", name='" + name + '\'' +"\n\t"+
                ", createdAt='" + createdAt + '\'' +"\n\t"+
                ", updatedAt='" + updatedAt + '\'' +"\n\t"+
                ", deletedAt='" + deletedAt + '\'' +"\n\t"+
                ", parentId='" + parentId + '\'' +"\n\t"+
                ", nodePath='" + nodePath + '\'' +"\n\t"+
                ", nodePathNames='" + nodePathNames + '\'' +"\n\t"+
                ", storagePath='" + storagePath + '\'' +"\n\t"+
                ", storageFileName='" + storageFileName + '\'' +"\n\t"+
                ", storagePathConverted='" + storagePathConverted + '\'' +"\n\t"+
                ", storagePathOcr='" + storagePathOcr + '\'' +"\n\t"+
                ", storageBucketName='" + storageBucketName + '\'' +"\n\t"+
                ", extension='" + extension + '\'' +"\n\t"+
                ", type='" + type + '\'' +"\n\t"+
                ", groupId='" + groupId + '\'' +"\n\t"+
                ", userGuid='" + userId + '\'' +"\n\t"+
                ", nodePathArray=" + nodePathArray +"\n\t"+
                ", size=" + size +"\n\t"+
                ", isDel=" + isDel +"\n\t"+
                ", isBin=" + isBin +"\n\t"+
                ", isConverted=" + isConverted +"\n\t"+
                ", isRecognized=" + isRecognized +"\n\t"+
                ", registered='" + registered + '\'' +"\n\t"+
                ", isSearchable=" + isSearchable +"\n\t"+
                '}';
    }
}
