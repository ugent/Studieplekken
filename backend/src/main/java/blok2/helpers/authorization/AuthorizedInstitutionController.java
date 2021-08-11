package blok2.helpers.authorization;

import blok2.daos.IBuildingDao;
import blok2.daos.ILocationDao;
import blok2.model.Building;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizedInstitutionController {

    private final ILocationDao locationDao;
    private final IBuildingDao buildingDao;

    @Autowired
    public AuthorizedInstitutionController(ILocationDao locationDao, IBuildingDao buildingDao) {
        this.locationDao = locationDao;
        this.buildingDao = buildingDao;
    }

    /**
     * Check if the given user can alter the location with given ID.
     * Returns true if the user is an admin.
     *
     * @param user       User that wants to alter the location.
     * @param locationId ID of the location that the user wants to alter.
     * @return whether or not the given user can alter the location with the given ID.
     */
    public boolean hasAuthorityLocation(User user, int locationId) {
        // Admin can alter all locations.
        if (user.isAdmin()) {
            return true;
        }

        // Only users with authority AND institution equal to the institution of the location/building they want to alter are allowed to alter that location.
        if (user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("HAS_AUTHORITIES"))) {
            return false;
        }
        Location location = locationDao.getLocationById(locationId);
        return location != null && location.getBuilding().getInstitution().equals(user.getInstitution());
    }

    /**
     * Check if the given user can alter the building with given ID.
     * Returns true if the user is an admin.
     *
     * @param user       User that wants to alter the building.
     * @param buildingId ID of the building that the user wants to alter.
     * @return whether or not the given user can alter the building with the given ID.
     */
    public boolean hasAuthorityBuilding(User user, int buildingId) {
        // Admin can alter all locations.
        if (user.isAdmin()) {
            return true;
        }

        // Only users with authority AND institution equal to the institution of the location they want to alter are allowed to alter that location.
        if (user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("HAS_AUTHORITIES"))) {
            return false;
        }
        Building building = buildingDao.getBuildingById(buildingId);
        return building != null && building.getInstitution().equals(user.getInstitution());
    }
}
