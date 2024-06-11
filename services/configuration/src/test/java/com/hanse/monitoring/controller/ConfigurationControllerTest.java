package com.hanse.monitoring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanse.monitoring.exception.MaxConfigurationsException;
import com.hanse.monitoring.service.ConfigurationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigurationController.class)
class ConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationService configurationService;

    @Test
    void testCreateConfiguration_Success() throws Exception {
        ConfigurationRequest request = new ConfigurationRequest("name", "uri", 1, true);
        int configurationId = 1;
        when(configurationService.createConfiguration(any(ConfigurationRequest.class)))
                .thenReturn(configurationId);

        mockMvc.perform(post("/api/v1/configurations")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(asJsonString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").value(configurationId));

        verify(configurationService, times(1)).createConfiguration(request);
    }

    @Test
    void testCreateConfiguration_ValidationError() throws Exception {
        ConfigurationRequest request = new ConfigurationRequest("", "", -1, true);

        mockMvc.perform(post("/api/v1/configurations")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(asJsonString(request)))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.errors.name").value("name should be present"))
               .andExpect(jsonPath("$.errors.uri").value("uri should be present"))
               .andExpect(jsonPath("$.errors.interval").value("must be greater than or equal to 0"));

        verify(configurationService, times(0)).createConfiguration(request);
    }

    @Test
    void testCreateConfiguration_MaxConfigurationsExceeded() throws Exception {
        ConfigurationRequest request = new ConfigurationRequest("name", "uri", 1, true);

        when(configurationService.createConfiguration(any(ConfigurationRequest.class)))
                .thenThrow(new MaxConfigurationsException("Maximum number of configurations exceeded"));

        mockMvc.perform(post("/api/v1/configurations")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(asJsonString(request)))
               .andExpect(status().isConflict())
               .andExpect(content().contentType(MediaType.TEXT_PLAIN))
               .andExpect(content().string("Maximum number of configurations exceeded"));

        verify(configurationService, times(1)).createConfiguration(request);
    }

    @Test
    void testUpdateConfiguration_Success() throws Exception {
        ConfigurationRequest request = new ConfigurationRequest("name", "uri", 1, true);
        int configurationId = 1;
        when(configurationService.updateConfiguration(any(Integer.class), any(ConfigurationRequest.class)))
                .thenReturn(true);

        mockMvc.perform(patch("/api/v1/configurations/{configuration-id}", configurationId)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(asJsonString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").value(true));

        verify(configurationService, times(1)).updateConfiguration(configurationId, request);
    }

    @Test
    void testUpdateConfiguration_EntityNotFound() throws Exception {
        ConfigurationRequest request = new ConfigurationRequest("name", "uri", 1, true);
        int configurationId = 1;
        when(configurationService.updateConfiguration(any(Integer.class), any(ConfigurationRequest.class)))
                .thenThrow(new EntityNotFoundException("Configuration not found"));

        mockMvc.perform(patch("/api/v1/configurations/{configuration-id}", configurationId)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(asJsonString(request)))
               .andExpect(status().isNotFound());

        verify(configurationService, times(1)).updateConfiguration(configurationId, request);
    }

    @Test
    void testFindAll_Success() throws Exception {
        ConfigurationResponse response1 = new ConfigurationResponse(1, "name1", "uri1", 1, true);
        ConfigurationResponse response2 = new ConfigurationResponse(2, "name2", "uri2", 3, true);
        List<ConfigurationResponse> responses = Arrays.asList(response1, response2);
        when(configurationService.findAllConfigurations(true)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/configurations?active=true"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].id", is(1)))
               .andExpect(jsonPath("$[0].name", is("name1")))
               .andExpect(jsonPath("$[0].uri", is("uri1")))
               .andExpect(jsonPath("$[0].interval", is(1)))
               .andExpect(jsonPath("$[1].id", is(2)))
               .andExpect(jsonPath("$[1].name", is("name2")))
               .andExpect(jsonPath("$[1].uri", is("uri2")))
               .andExpect(jsonPath("$[1].interval", is(3)));

        verify(configurationService, times(1)).findAllConfigurations(true);
    }

    @Test
    void testFindAll_NoConfigurations() throws Exception {
        when(configurationService.findAllConfigurations(true)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/configurations?active=true"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$", hasSize(0)));

        verify(configurationService, times(1)).findAllConfigurations(true);
    }

    @Test
    void testFindAll_InternalServerError() throws Exception {
        when(configurationService.findAllConfigurations(true)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/configurations?active=true"))
               .andExpect(status().isInternalServerError())
               .andExpect(content().contentType(MediaType.TEXT_PLAIN))
               .andExpect(content().string("Unexpected error"));

        verify(configurationService, times(1)).findAllConfigurations(true);
    }

    @Test
    void testFindById_Success() throws Exception {
        int configurationId = 1;
        ConfigurationResponse response = new ConfigurationResponse(1, "name", "uri", 1, true);
        when(configurationService.findById(configurationId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/configurations/{configuration-id}", configurationId))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(configurationId));

        verify(configurationService, times(1)).findById(configurationId);
    }

    @Test
    void testFindById_EntityNotFound() throws Exception {
        int configurationId = 1;
        when(configurationService.findById(configurationId)).thenThrow(new EntityNotFoundException("Configuration not found"));

        mockMvc.perform(get("/api/v1/configurations/{configuration-id}", configurationId))
               .andExpect(status().isNotFound())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.message").value("Configuration not found"));

        verify(configurationService, times(1)).findById(configurationId);
    }

    @Test
    void testDeleteById_Success() throws Exception {
        int configurationId = 1;

        doNothing().when(configurationService).deleteById(configurationId);

        mockMvc.perform(delete("/api/v1/configurations/{configuration-id}", configurationId))
               .andExpect(status().isAccepted());

        verify(configurationService, times(1)).deleteById(configurationId);
    }

    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
