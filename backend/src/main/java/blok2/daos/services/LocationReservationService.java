package blok2.daos.services;

import blok2.daos.ICalendarPeriodDao;
import blok2.daos.ILocationReservationDao;
import blok2.daos.db.DBLocationReservationDao;
import blok2.daos.repositories.LocationReservationRepository;
import blok2.daos.repositories.UserRepository;
import blok2.helpers.Pair;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationReservationService implements ILocationReservationDao {

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao; // TODO: delete after timeslot redesign

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
    // TODO: update this method after timeslot redesign
    public List<Pair<LocationReservation, CalendarPeriod>> getAllLocationReservationsAndCalendarPeriodsOfUser(String userId) {
        List<LocationReservation> locationReservations = locationReservationRepository.findAllByUserId(userId);
        return getLrAndCp(locationReservations);
    }

    @Override
    public LocationReservation getLocationReservation(String userId, Timeslot timeslot) {
        LocationReservation.LocationReservationId id = new LocationReservation.LocationReservationId(
                timeslot.getTimeslotSeqnr(), timeslot.getTimeslotDate(), timeslot.getCalendarId(), userId
        );

        return locationReservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location reservation found with id '%s'", id)));
    }

    @Override
    // TODO: update this method after timeslot redesign
    public List<Pair<LocationReservation, CalendarPeriod>> getUnattendedLocationReservations(LocalDate date) {
        List<LocationReservation> locationReservations = locationReservationRepository.findAllUnattendedByDate(date);
        return getLrAndCp(locationReservations);
    }

    @Override
    public List<User> getUsersWithReservationForWindowOfTime(LocalDate start, LocalDate end) {
        List<LocationReservation> locationReservations = locationReservationRepository.findAllInWindowOfTime(start, end);
        List<String> userIds = locationReservations
                .stream()
                .map((LocationReservation lr) -> lr.getId().userId)
                .collect(Collectors.toList());
        return userRepository.findAllById(userIds);
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfTimeslot(Timeslot timeslot) {
        return locationReservationRepository.findAllByTimeslot(
                timeslot.getTimeslotSeqnr(), timeslot.getTimeslotDate(), timeslot.getCalendarId()
        );
    }

    @Override
    public long countReservedSeatsOfTimeslot(Timeslot timeslot) {
        return locationReservationRepository.countReservedSeatsOfTimeslot(
                timeslot.getTimeslotSeqnr(), timeslot.getTimeslotDate(), timeslot.getCalendarId()
        );
    }

    @Override
    public int amountOfReservationsRightNow(int locationId) {
        return locationReservationRepository.getLocationReservationsAtLocationAtThisMoment(locationId).size();
    }

    @Override
    public void deleteLocationReservation(String userId, Timeslot timeslot) {
        locationReservationRepository.deleteById(new LocationReservation.LocationReservationId(
                timeslot.getTimeslotSeqnr(), timeslot.getTimeslotDate(), timeslot.getCalendarId(), userId
        ));
    }

    @Override
    public LocationReservation addLocationReservation(LocationReservation locationReservation) {
        return locationReservationRepository.save(locationReservation);
    }

    @Override
    public boolean setReservationAttendance(String userId, Timeslot timeslot, boolean attendance) {
        LocationReservation.LocationReservationId id = new LocationReservation.LocationReservationId(
                timeslot.getTimeslotSeqnr(), timeslot.getTimeslotDate(), timeslot.getCalendarId(), userId
        );

        LocationReservation locationReservation = locationReservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No location reservation found with id '%s'", id)));

        locationReservation.setAttended(attendance);
        locationReservation = locationReservationRepository.save(locationReservation);

        return locationReservation.getAttended() == attendance;
    }

    @Override
    public boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException {
        return locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation);
    }

    @Override
    public void setNotScannedStudentsToUnattended(Timeslot timeslot) {
        List<LocationReservation> locationReservations = locationReservationRepository.findAllUnattendedByTimeslot(
                timeslot.getTimeslotSeqnr(), timeslot.getTimeslotDate(), timeslot.getCalendarId()
        );

        locationReservations.forEach((LocationReservation lr) -> lr.setAttended(false));

        locationReservationRepository.saveAll(locationReservations);
    }

    // TODO: remove/update this method after timeslot redesign
    private List<Pair<LocationReservation, CalendarPeriod>> getLrAndCp(List<LocationReservation> locationReservations) {
        List<Pair<LocationReservation, CalendarPeriod>> ret = new ArrayList<>();

        locationReservations.forEach((LocationReservation lr) -> {
            try {
                CalendarPeriod cp = calendarPeriodDao.getById(lr.getId().calendarId);
                ret.add(new Pair<>(lr, cp));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        return ret;
    }

}
