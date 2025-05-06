package com.quodbiometria.controller;

import com.quodbiometria.model.dto.request.UserCreateDTO;
import com.quodbiometria.model.dto.request.UserUpdateDTO;
import com.quodbiometria.model.dto.response.ApiResponseDTO;
import com.quodbiometria.model.dto.response.UserResponseDTO;
import com.quodbiometria.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> createUser(@RequestBody UserCreateDTO userDTO) {
        UserResponseDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "User created successfully",
                createdUser
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> getUserById(@PathVariable String id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "User retrieved successfully",
                user
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "Users retrieved successfully",
                users
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> updateUser(
            @PathVariable String id,
            @RequestBody UserUpdateDTO userDTO
    ) {
        UserResponseDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "User updated successfully",
                updatedUser
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "User deleted successfully",
                null
        ));
    }
}