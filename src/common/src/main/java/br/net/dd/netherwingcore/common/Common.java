package br.net.dd.netherwingcore.common;

/**
 * The {@code Common} class provides a set of enums
 * and constants commonly used throughout the application.
 * It includes time-related constants, account types, locale settings,
 * and mathematical constants.
 *
 * This class is designed as a central repository for shared and reusable
 * definitions, which can help enforce consistency and reduce code duplication.
 */
public class Common {

    /**
     * {@code TimeConstants} is an enum for time-related constants,
     * providing conversions for units of time.
     */
    public enum TimeConstants {
        MINUTE(60),                                  // 60 seconds
        HOUR(60 * MINUTE.getValue()),                // 60 minutes
        DAY(24 * HOUR.getValue()),                   // 24 hours
        WEEK(7 * DAY.getValue()),                    // 7 days
        MONTH(30 * DAY.getValue()),                  // 30 days
        YEAR(365 * DAY.getValue()),                  // 365 days
        IN_MILLISECONDS(1000);                       // 1000 milliseconds (1 second)

        private final long value;

        TimeConstants(long value) {
            this.value = value;
        }

        /**
         * Gets the value of the constant in seconds or milliseconds, as applicable.
         *
         * @return the value of the constant.
         */
        public long getValue() {
            return value;
        }
    }

    /**
     * {@code AccountTypes} defines levels of access or security
     * that an account can have.
     */
    public enum AccountTypes {
        SEC_PLAYER(0),         // Player-level security
        SEC_MODERATOR(1),      // Moderator-level security
        SEC_GAMEMASTER(2),     // Game Master-level security
        SEC_ADMINISTRATOR(3),  // Administrator-level security
        SEC_CONSOLE(4);        // Console-level security (highest level)

        private final int value;

        AccountTypes(int value) {
            this.value = value;
        }

        /**
         * Gets the security level value of the account type.
         *
         * @return the security level value.
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * {@code LocaleConstant} represents the supported locales
     * of the application.
     */
    public enum LocaleConstant {
        LOCALE_enUS(0),
        LOCALE_koKR(1),
        LOCALE_frFR(2),
        LOCALE_deDE(3),
        LOCALE_zhCN(4),
        LOCALE_zhTW(5),
        LOCALE_esES(6),
        LOCALE_esMX(7),
        LOCALE_ruRU(8),
        LOCALE_none(9), // Fallback placeholder
        LOCALE_ptBR(10),
        LOCALE_itIT(11);

        public static final int TOTAL_LOCALES = LocaleConstant.values().length - 1;

        private final int value;

        LocaleConstant(int value) {
            this.value = value;
        }

        /**
         * Gets the value associated with the locale.
         *
         * @return the value of the locale.
         */
        public int getValue() {
            return value;
        }

        /**
         * Checks if the given locale is valid (not LOCALE_none).
         *
         * @param locale the locale to check.
         * @return true if valid; otherwise, false.
         */
        public static boolean isValidLocale(LocaleConstant locale) {
            return locale != LOCALE_none;
        }
    }

    /**
     * {@code CascLocaleBit} represents locale settings
     * for casc operations or configurations.
     */
    public enum CascLocaleBit {

        None(0),
        enUS(1),
        koKR(2),
        Reserved(3),
        frFR(4),
        deDE(5),
        zhCN(6),
        esES(7),
        zhTW(8),
        enGB(9),
        enCN(10),
        enTW(11),
        esMX(12),
        ruRU(13),
        ptBR(14),
        itIT(15),
        ptPT(16);

        private final int value;

        CascLocaleBit(int value) {
            this.value = value;
        }

        /**
         * Gets the value associated with the locale setting.
         *
         * @return the value of the locale setting.
         */
        public int getValue() {
            return value;
        }

    }

    // Default locale constant
    public static final LocaleConstant DEFAULT_LOCALE = LocaleConstant.LOCALE_enUS;

    // Array of locale names for reference
    public static final String[] localeNames = new String[LocaleConstant.TOTAL_LOCALES];

    static {
        // Initialize locale names array (you may fill these with the respective names as needed)
        localeNames[0] = "enUS";
        localeNames[1] = "koKR";
        localeNames[2] = "frFR";
        localeNames[3] = "deDE";
        localeNames[4] = "zhCN";
        localeNames[5] = "zhTW";
        localeNames[6] = "esES";
        localeNames[7] = "esMX";
        localeNames[8] = "ruRU";
        localeNames[9] = "none"; // Placeholder for LOCALE_none
        localeNames[10] = "ptBR";
        localeNames[11] = "itIT";
    }

    /**
     * Gets the {@code LocaleConstant} corresponding to the given locale name.
     *
     * @param name the name of the locale.
     * @return the corresponding {@code LocaleConstant}, or LOCALE_none if not found.
     */
    public static LocaleConstant getLocaleByName(String name) {
        for (int i = 0; i < localeNames.length; i++) {
            if (localeNames[i].equalsIgnoreCase(name)) {
                return LocaleConstant.values()[i];
            }
        }
        return LocaleConstant.LOCALE_none; // Default to LOCALE_none if not found
    }

    /**
     * LocalizedString provides a structure for storing and retrieving
     * locale-specific strings.
     */
    public static class LocalizedString {
        private final String[] strings = new String[LocaleConstant.TOTAL_LOCALES];

        /**
         * Retrieves the localized string for the given locale.
         *
         * @param locale the locale whose string is to be retrieved.
         * @return the string for the specified locale, or null if not set.
         */
        public String get(LocaleConstant locale) {
            return strings[locale.getValue()];
        }

        /**
         * Sets the localized string for the given locale.
         *
         * @param locale the locale whose string is to be set.
         * @param value the string value to set.
         */
        public void set(LocaleConstant locale, String value) {
            strings[locale.getValue()] = value;
        }
    }

    // Common mathematical constants
    /**
     * The mathematical constant PI (π).
     */
    public static final double M_PI = 3.14159265358979323846;

    /**
     * The value of π/4.
     */
    public static final double M_PI_4 = 0.785398163397448309616;

    // Max query length
    /**
     * The maximum allowed query length in bytes (32 KB).
     */
    public static final int MAX_QUERY_LEN = 32 * 1024;

}
