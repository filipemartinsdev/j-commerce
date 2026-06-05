package com.orders.application.handler;

import com.orders.application.exception.*;
import io.github.responsekit.core.StandardResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Void>> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error().message(exception.getMessage()).build());
    }

    @ExceptionHandler(SalesOrderNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleSalesOrderNotFound(SalesOrderNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(DeliveryAddressNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleDeliveryAddressNotFound(DeliveryAddressNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(CantCreateSalesOrderException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantCreateSalesOrder(CantCreateSalesOrderException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(CantCancelSalesOrderException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantCancelSalesOrder(CantCancelSalesOrderException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<StandardResponse<Void>> handleForbiddenOperation(ForbiddenOperationException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(InvalidDeliveryAddressCoordinatesException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidDeliveryAddressCoordinates(InvalidDeliveryAddressCoordinatesException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(InvalidDeliveryAddressException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidDeliveryAddress(InvalidDeliveryAddressException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(StorageAddressNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleStorageAddressNotFound(StorageAddressNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(InvalidEntityMapperException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidEntityMapper(InvalidEntityMapperException exception){
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error().message(exception.getMessage()).build());
    }

    @ExceptionHandler(NullResponsePageException.class)
    public ResponseEntity<StandardResponse<Void>> handleNullResponsePage(NullResponsePageException exception){
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error().message(exception.getMessage()).build());
    }

    @ExceptionHandler(InvalidRouteResponseException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidRouteResponse(InvalidRouteResponseException exception){
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(StandardResponse.error().message(exception.getMessage()).build());
    }

    @ExceptionHandler(CantUpdateShippingStatusException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantUpdateShippingStatus(CantUpdateShippingStatusException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(ShippingStatusNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleShippingStatusNotFound(ShippingStatusNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(ShippingNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleShippingNotFound(ShippingNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<StandardResponse<Void>> handleBadGateway(BadGatewayException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(StandardResponse.error().message(exception.getMessage()).build());
    }

    @ExceptionHandler(CantDispatchShippingException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantDispatchShipping(CantDispatchShippingException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(CantCheckInShippingException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantCheckInShipping(CantCheckInShippingException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(CantCheckOutShippingException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantCheckOutShipping(CantCheckOutShippingException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(CantCancelShippingException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantCancelShipping(CantCancelShippingException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(InvalidReverseGeocodingResponseException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidReverseGeocodingResponse(InvalidReverseGeocodingResponseException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(StandardResponse.error().message(exception.getMessage()).build());
    }
}