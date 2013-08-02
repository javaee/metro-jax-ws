
package fromjava.bare_710.server;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.Holder;
import javax.xml.ws.*;

/**
 * fromjava doc/bare mapping
 *
 * @author Jitenddra Kotamraju
 */
@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
@SOAPBinding(parameterStyle=ParameterStyle.BARE)
public class EchoImpl {

    public int add(NumbersRequest numRequest) {
        if (numRequest.number1 != 10)
            throw new WebServiceException("numRequest.number1 expected=10"+" got="+numRequest.number1);
        if (numRequest.number2 != 20)
            throw new WebServiceException("numRequest.number2 expected=10"+" got="+numRequest.number2);
        if (numRequest.guess != 25)
            throw new WebServiceException("numRequest.guess expected=25"+" got="+numRequest.guess);
        return numRequest.number1+numRequest.number2;
    }

    public void addNumbers(NumbersRequest numRequest,
                        @WebParam(mode=WebParam.Mode.OUT)
                        Holder<Integer> res) {
        if (numRequest.number1 != 10)
            throw new WebServiceException("numRequest.number1 expected=10"+" got="+numRequest.number1);
        if (numRequest.number2 != 20)
            throw new WebServiceException("numRequest.number2 expected=10"+" got="+numRequest.number2);
        if (numRequest.guess != 25)
            throw new WebServiceException("numRequest.guess expected=25"+" got="+numRequest.guess);
        res.value = numRequest.number1+numRequest.number2;
    }

//    > 1 IN body part. Throws an exception
//    public void sumNumbers(NumbersRequest numRequest,
//                        @WebParam(mode=WebParam.Mode.INOUT)
//                        Holder<Integer> res) {
//        if (numRequest.number1 != 10)
//            throw new WebServiceException("numRequest.number1 expected=10"+" got"+numRequest.number1);
//        if (numRequest.number2 != 20)
//            throw new WebServiceException("numRequest.number2 expected=10"+" got"+numRequest.number2);
//        if (res.value != 25)
//            throw new WebServiceException("res.value expected=25"+" got"+res.value);
//
//        res.value = numRequest.number1+numRequest.number2;
//    }

    public void echoString(@WebParam(mode=WebParam.Mode.INOUT)
                           Holder<String> str) {        
        if (!str.value.equals("test"))
            throw new WebServiceException("str.value expected=test"+" got="+str.value);
    }

    @WebResult(header=true)
    public String echoHeaders(@WebParam(mode=WebParam.Mode.INOUT)
                           Holder<String> str,
                           @WebParam(header=true)
                           String inHeader,
                           @WebParam(header=true, mode=WebParam.Mode.OUT)
                           Holder<String> outHeader,
                           @WebParam(header=true, mode=WebParam.Mode.INOUT)
                           Holder<String> inoutHeader) {
        if (!str.value.equals("test"))
            throw new WebServiceException("str.value expected=test"+" got="+str.value);
        if (!inHeader.equals("inHeader"))
            throw new WebServiceException("inHeader expected=inHeader"+" got="+inHeader);
        if (!inoutHeader.value.equals("inoutHeader"))
            throw new WebServiceException("inoutHeader expected=inoutHeader"+" got="+inoutHeader);
        outHeader.value = "outHeader";
        return "returnHeader";
    }

}
