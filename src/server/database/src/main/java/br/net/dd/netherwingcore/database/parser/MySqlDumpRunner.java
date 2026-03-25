package br.net.dd.netherwingcore.database.parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * MySqlDumpRunner is a utility class designed to execute SQL dump files against a MySQL database connection.
 * It reads SQL statements from a specified dump file, handles comments and string literals correctly,
 * and executes the statements while managing transactions and error handling.
 *
 * The class includes an inner static class, SqlStatementIterator, which is responsible for parsing the SQL dump file
 * and iterating over individual SQL statements, taking into account MySQL-specific syntax such as comments,
 * string literals, and custom delimiters.
 */
public final class MySqlDumpRunner {

    /**
     * Executes the SQL statements contained in the specified dump file against the provided database connection.
     * The method manages transactions, disables foreign key and unique checks for performance, and ensures
     * that any errors during execution are properly handled with rollbacks.
     *
     * @param conn     the Connection object representing the database connection to execute the SQL statements on
     * @param dumpFile the Path object representing the location of the SQL dump file to be executed
     * @throws IOException  if there is an error reading the dump file
     * @throws SQLException if there is an error executing any of the SQL statements
     */
    public static void runSqlDump(Connection conn, Path dumpFile) throws IOException, SQLException {
        conn.setAutoCommit(false);

        try (Reader r = Files.newBufferedReader(dumpFile, StandardCharsets.UTF_8);
             Statement st = conn.createStatement()) {

            // IMPORTANT: do not rely on /*!...*/ being executed. Force it here.
            st.execute("SET FOREIGN_KEY_CHECKS=0");
            st.execute("SET UNIQUE_CHECKS=0");

            // Optional: you can set SQL_MODE too if your dump expects it
            // st.execute("SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO'");

            SqlStatementIterator it = new SqlStatementIterator(r);

            int count = 0;
            while (it.hasNext()) {
                String sql = it.next();
                if (sql == null) continue;

                sql = sql.trim();
                if (sql.isEmpty()) continue;

                // Optional: skip mysql-client-only noise if you want
                // if (sql.startsWith("LOCK TABLES") || sql.startsWith("UNLOCK TABLES")) continue;

                try {
                    st.execute(sql);
                } catch (SQLException e) {
                    // Print the exact statement that failed to help debugging
                    System.err.println("FAILED SQL STATEMENT:\n" + sql + "\n---");
                    throw e;
                }

                count++;

                // incremental commits for large dumps
                if (count % 200 == 0) {
                    conn.commit();
                }
            }

            // Re-enable checks
            st.execute("SET UNIQUE_CHECKS=1");
            st.execute("SET FOREIGN_KEY_CHECKS=1");

            conn.commit();
        } catch (SQLException | IOException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * SqlStatementIterator is an iterator that reads from a Reader and yields complete SQL statements one at a time.
     * It correctly handles MySQL-specific syntax, including:
     * - Single quotes ('...') for string literals
     * - Double quotes ("...") for identifiers (if ANSI_QUOTES is enabled)
     * - Backticks (`...`) for identifiers
     * - Line comments starting with -- or #
     * - Block comments enclosed in /* ... *\/
     * - Custom delimiters specified by DELIMITER statements
     *
     * The iterator ensures that it does not split statements on delimiters that are inside string literals or comments.
     */
    static final class SqlStatementIterator implements Iterator<String> {
        private final Reader reader;
        private final StringBuilder current = new StringBuilder();
        private final Deque<Character> pushback = new ArrayDeque<>();
        private String next;

        private String delimiter = ";";

        // Estados
        private boolean inSingleQuote = false;
        private boolean inDoubleQuote = false;
        private boolean inBacktick = false;
        private boolean inLineComment = false;  // -- ... \n  ou # ... \n
        private boolean inBlockComment = false; // /* ... */

        SqlStatementIterator(Reader reader) {
            this.reader = reader;
            this.next = fetchNext();
        }

        /**
         * Checks if there are more SQL statements to be read from the reader.
         *
         * @return true if there is at least one more SQL statement available, false otherwise
         */
        @Override public boolean hasNext() { return next != null; }

        /**
         * Returns the next complete SQL statement from the reader. If there are no more statements, it throws a NoSuchElementException.
         * The method also prepares the next statement for subsequent calls by fetching it immediately after returning the current one.
         *
         * @return the next complete SQL statement as a String
         * @throws NoSuchElementException if there are no more SQL statements to read
         */
        @Override public String next() {
            if (next == null) throw new NoSuchElementException();
            String out = next;
            next = fetchNext();
            return out;
        }

        /**
         * Reads characters from the reader until it constructs a complete SQL statement based on the current delimiter.
         * It handles comments and string literals to ensure that delimiters inside them do not prematurely end the statement.
         * It also processes DELIMITER statements to change the statement boundary as needed.
         *
         * @return the next complete SQL statement, or null if there are no more statements
         */
        private String fetchNext() {
            current.setLength(0);

            try {
                int ch;
                while ((ch = read()) != -1) {
                    char c = (char) ch;

                    // Handle end of line comment
                    if (inLineComment) {
                        if (c == '\n') inLineComment = false;
                        continue;
                    }

                    // Handle block comment
                    if (inBlockComment) {
                        if (c == '*' && peek() == '/') {
                            read(); // consume '/'
                            inBlockComment = false;
                        }
                        continue;
                    }

                    // Detect comment start (only when not inside quotes/identifiers)
                    if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                        // -- comment (must be "-- " or "--\t" in MySQL dumps typically)
                        if (c == '-' && peek() == '-') {
                            read(); // second '-'
                            char p = (char) peek();
                            if (p == ' ' || p == '\t' || p == '\r' || p == '\n') {
                                inLineComment = true;
                                continue;
                            } else {
                                // not a comment, keep "--"
                                current.append('-').append('-');
                                continue;
                            }
                        }
                        // # comment
                        if (c == '#') {
                            inLineComment = true;
                            continue;
                        }
                        // /* block comment */
                        if (c == '/' && peek() == '*') {
                            read(); // '*'
                            inBlockComment = true;
                            continue;
                        }
                    }

                    // Track quote states (respect escaping in strings)
                    if (!inDoubleQuote && !inBacktick && c == '\'' ) {
                        if (!inSingleQuote) {
                            inSingleQuote = true;
                        } else if (!isEscaped(current)) {
                            inSingleQuote = false;
                        }
                        current.append(c);
                        continue;
                    }
                    if (!inSingleQuote && !inBacktick && c == '"') {
                        if (!inDoubleQuote) {
                            inDoubleQuote = true;
                        } else if (!isEscaped(current)) {
                            inDoubleQuote = false;
                        }
                        current.append(c);
                        continue;
                    }
                    if (!inSingleQuote && !inDoubleQuote && c == '`') {
                        inBacktick = !inBacktick;
                        current.append(c);
                        continue;
                    }

                    current.append(c);

                    // Statement boundary detection (delimiter) only when not in quotes/comments
                    if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                        if (endsWithDelimiter(current, delimiter)) {
                            // remove delimiter from end
                            int newLen = current.length() - delimiter.length();
                            String stmt = current.substring(0, newLen).trim();

                            // DELIMITER handling (MySQL client syntax)
                            String upper = stmt.toUpperCase(Locale.ROOT);
                            if (upper.startsWith("DELIMITER")) {
                                String[] parts = stmt.split("\\s+");
                                if (parts.length >= 2) delimiter = parts[1];
                                current.setLength(0);
                                continue; // fetch next statement
                            }

                            if (!stmt.isEmpty()) return stmt;

                            current.setLength(0);
                        }
                    }
                }

                // EOF: return remaining
                String tail = current.toString().trim();
                if (!tail.isEmpty()) return tail;

                return null;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Checks if the StringBuilder ends with the specified delimiter.
         * This is used to determine if a complete SQL statement has been read based on the current delimiter.
         *
         * @param sb        the StringBuilder containing the current SQL statement being constructed
         * @param delimiter the current statement delimiter (e.g., ";" or custom)
         * @return true if the StringBuilder ends with the delimiter, false otherwise
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

        /**
         * Checks if the last quote character in the StringBuilder is escaped by an odd number of backslashes.
         * This is important for correctly determining the end of string literals in SQL statements, as an escaped quote does not end the string.
         *
         * @param sb the StringBuilder containing the current SQL statement being constructed
         * @return true if the last quote character is escaped, false otherwise
         */
        private static boolean isEscaped(StringBuilder sb) {
            // Checks if the last quote char is escaped by odd number of backslashes
            int i = sb.length() - 2; // char before current quote we just appended
            int backslashes = 0;
            while (i >= 0 && sb.charAt(i) == '\\') {
                backslashes++;
                i--;
            }
            return (backslashes % 2) == 1;
        }

        /**
         * Reads the next character from the reader, taking into account any pushback characters that have been added.
         * If there are characters in the pushback deque, it returns the next one from there; otherwise, it reads from the underlying reader.
         *
         * @return the next character as an integer, or -1 if the end of the stream has been reached
         * @throws IOException if an I/O error occurs while reading from the reader
         */
        private int read() throws IOException {
            if (!pushback.isEmpty()) return pushback.removeFirst();
            return reader.read();
        }

        /**
         * Peeks at the next character without consuming it. If there is a character available, it is added back to the pushback deque so that it can be read again by the next call to read().
         *
         * @return the next character as an integer, or -1 if the end of the stream has been reached
         * @throws IOException if an I/O error occurs while reading from the reader
         */
        private int peek() throws IOException {
            int c = read();
            if (c != -1) pushback.addFirst((char) c);
            return c;
        }
    }

}
