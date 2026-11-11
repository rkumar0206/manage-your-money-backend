package com.rtb.manageyourmoneybackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.manageyourmoneybackend.firebase.sevice.FirestoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@EnableAsync
@SpringBootApplication
@RequiredArgsConstructor
public class ManageYourMoneyBackendApplication implements CommandLineRunner {

    private final FirestoreService firestoreService;

    static void main(String[] args) {
        SpringApplication.run(ManageYourMoneyBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

//        List<String> documentIds = firestoreService.updateAndGetMatchingDocumentIds(
//                "IgkfuqhwpZh2CIXjdqZKxzxbe4j1",
//                1791471000000L
//        );
//
//        System.out.println("documentIds = " + documentIds);
    }

    @Bean
    public RestClient.Builder restClient() {
        return RestClient.builder();
    }
}
