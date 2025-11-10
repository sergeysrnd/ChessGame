package com.chessgame.exception;

/**
 * Base exception for chess game related errors
 */
public class ChessGameException extends RuntimeException {
    public ChessGameException(String message) {
        super(message);
    }
    
    public ChessGameException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ChessGameException(Throwable cause) {
        super(cause);
    }
}