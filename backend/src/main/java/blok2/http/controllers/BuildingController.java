package blok2.http.controllers;

import blok2.database.dao.IActionLogDao;
import blok2.database.dao.IBuildingDao;
import blok2.extensions.exceptions.NotAuthorizedException;
import blok2.model.ActionLogEntry;
import blok2.model.Building;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("building")
public class BuildingController {

    private final Logger logger = LoggerFactory.getLogger(AuthorityController.class.getSimpleName());

    private final IBuildingDao buildingDao;
    private final IActionLogDao actionLogDao;

    @Autowired
    public BuildingController(IBuildingDao buildingDao, IActionLogDao actionLogDao) {
        this.buildingDao = buildingDao;
        this.actionLogDao = actionLogDao;
    }

    // *************************************
    // *   CRUD operations for Building   *
    // *************************************/

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Building> getAllBuildings() {
        return buildingDao.getAllBuildings();
    }

    @GetMapping("/{buildingId}")
    @PreAuthorize("permitAll()")
    public Building getBuilding(@PathVariable int buildingId) {
        return buildingDao.getBuildingById(buildingId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void addBuilding(@AuthenticationPrincipal User user, @RequestBody Building building) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.INSERTION, user, ActionLogEntry.Domain.BUILDING);
        actionLogDao.addLogEntry(logEntry);
        if (!user.isAdmin()) {
            if (!building.getInstitution().equals(user.getInstitution())) {
                throw new NotAuthorizedException("You are not authorized to add a new building for this institution.");
            }
        }
        buildingDao.addBuilding(building);
        logger.info(String.format("Added building '%s' as %s", building.getName(), building));
    }

    @PutMapping("/{buildingId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityBuilding(authentication.principal, #buildingId)")
    public void updateBuilding(@AuthenticationPrincipal User user, @PathVariable int buildingId, @RequestBody Building building) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.UPDATE, user, ActionLogEntry.Domain.BUILDING, buildingId);
        actionLogDao.addLogEntry(logEntry);
        if (!user.isAdmin()) {
            if (!building.getInstitution().equals(user.getInstitution()) || !buildingDao.getBuildingById(buildingId).getInstitution().equals(user.getInstitution())) {
                throw new NotAuthorizedException("You are not authorized to update a building to an institution other than the one you belong to.");
            }
        }
        buildingDao.updateBuilding(building);
        logger.info(String.format("Updated building with id '%d' to %s", buildingId, building));
    }

    @DeleteMapping("/{buildingId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteBuilding(@PathVariable int buildingId, @AuthenticationPrincipal User user) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.DELETION, user, ActionLogEntry.Domain.BUILDING, buildingId);
        actionLogDao.addLogEntry(logEntry);
        buildingDao.deleteBuilding(buildingId);
        logger.info(String.format("Removed building with id '%d'", buildingId));
    }
}
