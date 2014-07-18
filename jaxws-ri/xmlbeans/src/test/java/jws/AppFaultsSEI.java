package jws;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import jws.faults.*;

import com.bea.wli.sb.transports.ejb.test.ejb3.CheckedException;

@WebService(targetNamespace="http://www.openuri.org/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public interface AppFaultsSEI {
    public String appEx(String str) throws Exception, AppException;

//    public String chcEx(String str) throws CheckedException;
    public String chcEx(String str) throws Exception, AppException, CheckedException, AppStrArrayException;
    
    public String countryInfo(String str) throws CountryInfoException, CountriesException;
    
    public MyBean echoBean(MyBean b);
}