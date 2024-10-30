package blok2.model;

import blok2.model.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "authority")
public class Authority implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authority_id")
    private int authorityId;

    @Column(name = "authority_name")
    private String authorityName;

    @Column(name = "description")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "roles_user_authority",
        joinColumns = @JoinColumn(name = "authority_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    List<User> users;

    public Authority(int authorityId, String authorityName, String description) {
        this.authorityId = authorityId;
        this.authorityName = authorityName;
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
                Objects.equals(authorityName, authority.authorityName) &&
                Objects.equals(description, authority.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorityId, authorityName, description);
    }

    @Override
    public Authority clone() {
        try {
            return (Authority) super.clone();
        } catch (CloneNotSupportedException ignore) {
            // will never happen (Authority implements Cloneable)
            return null;
        }
    }

    // Note: don't use ToStringBuilder since users are fetched lazily and that may cause problems
    @Override
    public String toString() {
        return "Authority{" +
                "authorityId=" + authorityId +
                ", authorityName='" + authorityName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public int getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(int authorityId) {
        this.authorityId = authorityId;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
