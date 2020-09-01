package blok2.model.reservations;

import blok2.helpers.date.CustomDate;
import blok2.model.reservables.Locker;
import blok2.model.users.User;

import java.util.Objects;

public class LockerReservation {
    private User owner;
    private Locker locker;
    private CustomDate keyPickupDate;
    private CustomDate keyReturnedDate;

    public LockerReservation() {

    }

    public LockerReservation(Locker locker, User owner, CustomDate keyPickupDate, CustomDate keyReturnedDate) {
        this.locker = locker;
        this.owner = owner;
        this.keyPickupDate = keyPickupDate;
        this.keyReturnedDate = keyReturnedDate;
    }

    public LockerReservation(Locker locker, User owner) {
        this(locker, owner, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LockerReservation that = (LockerReservation) o;
        return owner.equals(that.owner) &&
                locker.equals(that.locker) &&
                Objects.equals(keyPickupDate, that.keyPickupDate) &&
                Objects.equals(keyReturnedDate, that.keyReturnedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, locker, keyPickupDate, keyReturnedDate);
    }

    @Override
    public String toString() {
        return "LockerReservation{" +
                "owner=" + owner +
                ", locker=" + locker +
                ", keyPickupDate=" + keyPickupDate +
                ", keyReturnedDate=" + keyReturnedDate +
                '}';
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public Locker getLocker() {
        return locker;
    }

    public User getOwner() {
        return owner;
    }

    public CustomDate getKeyPickupDate() {
        return keyPickupDate;
    }

    public CustomDate getKeyReturnedDate() {
        return keyReturnedDate;
    }

    public void setLocker(Locker locker) {
        this.locker = locker;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setKeyPickupDate(CustomDate keyPickupDate) {
        this.keyPickupDate = keyPickupDate;
    }

    public void setKeyReturnedDate(CustomDate keyReturnedDate) {
        this.keyReturnedDate = keyReturnedDate;
    }

    //</editor-fold>
}
