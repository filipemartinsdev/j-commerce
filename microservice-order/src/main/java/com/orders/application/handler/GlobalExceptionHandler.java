package com.orders.application.handler;

import com.orders.application.dto.StandardResponse;
import com.orders.application.exception.CantCancelSalesOrderException;
import com.orders.application.exception.CantCreateSalesOrderException;
import com.orders.application.exception.DeliveryAddressNotFoundException;
import com.orders.application.exception.ForbiddenOperationException;
import com.orders.application.exception.SalesOrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Void>> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error(e.getMessage()));
    }

    @ExceptionHandler(SalesOrderNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleSalesOrderNotFound(SalesOrderNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(DeliveryAddressNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleDeliveryAddressNotFound(DeliveryAddressNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(CantCreateSalesOrderException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantCreateSalesOrder(CantCreateSalesOrderException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(CantCancelSalesOrderException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantCancelSalesOrder(CantCancelSalesOrderException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<StandardResponse<Void>> handleForbiddenOperation(ForbiddenOperationException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(StandardResponse.fail(e.getMessage()));
    }
}