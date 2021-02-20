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
        mockMvc.perform(get("/locations/reservations/user?id="+student.getAugentID()).with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetReservationsOfStudentAsSelf() throws Exception {
        mockMvc.perform(get("/locations/reservations/user?id="+student.getAugentID()).with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testGetReservationsOfStudentAsForbidden() throws Exception {
        mockMvc.perform(get("/locations/reservations/user?id="+student.getAugentID()).with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testCreateReservation() throws Exception {

        Timeslot timeslot = new Timeslot(calendarPeriods.get(0), 1, calendarPeriods.get(0).getStartsAt().plusDays(1));
        mockMvc.perform(post("/locations/reservations").with(csrf()).content(objectMapper.writeValueAsString(timeslot)).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getAugentID());
        Assert.assertEquals(2, list.size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testCreateReservationDuplicate() throws Exception {

        Timeslot timeslot = new Timeslot(calendarPeriods.get(0), 0, calendarPeriods.get(0).getStartsAt().plusDays(1));
        mockMvc.perform(post("/locations/reservations").with(csrf()).content(objectMapper.writeValueAsString(timeslot)).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isConflict());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getAugentID());
        Assert.assertEquals(1, list.size());
    }

    // todo more tests for more specific calendar periods

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservation() throws Exception {

        Timeslot timeslot = new Timeslot(calendarPeriods.get(0), 0, calendarPeriods.get(0).getStartsAt().plusDays(1));
        LocationReservation reservation = new LocationReservation(student, null, timeslot, false);
        mockMvc.perform(delete("/locations/reservations").with(csrf()).content(objectMapper.writeValueAsString(reservation)).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getAugentID());
        Assert.assertEquals(0, list.size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservationAsAdmin() throws Exception {

        Timeslot timeslot = new Timeslot(calendarPeriods.get(0), 0, calendarPeriods.get(0).getStartsAt().plusDays(1));
        LocationReservation reservation = new LocationReservation(student, null, timeslot, false);
        mockMvc.perform(delete("/locations/reservations").with(csrf()).content(objectMapper.writeValueAsString(reservation)).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getAugentID());
        Assert.assertEquals(0, list.size());
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteReservationAsOther() throws Exception {

        Timeslot timeslot = new Timeslot(calendarPeriods.get(0), 0, calendarPeriods.get(0).getStartsAt().plusDays(1));
        LocationReservation reservation = new LocationReservation(student, null, timeslot, false);
        mockMvc.perform(delete("/locations/reservations").with(csrf()).content(objectMapper.writeValueAsString(reservation)).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isForbidden());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getAugentID());
        Assert.assertEquals(1, list.size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendance() throws Exception {

        Timeslot timeslot = new Timeslot(calendarPeriods.get(0), 0, calendarPeriods.get(0).getStartsAt().plusDays(1));

        String url = String.format("/locations/reservations/%s/%d/%s/%d/attendance", student.getAugentID(), timeslot.getCalendarId(), timeslot.getTimeslotDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), timeslot.getTimeslotSeqnr());
        JSONObject obj = new JSONObject().put("attended", true);
        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getAugentID());
        Assert.assertEquals(true, list.get(0).getAttended());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testSetAttendanceUnexisting() throws Exception {

        Timeslot timeslot = new Timeslot(calendarPeriods.get(0), 0, calendarPeriods.get(0).getStartsAt().plusDays(1));

        String url = String.format("/locations/reservations/%s/%d/%s/%d/attendance", student2.getAugentID(), timeslot.getCalendarId(), timeslot.getTimeslotDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), timeslot.getTimeslotSeqnr());
        JSONObject obj = new JSONObject().put("attended", true);
        mockMvc.perform(post(url).with(csrf()).content(obj.toString()).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound());

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(student.getAugentID());
        Assert.assertNull(list.get(0).getAttended());
    }
}
