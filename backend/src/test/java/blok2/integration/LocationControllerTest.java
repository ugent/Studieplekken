package blok2.integration;

import blok2.TestSharedMethods;
import blok2.daos.ILocationDao;
import blok2.daos.IVolunteerDao;
import blok2.model.reservables.Location;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class LocationControllerTest extends BaseIntegrationTest {

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IVolunteerDao volunteerDao;

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAllLocations() throws Exception {
        mockMvc.perform(get("/locations")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // only approved location
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAllUnapprovedLocations() throws Exception {
        mockMvc.perform(get("/locations/unapproved")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testPostNewLocationDuplicate() throws Exception {
        mockMvc.perform(post("/locations").with(csrf())
                .content(objectMapper.writeValueAsBytes(testLocation)).contentType("application/json"))
                .andDo(print()).andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testPostNewLocationUnauthorized() throws Exception {
        mockMvc.perform(post("/locations").with(csrf())
                .content(objectMapper.writeValueAsBytes(testLocation)).contentType("application/json"))
                .andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testPostNewLocation() throws Exception {
        Location testlocation3 = TestSharedMethods.testLocation3(authority, testBuilding);

        mockMvc.perform(post("/locations").with(csrf())
                .content(objectMapper.writeValueAsBytes(testlocation3)).contentType("application/json"))
                .andDo(print()).andExpect(status().isOk());

        Assert.assertEquals(2, locationDao.getAllUnapprovedLocations().size());
        Assert.assertEquals(2, locationDao.getAllActiveLocations().size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testApproveNewLocationUnauthorized() throws Exception {
        String jsonString = new JSONObject()
                .put("location", new JSONObject(objectMapper.writeValueAsString(testLocationUnapproved)))
                .put("approval", "true")
                .toString();

        mockMvc.perform(put("/locations/" + testLocationUnapproved.getLocationId() + "/approval")
                .with(csrf()).content(jsonString).contentType("application/json")).andDo(print())
                .andExpect(status().isForbidden());

        Assert.assertEquals(1, locationDao.getAllUnapprovedLocations().size());
        Assert.assertEquals(2, locationDao.getAllActiveLocations().size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testApproveNewLocation() throws Exception {
        String jsonString = new JSONObject()
                .put("location", new JSONObject(objectMapper.writeValueAsString(testLocationUnapproved)))
                .put("approval", true)
                .toString();

        mockMvc.perform(put("/locations/" + testLocationUnapproved.getLocationId() + "/approval")
                .with(csrf()).content(jsonString).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Assert.assertEquals(0, locationDao.getAllUnapprovedLocations().size());
        Assert.assertEquals(3, locationDao.getAllActiveLocations().size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteLocationUnauthorized() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocation.getLocationId()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(2, locationDao.getAllActiveLocations().size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteLocation() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocation.getLocationId()).with(csrf()))
                .andDo(print()).andExpect(status().isOk());

        Assert.assertEquals(1, locationDao.getAllActiveLocations().size());

        // You can manually check in the logs if the mail was sent by looking for
        // Blocked sending mail to 'student1@ugent.be' with template file name 'mail/reservation_slot_deleted' and subject '[Werk- en Studieplekken] Uw gereserveerd tijdslot werd verwijderd' because 'mail' is not an active profile.
    }

    @Test
    @WithUserDetails(value = "authholder", userDetailsServiceBeanName = "testUserDetails")
    public void testAddVolunteer() throws Exception {
        mockMvc.perform(post("/locations/" + testLocation.getLocationId() + "/volunteers/" + student.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isOk());

        Assert.assertEquals(2, volunteerDao.getVolunteers(testLocation.getLocationId()).size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testAddVolunteerUnauthorized() throws Exception {
        mockMvc.perform(post("/locations/" + testLocation.getLocationId() + "/volunteers/" + student.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(1, volunteerDao.getVolunteers(testLocation.getLocationId()).size());
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testAddVolunteerByVolunteerUnauthorized() throws Exception {
        mockMvc.perform(post("/locations/" + testLocation.getLocationId() + "/volunteers/" + student.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(1, volunteerDao.getVolunteers(testLocation.getLocationId()).size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testAddVolunteerUnexisting() throws Exception {
        mockMvc.perform(post("/locations/" + "35200" + "/volunteers/" + student.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isNotFound());

        Assert.assertEquals(1, volunteerDao.getVolunteers(testLocation.getLocationId()).size());

    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteVolunteer() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocation.getLocationId() + "/volunteers/" + student2.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isOk());

        Assert.assertEquals(0, volunteerDao.getVolunteers(testLocation.getLocationId()).size());

    }

    @Test
    @WithUserDetails(value = "authholder", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteVolunteerByAuthHolder() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocation.getLocationId() + "/volunteers/" + student2.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isOk());

        Assert.assertEquals(0, volunteerDao.getVolunteers(testLocation.getLocationId()).size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteVolunteerByStudentUnauthorized() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocation.getLocationId() + "/volunteers/" + student2.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(1, volunteerDao.getVolunteers(testLocation.getLocationId()).size());
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteVolunteerByVolunteerUnauthorized() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocation.getLocationId() + "/volunteers/" + student2.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(1, volunteerDao.getVolunteers(testLocation.getLocationId()).size());
    }

    @Test
    @WithUserDetails(value = "authholder", userDetailsServiceBeanName = "testUserDetails")
    public void testGetVolunteers() throws Exception {
        mockMvc.perform(get("/locations/" + testLocation.getLocationId() + "/volunteers").with(csrf()))
                .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithUserDetails(value = "authholder", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteLocationFromOtherInstitutionUnauthorized() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocationHoGent.getLocationId()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(2, locationDao.getAllActiveLocations().size());
    }

    @Test
    @WithUserDetails(value = "authholder", userDetailsServiceBeanName = "testUserDetails")
    public void testPostNewLocationToOtherInstitutionUnauthorized() throws Exception {
        // First remove the location from the database to prevent conflicts (already location with that name).
        locationDao.deleteLocation(testLocationHoGent.getLocationId());

        // Do test.
        mockMvc.perform(post("/locations").with(csrf())
                .content(objectMapper.writeValueAsBytes(testLocationHoGent)).contentType("application/json"))
                .andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "authholder", userDetailsServiceBeanName = "testUserDetails")
    public void testAddVolunteerToLocationFromOtherInstitutionUnauthorized() throws Exception {
        mockMvc.perform(post("/locations/" + testLocationHoGent.getLocationId() + "/volunteers/" + student.getUserId()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(0, volunteerDao.getVolunteers(testLocationHoGent.getLocationId()).size());
    }

}
