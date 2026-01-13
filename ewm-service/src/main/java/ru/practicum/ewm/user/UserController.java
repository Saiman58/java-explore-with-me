package ru.practicum.ewm.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/user/health")
    public String health() {
        return "Пользовательский API работает";
    }
}
