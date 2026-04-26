package com.notification.application.handler;

import com.notification.application.dto.StandardResponse;
import com.notification.application.exception.UserNotificationHasAlreadyBeenReadException;
import com.notification.application.exception.UserNotificationNotFoundException;
import com.notification.domain.entity.UserNotification;
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
                .body(StandardResponse.error());
    }

    @ExceptionHandler(UserNotificationNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleUserNotificationNotFound(UserNotificationNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(UserNotificationHasAlreadyBeenReadException.class)
    public ResponseEntity<StandardResponse<Void>> handleUserNotificationHasAlreadyBeenRead(UserNotificationHasAlreadyBeenReadException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.fail(e.getMessage()));
    }
}
