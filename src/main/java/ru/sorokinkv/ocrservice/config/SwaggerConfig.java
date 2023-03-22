 package ru.sorokinkv.ocrservice.config;

 import java.util.ArrayList;
 import java.util.List;

 import com.fasterxml.classmate.TypeResolver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import ru.sorokinkv.ocrservice.swagger.OcrUploadAndRec;
 import springfox.documentation.builders.PathSelectors;
 import springfox.documentation.builders.RequestHandlerSelectors;
 import springfox.documentation.builders.RequestParameterBuilder;
 import springfox.documentation.service.RequestParameter;
 import springfox.documentation.spi.DocumentationType;
 import springfox.documentation.spring.web.plugins.Docket;

 /**
  * SwaggerConfig class.
  */
 @Configuration
 public class SwaggerConfig {

     @Autowired
     TypeResolver typeResolver;
     @Value("${info.pattern.urnSwagger}")
     private String sslHostPort;

     /**
      * Swagger api bean.
      *
      * @return
      */
     @Bean
     public Docket api() {

         RequestParameterBuilder paramBuilder = new RequestParameterBuilder();
         List<RequestParameter> params = new ArrayList<>();
         paramBuilder.name("Authorization")
                 .in("header")
                 .required(false)
                 .build();
         params.add(paramBuilder.build());

         return new Docket(DocumentationType.SWAGGER_2)
                 .globalRequestParameters(params)
                 .host(sslHostPort)
                 .select()
                 .apis(RequestHandlerSelectors.any())
                 .paths(PathSelectors.any())
                 .build()
                 .additionalModels(typeResolver.resolve(OcrUploadAndRec.class));
     }

 }
