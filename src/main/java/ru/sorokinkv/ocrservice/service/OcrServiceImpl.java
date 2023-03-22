package ru.sorokinkv.ocrservice.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sorokinkv.ocrservice.exception.OcrException;
import ru.sorokinkv.ocrservice.model.Pair;
import ru.sorokinkv.ocrservice.utils.OcrUtils;
import ru.sorokinkv.ocrservice.utils.Utils;

@Slf4j
@Service
public class OcrServiceImpl implements OcrService {


    private final ITesseract tesseract;

    private final TessAPI1 api;

    @Autowired
    private OcrUtils ocrUtils;

    @Value("${dir.tessdata}")
    private String tessdata;

    @Value("${dir.ocr}")
    private String ocrFolder;


    public OcrServiceImpl() {
        this.api = new TessAPI1();
        this.tesseract = new Tesseract();
    }

    @Override
    public File recognize(File file, String language, int ocrEngineMode, int pageSegMode) {
        Utils.createFolder(ocrFolder);
        if (language.equals("")) language = "rus+eng";
        if (ocrEngineMode == -1) ocrEngineMode = ITessAPI.TessOcrEngineMode.OEM_DEFAULT;
        List<ITesseract.RenderedFormat> list = new ArrayList<ITesseract.RenderedFormat>();
        list.add(ITesseract.RenderedFormat.PDF);
        try {
            // File tessDataFolder = LoadLibs.extractTessResources("tessdata");
            String absolutePath = tessdata;
            log.info("Path tessdata: " + absolutePath);
            log.info("Path file: " + file.getAbsolutePath());
            tesseract.setDatapath(absolutePath);
            tesseract.setOcrEngineMode(ocrEngineMode);
            tesseract.setPageSegMode(pageSegMode);
            tesseract.setLanguage(language);  //rus, eng, deu
            Utils.createFolder(ocrFolder);
            long l = System.currentTimeMillis();
            ocrFolder = ocrFolder + "/" + l;
            Utils.createFolder(ocrFolder);
            String outputFile = ocrFolder + "/" + file.getName();
            outputFile = StringUtils.removeEnd(outputFile, ".pdf") + "_OCR";
            log.info(outputFile);
            log.info("Input file size: " + file.length());
            Long startTime = System.currentTimeMillis();
            tesseract.createDocuments(file.getAbsolutePath(), outputFile, list);
            log.info("Total time (def): " + (System.currentTimeMillis() - startTime) / 1000 + " s");
            File fileOut = new File(outputFile + ".pdf");
            log.info("Output '" + fileOut.getAbsolutePath() + "' file size: " + fileOut.length());
            return fileOut;
            // return tesseract.doOCR(file); //for onePage document
        } catch (Exception e) {
            // e.printStackTrace();
            throw new OcrException("Recognise ERROR", e);
        }

    }

    // public void recogn(File file, String language, int ocrEngineMode, int pageSegMode) {
    //     // System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
    //     log.info("!!! Start test 1 (multi FJP)");
    //     ForkJoinPool customThreadPool = new ForkJoinPool(4);
    //     long start = System.currentTimeMillis();
    //     List<File> files = null;
    //     List<String> fileList = Collections.synchronizedList(new ArrayList<>());
    //     try {
    //         files = OcrUtils.convertPdf2Tiff(file);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     if (files != null) {
    //         // files.stream().parallel().forEach(f ->
    //         files.parallelStream().forEach(f -> {
    //                     try {
    //                         String s = customThreadPool.submit(
    //                                 () -> createPdfs(f, language, ocrEngineMode, pageSegMode)
    //                         ).get();
    //                         fileList.add(s);
    //                     } catch (Exception e) {
    //                         e.printStackTrace();
    //                     }
    //
    //                 }
    //         );
    //
    //     }
    //     List<String> sortedFileList = fileList.parallelStream().filter(f -> f
    //             != null).sorted().collect(Collectors.toList());
    //     ocrUtils.mergePDFFiles(sortedFileList, ocrFolder + "/out_" + file.getName());
    //     log.info("Time 1 (multi FJP): " + (System.currentTimeMillis() - start) / 1000 + " s");
    //     if (files != null) {
    //         log.info("Total files 1 (multi FJP): " + files.size() + "( " + files.get(0).getAbsolutePath
    //        () + " )");
    //     } else log.info("Files is null");
    //     log.info("!!! End test 1 (multi FJP)");
    //
    //
    // }

