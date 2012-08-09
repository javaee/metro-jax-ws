
package fromjava.nosei_bare.server;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
@SOAPBinding(parameterStyle=ParameterStyle.BARE)
public class EchoImpl {
    boolean log=false;

    @WebMethod
    public void echoHolderString(
        @WebParam(name = "echoHolderString", header = true, mode = WebParam.Mode.INOUT, partName = "body")
        Holder<String> body) {
        body.value="Hello "+body.value;
    }


    
    // Generic Tests
    @WebMethod
    public GenericValue<String> echoGenericString(GenericValue<String> param) {
        if (param == null)
            return null;
        String tmp = param.value;
        return new GenericValue<String>(tmp+"&john");
    }

    @WebMethod
    public GenericValue<Integer> echoGenericInteger(GenericValue<Integer> value) {
  
        return value;
    }
    
    @WebMethod
    public <T> T echoGenericObject(T obj) {
        return obj;
    }
    
    
    // Result headers
    @WebMethod
    @WebResult(name="intHeaderResult", header=true, partName="intHeaderResultPart")
    public int echoIntHeaderResult(int in) {
       return in*2;
    }

//    public int test(int a,
//                     @WebParam(mode=WebParam.Mode.OUT)Holder<String> b) {return 0;}

    @WebMethod
    public String echoFoo(String foo) {
      return foo;
    }

    @WebMethod
    @WebResult
    public long echoLong(long longParam) {
        return longParam;
    }

	
    @WebMethod(operationName="echoBar", action="urn:echoBar")
    @WebResult(name="echoBar")
    public Bar echoBar(@WebParam(name="barParam", partName="barParamPart", mode=WebParam.Mode.IN)Bar param) throws Exception1 {
        return param;
    }

    @WebMethod(operationName="echoBar2", action="urn:echoBar")
    @WebResult(name="echoBar")
    public Bar echoBar2(@WebParam(name="barParam2", mode=WebParam.Mode.IN)Bar param) throws Exception1 {
        return param;
    }


    @WebMethod
//    @WebResult(name="echoStringResult")
    public String echoString(@WebParam(name="str") String str) throws Exception1, WSDLBarException, Fault1, Fault2 {
        if (str.equals("Exception1")) 
            throw new Exception1("my exception1");
        if (str.equals("Fault1")) {
            FooException fooException = new FooException();
            fooException.setVarString("foo");
            fooException.setVarInt(33);
            fooException.setVarFloat(44F);
            throw new Fault1("fault1", fooException);
        }
        if (str.equals("WSDLBarException")) 
            throw new WSDLBarException("my barException", new Bar(33));
        if (str.equals("Fault2"))
            throw new Fault2("my fault2", 33);
        return str;
    }


    @WebMethod
//    @WebResult(name="echoStringArrayResult", targetNamespace="mynamespace2")
    public String[] echoStringArray(@WebParam(name="strArray", targetNamespace="mynamespace2") String[] str) {
        return str;
    }
    
    @WebMethod
//    @WebResult(name="echoResult")
    public Bar[] echoBarArray(@WebParam(name="bar")Bar[] bar) {
        return bar;
    }
//    @WebMethod(operationName="echoBarAndBar", action="urn:echoBarAndBar")
//    public Bar[] echoTwoBar(Bar bar, Bar bar2) {
//        return new Bar[] { bar, bar2 };
//    }

    volatile boolean onewayCalled = false;
    @WebMethod
    @Oneway
    public void oneway(String bogus) {
        onewayCalled = true;
        System.out.println("oneway called ----------------------------------");
    }

    @WebMethod
    public boolean verifyOneway(int bogus) {
        return onewayCalled;
    }

    @WebMethod
    public void inOutString(@WebParam(name="inOutStringStr", mode=WebParam.Mode.INOUT)Holder<String> str) {
        log("-----------str: "+str);
        log("-----------str.value: "+str.value);
//	  String tmp = str.value;
        if (str.value != null)
            str.value += str.value;
//	  return tmp;
    }

