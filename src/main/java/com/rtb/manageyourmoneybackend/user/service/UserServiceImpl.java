package com.rtb.manageyourmoneybackend.user.service;

import com.rtb.manageyourmoneybackend.user.CustomUserDetails;
import com.rtb.manageyourmoneybackend.user.dto.EditRoleRequestDTO;
import com.rtb.manageyourmoneybackend.user.dto.UserResponse;
import com.rtb.manageyourmoneybackend.user.enums.Roles;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + username));

        return new CustomUserDetails(user);
    }

    @Override
    public UserResponse getCurrentUserDetails(Long id) {
        return userRepository.findById(id)
                .map(u -> new UserResponse(u.getUsername(), u.getEmail(), u.isEnabled(), u.getRoles()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + id));
    }

    @Override
    @Transactional
    public UserResponse editRoles(EditRoleRequestDTO editRoleRequestDTO) {

        if (editRoleRequestDTO.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Roles cannot be empty");
        }

        Optional<UserEntity> user = userRepository.findByUsernameOrEmail(null, editRoleRequestDTO.getEmail());

        if (user.isPresent()) {

            UserEntity userEntity = user.get();

            userEntity.setRoles(editRoleRequestDTO.getRoles().stream().map(Roles::getValue).collect(Collectors.toSet()));
            userRepository.saveAndFlush(userEntity);

            return new UserResponse(userEntity.getUsername(), userEntity.getEmail(), userEntity.isEnabled(), userEntity.getRoles());
        }

        throw new UsernameNotFoundException("User not found with identifier: " + editRoleRequestDTO.getEmail());
    }
}