package blok2.extensions.helpers;

import blok2.model.location.Location;

import javax.validation.Valid;

public class LocationWithApproval {
    @Valid
    Location location;
    boolean approval;

    public LocationWithApproval(@Valid Location location, boolean approval) {
        this.location = location;
        this.approval = approval;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isApproval() {
        return approval;
    }

    public void setApproval(boolean approval) {
        this.approval = approval;
    }
}
