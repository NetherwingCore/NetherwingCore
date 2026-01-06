package br.net.dd.netherwingcore.common;

import br.net.dd.netherwingcore.common.discord.DiscordMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Common {

    // Array containing the names of available locations.
    private static final String[] localeNames = {
            "enUS", "koKR", "frFR", "deDE", "zhCN", "zhTW", "esES", "esMX", "ruRU", "none", "ptBR", "itIT"
    };

    /**
     * Retrieves the locale by the given name.
     * If the locale name is not found, returns LocaleConstant.enUS by default.
     *
     * @param name Locale name
     * @return The constant corresponding to the locale
     */
    public static LocaleConstant getLocaleByName(String name) {
        for (int i = 0; i < localeNames.length; i++) {
            if (localeNames[i].equals(name)) {
                return LocaleConstant.values()[i];
            }
        }
        return LocaleConstant.LOCALE_enUS; // Returns enUS as the default case
    }

    // Utility Functions
    public static long atoul(String str) {
        return Long.parseUnsignedLong(str);
    }

    public static long atoull(String str) {
        return Long.parseUnsignedLong(str);
    }

    // Queue for managing Discord messages
    public static final BlockingQueue<DiscordMessage> discordMessageQueue = new LinkedBlockingQueue<>();

}
