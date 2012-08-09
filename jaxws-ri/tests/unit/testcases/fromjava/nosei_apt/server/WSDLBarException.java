
package fromjava.nosei_apt.server;

@javax.xml.ws.WebFault(name="BarException",
    targetNamespace="urn:test:types")
public class WSDLBarException extends Exception {
    Bar bar;

    public WSDLBarException(String message, Bar bar) {
	  super(message);
        this.bar = bar;
    }

    public Bar getFaultInfo() {
        return bar;
    }
}
