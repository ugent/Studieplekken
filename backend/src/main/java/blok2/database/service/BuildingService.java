package blok2.database.service;

import blok2.database.dao.IBuildingDao;
import blok2.database.repository.BuildingRepository;
import blok2.database.repository.LocationRepository;
import blok2.exception.NoSuchDatabaseObjectException;
import blok2.model.Building;
import blok2.model.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService implements IBuildingDao {

    private final BuildingRepository buildingRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public BuildingService(BuildingRepository buildingRepository,
                           LocationRepository locationRepository) {
        this.buildingRepository = buildingRepository;
        this.locationRepository = locationRepository;
    }
    
    @Override
    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }

    @Override
    public Building getBuildingById(int buildingId) {
        return buildingRepository.findById(buildingId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No building found with buildingId '%d'", buildingId)));
    }

    @Override
    public List<Location> getLocationsInBuilding(int buildingId) {
        return locationRepository.findAllByBuildingId(buildingId);
    }

    @Override
    public Building addBuilding(Building building) {
        return buildingRepository.saveAndFlush(building);
    }

    @Override
    public void updateBuilding(Building building) {
        buildingRepository.save(building);
    }

    @Override
    public void deleteBuilding(int buildingId) {
        buildingRepository.deleteById(buildingId);
    }
    
}
