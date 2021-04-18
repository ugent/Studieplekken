package blok2.helpers;

import blok2.model.calendar.CalendarPeriod;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CSVHelper {

    public String calendarPeriodCSV(List<CalendarPeriod> periods) {
        return calendarPeriodHeader() + calendarPeriodRows(periods);
    }

    private String calendarPeriodHeader() {
        return "location name,location address,starting date,ending date,opening time,closing time\n";
    }

    private String calendarPeriodRow(CalendarPeriod period) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY");
        return period.getLocation().getName() + "," + period.getLocation().getBuilding().getAddress();
    }

    private String calendarPeriodRows(List<CalendarPeriod> periodList) {
        return periodList.stream().map(this::calendarPeriodRow).collect(Collectors.joining());
    }
}
