package com.ww;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.UUID;

public class DownloadFiles {
    /**
     * Download any files that can be downloaded from the given links.
     *
     * @param links contain array of links
     */
    public static void downloadFiles(String[] links) {
        System.out.println("Download start.");

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
                Map<String, String> metaData = getFilenameAndExtension(url);

                BufferedInputStream bis = new BufferedInputStream(url.openStream());
                FileOutputStream fos = new FileOutputStream(
                        String.format("%s\\%s%s", dirPath, metaData.get("filename"), metaData.get("extension"))
                );
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                bos.write(bis.readAllBytes());
                bos.flush();

                bos.close();
                fos.close();
                bis.close();

            } catch (Exception e) {
                Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        System.out.println("Download finished.");
    }

    /**
     * Get filename and extension of downloaded file.
     *
     * @param url of downloaded file
     * @return Map<String, String>
     */
    private static Map<String, String> getFilenameAndExtension(URL url) {
        Map<String, String> data = new HashMap<>();
        try {
            URLConnection conn = url.openConnection();

            String contentType = conn.getContentType();
            String contentDisposition = conn.getHeaderField("Content-Disposition");

            String filename = UUID.randomUUID().toString().substring(0, 6);
            String extension = '.' + contentType.substring(contentType.lastIndexOf("/") + 1);

            if (contentDisposition != null && contentDisposition.contains("=")) {
                String temp = contentDisposition.split("=")[1].replaceAll("\"", "");
                int dotIndex = temp.lastIndexOf(".");
                filename = temp.substring(0, dotIndex);
                extension = temp.substring(dotIndex);
            }
            data.put("filename", filename);
            data.put("extension", extension);

        } catch (Exception e) {
            Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, e);
        }
        return data;
    }
}
