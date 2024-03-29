package blok2.model.translations;

import javax.persistence.*;

@Entity
@Table(name="translations")
public class Translation {
    @Id
    @GeneratedValue
    @Column(name="id")
    private Long id;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name="language")
    private Language language;

    @Column(name="value")
    private String value;

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

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}