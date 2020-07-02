package be.ugent.blok2.reservations;

import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Locker;

import java.util.Objects;

public class LockerReservation {
    private User owner;
    private Locker locker;
    private boolean keyPickedUp;
    private boolean keyBroughtBack;
    private CustomDate startDate;
    private CustomDate endDate;

    public LockerReservation() {

    }

    public LockerReservation(Locker locker, User owner, CustomDate startDate, CustomDate endDate) {
        this.locker = locker;
        this.owner = owner;
        this.startDate = startDate;
        this.endDate = endDate;
        this.keyPickedUp = false;
        this.keyBroughtBack = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LockerReservation that = (LockerReservation) o;
        return owner.equals(that.owner) &&
                locker.equals(that.locker) &&
                keyPickedUp == that.keyPickedUp &&
                keyBroughtBack == that.keyBroughtBack &&
                startDate.equals(that.startDate) &&
                endDate.equals(that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, locker, startDate, endDate);
    }

    @Override
    public String toString() {
        return "LockerReservation{" +
                "owner=" + owner +
                ", locker=" + locker +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public Locker getLocker() {
        return locker;
    }
    
    public User getOwner() {
        return owner;
    }
    
    public CustomDate getStartDate() {
        return startDate;
    }
    
    public CustomDate getEndDate() {
        return endDate;
    }

    public boolean getKeyPickedUp(){
        return keyPickedUp;
    }

    public boolean getKeyBroughtBack(){
        return keyBroughtBack;
    }
    
    public void setLocker(Locker locker) {
        this.locker = locker;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setStartDate(CustomDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(CustomDate endDate) {
        this.endDate = endDate;
    }

    public void setKeyPickedUp(boolean keyPickedUp){
        this.keyPickedUp = keyPickedUp;
    }

    public void setKeyBroughtBack(boolean keyBroughtBack){
        this.keyBroughtBack = keyBroughtBack;
    }
    //</editor-fold>
}
