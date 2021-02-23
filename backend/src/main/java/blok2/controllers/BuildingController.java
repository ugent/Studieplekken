package blok2.controllers;

import blok2.daos.IBuildingDao;
import blok2.helpers.exceptions.NoSuchBuildingException;
import blok2.model.Building;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
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
        try {
            return buildingDao.getAllBuildings();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{buildingId}")
    @PreAuthorize("permitAll()")
    public Building getBuilding(@PathVariable int buildingId) {
        try {
            Building building = buildingDao.getBuildingById(buildingId);
            if(building == null) {
                throw new NoSuchBuildingException("No such building");
            }
            return building;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void addBuilding(@RequestBody Building building) {
        try {
            buildingDao.addBuilding(building);
            logger.info(String.format("Added building '%s' as %s", building.getName(), building.toString()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{buildingId}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void updateBuilding(@PathVariable int buildingId, @RequestBody Building building) {
        try {
            building.setBuildingId(buildingId);
            buildingDao.updateBuilding(building);
            logger.info(String.format("Updated building with id '%d' to %s", buildingId, building.toString()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/{buildingId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteBuilding(@PathVariable int buildingId) {
        try {
            buildingDao.deleteBuilding(buildingId);
            logger.info(String.format("Removed building with id '%d'", buildingId));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
