package epr.wsepr_8188172.server;

import java.util.List;

import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * @author lingling guo
 */
@WebService(serviceName = "WseprService",portName = "WseprPort",name = "Wsepr",targetNamespace = "http://wseprservice.org/wsdl")
public class WseprImpl{
    @WebMethod
    public String get() {
        return "";
    }
}