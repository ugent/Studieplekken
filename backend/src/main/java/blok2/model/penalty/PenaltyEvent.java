package blok2.model.penalty;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "penalty_events")
public final class PenaltyEvent implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code")
    private Integer code;

    @Column(name = "points")
    private int points;

    @Column(name = "description_dutch")
    private String descriptionDutch;

    @Column(name = "description_english")
    private String descriptionEnglish;

    public PenaltyEvent() {

    }

    public PenaltyEvent(Integer code, int points, String descriptionDutch, String descriptionEnglish) {
        this.code = code;
        this.points = points;
        this.descriptionDutch = descriptionDutch;
        this.descriptionEnglish = descriptionEnglish;
    }

    @Override
    public PenaltyEvent clone() {
        try {
            return (PenaltyEvent) super.clone();
        } catch (CloneNotSupportedException ignore) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        PenaltyEvent that = (PenaltyEvent) o;

        return Objects.equals(code, that.code) &&
                that.points == points &&
                Objects.equals(descriptionDutch, that.descriptionDutch) &&
                Objects.equals(descriptionEnglish, that.descriptionEnglish);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getDescriptionDutch() {
        return descriptionDutch;
    }

    public void setDescriptionDutch(String descriptionDutch) {
        this.descriptionDutch = descriptionDutch;
    }

    public String getDescriptionEnglish() {
        return descriptionEnglish;
    }

    public void setDescriptionEnglish(String descriptionEnglish) {
        this.descriptionEnglish = descriptionEnglish;
    }

    //</editor-fold>

}
