package com.products.application.handler;

import com.products.application.exception.*;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @GraphQlExceptionHandler(Throwable.class)
    public GraphQLError handleThrowable(Throwable e, DataFetchingEnvironment env){
        log.error(e.getMessage(), e);

        return GraphQLError.newError()
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Internal server error")
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    private GraphQLError buildError(ErrorType errorType, Exception e, DataFetchingEnvironment env){
        return GraphQLError.newError()
                .errorType(errorType)
                .message(e.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    @GraphQlExceptionHandler(AuthorizationDeniedException.class)
    private GraphQLError handleAuthorizationDenied(AuthorizationDeniedException e, DataFetchingEnvironment env){
        return buildError(ErrorType.UNAUTHORIZED, e, env);
    }

    @GraphQlExceptionHandler(AuthenticationException.class)
    private GraphQLError handleAuthentication(AuthenticationException e, DataFetchingEnvironment env){
        return buildError(ErrorType.UNAUTHORIZED, e, env);
    }

    @GraphQlExceptionHandler(BadGatewayException.class)
    private GraphQLError handleBadGateway(BadGatewayException e, DataFetchingEnvironment env){
        return buildError(ErrorType.INTERNAL_ERROR, e, env);
    }

    @GraphQlExceptionHandler(DeliveryAddressNotFoundException.class)
    private GraphQLError handleDeliveryAddressNotFound(DeliveryAddressNotFoundException e, DataFetchingEnvironment env){
        return buildError(ErrorType.NOT_FOUND, e, env);
    }

    @GraphQlExceptionHandler(EmptyShoppingCartException.class)
    private GraphQLError handleEmptyShoppingCart(EmptyShoppingCartException e, DataFetchingEnvironment env){
        return buildError(ErrorType.BAD_REQUEST, e, env);
    }

    @GraphQlExceptionHandler(InvalidCatalogueQueryException.class)
    private GraphQLError handleInvalidCatalogueQuery(InvalidCatalogueQueryException e, DataFetchingEnvironment env){
        return buildError(ErrorType.BAD_REQUEST, e, env);
    }

    @GraphQlExceptionHandler(InvalidStockMovementReasonException.class)
    private GraphQLError handleInvalidStockMovementReason(InvalidStockMovementReasonException e, DataFetchingEnvironment env){
        return buildError(ErrorType.BAD_REQUEST, e, env);
    }

    @GraphQlExceptionHandler(InvalidStockMovementTypeException.class)
    private GraphQLError handleInvalidStockMovementType(InvalidStockMovementTypeException e, DataFetchingEnvironment env){
        return buildError(ErrorType.BAD_REQUEST, e, env);
    }

    @GraphQlExceptionHandler(ProductCategoryNotFoundException.class)
    private GraphQLError handleProductCategoryNotFound(ProductCategoryNotFoundException e, DataFetchingEnvironment env){
        return buildError(ErrorType.NOT_FOUND, e, env);
    }

    @GraphQlExceptionHandler(ProductEmbeddingNotFoundException.class)
    private GraphQLError handleProductEmbeddingNotFound(ProductEmbeddingNotFoundException e, DataFetchingEnvironment env){
        return buildError(ErrorType.NOT_FOUND, e, env);
    }

    @GraphQlExceptionHandler(ProductNotFoundException.class)
    private GraphQLError handleProductNotFound(ProductNotFoundException e, DataFetchingEnvironment env){
        return buildError(ErrorType.NOT_FOUND, e, env);
    }

    @GraphQlExceptionHandler(ProductOutOfStockException.class)
    private GraphQLError handleProductOutOfStock(ProductOutOfStockException e, DataFetchingEnvironment env){
        return buildError(ErrorType.BAD_REQUEST, e, env);
    }

    @GraphQlExceptionHandler(ProductSKUNotFoundException.class)
    private GraphQLError handleProductSKUNotFound(ProductSKUNotFoundException e, DataFetchingEnvironment env){
        return buildError(ErrorType.NOT_FOUND, e, env);
    }

    @GraphQlExceptionHandler(ShoppingCartItemAlreadyExistsException.class)
    private GraphQLError handleShoppingCartItemAlreadyExists(ShoppingCartItemAlreadyExistsException e, DataFetchingEnvironment env){
        return buildError(ErrorType.BAD_REQUEST, e, env);
    }

    @GraphQlExceptionHandler(ShoppingCartItemNotFoundException.class)
    private GraphQLError handleShoppingCartItemNotFound(ShoppingCartItemNotFoundException e, DataFetchingEnvironment env){
        return buildError(ErrorType.NOT_FOUND, e, env);
    }

    @GraphQlExceptionHandler(SKUAlreadyExistsException.class)
    private GraphQLError handleSKUAlreadyExists(SKUAlreadyExistsException e, DataFetchingEnvironment env){
        return buildError(ErrorType.BAD_REQUEST, e, env);
    }

    @GraphQlExceptionHandler(WishlistItemAlreadyExistsException.class)
    private GraphQLError handleWishlistItemAlreadyExists(WishlistItemAlreadyExistsException e, DataFetchingEnvironment env){
        return buildError(ErrorType.BAD_REQUEST, e, env);
    }

    @GraphQlExceptionHandler(WishlistItemNotFoundException.class)
    private GraphQLError handleWishlistItemNotFound(WishlistItemNotFoundException e, DataFetchingEnvironment env){
        return buildError(ErrorType.NOT_FOUND, e, env);
    }
}
