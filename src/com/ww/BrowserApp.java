package com.ww;

import java.net.URI;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrowserApp {
    public static void main(String[] args) {
        BrowserTextGenerator browserTextGenerator = new BrowserTextGenerator();

        System.out.println("BROWSER WW");
        System.out.println("==========");
        try {
            System.out.print("Enter the link:\n> ");
            Scanner scanner = new Scanner(System.in);
            String link = scanner.nextLine();

            while (!link.startsWith("exit")) {
                String html = browserTextGenerator.openWeb(link);
                List<String> links = BrowserTextGenerator.getAllLinks(html);
                DownloadFiles.downloadFiles(links.toArray(new String[0]));

                System.out.print(
                        "\n\n" +
                        "===============\n" +
                        "Enter the link:\n> ");
                link = scanner.nextLine();
            }

        } catch (Exception e) {
            Logger.getLogger(BrowserApp.class.getName()).log(Level.SEVERE, null, e);
        }

    }
}
