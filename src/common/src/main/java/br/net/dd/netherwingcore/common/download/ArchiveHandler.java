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
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * Utility class for downloading files and extracting archives with progress display.
 * Handles various archive formats supported by Apache Commons Compress, including .zip, .tar.gz, .7z, etc.
 * Provides robust handling of HTTP redirects and content-type heuristics to avoid saving HTML error pages as files.
 */
public class ArchiveHandler {

    /*
     * Prints a progress bar to the console. If total is known, shows percentage and a bar; otherwise shows bytes downloaded.
     * Uses carriage return to update the same line. Caller should print a newline after completion.
     *
     * @param prefix  A label to show before the progress (e.g. "Download" or "Extracting")
     * @param current The current progress value (e.g. bytes downloaded or extracted)
     * @param total   The total value for completion (e.g. total bytes to download or extract). If <= 0, total is unknown and only current will be shown.
     */
    public static void printProgress(String prefix, long current, long total) {
        int barLength = 50;
        if (total <= 0) {
            System.out.print("\r" + prefix + ": " + current + " bytes");
            return;
        }

        double rawPercent = (double) current / total;
        double percent = Math.max(0.0, Math.min(1.0, rawPercent)); // clamp 0..1
        int filled = (int) (percent * barLength);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < filled) bar.append('#');
            else bar.append('-');
        }

        int percentInt = (int) (percent * 100);
        System.out.print("\r" + prefix + " [" + bar + "] " + percentInt + "%");
    }

    /*
     * Downloads a file from the given URL to the specified destination path, following redirects and showing progress.
     * Heuristically checks if the downloaded content looks like an HTML page (e.g. error page) and warns the user.
     *
     * @param fileUrl     The URL of the file to download
     * @param destination The local file path to save the downloaded content
     * @return The Path to the downloaded file
     * @throws IOException If an I/O error occurs during downloading
     */
    public static Path downloadFile(String fileUrl, String destination) throws IOException {
        URL url = new URL(fileUrl);
        int maxRedirects = 5;
        int redirects = 0;
        HttpURLConnection conn = null;

        while (true) {
            conn = (HttpURLConnection) url.openConnection();
            // Prevent the server from sending compressed content that would make content-length unreliable
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java) NetherwingCore Downloader");
            conn.setInstanceFollowRedirects(false); // we'll handle redirects manually
            conn.connect();

            int status = conn.getResponseCode();
            if (status >= 300 && status < 400) {
                if (redirects++ >= maxRedirects) {
                    throw new IOException("Too many redirects when trying to download: " + fileUrl);
                }
                String location = conn.getHeaderField("Location");
                if (location == null) {
                    throw new IOException("Redirect with no Location header for: " + url);
                }
                url = new URL(url, location); // handle relative redirects
                conn.disconnect();
                continue;
            }
            break;
        }

        long contentLength = conn.getContentLengthLong();
        String contentType = conn.getContentType();
        System.out.println("Downloading from: " + conn.getURL());

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
            System.out.println();

            // Heuristic: if Content-Type is text/html or the downloaded file is small and starts with '<', warn user
            boolean looksLikeHtml = false;
            if (contentType != null && contentType.toLowerCase().contains("text/html")) {
                looksLikeHtml = true;
            } else if (totalRead < 1024 * 1024) { // under 1MB, peek into file start
                try (RandomAccessFile raf = new RandomAccessFile(destination, "r")) {
                    byte[] peek = new byte[256];
                    int read = raf.read(peek);
                    if (read > 0) {
                        String start = new String(peek, 0, read, java.nio.charset.StandardCharsets.UTF_8).trim().toLowerCase();
                        if (start.startsWith("<!doctype") || start.startsWith("<html") || start.contains("<script") || start.contains("404") || start.contains("error")) {
                            looksLikeHtml = true;
                        }
                    }
                } catch (IOException ignored) {
                }
            }

            if (looksLikeHtml) {
                System.err.println("Warning: downloaded content looks like an HTML page rather than the expected binary file. The URL may point to an HTML page (login/error) or a redirect landing page.");
            }

            System.out.println("Download complete! Total bytes: " + totalRead);
        } finally {
            if (conn != null) conn.disconnect();
        }

        return Paths.get(destination);
    }

    /*
     * Extracts an archive file (e.g. .zip, .tar.gz, .7z) to the specified target directory, showing progress.
     * Handles various formats supported by Apache Commons Compress. For .7z files, uses SevenZFile for better support.
     *
     * @param sourceFile The path to the archive file to extract
     * @param targetDir  The directory to extract the contents into
     * @throws Exception If an error occurs during extraction
     */
    public static void extract(Path sourceFile, Path targetDir) throws Exception {
        String fileName = sourceFile.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".7z")) {
            extract7z(sourceFile, targetDir);
            return;
        }

        // We'll try three strategies for progress:
        // 1) If archive entries expose uncompressed sizes, sum them and show progress based on uncompressed bytes (ideal)
        // 2) Otherwise, fall back to showing progress based on compressed input bytes (counting underlying file reads)
        // 3) For simple compressor streams (single member like .gz/.xz) show progress based on compressed bytes

        long compressedTotal = sourceFile.toFile().length();

        // First, try compressor (single-file compressed like .gz, .xz)
        CountingInputStream cIn = new CountingInputStream(Files.newInputStream(sourceFile));
        BufferedInputStream bIn = new BufferedInputStream(cIn);
        try {
            CompressorInputStream ci = new CompressorStreamFactory().createCompressorInputStream(bIn);
            Path outFile = targetDir.resolve("unpacked");
            Files.createDirectories(outFile.getParent());
            try (OutputStream out = Files.newOutputStream(outFile); CompressorInputStream cis = ci) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = cis.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    // show progress based on compressed bytes read so we don't exceed 100%
                    printProgress("Extracting", cIn.getCount(), compressedTotal);
                }
            }
            System.out.println("\nExtraction complete!");
            return;
        } catch (Exception ignored) {
            // not a single-file compressor; close streams and try archive handling
            try { bIn.close(); } catch (IOException e) { /* ignore */ }
            try { cIn.close(); } catch (IOException e) { /* ignore */ }
        }

        // Next, attempt archive handling. First pass: try to compute total uncompressed size by scanning entries.
        long totalUncompressed = 0;
        boolean haveUncompressedTotal = false;
        try (InputStream fi2 = Files.newInputStream(sourceFile);
             BufferedInputStream bi2 = new BufferedInputStream(fi2);
             ArchiveInputStream ai2 = new ArchiveStreamFactory().createArchiveInputStream(bi2)) {
            ArchiveEntry entry;
            while ((entry = ai2.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    long s = entry.getSize();
                    if (s > 0) totalUncompressed += s;
                }
            }
            if (totalUncompressed > 0) haveUncompressedTotal = true;
        } catch (Exception ignored) {
            // scanning failed; we'll fall back to compressed-based progress below
        }

        if (haveUncompressedTotal) {
            long processed = 0;
            try (InputStream fi3 = Files.newInputStream(sourceFile);
                 BufferedInputStream bi3 = new BufferedInputStream(fi3);
                 ArchiveInputStream ai3 = new ArchiveStreamFactory().createArchiveInputStream(bi3)) {
                ArchiveEntry entry;
                while ((entry = ai3.getNextEntry()) != null) {
                    Path newFile = targetDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        Files.createDirectories(newFile.getParent());
                        try (OutputStream out = Files.newOutputStream(newFile)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = ai3.read(buffer)) != -1) {
                                out.write(buffer, 0, len);
                                processed += len;
                                printProgress("Extracting", processed, totalUncompressed);
                            }
                        }
                    }
                }
            }
            System.out.println("\nExtraction complete!");
            return;
        }

        // Fallback: show progress based on compressed bytes read (so percentage won't exceed 100%)
        CountingInputStream cIn2 = new CountingInputStream(Files.newInputStream(sourceFile));
        try (BufferedInputStream bi4 = new BufferedInputStream(cIn2);
             ArchiveInputStream ai4 = new ArchiveStreamFactory().createArchiveInputStream(bi4)) {
            ArchiveEntry entry;
            while ((entry = ai4.getNextEntry()) != null) {
                Path newFile = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    try (OutputStream out = Files.newOutputStream(newFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = ai4.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                            // show progress based on compressed bytes read
                            printProgress("Extracting", cIn2.getCount(), compressedTotal);
                        }
                    }
                }
            }
        }
        System.out.println("\nExtraction complete!");
    }

    /*
     * Specialized extraction method for .7z files using SevenZFile, which provides better support for 7z archives.
     * Similar progress strategy: first try to compute total uncompressed size; if not available, fall back to compressed-based progress.
     *
     * @param sourceFile The path to the .7z archive file
     * @param targetDir  The directory to extract the contents into
     * @throws IOException If an I/O error occurs during extraction
     */
    public static void extract7z(Path sourceFile, Path targetDir) throws IOException {
        // First pass: sum entry sizes when available to compute uncompressed total
        long totalUncompressed = 0;
        try (SevenZFile zCount = new SevenZFile(sourceFile.toFile())) {
            SevenZArchiveEntry e;
            while ((e = zCount.getNextEntry()) != null) {
                if (!e.isDirectory()) {
                    long s = e.getSize();
                    if (s > 0) totalUncompressed += s;
                }
            }
        } catch (Exception ignored) {
        }

        if (totalUncompressed > 0) {
            long processed = 0;
            try (SevenZFile sevenZFile = new SevenZFile(sourceFile.toFile())) {
                SevenZArchiveEntry entry;
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
                            printProgress("Extracting", processed, totalUncompressed);
                        }
                    }
                }
            }
            System.out.println("\nExtraction complete!");
            return;
        }

        // Fallback: unknown uncompressed total — show progress based on compressed file size
        long compressedTotal = sourceFile.toFile().length();
        long processed = 0;
        try (SevenZFile sevenZFile = new SevenZFile(sourceFile.toFile())) {
            SevenZArchiveEntry entry;
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
                        // This may exceed compressedTotal but we cannot do better; cap display at compressedTotal
                        printProgress("Extracting", Math.min(processed, compressedTotal), compressedTotal);
                    }
                }
            }
        }
        System.out.println("\nExtraction complete!");
    }

    /*
     * A simple FilterInputStream that counts the number of bytes read through it. Used to track progress based on compressed bytes.
     */
    private static class CountingInputStream extends FilterInputStream {
        private long count = 0;

        protected CountingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b != -1) count++;
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int r = in.read(b, off, len);
            if (r > 0) count += r;
            return r;
        }

        public long getCount() {
            return count;
        }
    }

}
