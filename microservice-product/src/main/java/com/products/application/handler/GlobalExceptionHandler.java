package com.products.application.handler;

import com.products.application.exception.*;
import io.github.responsekit.core.StandardResponse;
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
                .body(StandardResponse.error().message(e.getMessage()).build());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductNotFound(ProductNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(ProductSKUPriceNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductSKUPriceNotFound(ProductSKUPriceNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(ProductSKUNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductSKUNotFound(ProductSKUNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(ProductStockNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductStockNotFound(ProductStockNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(ProductNotActiveException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductNotActive(ProductNotActiveException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(SKUAlreadyInUseException.class)
    public ResponseEntity<StandardResponse<Void>> handleSKUAlreadyInUse(SKUAlreadyInUseException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(InvalidProductPriceTypeException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidProductPriceType(ProductNotActiveException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(InvalidProductCategoryException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidProductCategory(InvalidProductCategoryException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(CantDeleteProductException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantDeleteProduct(CantDeleteProductException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(ProductSKUWithoutBasePriceException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductSKUWithoutBasePrice(ProductSKUWithoutBasePriceException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(WishlistItemAlreadyExistsException.class)
    public ResponseEntity<StandardResponse<Void>> handleWishlistItemAlreadyExists(WishlistItemAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(ShoppingCartItemNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleShoppingCartItemNotFound(ShoppingCartItemNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(WishlistItemNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleWishlistItemNotFound(WishlistItemNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(ShoppingCartItemAlreadyExistsException.class)
    public ResponseEntity<StandardResponse<Void>> handleShoppingCartItemAlreadyExists(ShoppingCartItemAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductOutOfStock(ProductOutOfStockException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(EmptyShoppingCartException.class)
    public ResponseEntity<StandardResponse<Void>> handleEmptyShoppingCartException(EmptyShoppingCartException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }

    @ExceptionHandler(DeliveryAddressNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleDeliveryAddressNotFound(DeliveryAddressNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(e.getMessage()).build());
    }
    
    @ExceptionHandler(ProductCategoryNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductCategoryNotFound(ProductCategoryNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(e.getMessage()).build());
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

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<StandardResponse<Void>> handleBadGateway(BadGatewayException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(StandardResponse.error().message(exception.getMessage()).build());
    }
}
