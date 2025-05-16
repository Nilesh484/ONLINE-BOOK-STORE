package com.example.onlinebookstore.service;

import com.example.onlinebookstore.exception.BookDeletionException;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.repository.BookRepository;
import com.example.onlinebookstore.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final OrderItemRepository orderItemRepository;

    public BookService(BookRepository bookRepository, OrderItemRepository orderItemRepository) {
        this.bookRepository = bookRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> searchBooks(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        long count = orderItemRepository.countByBookId(id);
        if (count > 0) {
            throw new BookDeletionException("Cannot delete book as it is referenced by existing orders.");
        }
        bookRepository.deleteById(id);
    }
}
