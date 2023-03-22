 package ru.sorokinkv.ocrservice.swagger;

 import io.swagger.annotations.ApiModelProperty;
 import io.swagger.annotations.ApiParam;
 import io.swagger.v3.oas.annotations.media.Schema;
 import lombok.Data;
 import lombok.NonNull;
 import org.springframework.web.multipart.MultipartFile;

 @Data
 public class OcrUploadAndRec {

     @NonNull
     @ApiParam(allowMultiple = true,
               required = true)
     @ApiModelProperty(name = "file",
             dataType = "File",
             reference = "File",
             value ="File",
             example = "example.pdf",
             required = true
     )
     @Schema(
             description = "PDF file to OCR",
             example = "example.pdf",
             required = true
     )
     private MultipartFile file;

     @ApiModelProperty(name = "locales",
             dataType = "String",
             reference = "String",
             value ="languages to OCR",
             example = "rus+eng")
     @Schema(
             description = "languages to OCR",
             example = "rus+eng"
     )
     private String locales;

     @ApiModelProperty(name = "ocrEngineMode",
             dataType = "int",
             reference = "int",
             value ="Ocr Engine Mode",
             example = "1"
     )
     @Schema(
             description = "Ocr Engine Mode",
             example = "1",
             defaultValue = "1"
     )
     private int ocrEngineMode;

     @ApiModelProperty(name = "pageSegMode",
             dataType = "int",
             reference = "int",
             value ="Page Segmentation Mode",
             example = "-1"
     )
     @Schema(
             description = "Page Segmentation Mode",
             example = "-1",
             defaultValue = "-1"
     )
     private int pageSegMode;


 }
