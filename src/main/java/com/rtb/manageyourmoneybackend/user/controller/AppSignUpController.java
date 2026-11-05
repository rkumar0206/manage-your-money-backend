package com.rtb.manageyourmoneybackend.user.controller;

import com.rksdev.security.api.AbstractSignUpController;
import com.rtb.manageyourmoneybackend.user.dto.AppSignUpRequest;
import com.rtb.manageyourmoneybackend.user.service.AppRegistrationHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AppSignUpController extends AbstractSignUpController<AppSignUpRequest> {

    private final AppRegistrationHandler appRegistrationHandler;

    public AppSignUpController(PasswordEncoder encoder, AppRegistrationHandler registrationService) {
        super(encoder);
        this.appRegistrationHandler = registrationService;
    }

    @Override
    protected Object register(AppSignUpRequest request, String encodedPassword) {
        return this.appRegistrationHandler.handleUserSignUp(request, encodedPassword);
    }
}
