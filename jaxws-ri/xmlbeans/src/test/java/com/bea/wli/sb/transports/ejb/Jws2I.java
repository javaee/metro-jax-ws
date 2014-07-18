package com.bea.wli.sb.transports.ejb;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace="http://www.openuri.org/", serviceName="JwsService", name="Jws")@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public interface Jws2I {


//    @WebMethod(action="http://www.openuri.org/convertRupeeToUSDollars")
//    @WebResult(name="return")
//    public java.lang.Double convertRupeeToUSDollars( @WebParam(name="arg0") final org.example.money.RupeeA arg0) throws Exception;
//
//    @WebMethod(action="http://www.openuri.org/convertUSDollarsToRupee")
//    @WebResult(name="return")
//    public java.lang.Double convertUSDollarsToRupee( @WebParam(name="arg0") final org.example.money.MoneyUS arg0) throws Exception;
}
