package com.rev.app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        logger.error("Internal Server Error: ", ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        System.out.println("DEBUG: GlobalExceptionHandler - ResourceNotFoundException caught: " + ex.getMessage());
        logger.error("Resource Not Found: ", ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        System.out.println(
                "DEBUG: GlobalExceptionHandler - NoHandlerFoundException caught for URL: " + ex.getRequestURL());
        logger.error("Page Not Found: ", ex);
        return "error/404";
    }
}
