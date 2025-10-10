package com.example.order_service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://localhost:8083")
public interface UserClient {
    @GetMapping("/user/{id}")
    String getUser(@PathVariable String id);
}
