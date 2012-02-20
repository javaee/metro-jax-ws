package wsa.w3c.fromwsdl.messagecontext.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;

/**
 * Tests WSA porpeties in MessageContext with Addressing enabled.
 * @author Rama Pulavarthi
 */
public class MessageContextTest extends TestCase {
    public MessageContextTest(String name) throws Exception {
        super(name);
    }

    AddNumbersPortType getStub() throws Exception {
        return new AddNumbersService().getAddNumbersPort();
    }

    private String getEndpointAddress() throws Exception{

        BindingProvider bp = ((BindingProvider) getStub());
        return
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

   public void testRequestResponse() throws Exception {
        AddNumbersPortType myport = getStub();
        myport.addNumbers(10, 10);
    }
}