package blok2.model;

import blok2.model.translations.Translatable;
import blok2.model.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "faq_items")
public class FaqItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("items")
    private FaqCategory category;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "title_translatable_id")
    private Translatable title;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "content_translatable_id")
    private Translatable content;

    @OneToOne
    @JoinColumn(name = "created_by_user_id")
    private User user;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
