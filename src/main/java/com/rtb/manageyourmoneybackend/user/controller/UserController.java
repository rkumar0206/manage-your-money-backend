package com.rtb.manageyourmoneybackend.user.controller;

import com.rksdev.security.web.CurrentUserId;
import com.rtb.manageyourmoneybackend.user.dto.EditRoleRequestDTO;
import com.rtb.manageyourmoneybackend.user.dto.UserResponse;
import com.rtb.manageyourmoneybackend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUserDetails(@CurrentUserId Long userId) {
        return ResponseEntity.ok(userService.getCurrentUserDetails(userId));
    }

    @PostMapping("/edit-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> editRoleForUser(@RequestBody @Valid EditRoleRequestDTO editRoleRequestDTO){
        return ResponseEntity.ok(userService.editRoles(editRoleRequestDTO));
    }
}
