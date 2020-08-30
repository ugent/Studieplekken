package be.ugent.blok2.shared;

import be.ugent.blok2.model.calendar.Period;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utility {
    public static void sortPeriodsBasedOnStartsAt(List<? extends Period> periods) {
        periods.sort((a, b) -> {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date dateA = format.parse(a.getStartsAt());
                Date dateB = format.parse(b.getStartsAt());
                return dateA.compareTo(dateB);
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    public static String formatDate_YYYY_MM_DD(String date) {
        String[] splitDate = date.split("-");

        if (splitDate[1].length() == 1) {
            splitDate[1] = "0" + splitDate[1];
        }

        if (splitDate[2].length() == 1) {
            splitDate[2] = "0" + splitDate[2];
        }

        return splitDate[0] + "-" + splitDate[1] + "-" + splitDate[2];
    }
}
