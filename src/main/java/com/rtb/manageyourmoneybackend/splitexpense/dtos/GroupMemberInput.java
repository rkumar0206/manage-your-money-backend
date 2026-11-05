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
public class GroupMemberInput {

    // Null if this is a dummy member (unregistered)
    private Long userId;

    @NotBlank(message = "Member name is required")
    private String name;
}