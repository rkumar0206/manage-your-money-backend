package com.rtb.manageyourmoneybackend.user.service;

import com.rtb.manageyourmoneybackend.user.dto.EditRoleRequestDTO;
import com.rtb.manageyourmoneybackend.user.dto.UserResponse;

public interface UserService {

    UserResponse getCurrentUserDetails(Long id);

    UserResponse editRoles(EditRoleRequestDTO editRoleRequestDTO);
}
