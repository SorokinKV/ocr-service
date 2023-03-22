package ru.sorokinkv.ocrservice.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.sorokinkv.ocrservice.aop.annotations.LogExecutionTime;
import ru.sorokinkv.ocrservice.exception.StorageException;
import ru.sorokinkv.ocrservice.model.dto.FileGlobalInfoDto;
import ru.sorokinkv.ocrservice.model.dto.ObjectDto;
import ru.sorokinkv.ocrservice.utils.Utils;

@Slf4j
@Service
public class FileTransferServiceImpl implements FileTransferService {
    
    @Value("${minio.bucket.download}")
    String bucket;
    @Value("${minio.bucket.ocr}")
    String bucketOcr;
    @Value("${dir.upload}")
    private String sourceFolder;
    @Value("${dir.ocr}")
    private String completeFolder;
    @Value("${storage.url}")
    String storageUrl;

    private MinioClient minioClient;
    private final Path rootLocation;

    AsyncHttpClient client = Dsl.asyncHttpClient();

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public FileTransferServiceImpl(StorageProperties properties, MinioClient minioClient) throws Exception {
        Utils.createFolder(properties.getLocation());
        this.rootLocation = Paths.get(properties.getLocation());
        this.minioClient = minioClient;
    }

    public FileGlobalInfoDto gerFileInfo(ObjectDto f) {
        Request getRequest = Dsl.get(storageUrl + f.getId()).setRequestTimeout(50000).build();
        Future<Response> responseFuture = client.executeRequest(getRequest);
        try {
            log.info("Request to mdm-storage ... " + getRequest.getUrl());
            Response response = responseFuture.get();
            log.info("Response from mdm-storage:");
            log.info("Status: " + response.getStatusText());
            log.info("Body: " + response.getResponseBody());
            FileGlobalInfoDto object = mapper.readValue(response.getResponseBody(),
                    FileGlobalInfoDto.class);
            String objName = object.getData().getObject().getStorageFileName();
            log.info(objName);
            return object;
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info(ex.getMessage());
            return null;
        }
    }

    @LogExecutionTime
    @Override
    public boolean uploadFileMinio(String object, String filename) {
        log.info("[uploadFileMinio] "+ filename );
        log.info("Uploading file to miniO: " + filename);
        try {
            boolean found =
                    minioClient.bucketExists(bucketOcr);
            if (!found) {
                minioClient.makeBucket(bucketOcr);
            } else {
                log.info("Bucket '" + bucketOcr + "' already exists.");
            }

            minioClient.putObject(bucketOcr, object, filename, null, null, null, null);
            log.info(
                    "[MINIO][upload] '" + filename + "' is successfully uploaded as "
                            + "object '" + object + "' to bucket '" + bucket + "'.");
            return Utils.deleteFolder(object);
        } catch (Exception e) {
            log.error("[ERROR][MINIO][upload] Error occurred: " + e);
            log.error("[ERROR][MINIO][upload] Message: " + e.getMessage());
            return false;
        }
    }

    @LogExecutionTime
    @Override
    public String downloadFileMinio(String object, String filename) {
        Utils.createFolder(sourceFolder);
        Utils.createFolder(completeFolder);
        try {
            log.info("[MINIO][download] Starting download file '" + filename + "' from minio.");
            String filenameF = filename.replaceAll(" ", "_");
            minioClient.getObject(bucket, object, sourceFolder+"/" + filenameF);

            log.info(
                    "[MINIO][download] Object '" + object + "' is successfully downloaded as "
                            + "file '" + filename + "' from bucket '" + bucket + "'.");
            return sourceFolder+"/" + filenameF;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[ERROR][MINIO][download] Error occurred: " + e);
            log.error("[ERROR][MINIO][download] Error message: " + e.getMessage());
            return null;
        }
    }

     @LogExecutionTime
     @Override
     public File upload(MultipartFile file) {
         long l = System.currentTimeMillis();
         Path path = Paths.get(this.rootLocation.toUri().getPath() + "/" + l);
         Utils.createFolder(path.toString());
         if (file.isEmpty()) {
             throw new StorageException("Failed to store empty file.");

         }
         Path destinationFile = this.rootLocation
                 .resolve(path)
                 .resolve(
                 Paths.get(file.getOriginalFilename()))
                 .normalize().toAbsolutePath();
         if (!destinationFile.getParent().equals(path.toAbsolutePath())) {
             throw new StorageException(
                     "Cannot store file outside current directory.");
         }
         try (InputStream inputStream = file.getInputStream()) {
             Files.copy(inputStream, destinationFile,
                     StandardCopyOption.REPLACE_EXISTING);
             return destinationFile.toFile();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }

//     @LogExecutionTime
//     @Override
//     public File download() {
//         return null;
//     }
}
