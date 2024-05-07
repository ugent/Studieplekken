package blok2.model;

import blok2.model.translations.Translatable;
import blok2.model.users.User;
import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "faq_categories")
public class FaqCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "name_translatable_id")
    private Translatable name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "description_translatable_id")
    private Translatable description;

    @OneToMany(mappedBy = "category")
    @JsonIgnoreProperties("category")
    private List<FaqItem> items;

    @OneToMany(mappedBy = "parent")
    @JsonIgnoreProperties({"parent", "items"})
    private List<FaqCategory> children;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("items")
    private FaqCategory parent;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User user;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
