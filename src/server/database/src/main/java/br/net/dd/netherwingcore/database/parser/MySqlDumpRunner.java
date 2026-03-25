package br.net.dd.netherwingcore.database.parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.util.*;

/**
 * MySqlDumpRunner is a utility class designed to execute SQL dump files against a MySQL database connection.
 * It reads SQL statements from a specified dump file, executes them in batches, and provides progress reporting.
 * The class handles various SQL statement types, including comments and delimiters, and ensures proper transaction management.
 */
public final class MySqlDumpRunner {

    /** Main method to run the SQL dump file against the provided database connection.
     * It reads the dump file, executes the SQL statements, and reports progress.
     *
     * @param conn     the Connection object representing the database connection
     * @param dumpFile the Path object representing the location of the SQL dump file
     * @throws IOException  if there is an error reading the dump file
     * @throws SQLException if there is an error executing the SQL statements
     */
    public static void runSqlDump(Connection conn, Path dumpFile) throws IOException, SQLException {
        long totalBytes = Files.size(dumpFile);

        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        long startNanos = System.nanoTime();

        // IntelliJ typically does not provide a "real console" (System.console() == null).
        boolean interactiveTerminal = (System.console() != null);
        ProgressReporter progress = new ProgressReporter(totalBytes, startNanos, interactiveTerminal);

        try (InputStream raw = Files.newInputStream(dumpFile);
             CountingInputStream counting = new CountingInputStream(raw);
             Reader r = new BufferedReader(new InputStreamReader(counting, StandardCharsets.UTF_8));
             Statement st = conn.createStatement()) {

            st.execute("SET FOREIGN_KEY_CHECKS=0");
            st.execute("SET UNIQUE_CHECKS=0");

            SqlStatementIterator it = new SqlStatementIterator(r);

            int statements = 0;

            while (it.hasNext()) {
                String sql = it.next();
                if (sql == null) continue;

                sql = sql.trim();
                if (sql.isEmpty()) continue;

                try {
                    st.execute(sql);
                } catch (SQLException e) {
                    System.err.println("FAILED SQL STATEMENT:\n" + sql + "\n---");
                    throw e;
                }

                statements++;

                if (statements % 200 == 0) conn.commit();

                progress.maybePrint(counting.getBytesRead(), statements);
            }

            st.execute("SET UNIQUE_CHECKS=1");
            st.execute("SET FOREIGN_KEY_CHECKS=1");

            conn.commit();

            progress.printFinal(counting.getBytesRead(), statements);

        } catch (SQLException | IOException e) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    // ---------------- Progress ----------------

    static final class ProgressReporter {
        private final long totalBytes;
        private final long startNanos;
        private final boolean interactiveTerminal;

        // Adjust the frequency here.
        private final long minIntervalNanos = Duration.ofSeconds(1).toNanos();

        private long lastPrintNanos;

        ProgressReporter(long totalBytes, long startNanos, boolean interactiveTerminal) {
            this.totalBytes = totalBytes;
            this.startNanos = startNanos;
            this.interactiveTerminal = interactiveTerminal;
            this.lastPrintNanos = startNanos;
        }

        /** Prints the progress of the SQL dump execution. It calculates the percentage of bytes read, the speed of execution, and the estimated time remaining.
         * The method ensures that progress is printed at most once per specified interval to avoid excessive output.
         *
         * @param bytesRead  the number of bytes read from the dump file so far
         * @param statements the number of SQL statements executed so far
         */
        void maybePrint(long bytesRead, int statements) {
            long now = System.nanoTime();
            if (now - lastPrintNanos < minIntervalNanos) return;
            lastPrintNanos = now;

            String line = formatLine(bytesRead, statements, now);

            if (interactiveTerminal) {
                // Overwrites the same line (great in Linux terminal and real cmd/powershell)
                System.out.print("\r" + line);
            } else {
                // IntelliJ/CI: prints a new line (more readable)
                System.out.println(line);
            }
        }

        /** Prints the final progress line when the SQL dump execution is complete. It provides a summary of the total bytes read, the number of statements executed, and the total time taken.
         *
         * @param bytesRead  the total number of bytes read from the dump file
         * @param statements the total number of SQL statements executed
         */
        void printFinal(long bytesRead, int statements) {
            String line = formatLine(bytesRead, statements, System.nanoTime());
            if (interactiveTerminal) {
                System.out.print("\r" + line);
                System.out.println();
            } else {
                System.out.println(line);
            }
        }

        /** Formats a progress line with the percentage of bytes read, the amount of data processed, the number of statements executed, the speed of execution, the elapsed time, and the estimated time remaining.
         *
         * @param bytesRead  the number of bytes read from the dump file so far
         * @param statements the number of SQL statements executed so far
         * @param nowNanos   the current time in nanoseconds for calculating elapsed time and speed
         * @return a formatted string representing the current progress of the SQL dump execution
         */
        private String formatLine(long bytesRead, int statements, long nowNanos) {
            double pct = totalBytes <= 0 ? 0.0 : (bytesRead * 100.0 / totalBytes);

            double elapsedSec = (nowNanos - startNanos) / 1_000_000_000.0;
            double mbRead = bytesRead / (1024.0 * 1024.0);
            double mbTotal = totalBytes / (1024.0 * 1024.0);
            double mbPerSec = elapsedSec <= 0 ? 0.0 : (mbRead / elapsedSec);

            // ETA simples (pode oscilar um pouco no começo)
            double remainingMb = Math.max(0.0, mbTotal - mbRead);
            double etaSec = mbPerSec <= 0 ? -1 : (remainingMb / mbPerSec);

            String eta = (etaSec < 0) ? "?" : formatDuration(etaSec);

            return String.format(
                    Locale.ROOT,
                    "Progress: %6.2f%% (%.2f / %.2f MB)  Statements: %d  Speed: %.2f MB/s  Elapsed: %s  ETA: %s",
                    pct, mbRead, mbTotal, statements, mbPerSec, formatDuration(elapsedSec), eta
            );
        }

        /** Formata uma duração em segundos para um formato legível, como "1h23m45s" ou "12m34s".
         *
         * @param secondsD a duração em segundos a ser formatada
         * @return uma string representando a duração formatada
         */
        private static String formatDuration(double secondsD) {
            long s = Math.max(0, (long) secondsD);
            long h = s / 3600;
            long m = (s % 3600) / 60;
            long sec = s % 60;

            if (h > 0) return String.format(Locale.ROOT, "%dh%02dm%02ds", h, m, sec);
            if (m > 0) return String.format(Locale.ROOT, "%dm%02ds", m, sec);
            return String.format(Locale.ROOT, "%ds", sec);
        }
    }

    // ---------------- CountingInputStream ----------------

    static final class CountingInputStream extends FilterInputStream {
        private long bytesRead;

        CountingInputStream(InputStream in) { super(in); }

        long getBytesRead() { return bytesRead; }

        @Override public int read() throws IOException {
            int b = super.read();
            if (b != -1) bytesRead++;
            return b;
        }

        @Override public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n > 0) bytesRead += n;
            return n;
        }
    }

    // ---------------- SQL parser (unchanged) ----------------

    static final class SqlStatementIterator implements Iterator<String> {
        private final Reader reader;
        private final StringBuilder current = new StringBuilder();
        private final Deque<Character> pushback = new ArrayDeque<>();
        private String next;

        private String delimiter = ";";

        private boolean inSingleQuote = false;
        private boolean inDoubleQuote = false;
        private boolean inBacktick = false;
        private boolean inLineComment = false;
        private boolean inBlockComment = false;

        SqlStatementIterator(Reader reader) {
            this.reader = reader;
            this.next = fetchNext();
        }

        /**
         * Checks if there are more SQL statements to read from the input.
         *
         * @return true if there is another SQL statement available, false otherwise
         */
        @Override public boolean hasNext() { return next != null; }

        /**
         * Retrieves the next SQL statement from the input. If there are no more statements, it throws a NoSuchElementException.
         *
         * @return the next SQL statement as a String
         * @throws NoSuchElementException if there are no more SQL statements to read
         */
        @Override public String next() {
            if (next == null) throw new NoSuchElementException();
            String out = next;
            next = fetchNext();
            return out;
        }

        /**
         * Fetches the next SQL statement from the input, handling comments, string literals, and delimiters appropriately.
         * It reads characters one by one, building the current statement until it encounters a complete statement based on the delimiter.
         *
         * @return the next SQL statement as a String, or null if there are no more statements to read
         */
        private String fetchNext() {
            current.setLength(0);

            try {
                int ch;
                while ((ch = read()) != -1) {
                    char c = (char) ch;

                    if (inLineComment) {
                        if (c == '\n') inLineComment = false;
                        continue;
                    }

                    if (inBlockComment) {
                        if (c == '*' && peek() == '/') {
                            read();
                            inBlockComment = false;
                        }
                        continue;
                    }

                    if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                        if (c == '-' && peek() == '-') {
                            read();
                            int p = peek();
                            if (p == ' ' || p == '\t' || p == '\r' || p == '\n') {
                                inLineComment = true;
                                continue;
                            } else {
                                current.append('-').append('-');
                                continue;
                            }
                        }

                        if (c == '#') {
                            inLineComment = true;
                            continue;
                        }

                        if (c == '/' && peek() == '*') {
                            read();
                            inBlockComment = true;
                            continue;
                        }
                    }

                    if (!inDoubleQuote && !inBacktick && c == '\'') {
                        if (!inSingleQuote) inSingleQuote = true;
                        else if (!isEscaped(current)) inSingleQuote = false;
                        current.append(c);
                        continue;
                    }
                    if (!inSingleQuote && !inBacktick && c == '"') {
                        if (!inDoubleQuote) inDoubleQuote = true;
                        else if (!isEscaped(current)) inDoubleQuote = false;
                        current.append(c);
                        continue;
                    }
                    if (!inSingleQuote && !inDoubleQuote && c == '`') {
                        inBacktick = !inBacktick;
                        current.append(c);
                        continue;
                    }

                    current.append(c);

                    if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                        if (endsWithDelimiter(current, delimiter)) {
                            int newLen = current.length() - delimiter.length();
                            String stmt = current.substring(0, newLen).trim();

                            String upper = stmt.toUpperCase(Locale.ROOT);
                            if (upper.startsWith("DELIMITER")) {
                                String[] parts = stmt.split("\\s+");
                                if (parts.length >= 2) delimiter = parts[1];
                                current.setLength(0);
                                continue;
                            }

                            if (!stmt.isEmpty()) return stmt;
                            current.setLength(0);
                        }
                    }
                }

                String tail = current.toString().trim();
                if (!tail.isEmpty()) return tail;
                return null;

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /** Verifica se o StringBuilder termina com o delimitador especificado.
         *
         * @param sb        o StringBuilder a ser verificado
         * @param delimiter o delimitador a ser comparado
         * @return true se o StringBuilder termina com o delimitador, false caso contrário
         */
        private static boolean endsWithDelimiter(StringBuilder sb, String delimiter) {
            int n = sb.length();
            int d = delimiter.length();
            if (n < d) return false;
            for (int i = 0; i < d; i++) {
                if (sb.charAt(n - d + i) != delimiter.charAt(i)) return false;
            }
            return true;
        }

        /** Verifica se o último caractere do StringBuilder é um caractere de escape (barra invertida) que não está escapado por outro caractere de escape.
         *
         * @param sb o StringBuilder a ser verificado
         * @return true se o último caractere é um caractere de escape não escapado, false caso contrário
         */
        private static boolean isEscaped(StringBuilder sb) {
            int i = sb.length() - 2;
            int backslashes = 0;
            while (i >= 0 && sb.charAt(i) == '\\') {
                backslashes++;
                i--;
            }
            return (backslashes % 2) == 1;
        }

        /** Lê o próximo caractere do Reader, considerando os caracteres de pushback. Se houver caracteres no pushback, retorna o próximo deles; caso contrário, lê do Reader.
         *
         * @return o próximo caractere lido como um inteiro, ou -1 se o final do stream for alcançado
         * @throws IOException se ocorrer um erro de leitura
         */
        private int read() throws IOException {
            if (!pushback.isEmpty()) return pushback.removeFirst();
            return reader.read();
        }

        /** Lê o próximo caractere do Reader sem removê-lo do fluxo de leitura. Se houver caracteres no pushback, retorna o próximo deles; caso contrário, lê do Reader e o adiciona ao pushback para que possa ser lido novamente posteriormente.
         *
         * @return o próximo caractere lido como um inteiro, ou -1 se o final do stream for alcançado
         * @throws IOException se ocorrer um erro de leitura
         */
        private int peek() throws IOException {
            int c = read();
            if (c != -1) pushback.addFirst((char) c);
            return c;
        }
    }
}
