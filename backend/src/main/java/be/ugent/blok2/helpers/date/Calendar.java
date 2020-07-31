package be.ugent.blok2.helpers.date;

import java.util.ArrayList;
import java.util.List;


public class Calendar {

    private List<Day> days;
    
    public Calendar(List<Day> days) {
        this.days = days;
    }

    public Calendar() {
        this.days = new ArrayList<>();
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public List<Day> getDays(){
        return days;
    }

    public void setDays(List<Day> days){
        this.days = days;
    }
    
    //</editor-fold>
}
