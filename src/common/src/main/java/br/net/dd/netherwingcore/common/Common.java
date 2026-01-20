package br.net.dd.netherwingcore.common;

public class Common {

    public enum TimeConstants {
        MINUTE(60),
        HOUR(60 * MINUTE.getValue()),
        DAY(24 * HOUR.getValue()),
        WEEK(7 * DAY.getValue()),
        MONTH(30 * DAY.getValue()),
        YEAR(365 * DAY.getValue()),
        IN_MILLISECONDS(1000);

        private final long value;

        TimeConstants(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }

    public enum AccountTypes {
        SEC_PLAYER(0),
        SEC_MODERATOR(1),
        SEC_GAMEMASTER(2),
        SEC_ADMINISTRATOR(3),
        SEC_CONSOLE(4); // must be always last in list, accounts must have less security level always also

        private final int value;

        AccountTypes(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

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
        LOCALE_none(9),
        LOCALE_ptBR(10),
        LOCALE_itIT(11);

        public static final int TOTAL_LOCALES = LocaleConstant.values().length - 1;

        private final int value;

        LocaleConstant(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean isValidLocale(LocaleConstant locale) {
            return locale != LOCALE_none;
        }
    }

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

        public int getValue() {
            return value;
        }

    }

    // Default locale
    public static final LocaleConstant DEFAULT_LOCALE = LocaleConstant.LOCALE_enUS;

    // Locale names array
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

    public static LocaleConstant getLocaleByName(String name) {
        for (int i = 0; i < localeNames.length; i++) {
            if (localeNames[i].equalsIgnoreCase(name)) {
                return LocaleConstant.values()[i];
            }
        }
        return LocaleConstant.LOCALE_none; // Default to LOCALE_none if not found
    }

    // LocalizedString struct equivalent
    public static class LocalizedString {
        private final String[] strings = new String[LocaleConstant.TOTAL_LOCALES];

        public String get(LocaleConstant locale) {
            return strings[locale.getValue()];
        }

        public void set(LocaleConstant locale, String value) {
            strings[locale.getValue()] = value;
        }
    }

    // Common mathematical constants
    public static final double M_PI = 3.14159265358979323846;
    public static final double M_PI_4 = 0.785398163397448309616;

    // Max query length
    public static final int MAX_QUERY_LEN = 32 * 1024;

}
