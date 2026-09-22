package com.rtb.manageyourmoneybackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@EnableAsync
@SpringBootApplication
public class ManageYourMoneyBackendApplication {

    static void main(String[] args) {
        SpringApplication.run(ManageYourMoneyBackendApplication.class, args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//
//        //expenseCategoryService.migrateFirebaseDataToExpenseCategory();
//    }

    @Bean
    public RestClient.Builder restClient() {
        return RestClient.builder();
    }
}
