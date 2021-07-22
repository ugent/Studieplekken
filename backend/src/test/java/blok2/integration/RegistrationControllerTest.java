package blok2.integration;

import blok2.helpers.Pair;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class RegistrationControllerTest extends BaseIntegrationTest {

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testGetReservationsOfStudentAsAdmin() throws Exception {
        mockMvc.perform(get("/locations/reservations/user?id=" + student.getUserId()).with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetReservationsOfStudentAsSelf() throws Exception {
        mockMvc.perform(get("/locations/reservations/user?id=" + student.getUserId()).with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testGetReservationsOfStudentAsForbidden() throws Exception {
        mockMvc.perform(get("/locations/reservations/user?id=" + student.getUserId()).with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testCreateReservation() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(1);

        mockMvc.perform(post("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(timeslot)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(3, list.size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testCreateReservationDuplicate() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        mockMvc.perform(post("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(timeslot)).contentType("application/json")).andDo(print())
                .andExpect(status().isConflict());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(2, list.size());
    }

    // todo more tests for more specific calendar periods

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservation() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        LocationReservation reservation = new LocationReservation(student, timeslot, false);

        mockMvc.perform(delete("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(reservation)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(1, list.size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservationAsAdmin() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        LocationReservation reservation = new LocationReservation(student, timeslot, false);

        mockMvc.perform(delete("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(reservation)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(1, list.size());

        // You can manually check in the logs if the mail was sent by looking for
        // Blocked sending mail to 'student1@ugent.be' with template file name 'mail/reservation_slot_deleted' and subject '[Werk- en Studieplekken] Uw gereserveerd tijdslot werd verwijderd' because 'mail' is not an active profile.
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservationAsOther() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        LocationReservation reservation = new LocationReservation(student, timeslot, false);

        mockMvc.perform(delete("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(reservation)).contentType("application/json")).andDo(print())
                .andExpect(status().isForbidden());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(2, list.size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendance() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        String url = String.format("/locations/reservations/%s/%d/%s/%d/attendance",
                student.getUserId(), timeslot.getCalendarId(),
                timeslot.getTimeslotDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", true);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(true, list.get(1).getAttended());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceNonExisting() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        String url = String.format("/locations/reservations/%s/%d/%s/%d/attendance",
                student2.getUserId(), timeslot.getCalendarId(),
                timeslot.getTimeslotDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", true);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isNotFound());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertNull(list.get(0).getAttended());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceNoAmountReservationDecrease() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        String url = String.format("/locations/reservations/%s/%d/%s/%d/attendance",
                student.getUserId(), timeslot.getCalendarId(),
                timeslot.getTimeslotDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", true);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(true, list.get(1).getAttended());

        Assert.assertEquals(timeslot.getAmountOfReservations(), timeslotAfterUpdate.getAmountOfReservations());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceAmountReservationsDecrease() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        String url = String.format("/locations/reservations/%s/%d/%s/%d/attendance",
                student.getUserId(), timeslot.getCalendarId(),
                timeslot.getTimeslotDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", false);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(false, list.get(1).getAttended());

        // when a user is set to unattended, the spot must be released so that others can make a reservation
        Assert.assertEquals(timeslot.getAmountOfReservations() - 1, timeslotAfterUpdate.getAmountOfReservations());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceAmountReservationsDecreaseOnlyOnce() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        String url = String.format("/locations/reservations/%s/%d/%s/%d/attendance",
                student.getUserId(), timeslot.getCalendarId(),
                timeslot.getTimeslotDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", false);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);


        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(false, list.get(1).getAttended());

        Assert.assertEquals(timeslot.getAmountOfReservations() - 1, timeslotAfterUpdate.getAmountOfReservations());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceThenDelete() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        String url = String.format("/locations/reservations/%s/%d/%s/%d/attendance",
                student.getUserId(), timeslot.getCalendarId(),
                timeslot.getTimeslotDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", false);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);
        Assert.assertEquals(timeslot.getAmountOfReservations() - 1, timeslotAfterUpdate.getAmountOfReservations());

        LocationReservation lr = new LocationReservation(student, timeslot, false);
        mockMvc.perform(delete("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(lr)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        timeslotAfterUpdate = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);
        Assert.assertEquals(timeslot.getAmountOfReservations() - 1, timeslotAfterUpdate.getAmountOfReservations());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAllUnknownUnattended() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        // Adding one extra reservation which doesn't get discounted
        locationReservationDao.addLocationReservationIfStillRoomAtomically(new LocationReservation(student2, timeslot, null));
        locationReservationDao.setReservationAttendance(student2.getUserId(), timeslot, true);
        timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);
        Assert.assertEquals(2, timeslot.getAmountOfReservations());


        mockMvc.perform(put("/locations/reservations/not-scanned").with(csrf()).content(objectMapper.writeValueAsString(timeslot)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);
        Assert.assertEquals(1, timeslotAfterUpdate.getAmountOfReservations());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        System.out.println(list);
        Assert.assertEquals(false, list.get(1).getAttended());
        list = locationReservationDao.getAllLocationReservationsOfUser(student2.getUserId());
        Assert.assertEquals(true, list.get(0).getAttended());

    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testGetUnattendedLocationReservations() throws Exception {
        // Add a calendar period and a reservation for it with the date of yesterday.
        List<CalendarPeriod> calendarPeriods = new ArrayList<>();
        CalendarPeriod period = new CalendarPeriod();
        period.setLocation(testLocation);

        LocalDate date = LocalDate.now();
        if (LocalTime.now().isAfter(LocalTime.of(21, 0))) {
            // Running tests after 21PM so we should get unattended reservations for the next day otherwise we would not send any mails in our test.
            // This is needed because we cannot manipulate LastModifiedDate in our tests.
            date = date.plusDays(1);
        }

        period.setStartsAt(date.minusDays(1));
        period.setEndsAt(date.minusDays(1));
        period.setOpeningTime(LocalTime.of(9, 0));
        period.setClosingTime(LocalTime.of(17, 0));
        period.setReservableFrom(LocalDateTime.of(date.minusDays(1), LocalTime.of(0, 0)));
        period.setReservable(true);
        period.setTimeslotLength(30);
        period.setSeatCount(testLocation.getNumberOfSeats());
        period.initializeLockedFrom();
        calendarPeriods.add(period);
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);

        Timeslot timeslot = calendarPeriodDao.getById(period.getId()).getTimeslots().get(0);
        locationReservationDao.addLocationReservationIfStillRoomAtomically(new LocationReservation(student2, timeslot, null));
        // Set the attendance to false now, so it is changed less than 24 hours ago which is when the previous unattendance mails were sent.
        locationReservationDao.setReservationAttendance(student2.getUserId(), timeslot, false);

        // Attendance for student is true so should not send mail for this reservation.
        locationReservationDao.addLocationReservationIfStillRoomAtomically(new LocationReservation(student, timeslot, null));
        locationReservationDao.setReservationAttendance(student.getUserId(), timeslot, true);

        // Attendance for admin is still null so should not send mail for this reservation.
        locationReservationDao.addLocationReservationIfStillRoomAtomically(new LocationReservation(admin, timeslot, null));

        List<Pair<LocationReservation, CalendarPeriod>> reservations =
                locationReservationDao.getUnattendedLocationReservationsWith21PMRestriction(date);

        Assert.assertEquals(1, reservations.size());
    }

}
