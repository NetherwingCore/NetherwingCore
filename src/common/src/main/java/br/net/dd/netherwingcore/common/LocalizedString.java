package br.net.dd.netherwingcore.common;

public class LocalizedString {

    private String[] strings = new String[LocaleConstant.TOTAL_LOCALES];

    public String getString(LocaleConstant locale) {
        return strings[locale.ordinal()];
    }

    public void setString(LocaleConstant locale, String value) {
        strings[locale.ordinal()] = value;
    }

}
