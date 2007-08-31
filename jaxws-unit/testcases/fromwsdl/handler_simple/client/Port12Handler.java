package fromwsdl.handler_simple.client;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import java.util.Set;

/**
 * This handler will be set on the soap 12 binding in the
 * customization file. It's used to test that bindings with multiple
 * ports actually use the correct ports. See bug 6353179 and
 * the HandlerClient tests cases.
 */
public class Port12Handler implements SOAPHandler<SOAPMessageContext> {

    private int called = 0;

    void resetCalled() {
        called = 0;
    }

    int getCalled() {
        return called;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        called++;
        return true;
    }

    /**** empty methods below ****/
    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext context) {}

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

}
