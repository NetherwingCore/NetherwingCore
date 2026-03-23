package br.net.dd.netherwingcore.database.updater;

import java.util.EnumSet;

/**
 * This enum represents the different databases that can be updated by the DBUpdater.
 * Each flag corresponds to a specific database and is represented as a bitmask.
 * The flags can be combined using bitwise operations to enable multiple databases at once.
 */
public enum DatabaseInfos {
    DATABASE_LOGIN(1),
    DATABASE_CHARACTER(2),
    DATABASE_WORLD(4),
    DATABASE_HOTFIX(8),
    DATABASE_SHOP(16);

    private final int mask;

    /**
     * Constructor for the DatabaseFlag enum.
     *
     * @param mask The integer bitmask value associated with this flag.
     */
    DatabaseInfos(final int mask) {
        this.mask = mask;
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
    public static EnumSet<DatabaseInfos> fromValue(int value) {
        EnumSet<DatabaseInfos> set = EnumSet.noneOf(DatabaseInfos.class);
        for (DatabaseInfos flag : DatabaseInfos.values()) {
            if (flag.isSet(value)) {
                set.add(flag);
            }
        }
        return set;
    }

}
