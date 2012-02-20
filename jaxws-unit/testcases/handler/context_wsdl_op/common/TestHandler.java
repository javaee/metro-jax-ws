package handler.context_wsdl_op.common;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.WebServiceException;
import java.util.Set;

/**
 * This handler verifies the WSDL_OPERATION property in MessageContext
 * @author Rama Pulavarthi
 */
public class TestHandler implements SOAPHandler<SOAPMessageContext> {
    private String runtime;
    private final QName expected_wsdl_op = new QName("urn:test", "sayHello");

    public TestHandler(String runtime) {
        this.runtime = runtime;
    }

    public TestHandler() {
        this.runtime = "SERVER-SIDE";
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        System.out.println(runtime + " Message Oubound: " + context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        QName got_wsdl_op = (QName) context.get(MessageContext.WSDL_OPERATION);
        //System.out.println(got_wsdl_op);
        if (expected_wsdl_op.equals(got_wsdl_op))
            return true;
        else
            throw new WebServiceException("WSDL Operation property not available in "+runtime+" handler");        
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
    }

}
