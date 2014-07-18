package jws;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace="http://www.openuri.org/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.BARE)
public interface ArraysBareI {

    @WebMethod(action="http://www.openuri.org/sort1")
    @WebResult(name="return")
    public java.lang.String[] sort1(final java.lang.String[] arg0) throws Exception;
}