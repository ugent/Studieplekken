package blok2.integration;

import org.junit.Test;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class LocationControllerTest extends BaseIntegrationTest{

    @Test
    public void testGetAllLocations() throws Exception {
        mockMvc.perform(get("/locations")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testPostNewLocation() throws Exception {
        mockMvc.perform(post("/locations").with(csrf()).content(objectMapper.writeValueAsBytes(testLocation)).contentType("application/json"))
                .andDo(print()).andExpect(status().isBadRequest());
    }
}
