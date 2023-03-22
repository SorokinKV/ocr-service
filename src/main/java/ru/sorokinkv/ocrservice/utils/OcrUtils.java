package ru.sorokinkv.ocrservice.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.ptr.PointerByReference;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI1;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static net.sourceforge.tess4j.ITessAPI.FALSE;
import static net.sourceforge.tess4j.ITessAPI.TRUE;

@Slf4j
@Service
public class OcrUtils {

    // private String language = "eng";
    // private String datapath;
    // private int psm = -1;
    // private int ocrEngineMode = ITessAPI.TessOcrEngineMode.OEM_DEFAULT;
    // private final Properties prop = new Properties();
    // private final List<String> configList = new ArrayList<String>();

    public static List<String> convertPdf2Img(String inputPdfFile, int cores) throws IOException {
        long startTime = System.currentTimeMillis();
        Path path = Files.createTempDirectory("mdm_tess_");
        File imageDir = path.toFile();

        PDDocument document = null;
        // List<File> collect = new ArrayList<>();
        List<String> fileList = new ArrayList<>();
        try {
            PDDocument documentF = Loader.loadPDF(new File(inputPdfFile));
//            PDDocument documentF = PDDocument.load(inputPdfFile);
            // PDDocument documentF = new PDDocument();
            // documentF.
//            document = documentF;
            fileList = convertMulti(documentF, "tiff", imageDir.getAbsolutePath(), cores);
            // collect = IntStream.range(0, documentF.getNumberOfPages())
            //         .mapToObj(i -> convert(i, documentF, imageDir)).collect(Collectors.toList());
            documentF.close();
        } catch (IOException ioe) {
            log.error("[convertPdf2Tiff]Error extracting PDF Document => " + ioe);
            ioe.printStackTrace();
        } finally {
            log.info("[convertPdf2Img]Finally");
            if (imageDir.list().length == 0) {
                log.info("[convertPdf2Img]Finally imageDir Deleting");
                imageDir.delete();
            }

            if (document != null) {
                try {
                    log.info("[convertPdf2Img]Finally document closing");
                    document.close();
                } catch (Exception e) {

                }
              log.info("[convertPdf2Img]Finally End");
            } else {
                log.error("[convertPdf2Img] document is null");
            }
        }
        log.info("__Converting to image time: " + (System.currentTimeMillis() - startTime)/1000 + "s" );
        return fileList;
    }

    private static File convert(int i, PDDocument document, File imageDir) {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage bim = null;
        try {
            String filename = String.format("workingimage%04d.png", i + 1);
            ImageIOUtil.writeImage(bim, new File(imageDir, filename).getAbsolutePath(), 300);
            log.info("End 2 thread: " + i);
            return new File(imageDir, filename);
        } catch (Exception e) {
            log.info("Convert error " + e.getMessage());
            e.printStackTrace();
            return null;
        }


    }

