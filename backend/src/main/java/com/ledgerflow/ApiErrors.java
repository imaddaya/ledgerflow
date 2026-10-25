package com.ledgerflow;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.ConcurrencyFailureException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(DomainException.class) ResponseEntity<Map<String,String>> domain(DomainException e) { return error(e.status,e.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,String>> validation(MethodArgumentNotValidException e) {
        String message=e.getBindingResult().getFieldErrors().stream().findFirst().map(v->v.getField()+": "+v.getDefaultMessage()).orElse("Invalid request");
        return error(400,message);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String,String>> malformed(Exception e) { return error(400,"Invalid request value or JSON structure"); }
    @ExceptionHandler(MaxUploadSizeExceededException.class) ResponseEntity<Map<String,String>> tooLarge(Exception e) { return error(400,"Upload a CSV smaller than 1 MB"); }
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<Map<String,String>> duplicate(Exception e) { return error(409,"This record conflicts with existing data; refresh and try again"); }
    @ExceptionHandler(ConcurrencyFailureException.class) ResponseEntity<Map<String,String>> concurrent(Exception e) { return error(409,"Another operation changed this record; refresh and retry"); }
    private ResponseEntity<Map<String,String>> error(int status,String message) { return ResponseEntity.status(status).body(Map.of("message",message)); }
}
