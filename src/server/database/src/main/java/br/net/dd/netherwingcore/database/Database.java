package br.net.dd.netherwingcore.database;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract class that represents a database handler. This class is responsible for managing
 * SQL statements and their associated metadata (e.g., connection flags). Subclasses should
 * implement the {@link #loadStatements()} method to define the SQL statements they need.
 *
 * <p>The class provides mechanisms to add SQL statements to a collection and retrieve them
 * by their unique statement names.
 */
public abstract class Database extends MySQLConnection {

    /** Collection of SQL statements managed by this database handler. */
    private final List<Statement> statementList;

    /**
     * Constructs a new {@code Database} instance and initializes the statement collection.
     * Subclasses should call {@link #loadStatements()} to populate the collection with
     * their specific SQL statements.
     */
    protected Database(String databaseInfoString) {
        super(new MySQLConnectionInfo(databaseInfoString));
        this.statementList = new ArrayList<>();
        loadStatements();
    }

    /**
     * Prepares and adds a new SQL statement to the collection.
     *
     * @param name           The unique name of the SQL statement.
     * @param query          The SQL query string associated with the statement.
     * @param connectionFlag Additional metadata defining the SQL statement's connection requirements.
     */
    protected void prepareStatement(String name, String query, ConnectionFlags connectionFlag){
        this.statementList.add(new Statement(name, query, connectionFlag));
    };

    /**
     * Prepares and adds a new SQL statement to the collection.
     *
     * @param statement An instance of {@link Statement} containing the SQL query and metadata.
     */
    protected void prepareStatement(Statement statement){
        this.statementList.add(statement);
    }

    /**
     * Retrieves a SQL statement by its unique name.
     *
     * @param name The unique name of the SQL statement to retrieve.
     * @return The {@link Statement} associated with the given name, or {@code null} if not found.
     */
    public Statement get(String name) {
        AtomicReference<Statement> ref = new AtomicReference<>(null);
        this.statementList.forEach(statement -> {
            if (statement.name().equals(name)) {
                ref.set(statement);
                return;
            }
        });
        return ref.get();
    }

    /**
     * Abstract method that must be implemented by subclasses to initialize
     * and load the SQL statements required for the specific database implementation.
     */
    public abstract void loadStatements();

}
