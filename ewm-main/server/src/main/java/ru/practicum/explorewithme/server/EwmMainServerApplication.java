package ru.practicum.explorewithme.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = {"ru.practicum.explorewithme.server", "ru.practicum.explorewithme.client", "ru.practicum.explorewithme.server.exception"})
public class EwmMainServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EwmMainServerApplication.class, args);
    }
}