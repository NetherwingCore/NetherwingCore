package br.net.dd.netherwingcore.database.updater;

import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.database.common.ConnectionInfos;
import br.net.dd.netherwingcore.database.util.DBTools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * DBUpdater is responsible for managing database updates based on configuration settings.
 * It handles the creation, population, and updating of databases as specified by the configuration.
 */
public class DBUpdater {

    private int autoCreateDatabases;
    private int autoCreateTables;
    private int autoSetup;
    private int redundancy;
    private int archivedRedundancy;
    private int allowRehash;

    private Log logger;

    private static DBUpdater instance;

    /**
     * Private constructor to prevent instantiation of the DBUpdater class.
     * This class is intended to be used as a singleton, and the run() method should be called to initialize it.
     */
    private DBUpdater() {
    }

    /**
     * Initializes the DBUpdater by reading configuration settings and setting up the logger.
     * It also checks which databases are enabled and performs the necessary setup, population, and updates based on the configuration.
     */
    public static void run() {

        if (instance == null) {
            instance = new DBUpdater();
        }

        instance.initialize();

    }

    /**
     * Initializes the DBUpdater by reading configuration settings and setting up the logger.
     * It also checks which databases are enabled and performs the necessary setup, population, and updates based on the configuration.
     */
    private void initialize() {

        int enableDatabases = Config.get("Updates.EnableDatabases", 1);

        if (enableDatabases != 1) {
            return;
        }

        this.autoCreateDatabases = Config.get("Updates.AutoCreateDatabases", 1);
        this.autoCreateTables = Config.get("Updates.AutoCreateTables", 1);
        this.autoSetup = Config.get("Updates.AutoSetup", 1);
        this.redundancy = Config.get("Updates.Redundancy", 1);
        this.archivedRedundancy = Config.get("Updates.ArchivedRedundancy", 0);
        this.allowRehash = Config.get("Updates.AllowRehash", 1);

        this.logger = Log.getLogger(DBUpdater.class.getSimpleName());

        DatabaseFlag.fromValue(enableDatabases).forEach(flag -> {

            logger.info("Database " + flag.getConfigKeyName() + " has been enabled.");

            ConnectionInfos connectionInfos = new ConnectionInfos(Config.get(flag.getConfigKeyName(), "\"\""));

            if (autoSetup == 1) {

                if (autoCreateDatabases == 1) {
                    create(connectionInfos);
                }

                if (autoCreateTables == 1) {
                    populate(connectionInfos, flag);
                }

                update(connectionInfos, flag);

            } else {
                logger.info("Database AutoSetup are disabled. Skipping creation, population, and update steps.");
            }

        });

    }

    private void create(ConnectionInfos connectionInfos) {

        if (!DBTools.checkDatabase(connectionInfos)) {

            if (DBTools.createUser(connectionInfos)) {
                logger.debug("User {} created successfully on {}.", connectionInfos.getUser(), connectionInfos.getHost());
            }

            if (DBTools.grantUsage(connectionInfos)) {
                logger.debug("User {} granted access *.* on {}.", connectionInfos.getUser(), connectionInfos.getHost());
            }

            if (DBTools.createDatabase(connectionInfos)) {
                logger.debug("Database {} created successfully on {}.", connectionInfos.getDatabase(), connectionInfos.getHost());
            }

            if (DBTools.grantAllPrivileges(connectionInfos)) {
                logger.debug("User {} granted all privileges on database {}.", connectionInfos.getUser(), connectionInfos.getDatabase());
            }

        } else {
            logger.info("Database already exists. Skipping creation.");
        }

        boolean dataBaseExists = DBTools.checkDatabase(connectionInfos);
        boolean userExists = DBTools.checkUser(connectionInfos);

        if (dataBaseExists && userExists) {
            logger.debug("Database and user for {} are set up correctly.", connectionInfos.getDatabase());
        } else {
            logger.error("Failed to set up database or user for {}. Database exists: {}, User exists: {}",
                    connectionInfos.getDatabase(), dataBaseExists, userExists);
            System.exit(1);
        }

    }

    private void populate(ConnectionInfos connectionInfos, DatabaseFlag flag) {

        logger.debug("Populating database with {} records.", connectionInfos.getDatabase());

        String sourceDir = Config.get("SourceDirectory", "").replace("\"", "");
        Path sqlPath = Paths.get(sourceDir, "sql", "base", flag.getInternalName() + "_database.sql");

        if (!Files.exists(sqlPath)) {
            logger.warn("SQL file {} not found. Skipping population for database {}.", sqlPath, connectionInfos.getDatabase());
            return;
        }

        if (DBTools.loadDump(connectionInfos, sqlPath)) {
            logger.info("Database {} has been populated.", connectionInfos.getDatabase());
        } else {
            logger.error("Failed to populate database {} using SQL file {}.", connectionInfos.getDatabase(), sqlPath);
        }

    }

    private void update(ConnectionInfos connectionInfos, DatabaseFlag flag) {

        logger.debug("Checking for updates for database: {} ({})", connectionInfos.getDatabase(), flag.getInternalName());

        DBTools.updateDatabaseFromFile(connectionInfos, null);

    }

}
