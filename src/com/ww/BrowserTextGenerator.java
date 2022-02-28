package com.ww;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserTextGenerator {
    public static String openWeb() throws IOException, URISyntaxException {
        System.out.printf("Enter the link:\n> ");
        Scanner scanner = new Scanner(System.in);
        String link = scanner.nextLine();
        URI uri = new URI(link);


        Socket socket = new Socket(uri.getHost(), 80);
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());

//        System.out.println(uri.getHost());
        String request = "GET " + uri.getPath()
                + (uri.getQuery() != null && !uri.getQuery().isBlank() ? ("?" + uri.getQuery()) : "")
                + " HTTP/1.1\r\n"
                + "Host: " + uri.getHost()
                + "\r\n\r\n";
//        System.out.println(request);
        bufferedOutputStream.write(request.getBytes());
        bufferedOutputStream.flush();


        int bufferSize = 100;
        byte[] responseInBytes = new byte[bufferSize];
        int c = dataInputStream.read(responseInBytes);
        String response = "";

        while (c != -1) {
            response += new String(responseInBytes);
            responseInBytes = new byte[bufferSize];
            c = dataInputStream.read(responseInBytes);
        }

        System.out.println("HTML:");
        System.out.println(response);

        dataInputStream.close();
        bufferedOutputStream.close();
        socket.close();
        return response;
    }

    public static List<String> getAllLinks(String html) {
        List<String> allLinks = new ArrayList<>();

        String regexOfUrl = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(regexOfUrl, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);


        while (matcher.find()) {
            String link = html.substring(matcher.start(0), matcher.end(0));
            allLinks.add(link);
        }

        System.out.println("List of all links: (" + allLinks.size() + " sizes)");
        for (String link :
                allLinks) {
            System.out.println(link);
        }

        return allLinks;
    }
}
