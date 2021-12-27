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

    @Column(name = "domain")
    private String domain;

    @Column(name = "domain_id")
    private int domainId;

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

    public ActionLogEntry(Type type, User user, Domain domain) {
        this(type, user, domain, 0);
    }

    public ActionLogEntry(Type type, User user, Domain domain, int domainId) {
        this.type = type.name();
        this.domain = domain.name();
        this.user = user;
        this.domainId = domainId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public Domain getDomainE() {
        return Domain.valueOf(this.domain);
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setDomain(Domain domain) {
        setDomain(domain.name());
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public String getType() {
        return type;
    }

    public Type getTypeE() {
        return Type.valueOf(this.type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType(Type type) {
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

    public static enum Type {
        INSERTION,
        DELETION,
        UPDATE,
        OTHER
    }

    public static enum Domain {
        LOCATION,
        BUILDING,
        AUTHORITY,
        USER,
        PASSWORD
    }

}
