package com.ww.test;

import com.ww.DownloadFiles;
import java.io.File;

public class DownloadFilesTest {
    /**
     * Assert that download files feature works as intended.
     *
     * @param args default args
     */
    public static void main(String[] args) {
        File dir = new File(".\\demo-progjar-1\\downloads");
        deleteDir(dir);

        String[] links = {
            "https://www.industrialempathy.com/img/remote/ZiClJf-1920w.jpg",
            "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg",
            "https://www.its.ac.id/informatika/wp-content/uploads/sites/44/2020/04/Appendix-5.6-BIP-Module-Handbook_en.pdf",
            "https://0.gravatar.com/avatar/3fb72052b43cc3f1467beaae56ebfed7?s=96&d=mm&r=g",
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
