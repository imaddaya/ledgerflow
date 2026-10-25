package com.ledgerflow;

public class DomainException extends RuntimeException {
    final int status;
    public DomainException(int status, String message) { super(message); this.status = status; }
    static DomainException missing(String kind) { return new DomainException(404, kind + " was not found."); }
    static DomainException conflict(String message) { return new DomainException(409, message); }
}
