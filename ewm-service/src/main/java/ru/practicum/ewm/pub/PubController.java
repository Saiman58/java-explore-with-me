package ru.practicum.ewm.pub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PubController {

    @GetMapping("/pub/health")
    public String health() {
        return "Публичный API работает";
    }
}
