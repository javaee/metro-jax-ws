package annotations.server;

public class AddNumbersException extends Exception {
    String info;

    public AddNumbersException(String message, String detail) {
      super(message);
        this.info = detail;
    }

    public String getFaultInfo() {
        return info;
    }
}
