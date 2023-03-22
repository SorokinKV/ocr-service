package ru.sorokinkv.ocrservice.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sorokinkv.ocrservice.model.dto.ObjectDto;
import ru.sorokinkv.ocrservice.utils.Utils;

@Slf4j
@Service
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    @Value("${minio.bucket.ocr}")
    String bucketOcr;

    @Value("${filetypes}")
    String[] fileTypes;

    @Value("${locales}")
    String locales;

    @Value("${ocrEngineMode}")
    int ocrEngineMode;

    @Value("${pageSegMode}")
    int pageSegMode;

    ObjectMapper mapper = new ObjectMapper();

    private final OcrService ocrService;

    private final FileTransferService fileTransferService;

    private KafkaProducerService kafkaProducerService;

    public KafkaConsumerServiceImpl(OcrService ocrService, FileTransferService fileTransferService, KafkaProducerService kafkaProducerService) {
        log.info("Initialising KafkaConsumerServiceImpl");
        this.ocrService = ocrService;
        this.fileTransferService = fileTransferService;
        log.info("Initialising kafkaProducerService");
        this.kafkaProducerService = kafkaProducerService;
          log.info("Initialising OK\n");
    }

    @Override
    public void receive(ConsumerRecord<Long, String> data) throws JsonProcessingException {
        log.info("Offset: " + data.offset() + " Key: " + data.key() + " End: " + data + "\n");
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("Detected " + processors + " core(s), ");
        if(processors>2){
            processors -= 1;
        }
        log.info("used " + processors + " core(s)");
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(processors));
        log.info("getParallelism=" + ForkJoinPool.commonPool().getParallelism());

        String fileInfo = data.value();
        if (fileInfo==null || fileInfo.equals("[]")) return;
        TypeFactory typeFactory = mapper.getTypeFactory();
        List<ObjectDto> objectDtoList;
        objectDtoList = mapper.readValue(fileInfo, typeFactory.constructCollectionType(List.class, ObjectDto.class));
        objectDtoList.stream().forEach(objectDto -> log.info(String.valueOf(objectDto)));
        if (objectDtoList != null && objectDtoList.size() > 0) {
            objectDtoList.stream().forEach(o -> {
                ObjectDto oldObjDto = o;
                List<ObjectDto> sendObjDtoList = new ArrayList<>();
                String fileName = o.getName();
                String storageFileName = o.getStorageFileName();
                List<String> allSupportedTypes = new ArrayList<>();
                AtomicBoolean isSupportedType = new AtomicBoolean(false);
                Arrays.stream(fileTypes).forEach(t -> allSupportedTypes.add(t));
                allSupportedTypes.add("pdf");
                allSupportedTypes.forEach(t -> {
                    if (StringUtils.endsWith(fileName.toLowerCase(), t)) {
                        log.info(fileName + " " + t);
                        isSupportedType.set(true);
                    }
                });
                if (!isSupportedType.get()) {
                    sendObjDtoList.add(o);
                    kafkaProducerService.send(sendObjDtoList);
                    return;
                }
                String downloadedFileName = fileTransferService.downloadFileMinio(storageFileName, fileName);
                if (downloadedFileName != null) {
                    log.info("[receive]downloadedFileName" + downloadedFileName);
                    String recognised = recognise(downloadedFileName);
                    if (recognised != null) {
                        if (!recognised.equals("-*-")) {
                            boolean uploaded = fileTransferService.uploadFileMinio(storageFileName, recognised);
                            if (uploaded) {
                                o.setStoragePathOcr("/" + bucketOcr + "/" + storageFileName);
                                o.setRecognized(true);
                                o.setSearchable(true);
                                sendObjDtoList.add(o);
                                kafkaProducerService.send(sendObjDtoList);
                                Utils.deleteFolder(new File(recognised).getParent());
                                Utils.deleteFolder(new File(downloadedFileName).getParent());
                            } else{
                                log.info("[ERROR][receive]downloadedFileName" + downloadedFileName);
                            }
                        } else {
                            o.setRecognized(false);
                            o.setSearchable(true);
                            sendObjDtoList.add(o);
                            kafkaProducerService.send(sendObjDtoList);
                        }
                    } else {
                        sendObjDtoList.add(o);
                        kafkaProducerService.send(sendObjDtoList);
                    }
                }
//                }
            });
        }
    }

    private String recognise(String file) {
        File upload = new File(file);
        long startTime = System.currentTimeMillis();
        List<String> listTypes = Arrays.asList(fileTypes);
        Long count = listTypes.stream().filter(t -> upload.getName().toLowerCase().endsWith(t))
                .count();
        String recOut = null;
        int processors = Runtime.getRuntime().availableProcessors();

        if (upload.getName().toLowerCase().endsWith(".pdf")) {
            recOut = ocrService.rec(upload.getAbsolutePath(), locales, ocrEngineMode, pageSegMode, processors);
            if (recOut == null) {
                recOut = "-*-";
//                recOut.setLastModified(0);
            }
        }
        if (count > 0L) {
            recOut = ocrService.recImg(upload.getAbsolutePath(), locales, ocrEngineMode, pageSegMode, 0);
        }
        if (recOut != null) {
            log.info("****************Statistics*********************");
            try {
                log.info("Filename: " + upload.getName() + ", size:" + ((Files.size(Paths.get(upload.getAbsolutePath())))/1024) + "KB");
            } catch (IOException e) {
            }
            log.info("Total time: " + ((System.currentTimeMillis()-startTime)/1000) +"s");
            log.info("Max memory:" + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB");
            log.info("Total memory:" + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
            log.info("Free memory:" + Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
            if (!recOut.equals("-*-")) {
                log.info("Recognised file: " + recOut);
            } else {
                log.info("File \'" + file + "\' already recognised");
            }
            log.info("************************************************");
            return recOut;
        } else
            return null;
    }


}
