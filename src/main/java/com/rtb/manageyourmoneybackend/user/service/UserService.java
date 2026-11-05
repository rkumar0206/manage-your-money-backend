package com.rtb.manageyourmoneybackend.user.service;

import com.rtb.manageyourmoneybackend.user.dto.EditRoleRequestDTO;
import com.rtb.manageyourmoneybackend.user.dto.UserResponse;
import com.rtb.manageyourmoneybackend.user.dto.UserSearchResponse;

import java.util.List;

public interface UserService {

    UserResponse getCurrentUserDetails(Long id);
    List<UserSearchResponse> searchUsers(Long userId, String query);
    UserResponse editRoles(EditRoleRequestDTO editRoleRequestDTO);
}
