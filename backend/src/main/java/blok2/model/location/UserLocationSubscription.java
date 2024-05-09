package blok2.model.location;

import blok2.model.users.User;

import javax.persistence.*;

@Entity
@Table(name = "user_location_subscription")
public class UserLocationSubscription implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private int subscriptionId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private Location location;

    public UserLocationSubscription(User user, Location location) {
        this.user = user;
        this.location = location;
    }

    public UserLocationSubscription() {

    }

    @Override
    public UserLocationSubscription clone() {
        try {
            return (UserLocationSubscription) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public Location getLocation() {
        return location;
    }
}
