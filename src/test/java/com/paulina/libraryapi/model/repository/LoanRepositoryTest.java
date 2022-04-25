package com.paulina.libraryapi.model.repository;


import com.paulina.libraryapi.model.entity.Book;
import com.paulina.libraryapi.model.entity.Loan;
import com.paulina.libraryapi.model.respository.LoanRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;


    private Book createNewBook() {
        return Book.builder().title("Meu Livro").isbn("1234").author("Jana").build();
    }

    @Test
    @DisplayName("Deve verificar se existe emprestimo nao devolvido para o livro")
    public void existsByBookAndNotReturnedTest(){
        // cenario
        Book book = createNewBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Camila").loanDate(LocalDate.now()).build();

        entityManager.persist(loan);


        //execucao
        boolean exists = repository.existsByBookAndNotReturned(book);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar um emprestimo pelo isbn do livro ou pelo customer")
    public  void findBookByISBNOrCustomerTest(){
        Book book = createNewBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Camila").loanDate(LocalDate.now()).build();

        entityManager.persist(loan);

        Page<Loan> result = repository.findByBookISBNOrCustomer("1234", "Camila", PageRequest.of(0, 10));

        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve obter emprestimos cujo a data for menor ou igual a tres dias atreas e nao retornados")
    public void findByLoanDateLessThanNotReturnedTest(){
        Book book = createNewBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Camila").loanDate(LocalDate.now().minusDays(5)).build();

        entityManager.persist(loan);

       List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).hasSize(1).contains(loan);

    }

    @Test
    @DisplayName("Deve retornar vazio quando não houver emprestimos atrasados")
    public void notFindByLoanDateLessThanNotReturnedTest(){
        Book book = createNewBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Camila").loanDate(LocalDate.now()).build();

        entityManager.persist(loan);

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).isEmpty();

    }




}
