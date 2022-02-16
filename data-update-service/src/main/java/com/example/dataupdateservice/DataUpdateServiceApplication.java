package com.example.dataupdateservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

@SpringBootApplication
@EnableFeignClients
@ImportAutoConfiguration(FeignAutoConfiguration.class)

public class DataUpdateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataUpdateServiceApplication.class, args);
        //System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "src/main/resources/");

        //
        System.out.println("Service has started");

    }

}
