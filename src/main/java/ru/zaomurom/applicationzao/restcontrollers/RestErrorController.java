package ru.zaomurom.applicationzao.restcontrollers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/error")
public class RestErrorController {
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "Access Denied";
    }

    @GetMapping("/error")
    public String error() {
        return "Error occurred";
    }
}
