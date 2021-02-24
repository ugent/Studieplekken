package blok2.integration;

import blok2.model.Authority;
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
public class AuthorityControllerTest extends BaseIntegrationTest {

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAllAuthorities() throws Exception {
        mockMvc.perform(get("/authority")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)); // only one authority
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetOneAuthority() throws Exception {
        mockMvc.perform(get("/authority/"+ authority.getAuthorityId())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(authority)));
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetInvalidAuthority() throws Exception {
        mockMvc.perform(get("/authority/"+ "7256884")).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testPostNewAuthorityDuplicate() throws Exception {
        Authority duplicate = new Authority(
                authority.getAuthorityId(),
                authority.getAuthorityName(),
                authority.getDescription()
        );

        mockMvc.perform(post("/authority").with(csrf())
                .content(objectMapper.writeValueAsBytes(duplicate)).contentType("application/json"))
                .andDo(print()).andExpect(status().isConflict());

        Assert.assertEquals(1, authorityDao.getAllAuthorities().size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testPostNewAuthority() throws Exception {
        Authority newAuthority = new Authority(-1, "authority name!", authority.getDescription());

        mockMvc.perform(post("/authority").with(csrf())
                .content(objectMapper.writeValueAsBytes(newAuthority)).contentType("application/json"))
                .andDo(print()).andExpect(status().isOk());

        Assert.assertEquals(2, authorityDao.getAllAuthorities().size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testPostNewAuthorityForbidden() throws Exception {
        Authority duplicate = new Authority(-1, "authority name!", authority.getDescription());

        mockMvc.perform(post("/authority").with(csrf())
                .content(objectMapper.writeValueAsBytes(duplicate)).contentType("application/json"))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(1, authorityDao.getAllAuthorities().size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteAuthorityUnauthorized() throws Exception {
        mockMvc.perform(delete("/authority/" + authority.getAuthorityId()).with(csrf()))
                .andDo(print()).andExpect(status().isForbidden());

        Assert.assertEquals(1, authorityDao.getAllAuthorities().size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteAuthority() throws Exception {
        mockMvc.perform(delete("/authority/" + authority.getAuthorityId()).with(csrf()))
                .andDo(print()).andExpect(status().isOk());

        Assert.assertEquals(0, authorityDao.getAllAuthorities().size());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testGetUsersFromAuthority() throws Exception {
        mockMvc.perform(get("/authority/" + authority.getAuthorityId() + "/users")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetUsersFromAuthorityForbidden() throws Exception {
        mockMvc.perform(get("/authority/" + authority.getAuthorityId() + "/users")).andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testGetUsersFromAuthorityMissing() throws Exception {
        mockMvc.perform(get("/authority/" + "17" + "/users")).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAuthorityFromUser() throws Exception {
        mockMvc.perform(get("/authority/users/" + student.getAugentID())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAuthorityFromUserForbidden() throws Exception {
        mockMvc.perform(get("/authority/users/" + student.getAugentID())).andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testGetAuthorityFromUserMissing() throws Exception {
        mockMvc.perform(get("/authority/users/" + 15)).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "testUserDetails")
    public void testPostUserToAuthority() throws Exception {
        mockMvc.perform(post("/authority/" + authority.getAuthorityId() + "/user/" + student2.getAugentID())
                .with(csrf())).andDo(print())
                .andExpect(status().isOk());

        Assert.assertEquals(2, authorityDao.getUsersFromAuthority(authority.getAuthorityId()).size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testPostUserToAuthorityForbidden() throws Exception {
        mockMvc.perform(post("/authority/" + authority.getAuthorityId() + "/user/" + student.getAugentID())
                .with(csrf())).andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/authority/" + authority.getAuthorityId() + "/user/" + student2.getAugentID())
                .with(csrf())).andDo(print())
                .andExpect(status().isForbidden());

        Assert.assertEquals(1, authorityDao.getUsersFromAuthority(authority.getAuthorityId()).size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testPostUserToAuthorityMissingAuthority() throws Exception {
        mockMvc.perform(post("/authority/" + "10" + "/user/" + student2.getAugentID()).with(csrf()))
                .andDo(print()).andExpect(status().isNotFound());

        Assert.assertEquals(1, authorityDao.getUsersFromAuthority(authority.getAuthorityId()).size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testPostUserToAuthorityMissingUser() throws Exception {
        mockMvc.perform(post("/authority/" + authority.getAuthorityId() + "/user/" + "10").with(csrf()))
                .andDo(print()).andExpect(status().isNotFound());

        Assert.assertEquals(1, authorityDao.getUsersFromAuthority(authority.getAuthorityId()).size());
    }

    @Test
    @WithUserDetails(value = "student1", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteUserFromAuthority() throws Exception {
        mockMvc.perform(delete("/authority/" + authority.getAuthorityId() + "/user/" + student.getAugentID())
                .with(csrf())).andDo(print())
                .andExpect(status().isOk()); // student1 is member of authority: therefore deleting should succeed

        Assert.assertEquals(0, authorityDao.getUsersFromAuthority(authority.getAuthorityId()).size());
    }

    @Test
    @WithUserDetails(value = "student2", userDetailsServiceBeanName = "testUserDetails")
    public void testDeleteUserFromAuthorityFail() throws Exception {
        mockMvc.perform(delete("/authority/" + authority.getAuthorityId() + "/user/" + student.getAugentID())
                .with(csrf())).andDo(print())
                .andExpect(status().isForbidden());

        Assert.assertEquals(1, authorityDao.getUsersFromAuthority(authority.getAuthorityId()).size());
    }
}
