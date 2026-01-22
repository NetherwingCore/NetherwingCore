package br.net.dd.netherwingcore.database.implementation;

import br.net.dd.netherwingcore.common.cache.Cache;
import br.net.dd.netherwingcore.database.Database;

import static br.net.dd.netherwingcore.database.MySQLConnection.ConnectionFlags.*;

/**
 * The {@code HotfixDatabase} class represents a specialized implementation of the {@link Database}
 * specifically designed to handle hotfix-related operations in the application.
 *
 * <p>This class extends the base {@link Database} class and provides a set of SQL statements,
 * configured through the {@code prepareStatement} method, which are necessary for hotfix management
 * and related operations.
 *
 * <p>This class is responsible for mapping SQL statements to predefined constants,
 * ensuring structured operations and managing connections in both synchronous and asynchronous modes.
 */
public class HotfixDatabase extends Database {

    /**
     * Constructs a new {@code HotfixDatabase} instance.
     * This constructor initializes the parent {@link Database} class.
     */
    public HotfixDatabase() {
        String hotfixDatabaseInfo = Cache.getConfiguration().get("HotfixDatabaseInfo", "127.0.0.1;3306;trinity;trinity;hotfix");
        super(hotfixDatabaseInfo);
    }

    /**
     * Loads SQL statements specific to the HotfixDatabase.
     * This method prepares and adds SQL statements to the database's statement collection.
     */
    @Override
    public void loadStatements() {
        prepareStatement("NONE", "SELECT * FROM `realm_list`", CONNECTION_ASYNC);
    }
}
