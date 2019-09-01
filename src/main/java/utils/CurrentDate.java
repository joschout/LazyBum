package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by joschout.
 */
public class CurrentDate {

    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final LocalDate localDate = LocalDate.now();

    public static String getCurrentDateAsString(){
        return dtf.format(localDate);
    }

}
