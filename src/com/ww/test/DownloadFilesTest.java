package com.ww.test;

import com.ww.DownloadFiles;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

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

        assert !isEmptyDir(dir) : "Downloads directory is empty.";
        System.out.println("Download files test success");
    }

    /**
     * Delete directory recursively
     *
     * @param dir
     */
    private static void deleteDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                deleteDir(file);
            }
        }
        dir.delete();
    }

    /**
     * Assert whether the given directory's path is empty or not.
     *
     * @param dir  directory
     * @return bool
     */
    private static boolean isEmptyDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir.toPath())) {
                return !directoryStream.iterator().hasNext();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
