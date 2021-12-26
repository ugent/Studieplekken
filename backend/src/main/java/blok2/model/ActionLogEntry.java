package blok2.model;

import blok2.model.users.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_log")
public class ActionLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private int id;

    @Column(name = "type")
    private String type;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id"
    )
    private User user;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime time;

    public ActionLogEntry() {

    }

    public ActionLogEntry(ActionType type, String description, User user) {
        this.type = type.name();
        this.description = description;
        this.user = user;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public ActionType getTypeE() {
        return ActionType.valueOf(this.type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType(ActionType type) {
        setType(type.name());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public static enum ActionType {
        INSERTION,
        DELETION,
        UPDATE,
        OTHER
    }

}
