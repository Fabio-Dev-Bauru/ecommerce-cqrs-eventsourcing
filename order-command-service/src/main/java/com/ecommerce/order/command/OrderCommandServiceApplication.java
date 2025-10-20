package com.ecommerce.order.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.ecommerce.order.command",
    "com.ecommerce.shared"
})
public class OrderCommandServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderCommandServiceApplication.class, args);
    }
}
