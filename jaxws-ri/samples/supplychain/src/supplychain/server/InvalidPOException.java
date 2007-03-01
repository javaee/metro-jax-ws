package supplychain.server;

public class InvalidPOException extends Exception {
    String detail;
    
    public InvalidPOException (String message) {
        super (message);
        this.detail = message;
    }
    
    public InvalidPOException (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
