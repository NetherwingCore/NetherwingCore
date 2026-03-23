package br.net.dd.netherwingcore.database.updater;

import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.database.implementation.LoginDatabase;
import br.net.dd.netherwingcore.database.implementation.LoginDatabaseStatements;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DBUpdater is responsible for managing database updates based on configuration settings.
 * It handles the creation, population, and updating of databases as specified by the configuration.
 */
public class DBUpdater {

    private static int enableDatabases;
    private static int autoSetup;
    private static int redundancy;
    private static int archivedRedundancy;
    private static int allowRehash;

    private static Log logger;

    private DBUpdater() {

    }

    public static void run() {

        enableDatabases = Config.get("Updates.EnableDatabases", 1);
        autoSetup = Config.get("Updates.AutoSetup", 1);
        redundancy = Config.get("Updates.Redundancy", 1);
        archivedRedundancy = Config.get("Updates.ArchivedRedundancy", 0);
        allowRehash = Config.get("Updates.AllowRehash", 1);

        logger = Log.getLogger(DBUpdater.class.getSimpleName());

        if (enableDatabases == 0) {
            return;
        }

        logger.info("Database updates are enabled. Starting the update process.");

        boolean connected = LoginDatabase.getInstance().connect();

        if (autoSetup == 1) {
            create();
            populate();
            update();
        } else {
            logger.info("Database AutoSetup are disabled. Skipping creation, population, and update steps.");
        }

    }

    private static void create(){

        DatabaseFlag.fromValue(enableDatabases).forEach(flag -> {
            logger.debug("Creating database for: " + flag.name());
        });

    };

    private static void populate(){

        DatabaseFlag.fromValue(enableDatabases).forEach(flag -> {
            logger.debug("Populating database for: " + flag.name());
        });

    }

    private static void update(){

        DatabaseFlag.fromValue(enableDatabases).forEach(flag -> {
            logger.debug("Updating database for: " + flag.name());
        });

    }

}
