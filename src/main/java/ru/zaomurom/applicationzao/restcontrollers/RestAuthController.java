package ru.zaomurom.applicationzao.restcontrollers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class RestAuthController {
    @GetMapping("/login")
    public String login() {
        return "Login page";
    }
}
