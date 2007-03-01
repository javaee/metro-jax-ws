package external_customize.server;

@javax.xml.ws.WebFault(name = "AddNumbersException", targetNamespace = "http://duke.example.org")
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
