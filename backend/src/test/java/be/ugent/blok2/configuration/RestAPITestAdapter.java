package be.ugent.blok2.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestAPITestAdapter {

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    public RestAPITestAdapter(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @WithUserDetails("admin")
    public void postCreated(String url, Object object) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf()).content(mapper.writeValueAsString(object))).andExpect(status().isCreated());
    }

    @WithUserDetails("admin")
    public void postCreated(String url) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf())).andExpect(status().isCreated());
    }

    @WithUserDetails("admin")
    public void postAccepted(String url, Object object) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf()).content(mapper.writeValueAsString(object))).andExpect(status().isAccepted());
    }

    @WithUserDetails("admin")
    public void postOk(String url, Object object) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf()).content(mapper.writeValueAsString(object))).andExpect(status().isOk());
    }

    @WithUserDetails("admin")
    public void postOk(String url) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf())).andExpect(status().isOk());
    }

    @WithUserDetails("admin")
    public void postBadRequest(String url, Object object) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf()).content(mapper.writeValueAsString(object))).andExpect(status().isBadRequest());
    }

    @WithUserDetails("admin")
    public void postConflict(String url, Object object) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf()).content(mapper.writeValueAsString(object))).andExpect(status().isConflict());
    }

    @WithUserDetails("admin")
    public void postConflict(String url) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf())).andExpect(status().isConflict());
    }

    @WithUserDetails("admin")
    public void postParamsAccepted(String url, MultiValueMap<String, String> params) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).params(params).
                with(csrf())).andExpect(status().isAccepted());
    }

    @WithUserDetails("admin")
    public <T> T getOk(String url, Class<T> valueClass) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(url).accept(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isOk()).andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        return mapper.readValue(json, valueClass);
    }

    @WithUserDetails("admin")
    public void deleteBadRequest(String url) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(url).with(csrf())).andExpect(status().isBadRequest());
    }

    @WithUserDetails("admin")
    public void deleteNotFound(String url) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(url).with(csrf())).andExpect(status().isNotFound());
    }

    @WithUserDetails("admin")
    public void deleteNoContent(String url) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(url).with(csrf())).andExpect(status().isNoContent());
    }

    @WithUserDetails("admin")
    public <T> T getNonExisting(String url, Class<T> valueClass) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(url).accept(MediaType.APPLICATION_JSON).with(csrf())).andReturn();
        if(mvcResult.getResponse().getContentAsString().trim().length()!=0){
            throw new Exception("Response was not empty");
        }
        return null;
    }

    @WithUserDetails("admin")
    public void getNotFound(String url) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url).accept(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isNotFound()).andReturn();
    }

    @WithUserDetails("admin")
    public void put(String url, Object object) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf()).content(mapper.writeValueAsString(object))).andExpect(status().isOk());
    }

    @WithUserDetails("admin")
    public void putBadRequest(String url, Object object) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf()).content(mapper.writeValueAsString(object))).andExpect(status().isBadRequest());
    }

    /*
        PUT request should return 404 not found status
     */
    @WithUserDetails("admin")
    public void putNotFound(String url, Object object) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).
                with(csrf()).content(mapper.writeValueAsString(object))).andExpect(status().isNotFound());
    }


}
