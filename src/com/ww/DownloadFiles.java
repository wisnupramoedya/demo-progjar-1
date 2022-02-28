package com.ww;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFiles {
    /**
     * Define downloadable content-type from HTTP response header.
     */
    private static String[] dowloadableExt = {
            "image/*",
            "model/*",
            "audio/*",
            "video/*",
            "application/pdf",
            "application/zip",
    };

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
                HttpResponseParser hrp = new HttpResponseParser(link);
                Hashtable<String, String> metaData = getFilenameAndExtension(hrp.getRequestHeaders());
                if (metaData.isEmpty()) continue;

                FileOutputStream fos = new FileOutputStream(
                        String.format("%s\\%s%s", dirPath, metaData.get("filename"), metaData.get("extension"))
                );
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                bos.write(hrp.getRequestBody());
                bos.flush();

                bos.close();
                fos.close();

            } catch (Exception e) {
                Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        System.out.println("Download finished.");
    }


    /**
     * Get filename and extension of downloaded file.
     *
     * @param requestHeaders hashtable that contain all request headers
     * @return Map<String, String>
     */
    private static Hashtable<String, String> getFilenameAndExtension(Hashtable<String, String> requestHeaders) {
        Hashtable<String, String> data = new Hashtable<>();
        try {
            String contentType = requestHeaders.get("Content-Type");
            String contentDisposition = requestHeaders.get("Content-Disposition");

            if (!isDownloadable(contentType)) return data;

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

    /**
     * Determine whether the file content type in HTTP response header is downloadable or not.
     *
     * @param contentType HTTP response header value.
     * @return boolean
     */
    private static boolean isDownloadable(String contentType) {
        for (String ext: dowloadableExt) {
            String[] parsedExt = ext.split("/");
            String[] parsedCType = contentType.split("/");

            if (Objects.equals(parsedExt[0], parsedCType[0])) {
                return (Objects.equals(parsedExt[1], "*") || Objects.equals(parsedExt[1], parsedCType[1]));
            }
        }
        return false;
    }
}
