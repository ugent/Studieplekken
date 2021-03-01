package blok2.integration;

import blok2.TestSharedMethods;
import blok2.model.reservables.Location;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class LocationControllerTest extends BaseIntegrationTest {

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAllLocations() throws Exception {
        mockMvc.perform(get("/locations")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)); // only approved location
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
        Assert.assertEquals(1, locationDao.getAllLocations().size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testApproveNewLocationUnauthorized() throws Exception {
        String jsonString = new JSONObject()
                .put("location", new JSONObject(objectMapper.writeValueAsString(testLocationUnapproved)))
                .put("approval", "true")
                .toString();

        mockMvc.perform(put("/locations/" + testLocationUnapproved.getName() + "/approval")
                .with(csrf()).content(jsonString).contentType("application/json")).andDo(print())
                .andExpect(status().isForbidden());

        Assert.assertEquals(1, locationDao.getAllUnapprovedLocations().size());
        Assert.assertEquals(1, locationDao.getAllLocations().size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testApproveNewLocation() throws Exception {
        String jsonString = new JSONObject()
                .put("location", new JSONObject(objectMapper.writeValueAsString(testLocationUnapproved)))
                .put("approval", true)
                .toString();

        mockMvc.perform(put("/locations/" + testLocationUnapproved.getName() + "/approval")
                .with(csrf()).content(jsonString).contentType("application/json")).andDo(print())
                .andExpect(status().isOk());

        Assert.assertEquals(0, locationDao.getAllUnapprovedLocations().size());
        Assert.assertEquals(2, locationDao.getAllLocations().size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteLocationUnauthorized() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocation.getName()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(1, locationDao.getAllLocations().size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteLocation() throws Exception {
        mockMvc.perform(delete("/locations/" + testLocation.getName()).with(csrf()))
                .andDo(print()).andExpect(status().isOk());

        Assert.assertEquals(0, locationDao.getAllLocations().size());
    }
}

