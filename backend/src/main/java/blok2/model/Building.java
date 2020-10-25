package blok2.model;

import java.util.Objects;

public class Building implements Cloneable {

    private int buildingId;
    private String name;
    private String address;

    public Building() {}

    public Building(int buildingId, String name, String address) {
        this.buildingId = buildingId;
        this.name = name;
        this.address = address;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Building)) return false;
        Building building = (Building) o;
        return buildingId == building.buildingId &&
                name.equals(building.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildingId, name);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Building{" +
                "buildingId=" + buildingId +
                ", name='" + name + '\'' +
                '}';
    }


}
