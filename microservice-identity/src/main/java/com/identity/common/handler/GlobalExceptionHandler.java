package com.identity.common.handler;

import com.identity.common.exception.InvalidEntityMapperException;
import com.identity.common.exception.NullResponsePageException;
import com.identity.profile.application.exception.UserProfileNotFoundException;
import com.identity.security.application.exception.ForbiddenOperationException;
import com.identity.security.application.exception.UserAlreadyExistsException;
import com.identity.security.application.exception.UserNotFoundException;
import io.github.responsekit.core.StandardResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleException(Exception exception){
        log.error(exception.getMessage(), exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    @ExceptionHandler(BadJwtException.class)
    public ResponseEntity<StandardResponse<Void>> handleBadJwt(BadJwtException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<Void> handleException(DataAccessResourceFailureException exception){
        log.warn("Database connection timeout: {}", exception.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleUserNotFound(UserNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<StandardResponse<Void>> handleUserAlreadyExists(UserAlreadyExistsException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleUserProfileNotFound(UserProfileNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail().message(exception.getMessage()).build());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<StandardResponse<Void>> handleForbiddenOperation(ForbiddenOperationException exception){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
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
}
