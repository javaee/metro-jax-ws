package com.bea.wli.sb.transports.ejb;

import java.security.PrivilegedExceptionAction;
//import weblogic.jws.WLLocalTransport;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.jws.soap.SOAPBinding.ParameterStyle;
//import com.bea.wli.sb.transports.ejb.jwsgeneration.BaseJwsService;
import java.util.HashMap;

//@WLLocalTransport(serviceUri="service.jws")
@WebService(targetNamespace="http://www.openuri.org/", serviceName="JwsService", name="Jws")@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public class Jws2 {
//HashMap props = null;
//public Jws(HashMap props)
//{
//this.props = props;
//}

//        @WebMethod(action="http://www.openuri.org/convertRupeeToUSDollars")
//        @WebResult(name="return")
//        public java.lang.Double convertRupeeToUSDollars( @WebParam(name="arg0") final org.example.money.RupeeA arg0) throws Exception
//        {
//            return null;
//        }
//
//        @WebMethod(action="http://www.openuri.org/convertUSDollarsToRupee")
//        @WebResult(name="return")
//        public java.lang.Double convertUSDollarsToRupee( @WebParam(name="arg0") final org.example.money.MoneyUS arg0) throws Exception
//        {
//            return null;
//        }
    }