    private static List<String> convertMulti(PDDocument documentF, String fileExtension, String outImage, int cores) {
        PDFRenderer pdfRenderer = new PDFRenderer(documentF);

        int dpi = 300;
        // AtomicReference<BufferedImage> bImage = null;
//        int processors = cores;
//        log.info("Found " + cores + " CPU cores");
//        if (cores > 1) {
//            processors -= 1;
//        }
//        final ForkJoinPool exec = new ForkJoinPool(processors);
//        ExecutorService exec = Executors.newFixedThreadPool(processors);

//        List<Future<?>> pending = new ArrayList<>();
        List<String> fileList = new ArrayList<>();
        List<String > finalFileList = fileList;
        IntStream.range(0, documentF.getNumberOfPages()).forEach(i -> {
            try {
                String filename = String.format("workingimage%04d." + fileExtension, i + 1);
                log.info("Getting page " + filename);
                BufferedImage bImage = pdfRenderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                String file = outImage + "/" + filename;

//                BufferedImage finalBImage = bImage;
//                finalFileList.add(write(finalBImage, fileExtension, file));
                log.info(filename + " bImage: h=" + bImage.getHeight() +", w="+bImage.getWidth());
                finalFileList.add(write(bImage, fileExtension, file));
//                finalFileList.add(write(pdfRenderer.renderImageWithDPI(i, dpi, ImageType.RGB), fileExtension, file));
                log.info("Getting page " + filename + " complete");
//                pending.add(exec.submit(() -> write(finalBImage, fileExtension, file)));
//                bImage.flush();

            } catch (IOException e) {
                log.info("IOException " + e.getMessage());
//                e.printStackTrace();
            }
        });
        try {
            if(documentF!=null) {
                documentF.close();
            }else {
                log.info("documentF is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        List<File> finalFileList = fileList;
//        pending.forEach(fut -> {
//            try {
//                File file = (File) fut.get();
//                finalFileList.add(file);
//                log.info("File: " + file.getName() + " completed");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        });


        // for (Future<?> fut : pending) {
        //     try {
        //         File file = (File) fut.get();
        //         fileList.add(file);
        //         log.info("File: "+ file.getName() + " completed");
        //
        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     } catch (ExecutionException e) {
        //         e.printStackTrace();
        //     }
        // }
//        exec.shutdown();
        fileList = finalFileList.stream().sorted().collect(Collectors.toList());
//        try {
//            if(!exec.isTerminated()){
//                log.info("Try FJ pool shutdown");
//                exec.awaitTermination(10, TimeUnit.SECONDS);
//                exec.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            exec.shutdownNow();
//        } finally {
//            log.info("Finally FJ pool shutdown");
//            exec.shutdownNow();
//            log.info("Finally FJ pending clear");
//            pending.clear();
//        }
        return fileList;
    }

    private static String write(BufferedImage image, String fileExtension, String file) {
        try {
//            ImageIO.scanForPlugins();
            log.info("ImageIO.write " + file);
            ImageIO.write(image, fileExtension, new File(file));
            log.info("ImageIO.write " + file + " saved");
//            image.flush();
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void rotatePic(String file, Integer angle) {
        if (angle == 0) return;
        log.info("Rotating: " + file);
        angle = 360 - angle;
        try {
            BufferedImage read = ImageIO.read(new File(file));
            BufferedImage bimg = read;

            double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                    cos = Math.abs(Math.cos(Math.toRadians(angle)));
            int w = bimg.getWidth();
            int h = bimg.getHeight();
            int neww = (int) Math.floor(w * cos + h * sin),
                    newh = (int) Math.floor(h * cos + w * sin);
            BufferedImage rotated = new BufferedImage(neww, newh, TYPE_INT_RGB);
            Graphics2D graphic = rotated.createGraphics();
            graphic.translate((neww - w) / 2, (newh - h) / 2);
            graphic.rotate(Math.toRadians(angle), w / 2, h / 2);
            graphic.drawRenderedImage(bimg, null);
            graphic.dispose();
            ImageIOUtil.writeImage(rotated, file, 300);
            rotated.flush();
            read.flush();
            bimg.flush();
        } catch (Exception e) {
            log.info("___ Rotate error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static void rotatePdf(String filename, int degree) throws Exception {
        if (degree == 0) return;
        degree = 360 - degree;
//        File file = new File(filename);
//        PDDocument document = PDDocument.load(file);
        PDDocument document = Loader.loadPDF(new File(filename));
        PDPage page = document.getDocumentCatalog().getPages().get(0);
        PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND,
                false, false);
        Matrix matrix = Matrix.getRotateInstance(Math.toRadians(degree), 0, 0);
        cs.transform(matrix);
        cs.close();

        PDRectangle cropBox = page.getCropBox();
        Rectangle rectangle = cropBox.transform(matrix).getBounds();
        PDRectangle newBox = new PDRectangle((float) rectangle.getX(), (float) rectangle.getY(),
                (float) rectangle.getWidth(), (float) rectangle.getHeight());
        page.setCropBox(newBox);
        page.setMediaBox(newBox);
        document.save(new File(filename));
        document.close();

    }

    public static int checkOrientation(String tessDataPath, String imagePath, String language) {
        TessAPI1 api = new TessAPI1();
        ITessAPI.TessBaseAPI handle = api.TessBaseAPICreate();
        log.info("TessBaseAPIDetectOrientationScript");
        File image = new File(imagePath);
        Leptonica leptInstance = Leptonica.INSTANCE;
        Pix pix = leptInstance.pixRead(image.getPath());
        log.info(image.getName() + " h=" + pix.h);
        log.info(image.getName() + " w=" + pix.w);
        log.info(image.getName() + " d=" + pix.d);
        api.TessBaseAPIInit3(handle, tessDataPath, language);
        api.TessBaseAPISetImage2(handle, pix);

        IntBuffer orient_degB = IntBuffer.allocate(1);
        FloatBuffer orient_confB = FloatBuffer.allocate(1);
        PointerByReference script_nameB = new PointerByReference();
        FloatBuffer script_confB = FloatBuffer.allocate(1);

        int result = api.TessBaseAPIDetectOrientationScript(handle, orient_degB, orient_confB, script_nameB,
                script_confB);

        api.TessBaseAPIClear(handle);
        api.TessBaseAPIDelete(handle);

        int orient_deg = 0;
        if (result == TRUE) {
            orient_deg = orient_degB.get();
            float orient_conf = orient_confB.get();
            String script_name = script_nameB.getValue().getString(0);
            float script_conf = script_confB.get();
            log.info(String.format("OrientationScript: orient_deg=%d, orient_conf=%f, script_name=%s, "
                    + "script_conf=%f", orient_deg, orient_conf, script_name, script_conf));

        }

        PointerByReference pRef = new PointerByReference();
        pRef.setValue(pix.getPointer());
//        leptInstance.pixDestroy(script_nameB);
        leptInstance.pixDestroy(pRef);
        try {
            log.info("[Orientation]Cleaning  buffers ...");
//            pix.clear();
            orient_degB.clear();
            orient_confB.clear();
            script_confB.clear();
//            script_nameB = null;
            log.info("[Orientation]All buffers is cleaned...");
        }catch (Exception e){

        }
//        Runtime.getRuntime().gc();
        return orient_deg;
    }

    public static boolean checkSearchablePdf(String filename) {
        try {
            PDDocument doc = Loader.loadPDF(new File(filename));
//            PDDocument doc = PDDocument.load(filename);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            doc.close();
            String regexp = "\\w";
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                // log.info(text);
                return true;
            } else {
                return false;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void mergePDFFiles(List<String> files,
                              String mergedFileName) {
        // try {
        PDFMergerUtility pdfmerger = new PDFMergerUtility();
        // for (String fileName : files) {
        files.parallelStream().forEachOrdered(fileName -> {
            File file = new File(fileName);
            PDDocument document = null;
            try {
                document = Loader.loadPDF(file);
//                document = PDDocument.load(file);
                pdfmerger.setDestinationFileName(mergedFileName);
                pdfmerger.addSource(file);
                pdfmerger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // }
        log.info("Merge complete " + mergedFileName);
        // } catch (IOException e) {
        //     log.info("Error to merge files. Error: " + e.getMessage());
        // }
    }

    public String doOcr(String tessDataPath, String imagePath, String outputbase, String language, int ocrEngineMode,
                      int pageSegMode) {
        log.info("Start doOcr " + outputbase);
        int set_only_init_params = FALSE;
        int oem = ocrEngineMode;
        PointerByReference configs = null;
        int configs_size = 0;

        String[] params = {"load_system_dawg", "tessedit_char_whitelist"};
        // String vals[] = {"F", ""}; //0123456789-.IThisalotfpnex
        //PointerByReference vars_vec = new PointerByReference();
        //vars_vec.setPointer(new Pointer(params));
        //PointerByReference vars_values = new PointerByReference();
        //vars_values.setPointer(new StringArray(vals));
        NativeSize vars_vec_size = new NativeSize(params.length);

        TessAPI1 api = new TessAPI1();
        ITessAPI.TessBaseAPI handle = api.TessBaseAPICreate();

        int rc = api.TessBaseAPIInit4(handle, tessDataPath, language, oem, configs, configs_size, null, null,
                vars_vec_size, set_only_init_params);
        api.TessBaseAPISetVariable(handle, "user_defined_dpi", "300");

        if (rc != 0) {
            api.TessBaseAPIDelete(handle);
            //logger.error("Could not initialize tesseract.");
            log.info("Could not initialize tesseract.");
        }

        String dataPath = api.TessBaseAPIGetDatapath(handle);
        // ITessAPI.TessResultRenderer renderer = api.TessPDFRendererCreate(outputbase, dataPath, FALSE);
        ITessAPI.TessResultRenderer renderer = api.TessHOcrRendererCreate(outputbase);
        api.TessResultRendererInsert(renderer, api.TessBoxTextRendererCreate(outputbase));
        api.TessResultRendererInsert(renderer, api.TessPDFRendererCreate(outputbase, tessDataPath, 0));
        api.TessResultRendererInsert(renderer, api.TessTextRendererCreate(outputbase));
        // log.info("dataPath: " + dataPath);
        int result = api.TessBaseAPIProcessPages(handle, imagePath, null, 0, renderer);
        // log.info("result " + result);
        if (result == FALSE) {
            //logger.error("Error during processing.");
            return null;
        }
        renderer = api.TessResultRendererNext(renderer);
        ITessAPI.TessResultRenderer rendererW = renderer;
        int i = 0;
        while (rendererW != null) {
            // log.info("Try " + (i++));
            String ext = api.TessResultRendererExtention(rendererW).getString(0);
            api.TessResultRendererEndDocument(rendererW);
            String log = String.format("TessResultRendererExtention: %s\nTessResultRendererTitle: "
                            + "%s\nTessResultRendererImageNum: %d",
                    ext,
                    api.TessResultRendererTitle(rendererW).getString(0),
                    api.TessResultRendererImageNum(rendererW));
            // log.info(log);
            rendererW = api.TessResultRendererNext(rendererW);
            // log.info("Starting new try " + i);
        }

        api.TessDeleteResultRenderer(renderer);
        api.TessBaseAPIEnd(handle);


        String ocrPdf = outputbase + ".pdf";
        if (!(new File(ocrPdf).exists())) {
            // TODO
            return null;
        }
        log.info("End doOcr " + outputbase);
        return ocrPdf;

    }
}
