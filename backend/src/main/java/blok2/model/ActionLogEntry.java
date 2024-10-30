package blok2.model;

import blok2.model.users.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime time;

    public ActionLogEntry(Type type, User user, Domain domain) {
        this(type, user, domain, 0);
    }

    public ActionLogEntry(Type type, User user, Domain domain, int domainId) {
        this.type = type.name();
        this.domain = domain.name();
        this.user = user;
        this.domainId = domainId;
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
