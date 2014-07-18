package jws;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import jws.faults.*;

import com.bea.wli.sb.transports.ejb.test.ejb3.CheckedException;

@WebService(targetNamespace="http://www.openuri.org/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public class AppFaultsSEB {

    public String appEx(String str) throws Exception, AppException {
        return str;
    }

    public String chcEx(String str) throws  Exception, AppException, CheckedException, AppStrArrayException{ //Exception, AppException,
        if (str.equals("AppStrArrayException"))  throw new AppStrArrayException("AppStrArrayException", "foo", "cool");
        if (str.equals("AppException"))  throw new AppException("AppExceptionX", "AppExceptionY", "AppExceptionZ");
        return str;
    }

    public String countryInfo(String str) throws CountryInfoException , CountriesException{
        return str;
    }
   

    public MyBean echoBean(MyBean b) { return b; }
}
