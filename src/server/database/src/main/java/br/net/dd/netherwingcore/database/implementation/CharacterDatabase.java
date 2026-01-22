package br.net.dd.netherwingcore.database.implementation;

import br.net.dd.netherwingcore.common.cache.Cache;
import br.net.dd.netherwingcore.database.Database;

import static br.net.dd.netherwingcore.database.MySQLConnection.ConnectionFlags.*;

/**
 * The {@code CharacterDatabase} class represents a specialized implementation of the {@link Database}
 * specifically designed to handle authentication and account operations in the application.
 *
 * <p>This class extends the base {@link Database} class and provides a set of SQL statements,
 * configured through the {@code prepareStatement} method, which are necessary for authentication,
 * account management, ban management, logging, and other related operations.
 *
 * <p>This class is responsible for mapping SQL statements to predefined constants,
 * ensuring structured operations and managing connections in both synchronous and asynchronous modes.
 */
public class CharacterDatabase extends Database {

    /**
     * Constructs a new {@code CharacterDatabase} instance.
     * This constructor initializes the parent {@link Database} class.
     */
    public CharacterDatabase() {
        String characterDatabaseInfo = Cache.getConfiguration().get("CharacterDatabaseInfo", "127.0.0.1;3306;trinity;trinity;character");
        super(characterDatabaseInfo);
    }

    @Override
    public void loadStatements() {
        prepareStatement("NONE", "SELECT * FROM", CONNECTION_ASYNC);
    }
}
