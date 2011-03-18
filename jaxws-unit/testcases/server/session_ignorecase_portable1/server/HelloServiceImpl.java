package server.session_ignorecase_portable1.server;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import javax.jws.WebMethod;
import javax.jws.WebService;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;
import java.util.*;

/**
 * Making sure that cookies are returned by client
 *
 * @author Jitendra Kotamraju
 */
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
public class HelloServiceImpl {
    
    @Resource
    private WebServiceContext wsc;
    
    @WebMethod
    public void introduce() {
        Map<String, List<String>> hdrs = new HashMap<String, List<String>>();
        hdrs.put("Set-Cookie", Collections.singletonList("a=b"));
        MessageContext mc = wsc.getMessageContext();
        mc.put(MessageContext.HTTP_RESPONSE_HEADERS, hdrs);
    }
    
    @WebMethod
    public boolean rememberMe() {
        MessageContext mc = wsc.getMessageContext();
        Map<String, List<String>> hdrs = (Map<String, List<String>>)mc.get(MessageContext.HTTP_REQUEST_HEADERS);
        List<String> cookieList = hdrs.get("Cookie");
        if (cookieList == null || cookieList.get(0) == null || !cookieList.get(0).equals("a=b")) {
            return false;
        }
        return true; 
    }
}
