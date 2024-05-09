package blok2.model.translations;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="translations")
public class Translation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="translatable_id")
    private Translatable translatable;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="language")
    private Language language;

    @NotNull
    @Column(name="value")
    private String value;
}
