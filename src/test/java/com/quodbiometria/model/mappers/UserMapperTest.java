package com.quodbiometria.model.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quodbiometria.model.dto.request.UserCreateDTO;
import com.quodbiometria.model.dto.request.UserUpdateDTO;
import com.quodbiometria.model.dto.response.UserResponseDTO;
import com.quodbiometria.model.entity.User;
import com.quodbiometria.model.enums.Role;

class UserMapperTest {

    private UserMapper userMapper;

    private User testUser;
    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

        testUser = new User();
        testUser.setId("user123");
        testUser.setName("Test User");
        testUser.setEmail("user@example.com");
        testUser.setPassword("hashedPassword");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        testUser.setRoles(roles);
        testUser.setActive(true);

        createDTO = new UserCreateDTO();
        createDTO.setName("New User");
        createDTO.setEmail("newuser@example.com");
        createDTO.setPassword("password123");
        createDTO.setRoles(List.of("ROLE_USER"));

        updateDTO = new UserUpdateDTO();
        updateDTO.setName("Updated User");
        updateDTO.setRoles(Arrays.asList("ROLE_USER", "ROLE_MANAGER"));
        updateDTO.setActive(true);
    }

    @Test
    void testToEntity_ValidDTO_ReturnsEntity() {
        // Act
        User result = userMapper.toEntity(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals("New User", result.getName());
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
        assertTrue(result.isActive());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    void testToEntity_NullDTO_ReturnsNull() {
        // Act
        User result = userMapper.toEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToEntity_InvalidRoles_UsesDefaultRole() {
        // Arrange
        createDTO.setRoles(List.of("INVALID_ROLE"));

        // Act
        User result = userMapper.toEntity(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    void testToDTO_ValidEntity_ReturnsDTO() {
        // Act
        UserResponseDTO result = userMapper.toDTO(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("user@example.com", result.getEmail());
        assertTrue(result.isActive());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains("ROLE_USER"));
    }

    @Test
    void testToDTO_NullEntity_ReturnsNull() {
        // Act
        UserResponseDTO result = userMapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToDTOList_ValidEntities_ReturnsDTOs() {
        // Arrange
        List<User> users = Collections.singletonList(testUser);

        // Act
        List<UserResponseDTO> result = userMapper.toDTOList(users);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user123", result.get(0).getId());
        assertEquals("Test User", result.get(0).getName());
    }

    @Test
    void testUpdateEntityFromDTO_ValidDTO_UpdatesEntity() {
        // Act
        userMapper.updateEntityFromDTO(updateDTO, testUser);

        // Assert
        assertEquals("Updated User", testUser.getName());
        assertEquals(2, testUser.getRoles().size());
        assertTrue(testUser.getRoles().contains(Role.ROLE_USER));
        assertTrue(testUser.getRoles().contains(Role.ROLE_MANAGER));
        assertTrue(testUser.isActive());
    }

    @Test
    void testUpdateEntityFromDTO_NullDTO_DoesNotUpdate() {
        // Arrange
        String originalName = testUser.getName();

        // Act
        userMapper.updateEntityFromDTO(null, testUser);

        // Assert
        assertEquals(originalName, testUser.getName());
    }

    @Test
    void testUpdateEntityFromDTO_NullEntity_DoesNothing() {
        assertDoesNotThrow(() -> userMapper.updateEntityFromDTO(updateDTO, null));
    }

    @Test
    void testUpdateEntityFromDTO_PartialUpdate_UpdatesOnlyProvidedFields() {
        // Arrange
        updateDTO.setName(null);
        String originalName = testUser.getName();

        // Act
        userMapper.updateEntityFromDTO(updateDTO, testUser);

        // Assert
        assertEquals(originalName, testUser.getName());
        assertEquals(2, testUser.getRoles().size());
        assertTrue(testUser.isActive());
    }
}
