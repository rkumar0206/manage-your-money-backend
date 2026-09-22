package com.rtb.manageyourmoneybackend.user.dto;

import com.rtb.manageyourmoneybackend.user.enums.Roles;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditRoleRequestDTO {

    @NotBlank(message = "email is required")
    String email;

    List<Roles> roles;
}
