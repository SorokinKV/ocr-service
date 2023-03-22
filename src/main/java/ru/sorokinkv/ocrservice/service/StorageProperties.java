package ru.sorokinkv.ocrservice.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    @Value("${dir.upload}")
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
