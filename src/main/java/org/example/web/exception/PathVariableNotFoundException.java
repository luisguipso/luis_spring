package org.example.web.exception;

public class PathVariableNotFoundException extends RuntimeException {
    public PathVariableNotFoundException(String message){
        super(message);
    }

    public PathVariableNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
