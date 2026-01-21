package br.net.dd.netherwingcore.database.impl.world;

import br.net.dd.netherwingcore.database.Database;

import static br.net.dd.netherwingcore.database.ConnectionFlag.*;

/**
 * The {@code WorldDatabase} class represents a specialized implementation of the {@link Database}
 * specifically designed to handle world-related operations in the application.
 *
 * <p>This class extends the base {@link Database} class and provides a set of SQL statements,
 * configured through the {@code prepareStatement} method, which are necessary for world management
 * and related operations.
 *
 * <p>This class is responsible for mapping SQL statements to predefined constants,
 * ensuring structured operations and managing connections in both synchronous and asynchronous modes.
 */
public class WorldDatabase extends Database {

    /**
     * Constructs a new {@code WorldDatabase} instance.
     * This constructor initializes the parent {@link Database} class.
     */
    public WorldDatabase() { super(); }

    /**
     * Loads SQL statements specific to the WorldDatabase.
     * This method prepares and adds SQL statements to the database's statement collection.
     */
    @Override
    public void loadStatements() {
        prepareStatement("NONE", "SELECT * FROM `realm_list`", CONNECTION_ASYNC);
    }
}
