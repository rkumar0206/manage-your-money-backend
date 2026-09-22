package com.rtb.manageyourmoneybackend.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Roles {

    ADMIN("ROLE_ADMIN"),
    REGULAR_USER("ROLE_REGULAR_USER");

    private final String value;
}
