package com.ww;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserTextGenerator {
    private String responseHTTP;
    private Hashtable<String, String> responseHeader;
    private String responseBody;
    private int accessCode;
    private String messageCode;
    int redirectCounter = 0, code = 0;
    private static final int IS_BASIC_AUTH = 187;
    private static final int IS_REDIRECT = 408;
    private static final int IS_NORMAL = 0;
    String username = "", password = "", redirectUrl = "";
    boolean isBody;


    public String openWeb(String link) throws Exception {
        URI uri;
        Socket socket;
        DataInputStream dataInputStream;
        BufferedOutputStream bufferedOutputStream;

        do {
            if (this.code == IS_BASIC_AUTH) {
                System.out.println("Need basic authorization");
                System.out.print("username > ");
                Scanner scannerAuth = new Scanner(System.in);
                username = scannerAuth.nextLine();

                System.out.print("password > ");
                password = scannerAuth.nextLine();
            }
            else if (this.code == IS_REDIRECT) {
                link = this.redirectUrl;
                System.out.println("Redirect to " + redirectUrl);
            }

            responseHeader = new Hashtable<>();
            uri = new URI(link);
            socket = new Socket(uri.getHost(), 80);
            dataInputStream = new DataInputStream(socket.getInputStream());
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());


            String request = "GET " + uri.getPath()
                    + (uri.getQuery() != null && !uri.getQuery().isBlank() ? ("?" + uri.getQuery()) : "")
                    + " HTTP/1.1\r\n"
                    + "Host: " + uri.getHost() + "\r\n"
                    + (this.code == IS_BASIC_AUTH ? "Authorization: Basic " + encodedToken(username, password) : "")
                    + "\r\n\r\n";
            bufferedOutputStream.write(request.getBytes());
            bufferedOutputStream.flush();


            int bufferSize = 1024, c;
            byte[] responseInBytes;
            StringBuilder response = new StringBuilder();
            do {
                responseInBytes = new byte[bufferSize];
                c = dataInputStream.read(responseInBytes);
                response.append(new String(responseInBytes));
            } while (c != -1);

            Scanner scanner = new Scanner(response.toString());
            int counter = 0;
            this.isBody = false;

            while (scanner.hasNextLine()) {
                String lineResponse = scanner.nextLine();

                if (!this.isBody) {
                    if (counter == 0) {
                        String[] firstLine = lineResponse.split(" ", 3);
                        this.accessCode = Integer.parseInt(firstLine[1]);
                        this.messageCode = firstLine[2];
                        boolean isError = accessCode > 300;
                        System.out.println("Access: " + accessCode + " => " + (isError ? "Message Error: " : "Message: ") + messageCode);
                    }
                    else if (lineResponse.length() <= 0) {
                        this.isBody = true;
                        continue;
                    }
                    else {
                        this.appendResponseHeader(lineResponse);
                    }
                }
                else if (this.isBody) {
                    StringBuilder responseOfBody = new StringBuilder();
                    responseOfBody.append(lineResponse);
                    while (scanner.hasNextLine()) {
                        responseOfBody.append(scanner.nextLine());
                    }
                    responseBody = responseOfBody.toString();
                    break;
                }
                counter++;
            }

            if (responseHeader.containsKey("WWW-Authenticate")) {
                this.code = IS_BASIC_AUTH;
            }
            else if (responseHeader.containsKey("Location")) {
                this.code = IS_REDIRECT;
                this.redirectUrl = uri.getScheme() + "://" + uri.getHost() + responseHeader.get("Location");
            }
            else {
                this.code = IS_NORMAL;
            }

            redirectCounter++;
        } while (this.code != IS_NORMAL);


        System.out.println("HTML:");
        System.out.println(responseBody);


        dataInputStream.close();
        bufferedOutputStream.close();
        socket.close();
        return responseBody;
    }

    private String encodedToken(String username, String password) {
        String plainCredentials = username + ":" + password;
        return new String(Base64.getEncoder().encode(plainCredentials.getBytes()));
    }


    private void appendResponseHeader(String header) throws Exception {
        int index = header.indexOf(":");
        if (index == -1) {
            throw new Exception("Invalid Header Parameter" + header);
        }
//        System.out.println(header.substring(0, index));
//        System.out.println(header.substring(index + 2));
        this.responseHeader.put(header.substring(0, index), header.substring(index + 2));
    }


    public static List<String> getAllLinks(String html) {
        List<String> allLinks = new ArrayList<>();

        String regexOfUrl = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)";
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
