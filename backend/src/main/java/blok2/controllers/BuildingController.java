package blok2.controllers;

import blok2.daos.IBuildingDao;
import blok2.model.Building;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("building")
public class BuildingController {

    private final Logger logger = LoggerFactory.getLogger(AuthorityController.class.getSimpleName());

    private final IBuildingDao buildingDao;

    @Autowired
    public BuildingController(IBuildingDao buildingDao) {
        this.buildingDao = buildingDao;
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
    public void addBuilding(@RequestBody Building building) {
        buildingDao.addBuilding(building);
        logger.info(String.format("Added building '%s' as %s", building.getName(), building));
    }

    @PutMapping("/{buildingId}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void updateBuilding(@PathVariable int buildingId, @RequestBody Building building) {
        buildingDao.updateBuilding(building);
        logger.info(String.format("Updated building with id '%d' to %s", buildingId, building));
    }

    @DeleteMapping("/{buildingId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteBuilding(@PathVariable int buildingId) {
        buildingDao.deleteBuilding(buildingId);
        logger.info(String.format("Removed building with id '%d'", buildingId));
    }
}
