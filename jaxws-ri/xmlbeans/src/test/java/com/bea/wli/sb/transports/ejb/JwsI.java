package com.bea.wli.sb.transports.ejb;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.xmlbeans.XmlObject;

@WebService(targetNamespace="http://www.openuri.org/", serviceName="JwsService", name="Jws")@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public interface JwsI {

          @WebMethod(action="http://www.openuri.org/addCountry")
          public com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument addCountry( @WebParam(name="arg0") final com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument arg0,  @WebParam(name="arg1") final com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType arg1) throws Exception
        ;

//          @WebMethod(action="http://www.openuri.org/getAddress")
//          @WebResult(name="return")
//          public com.bea.wli.sb.transports.ejb.test.xbean.MyAddress getAddress( @WebParam(name="arg0") final java.lang.String arg0,  @WebParam(name="arg1") final java.lang.String arg1) throws Exception
//         ;

          @WebMethod(action="http://www.openuri.org/getCountryInfo")
          @WebResult(name="return")
          public XmlObject getCountryInfo( @WebParam(name="arg0") final XmlObject arg0,  @WebParam(name="arg1") final java.lang.String arg1) throws Exception
          ;

          @WebMethod(action="http://www.openuri.org/getCountryName")
          @WebResult(name="return")
          public java.lang.String getCountryName( @WebParam(name="arg0") final com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument arg0,  @WebParam(name="arg1") final com.bea.wli.sb.transports.ejb.test.xbean.Code arg1) throws Exception
          ;

//          @WebMethod(action="http://www.openuri.org/getLastName")
//          @WebResult(name="return")
//          public java.lang.String getLastName( @WebParam(name="arg0") final com.bea.wli.sb.transports.ejb.test.xbean.MyAddress arg0) throws Exception
//          ;
}
