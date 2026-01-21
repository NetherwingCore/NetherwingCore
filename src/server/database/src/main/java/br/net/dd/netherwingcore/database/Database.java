package br.net.dd.netherwingcore.database;

import br.net.dd.netherwingcore.database.impl.auth.StatementName;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class that represents a database handler. This class is responsible for managing
 * SQL statements and their associated metadata (e.g., connection flags). Subclasses should
 * implement the {@link #loadStatements()} method to define the SQL statements they need.
 *
 * <p>The class provides mechanisms to add SQL statements to a collection and retrieve them
 * by their unique statement names.
 */
public abstract class Database {

    /**
     * A map that stores SQL statements indexed by their names.
     * The name is an instance of {@link StatementName}, and the value includes the SQL query
     * and related connection metadata.
     */
    private final Map<StatementName, StatementValue> statements;

    /**
     * Default constructor that initializes the SQL statements map
     * and loads predefined statements by calling {@link #loadStatements()}.
     */
    protected Database() {
        this.statements = new HashMap<>();
        loadStatements();
    }

    /**
     * Prepares and adds a new SQL statement to the collection.
     *
     * @param name           The unique name of the SQL statement (an instance of {@link StatementName}).
     * @param query          The SQL query string associated with the statement.
     * @param connectionFlag Additional metadata defining the SQL statement's connection requirements.
     */
    protected void prepareStatement(StatementName name, String query, ConnectionFlag connectionFlag){
        this.statements.put(name, new StatementValue(query, connectionFlag));
    };

    /**
     * Retrieves a prepared SQL statement by its name.
     *
     * @param name The unique name of the SQL statement to retrieve.
     * @return An instance of {@link StatementValue} containing the SQL query and metadata,
     *         or {@code null} if no statement with the given name exists.
     */
    public StatementValue get(StatementName name) {
        return this.statements.get(name);
    }

    /**
     * Abstract method that must be implemented by subclasses to initialize
     * and load the SQL statements required for the specific database implementation.
     */
    public abstract void loadStatements();

}
