package com.quirogaotero.beans.ordering.adapter.in.web;

import com.quirogaotero.beans.ordering.application.CheckoutNotFoundException;
import com.quirogaotero.beans.ordering.application.CheckoutNotPayableException;
import com.quirogaotero.beans.ordering.application.PaymentDeclinedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderingExceptionHandler {

    @ExceptionHandler(CheckoutNotFoundException.class)
    ProblemDetail handleNotFound(CheckoutNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    ProblemDetail handleDeclined(PaymentDeclinedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleConflict(IllegalStateException ex) {
        // e.g. paying an already-paid checkout
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CheckoutNotPayableException.class)
    ProblemDetail handleNotPayable(CheckoutNotPayableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

}