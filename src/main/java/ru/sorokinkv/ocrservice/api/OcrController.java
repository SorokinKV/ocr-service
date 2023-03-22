 package ru.sorokinkv.ocrservice.api;

 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 import java.util.List;

 import io.swagger.annotations.ApiOperation;
 import io.swagger.v3.oas.annotations.media.Content;
 import io.swagger.v3.oas.annotations.media.Schema;
 import io.swagger.v3.oas.annotations.responses.ApiResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.InputStreamResource;
 import org.springframework.core.io.Resource;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.RequestPart;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.bind.annotation.RestController;
 import org.springframework.web.multipart.MultipartFile;
 import ru.sorokinkv.ocrservice.service.FileTransferService;
 import ru.sorokinkv.ocrservice.service.OcrService;
 import ru.sorokinkv.ocrservice.swagger.OcrUploadAndRec;

 @RestController
 class OcrController {

     @Value("${filetypes}")
     String[] fileTypes;

     private final FileTransferService fileTransferService;
     private final OcrService ocrService;

     @Autowired
     public OcrController(FileTransferService fts, OcrService ocr) {
         this.fileTransferService = fts;
         this.ocrService = ocr;
     }

     // public OcrApi(FileTransferService fileTransService){
     //     this.fileTransferService = fileTransService;
     // }

     @ApiOperation("Распознать файл и получить результат (однопоточный)")
     @ResponseStatus(HttpStatus.OK)
     @PostMapping("/api/uploadAndRecOld")
     @ApiResponse(responseCode = "200", description = "Распознать файл и получить результат (однопоточный)",
             content = @Content(schema = @Schema(implementation = OcrUploadAndRec.class)))
     public ResponseEntity<Resource> uploadAndRec(@RequestPart("file") MultipartFile file,
                                                  @RequestParam("locales") String locales,
                                                  @RequestParam("ocrEngineMode") int ocrEngineMode,
                                                  @RequestParam("pageSegMode") int pageSegMode) throws FileNotFoundException, InterruptedException {
         File upload = fileTransferService.upload(file);

         File recOut = ocrService.recognize(upload, locales, ocrEngineMode, pageSegMode);
         return responseBuilder(recOut.getAbsolutePath());
         // File recognize = new File(recOut.getAbsolutePath());

         // while (recognize.lastModified()<300){
         //     System.out.println("wait: " +recognize.getAbsolutePath()+" "+ recognize.lastModified());
         //     Thread.sleep(300);
         // }
         // InputStreamResource resource = new InputStreamResource(new FileInputStream(recognize));
         //
         // HttpHeaders headers = new HttpHeaders();
         // headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
         // headers.add("Pragma", "no-cache");
         // headers.add("Expires", "0");
         // headers.add("Content-Disposition", "attachment; filename="+recognize.getName());
         //
         // return ResponseEntity.ok()
         //         .headers(headers)
         //         .contentLength(recognize.length())
         //         .contentType(MediaType.APPLICATION_OCTET_STREAM)
         //         .body(resource);
     }

     @ApiOperation("Распознать файл и получить результат (многопоточный)")
     @ResponseStatus(HttpStatus.OK)
     @PostMapping("/api/uploadAndRecNew")
     @ApiResponse(responseCode = "200", description = "Распознать файл и получить результат (многопоточный)",
             content = @Content(schema = @Schema(implementation = OcrUploadAndRec.class)))
     public ResponseEntity<Resource> uploadAndRecNew(@RequestPart("file") MultipartFile file,
                                                     @RequestParam("locales") String locales,
                                                     @RequestParam("ocrEngineMode") int ocrEngineMode,
                                                     @RequestParam("pageSegMode") int pageSegMode
     ) throws FileNotFoundException {
         File upload = fileTransferService.upload(file);

         List<String> listTypes = Arrays.asList(fileTypes);
         Long count = listTypes.stream().filter(t -> upload.getName().toLowerCase().endsWith(t))
                 .count();
         String recOut = null;
         String uploadFile = upload.getAbsolutePath();
         if (file.getName().toLowerCase().endsWith(".pdf")) {

             recOut = ocrService.rec(uploadFile, locales, ocrEngineMode, pageSegMode, 0);
         }
         if (count > 0L) {
             recOut = ocrService.recImg(uploadFile, locales, ocrEngineMode, pageSegMode, 0);
         }
         if (recOut != null) {
             System.out.println("Max memory:" + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB");
             System.out.println("Total memory:" + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
             System.out.println("Free memory:" + Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
             return responseBuilder(recOut);
         } else return responseBuilder(null);
     }

     @ApiOperation("Получить файл с диска по ссылке на сервере")
     @ResponseStatus(HttpStatus.OK)
     @GetMapping("/api/getFile")
     @ApiResponse(responseCode = "200", description = "Получить файл с диска по ссылке на сервере",
             content = @Content(schema = @Schema(implementation = OcrUploadAndRec.class)))
     public ResponseEntity<Resource> getFile(@RequestParam("fullPath") String path
     ) throws FileNotFoundException {
         return responseBuilder(path);
     }


     private ResponseEntity<Resource> responseBuilder(String path) throws FileNotFoundException {
         if (path == null) {
             return ResponseEntity.status(HttpStatus.NO_CONTENT)
                     .body(null);
         }
         File recognize = new File(path);

         InputStreamResource resource = new InputStreamResource(new FileInputStream(recognize));

         HttpHeaders headers = new HttpHeaders();
         headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
         headers.add("Pragma", "no-cache");
         headers.add("Expires", "0");
         headers.add("Content-Disposition", "attachment; filename=" + recognize.getName());

         return ResponseEntity.ok()
                 .headers(headers)
                 .contentLength(recognize.length())
                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
                 .body(resource);
     }
 }