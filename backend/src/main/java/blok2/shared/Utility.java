package blok2.shared;

import blok2.model.calendar.Period;

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

    public static void analyzeConfictsInPeriods(List<? extends Period> periods) {

    }
}
