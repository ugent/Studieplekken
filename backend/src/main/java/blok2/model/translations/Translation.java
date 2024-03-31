package blok2.model.translations;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="translations")
public class Translation {
    @EmbeddedId
    private TranslationId id;

    @Column(name="value")
    private String value;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Embeddable
    public static class TranslationId implements Serializable {
        @GeneratedValue
        @Column(name="id")
        private Long id;

        @Enumerated(EnumType.STRING)
        @Column(name="language")
        private Language language;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Language getLanguage() {
            return this.language;
        }

        public void setLanguage(Language language) {
            this.language = language;
        }
    }
}