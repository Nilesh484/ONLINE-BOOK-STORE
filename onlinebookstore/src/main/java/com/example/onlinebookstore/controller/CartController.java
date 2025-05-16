package com.example.onlinebookstore.controller;

import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.OrderItem;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.service.BookService;
import com.example.onlinebookstore.service.OrderService;
import com.example.onlinebookstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final BookService bookService;
    private final OrderService orderService;
    private final UserService userService;

    public CartController(BookService bookService, OrderService orderService, UserService userService) {
        this.bookService = bookService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Map<Long, OrderItem> cart = getCart(session);
        model.addAttribute("cartItems", new ArrayList<>(cart.values()));
        model.addAttribute("total", calculateTotal(cart));
        return "cart";
    }

    @PostMapping("/add/{bookId}")
    public String addToCart(@PathVariable Long bookId, HttpSession session) {
        Map<Long, OrderItem> cart = getCart(session);
        OrderItem item = cart.get(bookId);
        if (item == null) {
            Book book = bookService.getBookById(bookId).orElse(null);
            if (book != null) {
                item = new OrderItem();
                item.setBook(book);
                item.setQuantity(1);
                item.setPrice(book.getPrice());
                cart.put(bookId, item);
            }
        } else {
            item.setQuantity(item.getQuantity() + 1);
        }
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/update/{bookId}")
    public String updateQuantity(@PathVariable Long bookId, @RequestParam int quantity, HttpSession session) {
        Map<Long, OrderItem> cart = getCart(session);
        OrderItem item = cart.get(bookId);
        if (item != null) {
            if (quantity <= 0) {
                cart.remove(bookId);
            } else {
                item.setQuantity(quantity);
            }
        }
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{bookId}")
    public String removeFromCart(@PathVariable Long bookId, HttpSession session) {
        Map<Long, OrderItem> cart = getCart(session);
        cart.remove(bookId);
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/placeOrder")
    public String placeOrder(@RequestParam String contactNumber,
                             @RequestParam String fullAddress,
                             @RequestParam String fullName,
                             HttpSession session,
                             Authentication authentication,
                             Model model) {
        Map<Long, OrderItem> cart = getCart(session);
        if (cart.isEmpty()) {
            model.addAttribute("error", "Cart is empty");
            return "cart";
        }

        User user = null;
        if (authentication != null && authentication.isAuthenticated()) {
            user = userService.findByUsername(authentication.getName()).orElse(null);
        }

        try {
            orderService.placeOrder(user, new ArrayList<>(cart.values()), contactNumber, fullAddress, fullName);
            session.removeAttribute("cart");
            return "redirect:/orderHistory";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cartItems", new ArrayList<>(cart.values()));
            model.addAttribute("total", calculateTotal(cart));
            return "cart";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, OrderItem> getCart(HttpSession session) {
        Object cartObj = session.getAttribute("cart");
        if (cartObj == null) {
            Map<Long, OrderItem> cart = new HashMap<>();
            session.setAttribute("cart", cart);
            return cart;
        } else {
            return (Map<Long, OrderItem>) cartObj;
        }
    }

    private double calculateTotal(Map<Long, OrderItem> cart) {
        return cart.values().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
