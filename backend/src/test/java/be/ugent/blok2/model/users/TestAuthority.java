package be.ugent.blok2.model.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class TestAuthority {
    @Test
    public void testGetAuthority() {
        Role role = Role.EMPLOYEE;
        Authority authority = new Authority(role);
        assertEquals(authority.getAuthority(), role.toString());
    }

    @Test
    public void testEquals() {
        Role role = Role.STUDENT;
        Authority a1 = new Authority(role);
        Authority a2 = new Authority(role);
        assertEquals(a1, a2);
        assertEquals(a1, a1);
        assertEquals(a2, a2);
    }

    @Test
    public void testHashcode() {
        Role role = Role.EMPLOYEE;
        Authority a1 = new Authority(role);
        Authority a2 = new Authority(role);
        assertEquals(a1.hashCode(),a2.hashCode());
        assertEquals(a1.hashCode(),a1.hashCode());
        assertEquals(a2.hashCode(),a2.hashCode());
    }
}
