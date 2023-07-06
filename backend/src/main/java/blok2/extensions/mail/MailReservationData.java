package blok2.mail;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class MailReservationData {


    public MailReservationData(String locationName, LocalDateTime time, String locationReminderDutch, String locationReminderEnglish) {
        this.locationName = locationName;
        this.time = time;
        this.locationReminderDutch = locationReminderDutch;
        this.locationReminderEnglish = locationReminderEnglish;
    }

    public String locationName;
    public LocalDateTime time;
    public String locationReminderDutch;
    public String locationReminderEnglish;

    @Override
    public String toString() {
        return "MailReservationData{" +
                "locationName='" + locationName + '\'' +
                ", time=" + time +
                ", locationReminderDutch='" + locationReminderDutch + '\'' +
                ", locationReminderEnglish='" + locationReminderEnglish + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailReservationData that = (MailReservationData) o;
        return Objects.equals(locationName, that.locationName) && Objects.equals(time, that.time) && Objects.equals(locationReminderDutch, that.locationReminderDutch) && Objects.equals(locationReminderEnglish, that.locationReminderEnglish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationName, time, locationReminderDutch, locationReminderEnglish);
    }
}
