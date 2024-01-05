package org.example.web.exception;

public class MethodNotFoundException extends RuntimeException {
    public MethodNotFoundException(String message){
        super(message);
    }

    public MethodNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
