package br.net.dd.netherwingcore.database.updater;

import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.common.logging.Log;

/**
 * DBUpdater is responsible for managing database updates based on configuration settings.
 * It handles the creation, population, and updating of databases as specified by the configuration.
 */
public class DBUpdater {

    private int enableDatabases;
    private int autoSetup;
    private int redundancy;
    private int archivedRedundancy;
    private int allowRehash;

    private static Log logger;

    private static DBUpdater instance;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the configuration settings for database updates.
     */
    private DBUpdater() {
        this.enableDatabases = Config.get("Updates.EnableDatabases", 15);
        this.autoSetup = Config.get("Updates.AutoSetup", 1);
        this.redundancy = Config.get("Updates.Redundancy", 1);
        this.archivedRedundancy = Config.get("Updates.ArchivedRedundancy", 0);
        this.allowRehash = Config.get("Updates.AllowRehash", 1);

        Log logger = Log.getLogger(DBUpdater.class.getSimpleName());
    }

    public static void run() {
        if (instance == null) {
            instance = new DBUpdater();
        }

        if (instance.autoSetup == 1) {
            populate();
            populate();
            update();
        }
        
    }

    private static void create(){

        DatabaseFlag.fromValue(instance.enableDatabases).forEach(flag -> {
            logger.debug("Creating database for: " + flag.name());
        });

    };

    private static void populate(){

        DatabaseFlag.fromValue(instance.enableDatabases).forEach(flag -> {
            logger.debug("Populating database for: " + flag.name());
        });

    }

    private static void update(){

        DatabaseFlag.fromValue(instance.enableDatabases).forEach(flag -> {
            logger.debug("Updating database for: " + flag.name());
        });

    }

}
