package com.system.project1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        // Redirect to the customer management page
        return "redirect:/customer";
    }

    @GetMapping("/spa")
    public String singlePageApp() {
        // Serve the Single Page Application version
        return "redirect:/customer-spa.html";
    }
}
