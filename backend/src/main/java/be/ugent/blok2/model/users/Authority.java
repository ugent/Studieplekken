package be.ugent.blok2.model.users;

import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

/**
 * Wrapper class for the enum Role. This class can be used by spring
 * to check a user has the correct role to user certain methods of the REST API.
 */
public class Authority implements GrantedAuthority {

    private final Role role;

    public Authority(Role role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return role == authority.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(role);
    }
}
