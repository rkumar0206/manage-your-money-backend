package com.rtb.manageyourmoneybackend.splitexpense.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private Long id;           // GroupMember ID (Crucial for adding expenses)
    private Long userId;       // Null if dummy user
    private String name;
    private boolean isCreator; // True if this member is the group owner
}