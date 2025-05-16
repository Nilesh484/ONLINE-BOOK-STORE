package com.example.onlinebookstore.controller;

import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.service.BookService;
import com.example.onlinebookstore.service.OrderService;
import com.example.onlinebookstore.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final UserService userService;
    private final BookService bookService;

    public AdminController(OrderService orderService, UserService userService, BookService bookService) {
        this.orderService = orderService;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/orders")
    public String viewAllOrders(Model model) {
        List<Order> orders = orderService.findAllOrders(); // Changed to get all orders
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/users")
    public String viewAllUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/manageBooks")
    public String manageBooks(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("book", new Book());
        return "admin/manage_books";
    }

    @PostMapping("/manageBooks/add")
    public String addBook(@ModelAttribute Book book) {
        bookService.saveBook(book);
        return "redirect:/admin/manageBooks";
    }

    @PostMapping("/manageBooks/updateStock")
    public String updateStock(@RequestParam Long bookId, @RequestParam int stock) {
        Book book = bookService.getBookById(bookId).orElse(null);
        if (book != null) {
            book.setStock(stock);
            bookService.saveBook(book);
        }
        return "redirect:/admin/manageBooks";
    }

    @PostMapping("/manageBooks/delete")
    public String deleteBook(@RequestParam Long bookId, Model model) {
        try {
            bookService.deleteBook(bookId);
        } catch (com.example.onlinebookstore.exception.BookDeletionException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return manageBooks(model);
        }
        return "redirect:/admin/manageBooks";
    }
}
