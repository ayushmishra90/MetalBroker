package com.hugoserve.metalbroker.controller.advice;

import com.hugoserve.metalbroker.utils.ResponseErrorBuilder;
import com.hugoserve.metalbroker.utils.constants.ErrorCodes;
import com.hugoserve.metalbroker.utils.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Business / expected errors
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusiness(BusinessException ex) {

        return ResponseEntity.ok(
                ResponseErrorBuilder.error(
                        ex.getCode(),
                        ex.getMessage()
                )
        );
    }

    // ✅ Any uncaught exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnknown(Exception ex) {

        return ResponseEntity.ok(
                ResponseErrorBuilder.error(
                        ErrorCodes.INTERNAL_ERROR,
                        "Something went wrong"
                )
        );
    }
}
