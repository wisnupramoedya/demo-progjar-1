package com.ww;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFiles {
    /**
     * Download any files that can be downloaded from the given links.
     *
     * @param links contain array of links
     */
    public static void downloadFiles(String[] links) {
        System.out.println("Download start.");

        int count = 1;
        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(new Date());
        String dirPath =  String.format(".\\demo-progjar-1\\downloads\\%s", timestamp);

        try {
            Files.createDirectories(Paths.get(dirPath));
        } catch (Exception e) {
            Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, e);
            return;
        }

        for (String link : links) {
            try {
                URL url = new URL(link);
                String extension = link.substring(link.lastIndexOf(".")); // Example: .txt

                BufferedInputStream bis = new BufferedInputStream(url.openStream());
                FileOutputStream fos = new FileOutputStream(String.format("%s\\%03d%s", dirPath, count, extension));
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                bos.write(bis.readAllBytes());
                bos.flush();

                bos.close();
                fos.close();
                bis.close();

                count++;
            } catch (Exception e) {
                Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        System.out.println("Download finished.");
    }
}
