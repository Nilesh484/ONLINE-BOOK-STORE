package com.example.onlinebookstore.controller;

import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.service.OrderService;
import com.example.onlinebookstore.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class OrderHistoryController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderHistoryController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/orderHistory")
    public String orderHistory(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        model.addAttribute("orders", orders);
        return "order_history";
    }
}
