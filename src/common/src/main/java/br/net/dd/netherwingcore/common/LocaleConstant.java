package br.net.dd.netherwingcore.common;

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

    private final int value;

    public static final int TOTAL_LOCALES = values().length;

    LocaleConstant(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
