package jws;

public class AppException extends java.lang.Exception {
    private java.lang.String errorCode;
    private java.lang.String errorDetail;
    private java.lang.String errorMessage;
    public AppException() {
        super();
    }
    public AppException(Throwable throwable) {
        super(throwable);
    }
    public AppException(String message) {
        super(message);
    }
    public AppException(java.lang.String errorCode, java.lang.String errorMessage, java.lang.String errorDetail) {
        super();
        setErrorCode(errorCode);
        setErrorMessage(errorMessage);
        setErrorDetail(errorDetail);
    }
    public java.lang.String getErrorCode() {
        return this.errorCode;
    }
    public void setErrorCode(java.lang.String arg) {
        this.errorCode=arg;
    }
    public java.lang.String getErrorDetail() {
        return this.errorDetail;
    }
    public void setErrorDetail(java.lang.String arg) {
        this.errorDetail=arg;
    }
    public java.lang.String getErrorMessage() {
        return this.errorMessage;
    }
    public void setErrorMessage(java.lang.String arg) {
        this.errorMessage=arg;
    }
}

