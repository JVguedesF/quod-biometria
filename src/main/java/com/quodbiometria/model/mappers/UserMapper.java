package com.quodbiometria.model.mappers;

import com.quodbiometria.model.dto.request.UserCreateDTO;
import com.quodbiometria.model.dto.request.UserUpdateDTO;
import com.quodbiometria.model.dto.response.UserResponseDTO;
import com.quodbiometria.model.entity.User;
import com.quodbiometria.model.enums.Role;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(UserCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setActive(true);

        Set<Role> roles = new HashSet<>();
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            for (String roleStr : dto.getRoles()) {
                try {
                    roles.add(Role.valueOf(roleStr));
                } catch (IllegalArgumentException e) {
                    // Ignorar roles inválidas
                }
            }
        }

        if (roles.isEmpty()) {
            roles.add(Role.ROLE_USER);
        }

        user.setRoles(roles);

        return user;
    }

    public UserResponseDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setActive(entity.isEnabled());

        List<String> roleStrings = entity.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        dto.setRoles(roleStrings);

        return dto;
    }

    public List<UserResponseDTO> toDTOList(List<User> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(UserUpdateDTO dto, User entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        if (dto.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleStr : dto.getRoles()) {
                try {
                    roles.add(Role.valueOf(roleStr));
                } catch (IllegalArgumentException e) {
                    // Ignorar roles inválidas
                }
            }

            if (!roles.isEmpty()) {
                entity.setRoles(roles);
            }
        }
    }

}