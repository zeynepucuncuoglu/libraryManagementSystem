package com.zeynep.librarymanagementsystem.exception;


public class ActiveBorrowRecordNotFoundException extends RuntimeException {
    public ActiveBorrowRecordNotFoundException(String message) {
        super(message);
    }
}
