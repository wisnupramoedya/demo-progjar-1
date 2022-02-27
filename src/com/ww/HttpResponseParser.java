package com.ww;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpResponseParser {
    private String requestLine;
    private Hashtable<String, String> requestHeaders;
    private StringBuffer requestBody;

    public HttpResponseParser(String url) {
        requestHeaders = new Hashtable<String, String>();
        requestBody = new StringBuffer();
        parseResponse(url);
    }

    public void parseResponse(String url) {
        Hashtable<String, String> metaData = getHostnameAndPath(url);
        try {
            Socket socket = new Socket(metaData.get("hostname"), 80);
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            bos.write(String.format("GET %s HTTP/1.1\r\n", metaData.get("path")).getBytes());
            bos.write(String.format("HOST: %s\r\n\r\n", metaData.get("hostname")).getBytes());
            bos.flush();

            setRequestLine(br.readLine());

            String header = br.readLine();
            while (header.length() > 0) {
                appendRequestHeader(header);
                header = br.readLine();
            }

            String body = br.readLine();
            while (body != null) {
                appendRequestBody(body);
                body = br.readLine();
            }

            bos.close();
            br.close();
            isr.close();
            socket.close();
        } catch (Exception e) {
            Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private Hashtable<String, String> getHostnameAndPath(String url) {
        Hashtable<String, String> data = new Hashtable<String, String>();

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
        hostName = hostName.replaceFirst("www", "");

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

    private void appendRequestBody(String body) {
        this.requestBody.append(body).append("\r\n");
    }

    public String getRequestLine() {
        return this.requestLine;
    }

    public Hashtable<String, String> getRequestHeaders() {
        return this.requestHeaders;
    }

    public String getRequestBody() {
        return this.requestBody.toString();
    }
}
