package blok2.integration;

import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import java.time.format.DateTimeFormatter;
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
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetReservationsOfStudentAsSelf() throws Exception {
        mockMvc.perform(get("/locations/reservations/user?id=" + student.getUserId()).with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(1))
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
        Assert.assertEquals(2, list.size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testCreateReservationDuplicate() throws Exception {
        Timeslot timeslot = calendarPeriodDao.getById(calendarPeriods.get(0).getId()).getTimeslots().get(0);

        mockMvc.perform(post("/locations/reservations").with(csrf())
                .content(objectMapper.writeValueAsString(timeslot)).contentType("application/json")).andDo(print())
                .andExpect(status().isConflict());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getUserId());
        Assert.assertEquals(1, list.size());
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
        Assert.assertEquals(0, list.size());
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
        Assert.assertEquals(0, list.size());
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
        Assert.assertEquals(1, list.size());
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
        Assert.assertEquals(true, list.get(0).getAttended());
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
        Assert.assertEquals(true, list.get(0).getAttended());

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
        Assert.assertEquals(false, list.get(0).getAttended());

        // when a user is set to unattended, the spot must be released so that others can make a reservation
        Assert.assertEquals(timeslot.getAmountOfReservations()-1, timeslotAfterUpdate.getAmountOfReservations());
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
        Assert.assertEquals(false, list.get(0).getAttended());

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
        Assert.assertEquals(false, list.get(0).getAttended());
        list = locationReservationDao.getAllLocationReservationsOfUser(student2.getUserId());
        Assert.assertEquals(true, list.get(0).getAttended());

    }
    
}
