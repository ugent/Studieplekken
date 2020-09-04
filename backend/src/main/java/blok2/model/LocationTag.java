package blok2.model;

import java.util.Objects;

public class LocationTag {
    private int tagId;
    private String dutch;
    private String english;

    public LocationTag(int tagId, String dutch, String english) {
        this.tagId = tagId;
        this.dutch = dutch;
        this.english = english;
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

}
