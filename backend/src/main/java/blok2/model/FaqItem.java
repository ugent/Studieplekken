package blok2.model;

import blok2.model.translations.Translation;
import blok2.model.users.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="faq_items")
public class FaqItem {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private FaqCategory category;

    @OneToMany(mappedBy = "id")
    @Column(name="title_translation_id")
    private List<Translation> title;

    @OneToMany(mappedBy = "id")
    @Column(name="content_translation_id")
    private List<Translation> content;

    @OneToOne
    @JoinColumn(name = "created_by_user_id")
    private User user;

    @Column(name = "is_pinned")
    private Boolean is_pinned;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updated_at;
}
