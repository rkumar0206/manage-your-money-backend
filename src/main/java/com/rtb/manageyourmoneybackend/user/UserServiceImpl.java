package com.rtb.manageyourmoneybackend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public String getUidSecurityContext() {
        return getUID();
    }

    private String getUID() {

        // 1. Retrieve the UID from the Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        return (String) authentication.getPrincipal();
    }
}
