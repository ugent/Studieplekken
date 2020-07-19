package be.ugent.blok2.helpers.exceptions;

public class LdapException extends RuntimeException {
    public LdapException(String msg) { super(msg); }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
