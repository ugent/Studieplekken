package blok2.model.calendar;

import blok2.helpers.Resources;
import blok2.model.reservables.Location;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.threeten.extra.YearWeek;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.SECONDS;

public class CalendarPeriod implements Cloneable {
    private Integer id;
    private Location location;
    private LocalDateTime reservableFrom = LocalDateTime.now();

    private YearWeek week;
    private Integer parentId;
    private int groupId;

    private boolean isRepeated;


    public CalendarPeriod() { }

    // This is enough to identify
    public CalendarPeriod(Integer id) {
        this.id = id;
    }

    public CalendarPeriod(Integer id, int isoyear, int isoweek, Integer parentId, int groupId, LocalDateTime reservableFrom, boolean isRepeated, Location location) {
        this.id = id;
        this.location = location;
        this.reservableFrom = reservableFrom;
        this.week = YearWeek.of(isoyear, isoweek);
        this.parentId = parentId;
        this.groupId = groupId;
        this.isRepeated = isRepeated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarPeriod that = (CalendarPeriod) o;
        return this.id.equals(((CalendarPeriod) o).getId());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public CalendarPeriod clone() {
        try {
            CalendarPeriod clone = (CalendarPeriod) super.clone();
            clone.location = location.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDateTime getReservableFrom() {
        return reservableFrom;
    }

    public void setReservableFrom(LocalDateTime reservableFrom) {
        if(reservableFrom != null)
            this.reservableFrom = reservableFrom;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public YearWeek getWeek() {
        return week;
    }

    public void setWeek(YearWeek week) {
        this.week = week;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public boolean isRepeated() {
        return isRepeated;
    }

    public void setRepeated(boolean reservable) {
        isRepeated = reservable;
    }
}
