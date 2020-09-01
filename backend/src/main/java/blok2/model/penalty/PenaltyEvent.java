package blok2.model.penalty;

import blok2.helpers.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PenaltyEvent {
    private int code;
    private int points;
    private boolean publicAccessible;
    private Map<Language, String> descriptions;

    public static final int CODE_LATE_CANCEL = 16660;
    public static final int CODE_NO_SHOWUP = 16661;
    public static final int CODE_BLACKLIST_EVENT = 16662;

    public PenaltyEvent() {
        descriptions = new HashMap<>();
    }

    public PenaltyEvent(int code, int points, boolean publicAccessible, Map<Language, String> descriptions) {
        this.code = code;
        this.points = points;
        this.publicAccessible = publicAccessible;
        this.descriptions = descriptions;
    }

    @Override
    public PenaltyEvent clone() {
        Map<Language, String> _descriptions = new HashMap<>();
        for (Language l : descriptions.keySet())
            _descriptions.put(l, descriptions.get(l));
        return new PenaltyEvent(code, points, publicAccessible, _descriptions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        PenaltyEvent that = (PenaltyEvent) o;

        return that.code == code
                && that.points == points
                && that.publicAccessible == publicAccessible
                && Objects.equals(descriptions, that.descriptions);
    }

    @Override
    public String toString() {
        return "PenaltyEvent{" +
                "code=" + code +
                ", points=" + points +
                ", publicAccessible=" + publicAccessible +
                ", descriptions=" + descriptions +
                '}';
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public int getCode() {
        return code;
    }

    public int getPoints() {
        return points;
    }

    public boolean getPublicAccessible() {
        return publicAccessible;
    }

    public Map<Language, String> getDescriptions() {
        return descriptions;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setPublicAccessible(boolean publicAccessible) {
        this.publicAccessible = publicAccessible;
    }

    public void setDescriptions(Map<Language, String> descriptions) {
        this.descriptions = descriptions;
    }

    //</editor-fold>
}
