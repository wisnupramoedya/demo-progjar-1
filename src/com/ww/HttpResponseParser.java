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

    public HttpResponseParser(String url) {
        requestHeaders = new Hashtable<>();
        requestBody = new ByteArrayOutputStream();
        parseResponse(url);
    }

    /**
     * Parse response from the GET request to the given url.
     *
     * @param url for GET request
     */
    public void parseResponse(String url) {
        Hashtable<String, String> metaData = getHostnameAndPath(url);
        try {
            Socket socket = new Socket(metaData.get("hostname"), 80);
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            DataInputStream dis = new DataInputStream(bis);
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            bos.write(String.format("GET %s HTTP/1.1\r\n", metaData.get("path")).getBytes());
            bos.write(String.format("HOST: %s\r\n\r\n", metaData.get("hostname")).getBytes());
            bos.flush();

            setRequestLine(dis.readLine());

            String header = dis.readLine();
            while (header.length() > 0) {
                appendRequestHeader(header);
                header = dis.readLine();
            }

            int size = Integer.parseInt(this.requestHeaders.get("Content-Length")); // in byte
            int currentSize = 0;
            byte[] buffer = new byte[1024];

            do {
                int c = dis.read(buffer);
                currentSize += c;
                requestBody.writeBytes(buffer);
                System.out.format("Download %s: %d | %d\n", url, currentSize, size);
            } while (dis.available() > 0);

            bos.close();
            dis.close();
            bis.close();
            socket.close();
        } catch (Exception e) {
            Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Parse hostname and path from url.
     *
     * @param url
     * @return Hashtable<String, String>
     */
    private Hashtable<String, String> getHostnameAndPath(String url) {
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
        hostName = hostName.replaceFirst("www.", "");

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
