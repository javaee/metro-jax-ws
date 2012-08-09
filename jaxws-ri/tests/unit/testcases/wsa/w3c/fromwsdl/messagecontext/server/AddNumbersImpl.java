package wsa.w3c.fromwsdl.messagecontext.server;

import javax.jws.WebService;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.ws.developer.JAXWSProperties;

/**
 * Testcase for issue 629: Access all proeprties in MessageContext and verify WSA headers.
 * @author Rama Pulavarthi
 */

@WebService(serviceName="AddNumbersService", portName="AddNumbersPort",
        endpointInterface = "wsa.w3c.fromwsdl.messagecontext.server.AddNumbersPortType", targetNamespace = "http://example.com/")
public class AddNumbersImpl implements AddNumbersPortType {
    @Resource
    WebServiceContext wsc;
    public int addNumbers( int number1, int number2) throws AddNumbersFault_Exception {
        //Print all the preropteis int he message context.
        MessageContext mc = wsc.getMessageContext();
        for (String key : mc.keySet()) {
            System.out.println("Msg key: " + key);
            System.out.println("Msg value: " + mc.get(key));
        }

        String messageId = (String)mc.get(JAXWSProperties.ADDRESSING_MESSAGEID);
        if(messageId == null) {
            throw new WebServiceException("Expected wsa:MessageID header not present int he request message");
        }
        String action = (String)mc.get(JAXWSProperties.ADDRESSING_ACTION);
        if(action == null || !action.equals("urn:com:example:action")) {
            throw new WebServiceException("Expected wsa:Action header not present in the request message");
        }
                
        return number1 + number2;
    }


}