package com.example.user_service;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/{id}")
    public String getUser(@PathVariable String id) {
        return "User " + id + " - Alice";
    }
}
