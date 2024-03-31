package blok2.model.translations;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="translations")
@IdClass(TranslationId.class)
@GenericGenerator(
    name = "translation_id_generator",
    strategy = "blok2.model.generator.NullAwareSequenceStyleGenerator",
    parameters = {
        @Parameter(name = "sequence_name", value = "translations_id_seq")
    }
)
public class Translation {
    @Id
    @NotNull

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
