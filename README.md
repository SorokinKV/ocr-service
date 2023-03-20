
<h1>ocr-service </h1>
Сервис по распознаванию текста с картинок и отсканированных документов основанный на Spring Boot и Tesseract-OCR


<h3>Первый запуск</h3>
Для запуска и работы сервиса нужно скачать и распаковать <br>
https://github.com/tesseract-ocr/tessdata/archive/refs/tags/4.1.0.zip (или новее) <br>
из репозитория https://github.com/tesseract-ocr/tessdata.

Далее в **_application.yml_** необходимо указать путь к распакованной папке в параметре _**dir.tessdata**_ <br>

Например, если содержимое архива помещено в /opt/ocr-service/tessdata и параметры persistance volume-ов для микросервиса в файле docker-compose.yml сопоставлены - 

```
    volumes:
      - /opt/ocr-service/tessdata:/opt/tessdata
```
тогда в **_application.yml_** - 
```
dir:
    tessdata: /opt/tessdata/
```

<h3>Работа с сервисом</h3>

У сервиса есть следующие EndPoints:

EndPoint|Описание|Тип запроса|Параметры запроса
---|---|---|---
/api/uploadAndRec|загрузка и распознавание файла|POST|*Авторизация* - JWT Token <br> *body:* form-data<br>*params:* file, locales<br>file - файл<br>locales - языки текста

