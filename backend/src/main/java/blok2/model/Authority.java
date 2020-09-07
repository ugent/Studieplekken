package blok2.model;

import java.util.Objects;

public class Authority implements Cloneable {
    private int authorityId;
    private String name;
    private String description;

    public Authority(int authorityId, String name, String description) {
        this.authorityId = authorityId;
        this.name = name;
        this.description = description;
    }

    public Authority() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return Objects.equals(authorityId, authority.authorityId) &&
                Objects.equals(name, authority.name) &&
                Objects.equals(description, authority.description);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(int authorityId) {
        this.authorityId = authorityId;
    }
}
