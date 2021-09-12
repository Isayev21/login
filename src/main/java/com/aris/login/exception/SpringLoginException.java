package com.aris.login.exception;

public class SpringLoginException extends RuntimeException{

    public SpringLoginException(String message){
        super(message);
    }

    public SpringLoginException(String message, Exception exception) {
        super(message, exception);
    }
}
