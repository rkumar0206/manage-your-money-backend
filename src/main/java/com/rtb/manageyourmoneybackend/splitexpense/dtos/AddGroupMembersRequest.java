package com.rtb.manageyourmoneybackend.splitexpense.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddGroupMembersRequest {

    @Valid
    @NotEmpty(message = "At least one member must be provided")
    private List<GroupMemberInput> members;
}