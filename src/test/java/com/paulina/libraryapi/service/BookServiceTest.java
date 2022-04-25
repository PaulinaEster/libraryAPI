package com.paulina.libraryapi.service;

import com.paulina.libraryapi.api.exception.BusinessException;
import com.paulina.libraryapi.model.entity.Book;
import com.paulina.libraryapi.model.entity.Loan;
import com.paulina.libraryapi.model.respository.BookRepository;
import com.paulina.libraryapi.service.imp.BookServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Assertions;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImp(repository);
    }

    @Test
    @DisplayName("Deve salvar o livro")
    public void saveBookTest(){
        // cenario
        Book book = createBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        Mockito.when( repository.save(book) )
                .thenReturn(Book.builder().id(11L).isbn("1235543").title("Amanhã você vai entender").author("Camila").build());

        // execucao
        Book savedBook = service.save(book);

        // verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("1235543");
        assertThat(savedBook.getTitle()).isEqualTo("Amanhã você vai entender");
        assertThat(savedBook.getAuthor()).isEqualTo("Camila");
    }

    @Test
    @DisplayName("Deve lancar erro de negocio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN(){
        // cenario
        Book book = createBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // execucao
        Throwable exception = catchThrowable(() -> service.save(book));

        //validacao
        assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("ISBN já cadastrado");

        Mockito.verify(repository, Mockito.never()).save(book);


    }

    @Test
    @DisplayName("Deve obter um livro po id")
    public void getByIdTest(){
        //cenario
        Long id = 22L;
        Book book = createBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        // execucao
        Optional<Book> foundbOOK = service.getById(id);

        //verificacao
        assertThat( foundbOOK.isPresent()).isTrue();
        assertThat( foundbOOK.get().getId()).isEqualTo(book.getId());
        assertThat( foundbOOK.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat( foundbOOK.get().getTitle()).isEqualTo(book.getTitle());
        assertThat( foundbOOK.get().getIsbn()).isEqualTo(book.getIsbn());

    }

    @Test
    @DisplayName("Deve retornar vazio ao procurar um livro por id que nao existe")
    public void bookNotFoundByIdTest(){
        //cenario
        Long id = 22L;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        // execucao
        Optional<Book> foundbOOK = service.getById(id);

        //verificacao
        assertThat( foundbOOK.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Deve deletar um livro com sucesso")
    public void deleteTest(){
        //cenario
        Book book = createBook();
        book.setId(11L);

        //execucao
        Assertions.assertDoesNotThrow( () -> service.delete(book));

        //verificacao
        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve retornar um excecao ao tentar deletar um livro com id nulo")
    public void deleteBookIdNullTest(){
        //cenario
        Book book = Book.builder().id(null).build();
        //execucao
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        //verificacao
        Assertions.assertEquals(exception.getMessage(), "Book id cant be null.");
        Mockito.verify(repository, Mockito.times(0)).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro com sucesso")
    public void updateBook(){
        //cenario
        Book book = createBook();
        book.setId(11L);

        Book updateBook = Book.builder().id(11L).author("Luciana da Silva").isbn("1235543").title("Amanhã de tarde").build();

        Mockito.when(repository.save(book)).thenReturn(updateBook);
        //execucao
        Book updatedBook =  service.update(book);

        //verificacao
        assertThat(updatedBook).isNotNull();
        assertThat(updatedBook.getTitle()).isEqualTo(updateBook.getTitle());
        assertThat(updatedBook.getAuthor()).isEqualTo(updateBook.getAuthor());
        Mockito.verify(repository, Mockito.times(1)).save(book);
    }

    @Test
    @DisplayName("Deve retornar uma excecao quando o id for nulo")
    public void updateBookWithIDNull(){

        //execucao
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(createBook()));

        //verificacao
        Assertions.assertEquals(exception.getMessage(), "Book id cant be null.");
        Mockito.verify(repository, Mockito.times(0)).save(createBook());
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void filterBookTest(){
        //cenario
        Book book = createBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 10), 1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execucao
        Page<Book> result = service.find(book, pageRequest);

        //verificacao
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookIsbnTest(){
        String isbn = "123";
        Mockito.when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1l).isbn(isbn).build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1l);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        Mockito.verify(repository, Mockito.times(1)).findByIsbn(isbn);
    }

    private Book createBook() {
        return Book.builder().author("Camila").isbn("1235543").title("Amanhã você vai entender").build();
    }
}











