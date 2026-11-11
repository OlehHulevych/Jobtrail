package com.huleych.jobtrail.shared.web;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import com.huleych.jobtrail.identity.domain.exceptions.InvalidCredentialsException;
import com.huleych.jobtrail.identity.domain.exceptions.RefreshTokenNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.InvalidStatusTransitionException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.domain.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<String> handleEmailAlreadyTakenException(EmailAlreadyTakenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<String> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<String> handleApplicationNotFoundException(ApplicationNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());

    }
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(NotApplicationOwnerException.class)
    public ResponseEntity<String> handleNotApplicationOwnerException(NotApplicationOwnerException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());

    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<String> handleInvalidStatusTransitionException(InvalidStatusTransitionException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());

    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());

    }


}
