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
public class AccountControllerTest extends BaseIntegrationTest{
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAllAdmins() throws Exception {
        mockMvc.perform(get("/account/admins")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)); // only approved location
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAllAdminsForbidden() throws Exception {
        mockMvc.perform(get("/account/admins")).andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testGetUser() throws Exception {
        mockMvc.perform(get("/account/id?id=" + student.getAugentID())).andDo(print())
                .andExpect(status().isOk());
    }
}
