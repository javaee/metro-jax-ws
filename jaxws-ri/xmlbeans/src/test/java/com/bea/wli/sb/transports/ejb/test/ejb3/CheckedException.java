package com.bea.wli.sb.transports.ejb.test.ejb3;

public class CheckedException extends Exception {
    public CheckedException() { super(); }
    private String error;
    public CheckedException(String error) {
        super();
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
