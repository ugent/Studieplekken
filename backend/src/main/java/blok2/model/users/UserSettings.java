package blok2.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_settings")
public class UserSettings {

    @Id
    @Column(name="user_id")
    @JsonIgnore
    private String userId;

    @Column(name="receive_mail_confirmation")
    private boolean receiveMailConfirmation;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isReceiveMailConfirmation() {
        return receiveMailConfirmation;
    }

    public void setReceiveMailConfirmation(boolean receiveMailConfirmation) {
        this.receiveMailConfirmation = receiveMailConfirmation;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "userId='" + userId + '\'' +
                ", receiveMailConfirmation=" + receiveMailConfirmation +
                '}';
    }
}
