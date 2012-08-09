package fromjava.schema_inline.server;

import javax.xml.ws.WebFault;

/**
 * @author Jitendra Kotamraju
 */
@WebFault(name = "BarException", targetNamespace = "urn:test:types")
public class WSDLBarException extends Exception {
    private final Bar bar;

    public WSDLBarException(String message, Bar bar) {
        super(message);
        this.bar = bar;
    }

    public Bar getFaultInfo() {
        return bar;
    }
}
