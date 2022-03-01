package com.ww;

import java.io.*;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

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
    private void parseResponse(String url) {
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
                if (metaData.isEmpty()) {
                    throw new Exception("Failed to parse hostname and path");
                }

                socket = new Socket(metaData.get("hostname"), 80);
                bis = new BufferedInputStream(socket.getInputStream());
                dis = new DataInputStream(bis);
                bos = new BufferedOutputStream(socket.getOutputStream());

                bos.write(String.format("GET %s HTTP/1.1\r\n", metaData.get("path")).getBytes());
                bos.write(String.format("HOST: %s\r\n\r\n", metaData.get("hostname")).getBytes());
                bos.flush();

                // Prevent stuck because of no response received.
                int counter = 0;
                while (dis.available() <= 0) {
                    if (counter == 10) {
                        throw new Exception("Connection error, response not received.");
                    }
                    TimeUnit.SECONDS.sleep(1);
                    counter += 1;
                }
                setRequestLine(dis.readLine());

                String header = dis.readLine();
                while (header.length() > 0) {
                    appendRequestHeader(header);
                    header = dis.readLine();
                }
                accessCode = Integer.parseInt(requestLine.split(" ")[1]);
                redirectCounter += 1;
            } while (accessCode == 301 || accessCode == 308); // do it again when HTTP code is equal to 301

            /**
             * This if statement is used to optimize this class performance by not fetching response body
             * that have a content-type should not be downloaded by the system.
             *
             * Disable this if statement to make this class able to fetch response body in other content types.
             */
            if (DownloadFiles.isDownloadable(getRequestHeaderObj("Content-Type"))) {
                int size = Integer.parseInt(getRequestHeaderObj("Content-Length")); // in byte
                int bufferSize = 10240;
                int currSize = 0;
                byte[] buffer;

                do {
                    currSize = requestBody.size();
                    int diff = size - currSize;

                    buffer = dis.readNBytes(Math.min(diff, bufferSize));
                    requestBody.writeBytes(buffer);
                    if (!quiet) System.out.format("Download %s: %d | %d byte\n", url, currSize, size);
                } while (currSize < size);
            }

            bos.close();
            dis.close();
            bis.close();
            socket.close();

            if ((accessCode / 100) == 4) {
                String msg = "Content not found";
                System.out.println(msg); // TODO:: Comment on prod
                throw new Exception(msg);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Content-Length")) {
                System.out.println("The url: " + url + " is not downloadable.");
            }
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
        if (idx == -1) {
            return data;
        }
        hostName = hostName.substring(0, idx);

        // Get path
        String path = url.substring(idx);

        data.put("hostname", hostName);
        data.put("path", path);
        return data;
    }

    /**
     * Set request line from response header.
     *
     * @param requestLine is a line that contain HTTP version, HTTP code, and HTTP message.
     * @throws Exception will be thrown when the given request line is empty.
     */
    private void setRequestLine(String requestLine) throws Exception {
        if (requestLine == null || requestLine.length() == 0) {
            throw new Exception("Invalid Request-Line: " + requestLine);
        }
        this.requestLine = requestLine;
    }

    /**
     * Append a header line to this class request header variable.
     *
     * @param header var is a line that contain header's key and value.
     * @throws Exception will be thrown when the given header isn't a key-value pair.
     */
    private void appendRequestHeader(String header) throws Exception {
        int idx = header.indexOf(":");
        if (idx == -1) {
            throw new Exception("Invalid Header Parameter: " + header);
        }
        this.requestHeaders.put(header.substring(0, idx), header.substring(idx + 2));
    }

    /**
     * Get a response header value by key.
     *
     * @param obj is a variable containing the response header's key.
     * @return String
     * @throws Exception will be thrown when the given obj wasn't found in the request headers variable.
     */
    public String getRequestHeaderObj(String obj) throws Exception {
        String temp = this.requestHeaders.get(obj);
        if (temp == null) {
            throw new Exception("Bad response header, " + obj + " not found.");
        }
        return temp;
    }

    /**
     * Get a response header value by key, but wont throw any exception when the given key isn't found.
     *
     * @param obj is a variable containing the response header's key.
     * @param notStrict is variable that exists just to override the previous method definition.
     * @return String
     */
    public String getRequestHeaderObj(String obj, boolean notStrict) {
        return this.requestHeaders.get(obj);
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
