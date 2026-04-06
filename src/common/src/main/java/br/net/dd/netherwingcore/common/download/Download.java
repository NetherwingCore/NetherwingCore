package br.net.dd.netherwingcore.common.download;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Download {

    public static void printProgress(String prefix, long current, long total) {
        int barLength = 50;
        double percent = (double) current / total;
        int filled = (int) (percent * barLength);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < filled) bar.append('#');
            else bar.append('-');
        }

        int percentInt = (int) (percent * 100);
        System.out.print("\r" + prefix + " [" + bar + "] " + percentInt + "%");
    }

    public static Path downloadFile(String fileUrl, String destination) throws IOException {
        URL url = new URL(fileUrl);
        URLConnection conn = url.openConnection();
        int contentLength = conn.getContentLength();

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                if (contentLength > 0) {
                    printProgress("Download", totalRead, contentLength);
                } else {
                    System.out.print("\rDownload: " + totalRead + " bytes");
                }
            }
        }
        System.out.println("\nDownload complete!");
        return Paths.get(destination);
    }

    public static void extract(Path sourceFile, Path targetDir) throws Exception {
        String fileName = sourceFile.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".7z")) {
            extract7z(sourceFile, targetDir);
            return;
        }

        long totalSize = sourceFile.toFile().length();
        long processed = 0;

        try (InputStream fi = Files.newInputStream(sourceFile);
             BufferedInputStream bi = new BufferedInputStream(fi)) {

            // Tenta como compressor (gzip, bzip2, xz, etc.)
            try {
                CompressorInputStream ci = new CompressorStreamFactory().createCompressorInputStream(bi);
                Path outFile = targetDir.resolve("unpacked");
                Files.createDirectories(outFile.getParent());
                try (OutputStream out = Files.newOutputStream(outFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = ci.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        processed += len;
                        printProgress("Extracting", processed, totalSize);
                    }
                }
                System.out.println("\nExtraction complete!");
                return;
            } catch (Exception ignored) {
                // Não era compressor, tenta como arquivo (zip, tar, etc.)
            }

            try (ArchiveInputStream ai = new ArchiveStreamFactory().createArchiveInputStream(bi)) {
                ArchiveEntry entry;
                while ((entry = ai.getNextEntry()) != null) {
                    Path newFile = targetDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        Files.createDirectories(newFile.getParent());
                        try (OutputStream out = Files.newOutputStream(newFile)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = ai.read(buffer)) != -1) {
                                out.write(buffer, 0, len);
                                processed += len;
                                printProgress("Extracting", processed, totalSize);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("\nExtraction complete!");
    }

    public static void extract7z(Path sourceFile, Path targetDir) throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(sourceFile.toFile())) {
            SevenZArchiveEntry entry;
            long totalSize = sourceFile.toFile().length();
            long processed = 0;

            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                Path newFile = targetDir.resolve(entry.getName());
                Files.createDirectories(newFile.getParent());

                try (OutputStream out = Files.newOutputStream(newFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = sevenZFile.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead);
                        processed += bytesRead;
                        printProgress("Extracting", processed, totalSize);
                    }
                }
            }
        }
        System.out.println("\nExtraction complete!");
    }

}
