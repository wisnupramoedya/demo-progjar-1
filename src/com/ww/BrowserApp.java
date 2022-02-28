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
        } catch (Exception e) {
            Logger.getLogger(BrowserApp.class.getName()).log(Level.SEVERE, null, e);
        }
        String[] anchor_links;
        String[] downloadable_links;
        // class.static_method(downloadable_links);

    }
}
