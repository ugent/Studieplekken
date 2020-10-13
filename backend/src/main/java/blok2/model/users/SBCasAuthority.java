package blok2.model.users;

import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

/**
 * SBCasAuthority stands for Spring Boot CAS Authority, this name is chosen
 * to avoid conflicts with the blok2.model.Authority.
 *
 * It is a wrapper class around the blok2.model.users.Role enumeration class.
 * This class will be used by Spring Security to check whether or not a
 * logged in user is granted authority or not.
 */
public class SBCasAuthority implements GrantedAuthority {

    private final Role role;

    public SBCasAuthority(Role role) {
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
        SBCasAuthority that = (SBCasAuthority) o;
        return role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(role);
    }
}
