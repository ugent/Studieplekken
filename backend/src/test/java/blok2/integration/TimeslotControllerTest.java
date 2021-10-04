package blok2.integration;

import blok2.model.calendar.Timeslot;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;
import org.threeten.extra.Weeks;

import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class TimeslotControllerTest extends BaseIntegrationTest {

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testUpdateTimeslot() throws Exception {
        Timeslot toUpdate = this.calendarPeriods.get(2);
        toUpdate.setLocation(testLocationUnapproved.getLocationId());
        toUpdate.setTimeslotDate(toUpdate.timeslotDate().plus(Weeks.ONE));

        mockMvc.perform(put("/locations/timeslots/").with(csrf())
                .content(objectMapper.writeValueAsBytes(toUpdate)).contentType("application/json"))
                .andDo(print()).andExpect(status().isOk());

        Timeslot nowInDb = timeslotDAO.getTimeslot(toUpdate.getTimeslotSeqnr());
        Assert.assertEquals(toUpdate.getLocationId(), nowInDb.getLocationId());
        Assert.assertEquals(toUpdate.timeslotDate(), nowInDb.timeslotDate());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testFalseUpdateTimeslot() throws Exception {
        Timeslot toUpdate = this.calendarPeriods.get(2);
        toUpdate.setLocation(testLocationUnapproved.getLocationId());
        toUpdate.setTimeslotDate(toUpdate.timeslotDate().plus(Weeks.ONE));

        mockMvc.perform(put("/locations/timeslots/").with(csrf())
                .content(objectMapper.writeValueAsBytes(toUpdate)).contentType("application/json"))
                .andDo(print()).andExpect(status().isOk());

        toUpdate.setClosingHour(LocalTime.NOON);
        Timeslot nowInDb = timeslotDAO.getTimeslot(toUpdate.getTimeslotSeqnr());
        Assert.assertEquals(toUpdate.getLocationId(), nowInDb.getLocationId());
        Assert.assertEquals(toUpdate.timeslotDate(), nowInDb.timeslotDate());
        Assert.assertNotEquals(toUpdate.getClosingHour(), nowInDb.getClosingHour());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testValidTimeslot() throws Exception {
        mockMvc.perform(put("/locations/timeslots/").with(csrf())
                .content(objectMapper.writeValueAsString(testLocation)).contentType("application/json"))
                .andDo(print()).andExpect(status().is4xxClientError());
    }
}
