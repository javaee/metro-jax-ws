package handler.context_wsdl_op.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;

import handler.context_wsdl_op.common.TestHandler;

/**
 * Tests WSDL_OPERATION property in MessageContext
 * @author Rama Pulavarthi
 */
public class MessageContextTest extends TestCase {
    public MessageContextTest(String name) throws Exception {
        super(name);
    }

    public void testRequestResponse() throws Exception {
      HelloService  helloService = new HelloService();
      Hello helloPort = helloService.getHelloPort();
      Binding binding = ((BindingProvider)helloPort).getBinding();
      List<Handler> handlers =  new ArrayList<Handler>();
      handlers.add(new TestHandler("CLIENT-SIDE"));
      binding.setHandlerChain(handlers);  
      int x = 1;
      int y = helloPort.sayHello(x);
      assertTrue(y == x);
    }
}