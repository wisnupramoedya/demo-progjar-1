package com.ww.test;

import com.ww.DownloadFiles;
import java.io.*;

public class DownloadFilesTest {
    /**
     * Assert that download files feature works as intended.
     *
     * @param args default args
     */
    public static void main(String[] args) {
        File dir = new File(".\\downloads");
        deleteDir(dir);

        String[] links = {
            "https://bit.ly/3MgmxVu",
            "http://www.cs.kent.edu/~svirdi/Ebook/wdp/ch01.pdf",
            "http://www.philippine-embassy.org.sg/wp-content/uploads/274183812_317773887042213_5615031951159088216_n.jpg",
            "http://intronetworks.cs.luc.edu/current2/ComputerNetworks.pdf",
        };
        DownloadFiles.downloadFiles(links);

        assert !isEmptyDir(dir) : "No file is downloaded.";
        System.out.println("Download files test success");
    }

    /**
     * Delete directory recursively
     *
     * @param dir directory path that want to be deleted
     */
    private static void deleteDir(File dir) {
        if (!dir.exists()) return;
        if (!dir.isDirectory() && dir.isFile()) {
            dir.delete();
            return;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                deleteDir(file);
            }
        }
        dir.delete();
    }

    /**
     * Assert whether the given directory's path has any file in it or not.
     *
     * @param dir  directory
     * @return bool
     */
    private static boolean isEmptyDir(File dir) {
        if (!dir.exists()) return true;
        if (!dir.isDirectory() && dir.isFile()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null) return true;

        for (final File file : files) {
            if (!isEmptyDir(file)) return false;
        }
        return true;
    }
}
