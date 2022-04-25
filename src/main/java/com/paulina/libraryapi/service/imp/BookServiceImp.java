package com.paulina.libraryapi.service.imp;

import com.paulina.libraryapi.api.exception.BusinessException;
import com.paulina.libraryapi.model.entity.Book;
import com.paulina.libraryapi.model.respository.BookRepository;
import com.paulina.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImp implements BookService {


    private BookRepository repository;


    public BookServiceImp(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn()) ){
            throw new BusinessException("ISBN já cadastrado");
        }
        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if(book.getId() == null){
            throw new IllegalArgumentException("Book id cant be null.");
        }
        this.repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(book.getId() == null){
            throw new IllegalArgumentException("Book id cant be null.");
        }
        return this.repository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        Example<Book> example = Example.of(filter, ExampleMatcher.matching().withIgnoreCase()
                                                        .withIgnoreNullValues()
                                                        .withStringMatcher( ExampleMatcher.StringMatcher.CONTAINING ));
        return repository.findAll(example, pageRequest);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return repository.findByIsbn(isbn);
    }


}
