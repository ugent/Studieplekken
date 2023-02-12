package blok2.model.stats;

import javax.persistence.*;
import java.util.Map;

@Entity
public class InstitutionOverviewStat {

    @Id
    private String institution;

    @ElementCollection
    @MapKeyColumn(name = "HOI")
    @Column(name = "count")
    private Map<String, Long> outgoingStudentsPerHOI;

    @ElementCollection
    @MapKeyColumn(name = "HOI")
    @Column(name = "count")
    private Map<String, Long> incomingStudentsPerHOI;

    @ElementCollection
    @MapKeyColumn(name = "date")
    @Column(name = "count")
    private Map<String, Long> reservationsPerDay;

    public InstitutionOverviewStat() {
    }

    public InstitutionOverviewStat(String institution, Map<String, Long> outgoingStudentsPerHOI, Map<String, Long> incomingStudentsPerHOI, Map<String, Long> reservationsPerDay) {
        this.institution = institution;
        this.outgoingStudentsPerHOI = outgoingStudentsPerHOI;
        this.incomingStudentsPerHOI = incomingStudentsPerHOI;
        this.reservationsPerDay = reservationsPerDay;
    }

    public String getInstitution() {
        return institution;
    }

    public Map<String, Long> getOutgoingStudentsPerHOI() {
        return outgoingStudentsPerHOI;
    }

    public Map<String, Long> getIncomingStudentsPerHOI() {
        return incomingStudentsPerHOI;
    }

    public Map<String, Long> getReservationsPerDay() {
        return reservationsPerDay;
    }
}