    @WebMethod
    public void inOutLong(@WebParam(name="InOutLong", mode=WebParam.Mode.INOUT)Holder<Long> lng) {
        log("-----------lng: "+lng);
        log("-----------lng.value: "+lng.value);
        lng.value = lng.value * 2l;
    }
 


//    @WebResult(name="outLong")
//    @WebMethod
//    public void outLong(@WebParam(name="outLong", mode=WebParam.Mode.OUT)Holder<Long> lng) {
//        log("-----------lng: "+lng);
//        log("-----------lng.value: "+lng.value);
//     
//        lng.value = 345L;
//    }


    // Headers, modes and holders
    @WebResult(name="inHeaderResponse", partName="inHeaderResponsePart")
    @WebMethod
    public Long echoInHeader(@WebParam(name="inHeader", partName="inHeaderPart")Integer age, @WebParam(name="num", header=true, partName="numPart", targetNamespace="foo/bar")Long num) {
        log("-----------num: "+num);
        return num+age;
    }

    @WebResult(name="in2HeaderResponse")
    @WebMethod
    public Long echoIn2Header(@WebParam(name="in2Header")Integer age, @WebParam(name="num", header=true, targetNamespace="foo/bar")Long num,
                              @WebParam(name="name", header=true)String name) {
        log("-----------num: "+num);
        log("-----------name: "+name);
        return num+age;
    }


//    @WebMethod
//    @WebResult(name="inOutHeaderResponse")
//    public Long echoInOutHeader(@WebParam(name="inOutHeader")Integer age, @WebParam(name="num", mode=WebParam.Mode.INOUT, header=true, targetNamespace="foo/bar")LongWrapperHolder num) {
//        num.value = num.value*2;
//        return num.value+age;
//    }

    @WebMethod
    @WebResult(name="inOutHeaderResponse")
    public Long echoInOutHeader(@WebParam(name="inOutHeader")Integer age, @WebParam(name="num", mode=WebParam.Mode.INOUT, header=true, targetNamespace="foo/bar")Holder<Long> num) {
        num.value = num.value*2;
        return num.value+age;
    }

    
//    @WebMethod
//    @WebResult(name="outHeaderResponse")
//    public Long echoOutHeader(@WebParam(name="outHeader")Integer age, @WebParam(name="num", mode=WebParam.Mode.OUT, header=true, targetNamespace="foo/bar")LongWrapperHolder num) {
//        log("-----------age: "+age);
//        num.value = new Long(age);;
//        log("-----------num.value: "+num.value);
//        log("-----------num.value+age: "+(num.value+age));
//        return num.value+age;
//    }
    
    @WebMethod
    @WebResult(name="outHeaderResponse")
    public Long echoOutHeader(@WebParam(name="outHeader")Integer age, @WebParam(name="num", mode=WebParam.Mode.OUT, header=true, targetNamespace="foo/bar")Holder<Long> num) {
        log("-----------age: "+age);
        num.value = new Long(age);;
        log("-----------num.value: "+num.value);
        log("-----------num.value+age: "+(num.value+age));
        return num.value+age;
    }


    @WebMethod
    @WebResult(name="out2HeaderResponse")
    public Long echoOut2Header(@WebParam(name="out2Header")Integer age, @WebParam(name="num", mode=WebParam.Mode.OUT, header=true, targetNamespace="foo/bar")Holder<Long> num,
                        @WebParam(name="name", mode=WebParam.Mode.OUT, header=true)Holder<String> name) {
        log("-----------age: "+age);
        num.value = new Long(age);
        name.value="Fred";
        log("-----------num.value: "+num.value);
        log("-----------num.value+age: "+(num.value+age));
        return num.value+age;
    }

    @WebMethod
//    @WebResult(name="overLoadResponse")
    public String overloadedOperation(@WebParam(name="overLoad")String param) throws java.rmi.RemoteException {
        return param;
    }

    @WebMethod(operationName="overloadedOperation2")
    @WebResult(name="overLoad2Response")
    public long overloadedOperation(@WebParam(name="overLoad2")long param) throws java.rmi.RemoteException {
        return param;
    }

//    @WebMethod
    void log(String msg) {
        if (log)
            System.out.println(msg);
    }
}
