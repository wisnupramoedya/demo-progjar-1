package com.ww;

import java.io.*;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpResponseParser {
    private String requestLine;
    private Hashtable<String, String> requestHeaders;
    private ByteArrayOutputStream requestBody;
    private boolean quiet;

    public HttpResponseParser(String url, boolean quiet) {
        requestHeaders = new Hashtable<>();
        requestBody = new ByteArrayOutputStream();
        this.quiet = quiet;
        parseResponse(url);
    }

    /**
     * Parse response from the GET request to the given url.
     *
     * @param url for GET request
     */
    public void parseResponse(String url) {
        Hashtable<String, String> metaData = null;
        Socket socket;
        BufferedInputStream bis;
        DataInputStream dis;
        BufferedOutputStream bos;
        int accessCode = 0;
        int redirectCounter = 0;

        try {
            do {
                if (redirectCounter > 5) {
                    throw new Exception("Too many redirection");
                }
                if (!requestHeaders.isEmpty() && requestHeaders.containsKey("Location")) {
                    url = requestHeaders.get("Location");
                    System.out.println("Relocating to " + url); // TODO:: Comment on prod
                }

                metaData = getMetaData(url);

                socket = new Socket(metaData.get("hostname"), 80);
                bis = new BufferedInputStream(socket.getInputStream());
                dis = new DataInputStream(bis);
                bos = new BufferedOutputStream(socket.getOutputStream());

                bos.write(String.format("GET %s HTTP/1.1\r\n", metaData.get("path")).getBytes());
                bos.write(String.format("HOST: %s\r\n\r\n", metaData.get("hostname")).getBytes());
                bos.flush();

                setRequestLine(dis.readLine());

                String header = dis.readLine();
                while (header.length() > 0) {
                    appendRequestHeader(header);
                    header = dis.readLine();
                }
                accessCode = Integer.parseInt(requestLine.split(" ")[1]);
                redirectCounter += 1;
            } while (accessCode == 301 || accessCode == 308); // do it again when HTTP code is equal to 301

            int size = Integer.parseInt(this.requestHeaders.get("Content-Length")); // in byte
            int bufferSize = 102400;
            int currSize = 0;
            byte[] buffer;

            do {
                currSize = requestBody.size();
                int diff = size - currSize;

                buffer = dis.readNBytes(Math.min(diff, bufferSize));
                requestBody.writeBytes(buffer);
                if (!quiet) System.out.format("Download %s: %d | %d byte\n", url, currSize, size);
            } while (currSize < size);

            bos.close();
            dis.close();
            bis.close();
            socket.close();

            if ((accessCode / 100) == 4) {
                String msg = "Bad request: " + requestBody.toString();
                System.out.println(msg); // TODO:: Comment on prod
                throw new Exception(msg);
            }
        } catch (Exception e) {
            Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Parse hostname and path protocol from url.
     *
     * @param url
     * @return Hashtable<String, String>
     */
    private Hashtable<String, String> getMetaData(String url) {
        Hashtable<String, String> data = new Hashtable<>();

        // Remove https:// or http:// from url
        int idx = url.indexOf("://");
        if (idx != -1) {
            url = url.substring(idx + 3);
        }

        // Get host name
        String hostName = url;
        idx = hostName.indexOf("/");
        if (idx != -1) {
            hostName = hostName.substring(0, idx);
        }

        // Get path
        String path = url.substring(idx);

        data.put("hostname", hostName);
        data.put("path", path);
        return data;
    }

    private void setRequestLine(String requestLine) throws Exception {
        if (requestLine == null || requestLine.length() == 0) {
            throw new Exception("Invalid Request-Line: " + requestLine);
        }
        this.requestLine = requestLine;
    }

    private void appendRequestHeader(String header) throws Exception {
        int idx = header.indexOf(":");
        if (idx == -1) {
            throw new Exception("Invalid Header Parameter: " + header);
        }
        this.requestHeaders.put(header.substring(0, idx), header.substring(idx + 2));
    }

    public String getRequestLine() {
        return this.requestLine;
    }

    public Hashtable<String, String> getRequestHeaders() {
        return this.requestHeaders;
    }

    public byte[] getRequestBody() {
        return this.requestBody.toByteArray();
    }
}
