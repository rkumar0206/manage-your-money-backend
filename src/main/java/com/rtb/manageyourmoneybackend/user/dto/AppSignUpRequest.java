package com.rtb.manageyourmoneybackend.user.dto;

import com.rksdev.security.dto.SignUpPayload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppSignUpRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotBlank @Size(max = 100) String fullName
) implements SignUpPayload {

    @Override
    public String toString() {
        return "AppSignUpRequest[username=" + username + "]"; // never log the password
    }
}