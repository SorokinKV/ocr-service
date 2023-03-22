package ru.sorokinkv.ocrservice.service;

import java.io.File;

// import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;

public interface FileTransferService {
//    FileGlobalInfoDto gerFileInfo(FileDto f);

    boolean uploadFileMinio(String object, String filename);

    String downloadFileMinio(String object, String filename);

     File upload(MultipartFile file);
    // File download();
}