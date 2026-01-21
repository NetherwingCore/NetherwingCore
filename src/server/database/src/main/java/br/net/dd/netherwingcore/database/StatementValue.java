package br.net.dd.netherwingcore.database;

/**
 * Represents a database statement with its associated query string and connection flag.
 * This record is a simple and immutable data holder for database-related operations.
 *
 * @param query          The SQL query string that represents the database statement.
 * @param connectionFlag The flag indicating the type of database connection required for execution.
 */
public record StatementValue(String query, ConnectionFlag connectionFlag) {
}
