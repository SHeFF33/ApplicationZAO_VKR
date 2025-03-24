package ru.zaomurom.applicationzao.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ErrorController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        String message = (String) request.getAttribute("javax.servlet.error.message");
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String exceptionType = (String) request.getAttribute("javax.servlet.error.exception_type");

        model.addAttribute("message", message);
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("exceptionType", exceptionType);

        return "error";
    }
}
