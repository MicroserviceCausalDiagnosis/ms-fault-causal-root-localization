package com.example.order_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserClient userClient;

    @GetMapping("/{orderId}")
    public String getOrder(@PathVariable String orderId) throws InterruptedException {
        // 模拟性能瓶颈：线程休眠1秒
        Thread.sleep(1000);

        // 调用用户服务
        String userInfo = userClient.getUser("123");

        return "Order " + orderId + " for " + userInfo;
    }
}