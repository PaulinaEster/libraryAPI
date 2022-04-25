package com.paulina.libraryapi.service;

import com.paulina.libraryapi.api.dto.LoanFilterDTO;
import com.paulina.libraryapi.api.exception.BusinessException;
import com.paulina.libraryapi.model.entity.Book;
import com.paulina.libraryapi.model.entity.Loan;
import com.paulina.libraryapi.model.respository.LoanRepository;
import com.paulina.libraryapi.service.imp.LoanServiceImp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    private LoanService service;

    @MockBean
    private LoanRepository reposotory;

    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImp(reposotory);

    }

    @Test
    @DisplayName("Deve salvar um emprestimo")
    public void saveLoanTest(){
        Book book = Book.builder().id(1l).isbn("123").build();

        Loan loan = Loan.builder()
                .book(book)
                .id(1l)
                .customer("Camila")
                .loanDate(LocalDate.now())
                .build();

        Loan saveLoan = Loan.builder().id(1l).loanDate(LocalDate.now()).customer("Camila").book(book).build();

        Mockito.when(reposotory.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(reposotory.save(loan)).thenReturn(saveLoan);

        Loan savingLoan = service.save(loan);

        assertThat(loan.getId()).isEqualTo(saveLoan.getId());
        assertThat(loan.getBook().getId()).isEqualTo(saveLoan.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(saveLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(saveLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lancar erro de negocio ao salvar um emprestimo com livro emprestado")
    public void loanedBookSaveTest(){
        Loan loan = createLoan();
        Mockito.when(reposotory.existsByBookAndNotReturned(loan.getBook())).thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(loan));

        assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Book already loaned");

        Mockito.verify(reposotory, Mockito.never()).save(loan);

    }



    @Test
    @DisplayName("Deve obter as informações de um emprestimo pelo id")
    public void getLoanDetailsTest(){
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(reposotory.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> retult = service.getById(id);

        assertThat(retult.isPresent()).isTrue();
        assertThat(retult.get().getId()).isEqualTo(id);
        assertThat(retult.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(retult.get().getBook()).isEqualTo(loan.getBook());
        assertThat(retult.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify(reposotory).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um emprestimo")
    public void updateLoanTest(){
        Loan loan = createLoan();
        loan.setId(1l);

        loan.setReturned(true);

        when(reposotory.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();
        verify(reposotory).save(loan);

    }


    @Test
    @DisplayName("Deve filtrar emprestimos pelas propriedades")
    public void filterLoanTest(){
        //cenario
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Camila").isbn("123").build();
        Loan loan = createLoan();
        loan.setId(1l);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = Arrays.asList(loan);
        Page<Loan> page = new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1);
        Mockito.when(reposotory.findByBookISBNOrCustomer(Mockito.anyString(),Mockito.anyString() , Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execucao
        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        //verificacao
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(list);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }


    public static Loan createLoan() {
        Book book = Book.builder().id(1l).isbn("123").build();

        return Loan.builder()
                .book(book)
                .id(1l)
                .customer("Camila")
                .loanDate(LocalDate.now())
                .build();
    }



}
