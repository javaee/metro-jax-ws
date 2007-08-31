package fromwsdl.handler_simple.common;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.ProtocolException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Set;

public class SOAPTestHandler implements SOAPHandler<SOAPMessageContext> {

    public static final int THROW_RUNTIME_EXCEPTION = -100;
    public static final int THROW_PROTOCOL_EXCEPTION = -101;
    public static final int THROW_SOAPFAULT_EXCEPTION = -102;

    @PostConstruct
    public void initMeNoArg() {
        // just here for debugging
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        try {
            SOAPMessage message = smc.getMessage();
            SOAPBody body = message.getSOAPBody();

            SOAPElement paramElement =
                (SOAPElement) body.getFirstChild().getFirstChild();
            int number = Integer.parseInt(paramElement.getValue());

            if (number == THROW_RUNTIME_EXCEPTION) {
                throw new RuntimeException("EXPECTED EXCEPTION");
            } else if (number == THROW_PROTOCOL_EXCEPTION) {
                throw new ProtocolException("EXPECTED EXCEPTION");
            } else if (number == THROW_SOAPFAULT_EXCEPTION) {
                //todo
            }

            paramElement.setValue(String.valueOf(++number));
        } catch (SOAPException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @PreDestroy
    public void destroyMe() {
        // just here for debugging
    }

    public void close(MessageContext context) {}

}
