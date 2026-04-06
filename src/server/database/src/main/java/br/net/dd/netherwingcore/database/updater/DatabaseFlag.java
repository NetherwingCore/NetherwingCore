package br.net.dd.netherwingcore.database.updater;

import java.util.EnumSet;

/**
 * This enum represents the different databases that can be updated by the DBUpdater.
 * Each flag corresponds to a specific database and is represented as a bitmask.
 * The flags can be combined using bitwise operations to enable multiple databases at once.
 */
public enum DatabaseFlag {
    DATABASE_LOGIN(1, "LoginDatabaseInfo", "auth"),
    DATABASE_CHARACTER(2, "CharacterDatabaseInfo", "characters"),
    DATABASE_WORLD(4, "WorldDatabaseInfo", "world"),
    DATABASE_HOTFIX(8, "HotfixDatabaseInfo", "hotfix"),
    DATABASE_SHOP(16, "ShopDatabaseInfo", "shop"),;

    private final int mask;
    private final String configKeyName;
    private final String internalName;

    /**
     * Constructs a DatabaseFlag with the specified bitmask, configuration key name, and internal name.
     *
     * @param mask The integer bitmask representing this flag.
     * @param configKeyName The name of the configuration key associated with this flag.
     * @param internalName The internal name associated with this flag, corresponding to the database name.
     */
    DatabaseFlag(int mask, String configKeyName, String internalName) {
        this.mask = mask;
        this.configKeyName = configKeyName;
        this.internalName = internalName;
    }

    /**
     * Gets the bitmask value associated with this flag.
     *
     * @return The integer bitmask for this flag.
     */
    public int getMask() {
        return mask;
    }

    /**
     * Gets the name associated with this flag, which corresponds to a configuration key.
     *
     * @return The name of the flag.
     */
    public String getConfigKeyName() {
        return configKeyName;
    }

    /**
     * Gets the internal name associated with this flag, which corresponds to the database name.
     *
     * @return The internal name of the flag.
     */
    public String getInternalName() {
        return internalName;
    }

    /**
     * Checks if the given value has this flag set.
     *
     * @param value The integer value to check against the flag.
     * @return true if the flag is set in the value, false otherwise.
     */
    public boolean isSet(int value) {
        return (value & mask) != 0;
    }

    /**
     * Converts an integer value into a set of DatabaseFlags that are enabled in that value.
     *
     * @param value The integer value representing the combined flags.
     * @return An EnumSet of DatabaseFlags that are set in the given value.
     */
    public static EnumSet<DatabaseFlag> fromValue(int value) {
        EnumSet<DatabaseFlag> set = EnumSet.noneOf(DatabaseFlag.class);
        for (DatabaseFlag flag : DatabaseFlag.values()) {
            if (flag.isSet(value)) {
                set.add(flag);
            }
        }
        return set;
    }

}
