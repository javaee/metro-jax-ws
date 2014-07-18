package jws;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace="http://www.openuri.org/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public interface TypedXmlBeansDOCI {

    @WebMethod(action="http://www.openuri.org/addCountry")
    public com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument addCountry(final com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument countries, final com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType info) throws Exception;

    @WebMethod(action="http://www.openuri.org/getCountryInfo")
    @WebResult(name="info")
    public com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType getCountryInfo(final java.lang.String code, final java.lang.String name) throws Exception;

    @WebMethod(action="http://www.openuri.org/getCountryName")
//    @WebResult(name="name")
    public java.lang.String getCountryName(final com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument countries, final java.lang.String code) throws Exception;

    @WebMethod(action="http://www.openuri.org/getCountryNameXml")
    @WebResult(name="name")
    public java.lang.String getCountryNameXml(final com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument countries, final com.bea.wli.sb.transports.ejb.test.xbean.Code code) throws Exception;
}

