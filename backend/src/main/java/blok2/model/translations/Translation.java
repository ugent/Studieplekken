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
@IdClass(TranslationId.class)
public class Translation {
    @Id
    @NotNull
    @SequenceGenerator(name="translations_id_generator", sequenceName="translations_id_seq", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "translations_id_generator")
    @Column(name="id")
    private Long id;

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="language")
    private Language language;

    @NotNull
    @Column(name="value")
    private String value;
}
