package blok2.model;

import blok2.model.reservables.Location;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "tags")
public class LocationTag implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Integer tagId;

    @Column(name = "dutch")
    private String dutch;

    @Column(name = "english")
    private String english;

    @ManyToMany(fetch = FetchType.LAZY) // in Location, the tags are fetched eagerly
    @JoinTable(
        name = "location_tags",
        joinColumns = @JoinColumn(name = "tag_id"),
        inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    @JsonIgnore
    private List<Location> locations;

    public LocationTag(int tagId, String dutch, String english) {
        this.tagId = tagId;
        this.dutch = dutch;
        this.english = english;
        locations = new ArrayList<>();
    }

    public LocationTag() {
        locations = new ArrayList<>();
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getDutch() {
        return dutch;
    }

    public void setDutch(String dutch) {
        this.dutch = dutch;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationTag tag = (LocationTag) o;
        return Objects.equals(tagId, tag.tagId) &&
                Objects.equals(dutch, tag.dutch) &&
                Objects.equals(english, tag.english);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, dutch, english);
    }

    @Override
    public LocationTag clone() {
        try {
            return (LocationTag) super.clone();
        } catch (CloneNotSupportedException ignore) {
            // will never happen (LocationTag implements Cloneable)
            return null;
        }
    }

    // Note: don't use ToStringBuilder since locations are fetched lazily and that may cause problems
    @Override
    public String toString() {
        return "LocationTag{" +
                "tagId=" + tagId +
                ", dutch='" + dutch + '\'' +
                ", english='" + english + '\'' +
                '}';
    }

}
