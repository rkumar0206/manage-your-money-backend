package com.rtb.manageyourmoneybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ManageYourMoneyBackendApplication {

    static void main(String[] args) {
        SpringApplication.run(ManageYourMoneyBackendApplication.class, args);
    }

}
