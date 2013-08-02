package fromjava.handlerchain.common;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.util.Set;

public class TestHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        try {
            SOAPMessageContext smc = (SOAPMessageContext) context;
            SOAPMessage message = smc.getMessage();
            SOAPBody body = message.getSOAPBody();

            SOAPElement paramElement =
                (SOAPElement) body.getFirstChild().getFirstChild();
            int number = Integer.parseInt(paramElement.getValue());
            paramElement.setValue(String.valueOf(++number));
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {}

}
