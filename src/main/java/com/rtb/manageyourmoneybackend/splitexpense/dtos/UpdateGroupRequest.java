package com.rtb.manageyourmoneybackend.splitexpense.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;
    private String description;
}