package server.session_ignorecase_834.server;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import javax.jws.WebMethod;
import javax.jws.WebService;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;

/**
 * HTTP session test
 *
 * @author Jitendra Kotamraju
 */
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
public class HelloServiceImpl {
    
    @Resource
    private WebServiceContext wsc;
    private Set<String> clients;

    public HelloServiceImpl() {
        clients = new HashSet<String>();
    }
    
    @WebMethod
    public void introduce() {
        String id = getClientId();
        System.out.println("** storing session id: " + id);
        clients.add(id);
    }
    
    @WebMethod
    public boolean rememberMe() {
        String id = getClientId();
        System.out.println("** looking up id: " + id);
        return clients.contains(id);
    }
    
    private String getClientId() {
        HttpServletRequest req = (HttpServletRequest)
            wsc.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        HttpSession session = req.getSession();
        return session.getId();
    }
    
}
