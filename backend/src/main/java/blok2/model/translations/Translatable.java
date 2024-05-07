package blok2.model.translations;

import blok2.model.FaqItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="translations")
public class Translation {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "translations")
    @MapKeyColumn(name = "language")
    @Column(name = "value")
    private Map<String, String> values;
}
