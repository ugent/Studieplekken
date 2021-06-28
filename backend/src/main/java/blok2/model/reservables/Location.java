package blok2.model.reservables;

import blok2.helpers.Equality;
import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.calendar.Timeslot;
import blok2.model.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

@Entity
@Table(name = "locations")
public class Location implements Cloneable {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private int locationId;

    @Column(name = "name")
    private String name;

    @Column(name = "number_of_seats")
    private int numberOfSeats;

    @Column(name = "number_of_lockers")
    private int numberOfLockers;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description_dutch")
    private String descriptionDutch = "";

    @Column(name = "description_english")
    private String descriptionEnglish= "";

    @OneToOne
    @JoinColumn(name = "building_id", referencedColumnName = "building_id")
    private Building building;

    @OneToOne
    @JoinColumn(name = "authority_id", referencedColumnName = "authority_id")
    private Authority authority;

    @Column(name = "for_group")
    private boolean forGroup;

    @Column(name = "approved")
    private boolean approved;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "location_tags",
        joinColumns = @JoinColumn(name = "location_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<LocationTag> assignedTags;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "roles_user_volunteer",
        joinColumns = @JoinColumn(name = "location_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> volunteers;

    @Transient
    private Pair<LocationStatus, String> status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Transient
    private Timeslot currentTimeslot;

    public Location(int locationId, String name, int numberOfSeats, int numberOfLockers, String imageUrl,
                    Authority authority, String descriptionDutch, String descriptionEnglish, Building building,
                    boolean forGroup, List<LocationTag> assignedTags, Pair<LocationStatus, String> status) {
        this(locationId, name, numberOfSeats,numberOfLockers,imageUrl,authority,descriptionDutch,descriptionEnglish, building, forGroup, assignedTags, status, null);
    }

    public Location(int locationId, String name, int numberOfSeats, int numberOfLockers, String imageUrl,
                    Authority authority, String descriptionDutch, String descriptionEnglish, Building building,
                    boolean forGroup, List<LocationTag> assignedTags, Pair<LocationStatus, String> status, Timeslot timeslot) {
        this.locationId = locationId;
        this.name = name;
        this.numberOfSeats = numberOfSeats;
        this.numberOfLockers = numberOfLockers;
        this.imageUrl = imageUrl;
        this.descriptionDutch = descriptionDutch;
        this.descriptionEnglish = descriptionEnglish;
        this.authority = authority;
        this.building = building;
        this.forGroup = forGroup;
        this.assignedTags = assignedTags;
        this.status = status;
        this.currentTimeslot = timeslot;
    }

    // default constructor necessary for testing purposes
    public Location() {
        assignedTags = new ArrayList<>();
        status = new Pair<>(LocationStatus.CLOSED, "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return numberOfSeats == location.numberOfSeats &&
                numberOfLockers == location.numberOfLockers &&
                forGroup == location.forGroup &&
                Objects.equals(locationId, location.locationId) &&
                Objects.equals(name, location.name) &&
                Objects.equals(imageUrl, location.imageUrl) &&
                Objects.equals(descriptionDutch, location.descriptionDutch) &&
                Objects.equals(descriptionEnglish, location.descriptionEnglish) &&
                Objects.equals(authority, location.authority) &&
                Equality.listEqualsIgnoreOrder(assignedTags, location.assignedTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                locationId, name, numberOfSeats, numberOfLockers, imageUrl,
                descriptionDutch, descriptionEnglish);
    }

    @Override
    public Location clone() {
        try {
            Location l = (Location) super.clone();

            // deep copy the mutable attributes
            l.authority = authority.clone();
            l.assignedTags = new ArrayList<>(assignedTags.size());

            for (int i = 0; i < assignedTags.size(); i++) {
                l.assignedTags.set(i, assignedTags.get(i).clone());
            }

            return l;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public int getNumberOfLockers() {
        return numberOfLockers;
    }

    public void setNumberOfLockers(int numberOfLockers) {
        this.numberOfLockers = numberOfLockers;
    }

    public boolean getForGroup() {
        return forGroup;
    }

    public void setForGroup(boolean forGroup) {
        this.forGroup = forGroup;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescriptionDutch() {
        return descriptionDutch;
    }

    public void setDescriptionDutch(String descriptionDutch) {
        this.descriptionDutch = descriptionDutch;
    }

    public String getDescriptionEnglish() {
        return descriptionEnglish;
    }

    public void setDescriptionEnglish(String descriptionEnglish) {
        this.descriptionEnglish = descriptionEnglish;
    }

    public Authority getAuthority() {
        return authority;
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    public List<LocationTag> getAssignedTags() {
        return assignedTags;
    }

    public void setAssignedTags(List<LocationTag> assignedTags) {
        this.assignedTags = assignedTags;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Pair<LocationStatus, String> getStatus() {
        return status;
    }

    public void setStatus(Pair<LocationStatus, String> status) {
        this.status = status;
    }

    public Timeslot getCurrentTimeslot() {
        return currentTimeslot;
    }

    public void setCurrentTimeslot(Timeslot currentTimeslot) {
        this.currentTimeslot = currentTimeslot;
    }

    public boolean isForGroup() {
        return forGroup;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public List<User> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<User> volunteers) {
        this.volunteers = volunteers;
    }

    //</editor-fold>

}
