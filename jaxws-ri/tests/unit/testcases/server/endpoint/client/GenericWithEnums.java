package server.endpoint.client;

import java.util.Collections;
import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author lingling guo
 */
@WebService(serviceName = "GenericWithEnumsService",portName = "GenericWithEnumsPort",name = "GenericWithEnums",targetNamespace = "http://GenericWithEnumsservice.org/wsdl")
public class GenericWithEnums {    
    public GenericWithEnums() {}
    public enum AnEnum { VAL1, VAL2 }  
    @WebMethod
    public List<String> getListOfStringFromEnumArray(AnEnum[] anEnums) {
        return Collections.emptyList();
    }
}