package ru.sorokinkv.ocrservice.service;

import java.io.File;

public interface OcrService {

    File recognize(File file, String language, int ocrEngineMode, int pageSegMode);
    String rec(String file, String language, int ocrEngineMode, int pageSegMode, int cores);

    String recImg(String file, String language, int ocrEngineMode, int pageSegMode, int cores);
}
