package com.ww;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrowserApp {
    public static void main(String[] args) {
        System.out.println("Hello progjar");
        try {
            String html = BrowserTextGenerator.openWeb();
            List<String> links = BrowserTextGenerator.getAllLinks(html);
            DownloadFiles.downloadFiles(links.toArray(new String[0]));
        } catch (Exception e) {
            Logger.getLogger(BrowserApp.class.getName()).log(Level.SEVERE, null, e);
        }

    }
}
