package be.ugent.blok2.helpers.date;

import java.util.ArrayList;
import java.util.Collection;


public class Calendar {
    private Collection<Day> days;
    
    public Calendar(Collection<Day> days) {
        this.days = days;
    }

    public Calendar(){
        this.days = new ArrayList<Day>();
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public Collection<Day> getDays(){
        return days;
    }

    public void setDays(Collection<Day> days){
        this.days = days;
    }
    
    //</editor-fold>
}
