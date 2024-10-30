package blok2.model.translations;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="translatables")
public class Translatable {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "translations")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "language")
    @Column(name = "value")
    @NotEmpty
    private Map<Language, String> translations = new HashMap<>();
}
