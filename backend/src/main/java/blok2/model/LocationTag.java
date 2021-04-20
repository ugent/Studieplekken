package blok2.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "location_tags")
public class LocationTag implements Cloneable {
    @Id
    private int tagId;
    private String dutch;
    private String english;

    public LocationTag(int tagId, String dutch, String english) {
        this.tagId = tagId;
        this.dutch = dutch;
        this.english = english;
    }

    public LocationTag() {
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
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
    public LocationTag clone() {
        try {
            return (LocationTag) super.clone();
        } catch (CloneNotSupportedException ignore) {
            // will never happen (LocationTag implements Cloneable)
            return null;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
