package blok2.integration;

import org.junit.Test;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class BuildingControllerTest extends BaseIntegrationTest {

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAllBuildings() throws Exception {
        mockMvc.perform(get("/building")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)); // only approved location
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetOneBuilding() throws Exception {
        mockMvc.perform(get("/building")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)); // only approved location
    }

}
