package br.net.dd.netherwingcore.database;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@code Database} class serves as an abstract base for managing database connections
 * and SQL statements within the application. It extends the {@link MySQLConnection} class,
 * providing functionality to prepare, store, and retrieve SQL statements.
 *
 * <p>This class maintains a list of {@link Statement} objects, allowing subclasses to
 * define and load specific SQL statements through the abstract {@code loadStatements} method.
 * It also provides methods to prepare new statements and retrieve existing ones by name.
 *
 * <p>Subclasses of {@code Database} are expected to implement the {@code loadStatements}
 * method to populate the statement list with relevant SQL queries for their specific use cases.
 *
 * @see MySQLConnection
 * @see Statement
 */
public abstract class Database extends MySQLConnection {

    private final List<Statement> statementList;

    /**
     * Constructs a new {@code Database} instance with the specified database information string.
     *
     * @param databaseInfoString the database connection information in string format
     */
    protected Database(String databaseInfoString) {
        super(new MySQLConnectionInfo(databaseInfoString));
        this.statementList = new ArrayList<>();
        loadStatements();
    }

    /**
     * Prepares and adds a new SQL statement to the database's statement list.
     *
     * @param name           the name of the SQL statement
     * @param query          the SQL query string
     * @param connectionFlag the connection flag indicating synchronous or asynchronous execution
     */
    protected void prepareStatement(String name, String query, ConnectionFlags connectionFlag){
        this.statementList.add(new Statement(name, query, connectionFlag));
    };

    /**
     * Prepares and adds an existing {@link Statement} object to the database's statement list.
     *
     * @param statement the {@link Statement} object to be added
     */
    protected void prepareStatement(Statement statement){
        this.statementList.add(statement);
    }

    /**
     * Retrieves a {@link Statement} object by its name from the database's statement list.
     *
     * @param name the name of the SQL statement to retrieve
     * @return the {@link Statement} object with the specified name, or {@code null} if not found
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
     * Clears all SQL statements from the database's statement list.
     */
    protected void clearStatements() {
        if (!this.statementList.isEmpty()) {
            this.statementList.clear();
        }
    }

    /**
     * Loads SQL statements specific to the database implementation.
     * Subclasses must implement this method to prepare and add SQL statements
     * to the database's statement collection.
     */
    public abstract void loadStatements();

}