    @Override
    public String rec(String file, String language, int ocrEngineMode, int pageSegMode, int cores) {
        if (OcrUtils.checkSearchablePdf(file)) {
            log.info("File \"" + file + "\" is searchable. \nSkipping..");
            return null;
        }
        log.info("!!! Start test 2 (multi) File:" + file);
        Utils.createFolder(ocrFolder);
        long start = System.currentTimeMillis();
        List<String> files = null;
        List<String> fileList = new ArrayList<>();
        try {
            files = OcrUtils.convertPdf2Img(file, cores);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Pair<String, Integer>> pairList = new ArrayList<>();
        long orientTime = System.currentTimeMillis();
        files.parallelStream().forEach(f -> {
            Pair<String, Integer> imgAndDegreePair = new Pair<>();
            imgAndDegreePair.setLeft(f);
            imgAndDegreePair.setRight(OcrUtils.checkOrientation(tessdata, f, language));
            pairList.add(imgAndDegreePair);
        });
        log.info("__Check orientation time: " + (System.currentTimeMillis() - orientTime) / 1000 + "s File:" + file);
        long rotateTime = System.currentTimeMillis();
        if (pairList != null) {
            // files.stream().parallel().forEach(f ->
            pairList.parallelStream().forEach(p -> {
                OcrUtils.rotatePic(p.getLeft(), p.getRight());
            });
            pairList.parallelStream().forEach(p ->
                    fileList.add(createPdfs(p, language, ocrEngineMode, pageSegMode)));
        }
        List<String> sortedFileList = fileList.parallelStream().filter(f -> f
                != null).sorted().collect(Collectors.toList());
        log.info("__Rotating time: " + (System.currentTimeMillis() - rotateTime) / 1000 + "s File:" + file);
        long mergeTime = System.currentTimeMillis();
        ocrUtils.mergePDFFiles(sortedFileList, ocrFolder + "/out_" + new File(file).getName());

        log.info("__Merge time: " + (System.currentTimeMillis() - mergeTime) / 1000 + "s File:" + file);
        log.info("Time 2 (multi): " + (System.currentTimeMillis() - start) / 1000 + " s File:" +
                file + " (" + file + ")");
        if (files != null) {
            log.info("Total files 2 (multi): " + files.size() + "( " + files.get(0) + " )");
            Utils.deleteFolder(new File(files.get(0)).getParent());
            Utils.deleteFolder(new File(sortedFileList.get(0)).getParent());

        } else log.info("Files is null");
        log.info("!!! End test 2 (multi)");
        return ocrFolder + "/out_" + new File(file).getName();

    }

    @Override
    public String recImg(String file, String language, int ocrEngineMode, int pageSegMode, int cores) {
//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(cores));
        log.info("getParallelism=" + ForkJoinPool.commonPool().getParallelism());
        log.info("!!! Start test IMG (multi)");
        Utils.createFolder(ocrFolder);
        long start = System.currentTimeMillis();
        List<Pair<String, Integer>> pairList = new ArrayList<>();
        long orientTime = System.currentTimeMillis();
        Pair<String, Integer> imgAndDegreePair = new Pair<>();
        imgAndDegreePair.setLeft(file);
        imgAndDegreePair.setRight(OcrUtils.checkOrientation(tessdata, file, language));
        pairList.add(imgAndDegreePair);

        log.info("__Check orientation time: " + (System.currentTimeMillis() - orientTime) / 1000 + "s");
        long rotateTime = System.currentTimeMillis();
        List<String> fileList = new ArrayList<>();
        if (pairList != null) {
            // files.stream().parallel().forEach(f ->
            pairList.parallelStream().forEach(p -> {
                OcrUtils.rotatePic(p.getLeft(), p.getRight());
            });
            pairList.parallelStream().forEach(p ->
                    fileList.add(createPdfs(p, language, ocrEngineMode, pageSegMode)));
        }
        List<String> sortedFileList = fileList.parallelStream().filter(f -> f
                != null).sorted().collect(Collectors.toList());
        log.info("__Rotating time: " + (System.currentTimeMillis() - rotateTime) / 1000 + "s");
        long mergeTime = System.currentTimeMillis();
        String[] split = file.split("\\.");
        String extension = split[split.length - 1];
        String s = StringUtils.removeEnd(file, extension) + "pdf";
        String fileOcr = s;
        ocrUtils.mergePDFFiles(sortedFileList, ocrFolder + "/out_" + new File(fileOcr).getName());
        log.info("__Merge time: " + (System.currentTimeMillis() - mergeTime) / 1000 + "s");
        log.info("Time IMG (multi): " + (System.currentTimeMillis() - start) / 1000 + " s");
        if (file != null) {
            log.info("Total files IMG (multi): " + 1 + "( " + fileOcr + " )");
            Utils.deleteFolder(new File(file).getParent());
            Utils.deleteFolder(new File(sortedFileList.get(0)).getParent());
        } else log.info("File is null");
        log.info("!!! End test IMG (multi)");
        return ocrFolder + "/out_" + new File(fileOcr).getName();

    }

    private String createPdfs(Pair<String, Integer> pair, String language, int ocrEngineMode, int pageSegMode) {

        try {
            log.info("OCR file " + pair.getLeft() + " start");
            String file = ocrUtils.doOcr(tessdata, pair.getLeft(),
                    StringUtils.removeEnd(pair.getLeft(),
                            ".pdf") + "_OCR", language, ocrEngineMode, pageSegMode);
            OcrUtils.rotatePdf(file, pair.getRight());
            log.info("OCR file " + file + " completed");
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
