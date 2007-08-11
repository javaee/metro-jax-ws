package async.wsdl_hello_lit.client;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class SOAPTestHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        try {
            SOAPMessage message = smc.getMessage();
            SOAPBody body = message.getSOAPBody();

            SOAPElement paramElement =
                (SOAPElement) body.getFirstChild();
            int number = Integer.parseInt(paramElement.getValue());
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

    public void close(MessageContext context) {}

}
