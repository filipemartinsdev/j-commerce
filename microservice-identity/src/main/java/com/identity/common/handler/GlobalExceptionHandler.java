package com.identity.common.handler;

import com.identity.common.dto.StandardResponse;
import com.identity.security.application.exception.ForbiddenOperationException;
import com.identity.security.application.exception.UserAlreadyExistsException;
import com.identity.security.application.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Void>> handleException(Exception exception){
        log.error(exception.getMessage(), exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(BadJwtException.class)
    public ResponseEntity<StandardResponse<Void>> handleBadJwt(BadJwtException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleUserNotFound(UserNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<StandardResponse<Void>> handleUserAlreadyExists(UserAlreadyExistsException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<StandardResponse<Void>> handleForbiddenOperation(ForbiddenOperationException exception){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(StandardResponse.fail(exception.getMessage()));
    }
}
