package com.quodbiometria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.quodbiometria.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodbiometria.model.dto.request.UserCreateDTO;
import com.quodbiometria.model.dto.request.UserUpdateDTO;
import com.quodbiometria.model.dto.response.UserResponseDTO;
import com.quodbiometria.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        createDTO = new UserCreateDTO();
        createDTO.setName("New User");
        createDTO.setEmail("newuser@example.com");
        createDTO.setPassword("password123");
        createDTO.setRoles(List.of("ROLE_USER"));

        updateDTO = new UserUpdateDTO();
        updateDTO.setName("Updated User");
        updateDTO.setRoles(Arrays.asList("ROLE_USER", "ROLE_MANAGER"));
        updateDTO.setActive(true);

        responseDTO = new UserResponseDTO();
        responseDTO.setId("user123");
        responseDTO.setName("Test User");
        responseDTO.setEmail("user@example.com");
        responseDTO.setRoles(List.of("ROLE_USER"));
        responseDTO.setActive(true);
    }

        @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser_ValidData_ReturnsCreatedUser() throws Exception {
        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value("user123"))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }


    @Test
    @WithMockUser(roles = "USER")
    void testCreateUser_NonAdminUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_ExistingId_ReturnsUser() throws Exception {
        when(userService.getUserById("user123")).thenReturn(responseDTO);

        mockMvc.perform(get("/api/users/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value("user123"))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers_ReturnsAllUsers() throws Exception {
        List<UserResponseDTO> users = Collections.singletonList(responseDTO);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value("user123"))
                .andExpect(jsonPath("$.data[0].name").value("Test User"))
                .andExpect(jsonPath("$.data[0].email").value("user@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_ExistingId_ReturnsUpdatedUser() throws Exception {
        when(userService.updateUser(eq("user123"), any(UserUpdateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/users/user123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.id").value("user123"))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_ExistingId_ReturnsSuccess() throws Exception {
        doNothing().when(userService).deleteUser("user123");

        mockMvc.perform(delete("/api/users/user123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @WithMockUser(roles = "USER")
    void testDeleteUser_NonAdminUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/user123"))
                .andExpect(status().isForbidden());
    }
}
