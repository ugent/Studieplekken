package blok2.model;

public class Token {

    private String token;
    private String purpose;
    private String email;
    private boolean isUsed;

    public Token() {
    }

    public Token(String token, String purpose, String email, boolean isUsed) {
        this.token = token;
        this.purpose = purpose;
        this.email = email;
        this.isUsed = isUsed;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(boolean used) {
        isUsed = used;
    }

    @Override
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                ", purpose='" + purpose + '\'' +
                ", email='" + email + '\'' +
                ", isUsed=" + isUsed + '\'' +
                '}';
    }
}
