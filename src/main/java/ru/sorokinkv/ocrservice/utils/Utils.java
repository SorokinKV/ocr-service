package ru.sorokinkv.ocrservice.utils;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public class Utils {
    public static void createFolder(String folder) {
        File directory = new File(folder);
        if (!directory.exists()) {
            directory.mkdir();
            log.info("Folder '" + folder + "' created");
        }
    }

    public static boolean deleteFolder(String folder) {
        File directory = new File(folder);
        if (directory.exists()) {
            try {
                FileUtils.deleteDirectory(directory);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!directory.exists()) {
            log.info("Folder '" + folder + "' deleted");
            return true;
        } else {
            log.info("Folder '" + folder + "' NOT deleted");
            return false;
        }

    }

}
