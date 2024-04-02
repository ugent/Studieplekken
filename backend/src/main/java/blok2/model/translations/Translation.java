package blok2.model.translations;

import blok2.model.FaqItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

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
    @GenericGenerator(name = "translation_id_generator", strategy = "blok2.model.generator.NullAwareGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = "sequence_name", value = "translations_id_seq")
        }
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "translation_id_generator")
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
