package blok2.model.translations;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Data
public class TranslationId implements Serializable {
    private Long id;
    private Language language;
}
