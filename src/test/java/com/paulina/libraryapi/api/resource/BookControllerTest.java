package com.paulina.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paulina.libraryapi.api.dto.BookDTO;
import com.paulina.libraryapi.api.exception.BusinessException;
import com.paulina.libraryapi.model.entity.Book;
import com.paulina.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {

        // cenario
        BookDTO dto = createNewBook();

        Book savedBook = Book.builder().id(12L).author("Artur").title("As Aventuras").isbn("123456").build();

        // execução
        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        // verificacao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON).content(json);

        mvc.perform(request).andExpect(status().isCreated()).andExpect( jsonPath("id").isNotEmpty() )
                .andExpect( jsonPath("title").value(dto.getTitle()))
                .andExpect( jsonPath("author").value(dto.getAuthor()))
                .andExpect( jsonPath("isbn").value(dto.getIsbn()));


    } 

    @Test
    @DisplayName("Deve lancar erro de validação quando nao houver dados suficientes para criação do livro")
    public void createIvalidBookTest() throws Exception {
        // cenario

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(json);

        mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", hasSize(3)));
        //
    }

    @Test
    @DisplayName("Deve lancar erro ao tentar cadastrar um livro com isbn ja utilizado por outro.")
    public void createBookWithDuplicatedISBN() throws Exception{

        BookDTO dto = createNewBook();

        String json = new ObjectMapper().writeValueAsString(dto);
        BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException("ISBN já cadastrado"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(json);

        mvc.perform(request).andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("ISBN já cadastrado"));

    }

    @Test
    @DisplayName("Deve obter informações de um livro.")
    public void getBookDetailsTest() throws Exception {
        // cenario
        Long id = 11L;
        Book book = Book.builder().title(createNewBook().getTitle()).author(createNewBook().getAuthor()).id(id).isbn(createNewBook().getIsbn()).build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        // execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/"+id)).accept(MediaType.APPLICATION_JSON);

        // verificacao
        mvc.perform(request)
                .andExpect(jsonPath("id").value(id))
                .andExpect( jsonPath("title").value(createNewBook().getTitle()))
                .andExpect( jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect( jsonPath("isbn").value(createNewBook().getIsbn()));

    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado nao existir")
    public void bookNotFoundTest() throws Exception{
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/"+1)).accept(MediaType.APPLICATION_JSON);

        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception{
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/"+1));

        mvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando nao encontrar um livro para deletar")
    public void deleteInexistentBookTest() throws Exception{
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/"+1));

        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception{
        Long id = 11L;

        Book updateBook = Book.builder().id(id).author("Artur").title("As Aventuras").isbn("123456").build();
        String json = new ObjectMapper().writeValueAsString(updateBook);
        Book updatingBook = Book.builder().id(id).title("Sem nocao").author("Luciana").isbn("123456").build();


        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));

        BDDMockito.given(service.update(updatingBook)).willReturn(updateBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/"+11)).content(json).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect( jsonPath("title").value(createNewBook().getTitle()))
                .andExpect( jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect( jsonPath("isbn").value(createNewBook().getIsbn()));;

    }

    @Test
    @DisplayName("Deve retornar 404 ao tentat atualizar um livro inexistente")
    public void updateInexistentBookTest() throws Exception{
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/"+11)).content(json).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception{
        //cenario
        Long id = 11L;
        Book book = Book.builder().id(id).author("Artur").title("As Aventuras").isbn("123456").build();

        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        //execucao
        //"/api/books?"
        String queryString = String.format("?title=%s&author=%s&page=0&size=100",book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString)).accept(MediaType.APPLICATION_JSON);

        mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

    }

    private BookDTO createNewBook() {
        return BookDTO.builder().author("Artur").title("As Aventuras").isbn("123456").build();
    }
}
























