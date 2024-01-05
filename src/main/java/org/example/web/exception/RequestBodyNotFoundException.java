package org.example.web.exception;

public class RequestBodyNotFoundException extends RuntimeException{
    public RequestBodyNotFoundException(String message){
        super(message);
    }

    public RequestBodyNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
