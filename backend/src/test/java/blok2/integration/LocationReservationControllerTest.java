package blok2.integration;

import blok2.helpers.Base64String;
import blok2.helpers.Pair;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class LocationReservationControllerTest extends BaseIntegrationTest {

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
        int currentAmount = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId()).size();

        mockMvc.perform(get("/locations/reservations/user?id=" + student.getUserId()).with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(currentAmount))
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
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testCreateReservation() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        mockMvc.perform(post("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(timeslot)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student2.getUserId());
        Assert.assertEquals(1, list.stream().filter(lr -> lr.getTimeslot().getTimeslotSeqnr() == timeslot.getTimeslotSeqnr()).count());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testCreateReservationDuplicate() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        int currentAmount = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId()).size();
        mockMvc.perform(post("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(timeslot)).contentType("application/json")).andDo(print())
                .andExpect(status().isConflict());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(currentAmount, list.size());
    }

    // todo more tests for more specific calendar periods

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservation() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        int amountOfReservations = timeslot.getAmountOfReservations();


        LocationReservation reservation = new LocationReservation(student, timeslot, LocationReservation.State.ABSENT);

        int currentAmount = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId()).size();

        mockMvc.perform(delete("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(reservation)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(currentAmount - 1, list.size());

        timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        Assert.assertEquals(timeslot.getAmountOfReservations(), amountOfReservations -1);

    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservationAsAdmin() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        int amountOfReservations = timeslot.getAmountOfReservations();


        LocationReservation reservation = new LocationReservation(student, timeslot, LocationReservation.State.ABSENT);

        int currentAmount = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId()).size();
        mockMvc.perform(delete("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(reservation)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(currentAmount-1, list.size());

        timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        Assert.assertEquals(timeslot.getAmountOfReservations(), amountOfReservations -1);


        // You can manually check in the logs if the mail was sent by looking for
        // Blocked sending mail to 'student1@ugent.be' with template file name 'mail/reservation_slot_deleted' and subject '[Werk- en Studieplekken] Uw gereserveerd tijdslot werd verwijderd' because 'mail' is not an active profile.
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservationAsOther() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        int amountOfReservations = timeslot.getAmountOfReservations();

        LocationReservation reservation = new LocationReservation(student, timeslot, LocationReservation.State.ABSENT);

        int currentAmount = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId()).size();
        mockMvc.perform(delete("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(reservation)).contentType("application/json")).andDo(print())
                .andExpect(status().isForbidden());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());

        timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        Assert.assertEquals(timeslot.getAmountOfReservations(), amountOfReservations);
        Assert.assertEquals(currentAmount, list.size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendance() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        String url = String.format("/locations/reservations/%s/%d/attendance",
                Base64String.base64Encode(student.getUserId()),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", true);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        // Check that reservation is attended
        Assert.assertEquals(LocationReservation.State.PRESENT, list.stream().filter(lr -> lr.getTimeslot().getTimeslotSeqnr() == timeslot.getTimeslotSeqnr()).findFirst().get().getStateE());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceNonExisting() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        String url = String.format("/locations/reservations/%s/%d/attendance",
                Base64String.base64Encode(student2.getUserId()),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", true);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isNotFound());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(LocationReservation.State.APPROVED, list.get(0).getStateE());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceNoAmountReservationDecrease() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        String url = String.format("/locations/reservations/%s/%d/attendance",
                Base64String.base64Encode(student.getUserId()),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", true);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        // Check that reservation is unattended
        Assert.assertEquals(LocationReservation.State.PRESENT, list.stream().filter(lr -> lr.getTimeslot().getTimeslotSeqnr() == timeslot.getTimeslotSeqnr()).findFirst().get().getStateE());

        Assert.assertEquals(timeslot.getAmountOfReservations(), timeslotAfterUpdate.getAmountOfReservations());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceAmountReservationsDecrease() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        String url = String.format("/locations/reservations/%s/%d/attendance",
                Base64String.base64Encode(student.getUserId()),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", false);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        // Check that reservation is unattended
        Assert.assertEquals(LocationReservation.State.ABSENT, list.stream().filter(lr -> lr.getTimeslot().getTimeslotSeqnr() == timeslot.getTimeslotSeqnr()).findFirst().get().getStateE());

        // when a user is set to unattended, the spot must be released so that others can make a reservation
        Assert.assertEquals(timeslot.getAmountOfReservations() - 1, timeslotAfterUpdate.getAmountOfReservations());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceAmountReservationsDecreaseOnlyOnce() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        String url = String.format("/locations/reservations/%s/%d/attendance",
                Base64String.base64Encode(student.getUserId()),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", false);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());


        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        // Check that reservation is unattended
        Assert.assertEquals(LocationReservation.State.ABSENT, list.stream().filter(lr -> lr.getTimeslot().getTimeslotSeqnr() == timeslot.getTimeslotSeqnr()).findFirst().get().getStateE());

        Assert.assertEquals(timeslot.getAmountOfReservations() - 1, timeslotAfterUpdate.getAmountOfReservations());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceThenDelete() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        String url = String.format("/locations/reservations/%s/%d/attendance",
                Base64String.base64Encode(student.getUserId()),
                timeslot.getTimeslotSeqnr());

        JSONObject obj = new JSONObject().put("attended", false);

        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        Assert.assertEquals(timeslot.getAmountOfReservations() - 1, timeslotAfterUpdate.getAmountOfReservations());

        LocationReservation lr = new LocationReservation(student, timeslot, LocationReservation.State.ABSENT);
        mockMvc.perform(delete("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(lr)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        timeslotAfterUpdate = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        Assert.assertEquals(timeslot.getAmountOfReservations() - 1, timeslotAfterUpdate.getAmountOfReservations());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAllUnknownUnattended() throws Exception {
        Timeslot timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());

        // Adding one extra reservation which doesn't get discounted
        locationReservationDao.addLocationReservationIfStillRoomAtomically(new LocationReservation(student2, timeslot, null));
        locationReservationDao.setReservationAttendance(student2.getUserId(), timeslot, true);
        timeslot = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        Assert.assertEquals(2, timeslot.getAmountOfReservations());


        mockMvc.perform(put("/locations/reservations/not-scanned").with(csrf()).content(objectMapper.writeValueAsString(timeslot)).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Timeslot timeslotAfterUpdate = timeslotDAO.getTimeslot(calendarPeriods.get(0).getTimeslotSeqnr());
        Assert.assertEquals(1, timeslotAfterUpdate.getAmountOfReservations());

        Timeslot finalTimeslot1 = timeslot;
        LocationReservation lr = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId())
                                .stream().filter(l -> l.getTimeslot().getTimeslotSeqnr() == finalTimeslot1.getTimeslotSeqnr()).findFirst().get();
        System.out.println(lr);
        Assert.assertEquals(LocationReservation.State.ABSENT, lr.getStateE());
        lr = locationReservationDao.getAllLocationReservationsOfUser(student2.getUserId()).stream().filter(l -> l.getTimeslot().getTimeslotSeqnr() == finalTimeslot1.getTimeslotSeqnr()).findFirst().get();
        Assert.assertEquals(LocationReservation.State.PRESENT, lr.getStateE());

    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testGetUnattendedLocationReservations() throws Exception {
        // Add a calendar period and a reservation for it with the date of yesterday.
        Timeslot timeslot = new Timeslot();

        timeslot.setLocation(testLocation.getLocationId());

        LocalDate date = LocalDate.now();
        if (LocalTime.now().isAfter(LocalTime.of(21, 0))) {
            // Running tests after 21PM so we should get unattended reservations for the next day otherwise we would not send any mails in our test.
            // This is needed because we cannot manipulate LastModifiedDate in our tests.
            date = date.plusDays(1);
        }

        timeslot.setTimeslotDate(date.minusDays(1));
        timeslot.setOpeningHour(LocalTime.of(9, 0));
        timeslot.setClosingHour(LocalTime.of(17, 0));
        timeslot.setReservableFrom(LocalDateTime.of(date.minusDays(1), LocalTime.of(0, 0)));
        timeslot.setReservable(true);
        timeslot.setSeatCount(testLocation.getNumberOfSeats());
        timeslot.setTimeslotSeqnr((int) (Math.random() * 5000));
        timeslot = timeslotDAO.addTimeslot(timeslot);
        boolean success = locationReservationDao.addLocationReservationIfStillRoomAtomically(new LocationReservation(student2, timeslot, null));
        // Set the attendance to false now, so it is changed less than 24 hours ago which is when the previous unattendance mails were sent.
        locationReservationDao.setReservationAttendance(student2.getUserId(), timeslot, false);
    }
}
