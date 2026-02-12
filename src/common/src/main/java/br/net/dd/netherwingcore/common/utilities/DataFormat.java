package br.net.dd.netherwingcore.common.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataFormat {

    public static String REGEX_DATE_IP = "dd/MM/yyyy";
    public static String REGEX_DATE_FILENAME = "dd-MM-yyyy";
    public static String REGEX_DATE_LOG_EVENT = "EEE dd MMM yyyy HH:mm:ss.SSSZ";

    public static String format(Date date, String regex) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(regex);
        return simpleDateFormat.format(date);
    }

}
