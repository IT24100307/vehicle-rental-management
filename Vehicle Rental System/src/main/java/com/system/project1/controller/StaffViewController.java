package com.system.project1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffViewController {

    @GetMapping("/staff")
    public String staffPage() {
        return "forward:/staff.html";
    }
}