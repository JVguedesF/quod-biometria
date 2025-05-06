package com.quodbiometria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.quodbiometria.model.dto.request.UserCreateDTO;
import com.quodbiometria.model.dto.request.UserUpdateDTO;
import com.quodbiometria.model.dto.response.UserResponseDTO;
import com.quodbiometria.model.entity.User;
import com.quodbiometria.model.enums.Role;
import com.quodbiometria.model.mappers.UserMapper;
import com.quodbiometria.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UserCreateDTO createDTO;
    private UserResponseDTO responseDTO;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
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

        responseDTO = new UserResponseDTO();
        responseDTO.setId("user123");
        responseDTO.setName("Test User");
        responseDTO.setEmail("user@example.com");
        responseDTO.setRoles(List.of("ROLE_USER"));
        responseDTO.setActive(true);

        updateDTO = new UserUpdateDTO();
        updateDTO.setName("Updated User");
        updateDTO.setRoles(Arrays.asList("ROLE_USER", "ROLE_MANAGER"));
        updateDTO.setActive(true);
    }

    @Test
    void testLoadUserByUsername_ExistingUser_ReturnsUser() {
        // Arrange
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = (User) userService.loadUserByUsername("user@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void testLoadUserByUsername_NonExistingUser_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent@example.com"));
    }

    @Test
    void testCreateUser_ValidData_CreatesUser() {
        // Arrange
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("password123");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userMapper.toEntity(createDTO)).thenReturn(newUser);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDTO(newUser)).thenReturn(responseDTO);

        // Act
        UserResponseDTO result = userService.createUser(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getId());
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(newUser);
        verify(userMapper).toDTO(newUser);
    }

    @Test
    void testCreateUser_ExistingEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> userService.createUser(createDTO));

        verify(userRepository).existsByEmail("newuser@example.com");
        verifyNoMoreInteractions(userMapper, passwordEncoder, userRepository);
    }

    @Test
    void testGetUserById_ExistingId_ReturnsUser() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(testUser)).thenReturn(responseDTO);

        // Act
        UserResponseDTO result = userService.getUserById("user123");

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("Test User", result.getName());
        verify(userRepository).findById("user123");
        verify(userMapper).toDTO(testUser);
    }

    @Test
    void testGetUserById_NonExistingId_ThrowsException() {
        // Arrange
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> userService.getUserById("nonexistent"));

        verify(userRepository).findById("nonexistent");
    }

    @Test
    void testGetAllUsers_ReturnsAllUsers() {
        // Arrange
        List<User> users = Collections.singletonList(testUser);
        List<UserResponseDTO> expectedResponses = Collections.singletonList(responseDTO);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDTOList(users)).thenReturn(expectedResponses);

        // Act
        List<UserResponseDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user123", result.get(0).getId());
        verify(userRepository).findAll();
        verify(userMapper).toDTOList(users);
    }

    @Test
    void testUpdateUser_ExistingId_UpdatesUser() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        doNothing().when(userMapper).updateEntityFromDTO(updateDTO, testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDTO(testUser)).thenReturn(responseDTO);

        // Act
        UserResponseDTO result = userService.updateUser("user123", updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getId());
        verify(userRepository).findById("user123");
        verify(userMapper).updateEntityFromDTO(updateDTO, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toDTO(testUser);
    }

    @Test
    void testUpdateUser_NonExistingId_ThrowsException() {
        // Arrange
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> userService.updateUser("nonexistent", updateDTO));

        verify(userRepository).findById("nonexistent");
    }

    @Test
    void testDeleteUser_ExistingId_DeletesUser() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser("user123");

        // Assert
        verify(userRepository).findById("user123");
        verify(userRepository).delete(testUser);
    }

    @Test
    void testDeleteUser_NonExistingId_ThrowsException() {
        // Arrange
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser("nonexistent"));

        verify(userRepository).findById("nonexistent");
    }

    @Test
    void testLoadUserById_ExistingId_ReturnsUser() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act
        User result = (User) userService.loadUserById("user123");

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void testLoadUserById_NonExistingId_ThrowsException() {
        // Arrange
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserById("nonexistent"));
    }
}

