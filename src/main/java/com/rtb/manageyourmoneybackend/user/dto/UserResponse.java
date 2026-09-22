package com.rtb.manageyourmoneybackend.user.dto;

import java.util.Set;

public record UserResponse(
        String username,
        String email,
        boolean enabled,
        Set<String> roles
) { }