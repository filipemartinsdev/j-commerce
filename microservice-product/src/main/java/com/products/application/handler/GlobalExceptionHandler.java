package com.products.application.handler;

import com.products.application.dto.StandardResponse;
import com.products.application.exception.*;
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

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductNotFound(ProductNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ProductSKUPriceNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductSKUPriceNotFound(ProductSKUPriceNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ProductSKUNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductSKUNotFound(ProductSKUNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ProductStockNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductStockNotFound(ProductStockNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ProductNotActiveException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductNotActive(ProductNotActiveException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(InvalidProductPriceTypeException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidProductPriceType(ProductNotActiveException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(InvalidProductCategoryException.class)
    public ResponseEntity<StandardResponse<Void>> handleInvalidProductCategory(InvalidProductCategoryException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(CantDeleteProductException.class)
    public ResponseEntity<StandardResponse<Void>> handleCantDeleteProduct(CantDeleteProductException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ProductSKUWithoutBasePriceException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductSKUWithoutBasePrice(ProductSKUWithoutBasePriceException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(WishlistItemAlreadyExistsException.class)
    public ResponseEntity<StandardResponse<Void>> handleWishlistItemAlreadyExists(WishlistItemAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ShoppingCartItemNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleShoppingCartItemNotFound(ShoppingCartItemNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(WishlistItemNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleWishlistItemNotFound(WishlistItemNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ShoppingCartItemAlreadyExistsException.class)
    public ResponseEntity<StandardResponse<Void>> handleShoppingCartItemAlreadyExists(ShoppingCartItemAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<StandardResponse<Void>> handleProductOutOfStock(ProductOutOfStockException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(StandardResponse.fail(e.getMessage()));
    }
}
