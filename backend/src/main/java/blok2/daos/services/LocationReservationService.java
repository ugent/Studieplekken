package blok2.daos.services;

import blok2.daos.ILocationReservationDao;
import blok2.daos.db.DBLocationReservationDao;
import blok2.daos.repositories.LocationReservationRepository;
import blok2.daos.repositories.UserRepository;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.threeten.extra.YearWeek;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationReservationService implements ILocationReservationDao {

    private final LocationReservationRepository locationReservationRepository;
    private final UserRepository userRepository;

    private final DBLocationReservationDao locationReservationDao;

    @Autowired
    public LocationReservationService(LocationReservationRepository locationReservationRepository,
                                      UserRepository userRepository,
                                      DBLocationReservationDao locationReservationDao) {
        this.locationReservationRepository = locationReservationRepository;
        this.userRepository = userRepository;
        this.locationReservationDao = locationReservationDao;
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfUser(String userId) {
        return locationReservationRepository.findAllByUserId(userId);
    }

    @Override
    public LocationReservation getLocationReservation(String userId, Timeslot timeslot) {
        LocationReservation.LocationReservationId id = new LocationReservation.LocationReservationId(
                timeslot.getTimeslotSeqnr(), userId
        );

        return locationReservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location reservation found with id '%s'", id)));
    }

    @Override
    // TODO: update this method after timeslot redesign
    public List<LocationReservation> getUnattendedLocationReservations(LocalDate date) {
        return locationReservationRepository.findAllUnattendedByDate(YearWeek.from(date).getYear(), YearWeek.from(date).getWeek(), DayOfWeek.from(date));
    }


    // TODO: Fix this method
    @Override
    public List<User> getUsersWithReservationForWindowOfTime(LocalDate start, LocalDate end) {

        return Collections.emptyList();
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfTimeslot(Timeslot timeslot) {
        return locationReservationRepository.findAllByTimeslot(
                timeslot.getTimeslotSeqnr()
        );
    }

    @Override
    public long countReservedSeatsOfTimeslot(Timeslot timeslot) {
        return locationReservationRepository.countReservedSeatsOfTimeslot(
                timeslot.getTimeslotSeqnr());
    }

    @Override
    public int amountOfReservationsRightNow(int locationId) {
        return locationReservationRepository.getLocationReservationsAtLocationAtThisMoment(locationId).size();
    }

    @Override
    public void deleteLocationReservation(String userId, Timeslot timeslot) {
        locationReservationRepository.deleteById(new LocationReservation.LocationReservationId(
                timeslot.getTimeslotSeqnr(), userId
        ));
    }

    @Override
    public LocationReservation addLocationReservation(LocationReservation locationReservation) {
        return locationReservationRepository.saveAndFlush(locationReservation);
    }

    @Override
    @Transactional
    public boolean setReservationAttendance(String userId, Timeslot timeslot, boolean attendance) {
        LocationReservation.LocationReservationId id = new LocationReservation.LocationReservationId(
                timeslot.getTimeslotSeqnr(), userId
        );

        LocationReservation locationReservation = locationReservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location reservation found with id '%s'", id)));
        Boolean currentAttendance = locationReservation.getAttended();

        // if attendance goes from null or true to false, decrement reservation count
        if (!attendance && (currentAttendance == null || currentAttendance))
            locationReservation.getTimeslot().decrementAmountOfReservations();

        // if attendance goes from false to true, increment the current reservation count since this person is here now
        if (attendance && (currentAttendance != null && !currentAttendance))
            locationReservation.getTimeslot().incrementAmountOfReservations();

        locationReservation.setAttended(attendance);
        locationReservation = locationReservationRepository.saveAndFlush(locationReservation);

        return locationReservation.getAttended() == attendance;
    }

    @Override
    public boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException {
        return locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation);
    }

    @Override
    public void setNotScannedStudentsToUnattended(Timeslot timeslot) {
        List<LocationReservation> locationReservations = locationReservationRepository.findAllUnknownAttendanceByTimeslot(
                timeslot.getTimeslotSeqnr()
        );

        // for each unattended reservation, set the attendance to false and decrement the reservation count
        // so that other students are able to make use of the freed spot
        locationReservations.forEach((LocationReservation lr) -> {
            lr.setAttended(false);
            lr.getTimeslot().decrementAmountOfReservations();
        });

        locationReservationRepository.saveAll(locationReservations);
    }

    @Override
    public List<LocationReservation> getAllFutureLocationReservationsOfLocation(int locationId) {
        YearWeek n = YearWeek.now();
        return locationReservationRepository.findAllByLocationIdAndDateAfter(locationId, n.getYear(), n.getWeek(), LocalDate.now().getDayOfWeek().getValue());
    }

}
