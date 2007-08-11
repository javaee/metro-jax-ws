package fromjava.nosei_rpclit_apt.server;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;
import java.rmi.RemoteException;


@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class EchoImpl {


    // Enum tests
    @WebMethod
    public Book echoBook(Book book) {
        return book;
    }
    public enum Status {RED, YELLOW, GREEN}
    @WebMethod
    public Status echoStatus(Status status) {
        return status;
    }


    // Generic Tests
    @WebMethod
    public GenericValue<String> echoGenericString(GenericValue<String> param) {
        if (param.value == null)
            return param;
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


//    @WebMethod
//    public List<Bar> echoBarList(List<Bar> list) {
//        return list;
//    }

//    @WebMethod
//    public <T> T echoTList(List<T> list) {
//        if (list.size() == 0)
//            return null;
//        return list.iterator().next();
//    }

//    @WebMethod
//    public List<? extends Bar> echoWildcardBar(List<? extends Bar> list) {
//        return list;
//    }


   // Result headers
    @WebMethod
    @WebResult(name="intHeaderResult", partName="intHeaderResultPart", header=true, targetNamespace="foo/bar")
    public int echoIntHeaderResult(@WebParam(name="fred", partName="myIn")int in) {
       return in*2;
    }


    //standard tests
    @WebMethod(operationName="echoBar", action="urn:echoBar")
//    public Bar echoBar(@WebParam(name="bar", mode=WebParam.Mode.IN)Bar param) {
    public Bar echoBar(Bar param) {
        return param;
    }

    @WebMethod
    public Bar.InnerBar echoInnerBar(Bar.InnerBar param) throws Exception1 {
        Bar.InnerBar innerBar = new Bar.InnerBar();
        innerBar.setName(param.getName()+param.getName());
        return innerBar;
    }

    @WebMethod
    @WebResult
    public Integer echoInteger(Integer param) {
        return param;
    }

    @WebMethod
    public String echoString(@WebParam(name="str") String str) throws Exception1, WSDLBarException, Fault1, Fault2 {
		if (str == null)
			return str;
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
    public String[] echoStringArray(@WebParam(name="str")String[] str) {
        return str;
    }

    @WebMethod
    @WebResult(name="echoResult")
    public Bar[] echoBarArray(@WebParam(name="bar")Bar[] bar) {
        return bar;
    }

    @WebMethod(operationName="echoBarAndBar", action="urn:echoBarAndBar")
    public Bar[] echoTwoBar(@WebParam(name="bar")Bar bar, @WebParam(name="bar2")Bar bar2) {
        return new Bar[] { bar, bar2 };
    }

    boolean onewayCalled = false;
    @WebMethod
    @Oneway
    public void oneway() {
        onewayCalled = true;
    }

    @WebMethod
    public boolean verifyOneway() {
        return onewayCalled;
    }

    @WebMethod
    public int echoInt(@WebParam(name="x")int x) {
        return x;
    }

    // Holders and modes
    @WebMethod
    public String outString(@WebParam(name="tmp")String tmp,
                            @WebParam(name="str", mode=WebParam.Mode.OUT)Holder<String> str,
                            @WebParam(name="age")int age) {
        str.value = tmp+age;
	  return tmp;
    }


    @WebMethod
    public String inOutString(@WebParam(name="tmp")String tmp,
                              @WebParam(name="str", mode=WebParam.Mode.INOUT)Holder<String> str,
                              @WebParam(name="age")int age) {
        str.value += str.value;
	  return tmp;
    }


    @WebMethod
    public int outLong(@WebParam(name="age")int age,
                       @WebParam(name="lng", mode=WebParam.Mode.OUT)Holder<Long> lng,
                       @WebParam(name="bogus")String bogus) {
        lng.value = 345L;
	  return age;
    }

    @WebMethod
    public int inOutLong(@WebParam(name="age")int age,
                         @WebParam(name="lng", mode=WebParam.Mode.INOUT)Holder<Long> lng,
                         @WebParam(name="bogus")String bogus) {
        lng.value = 2*lng.value;
	  return age;
    }

    // Headers, modes and holders
    @WebMethod
//    public Long echoInHeader(@WebParam(name="age")int age,
//                             @WebParam(name="num", header=true, targetNamespace="foo/bar")Long num,
//                             @WebParam(name="str")String str) {
    public Long echoInHeader(int age,
                             @WebParam(name="num2", header=true, targetNamespace="foo/bar")Long num,
                             String str) {
        return num;
    }

//    @WebMethod
//    public String echoInOutHeader(@WebParam(name="age")int age,
//                                  @WebParam(name="num", mode=WebParam.Mode.INOUT, header=true)LongHolder num,
//                                  @WebParam(name="str")String str) {
//        num.value = num.value*2;
//        return str+num.value;
//    }

    @WebMethod
    public String echoInOutHeader(@WebParam(name="age")int age,
                                  @WebParam(name="num", mode=WebParam.Mode.INOUT, partName="numPart", header=true)Holder<Long> num,
                                  @WebParam(name="str")String str) {
        num.value = num.value*2;
        return str+num.value;
    }

//    @WebMethod
//    public String echoOutHeader(@WebParam(name="age")int age,
//                                @WebParam(name="num", mode=WebParam.Mode.OUT, header=true)LongHolder num,
//                                @WebParam(name="str")String str) {
//        num.value = new Long(age);;
//        return str+num.value;
//    }

    @WebMethod
    public String echoOutHeader(@WebParam(name="age")int age,
                                @WebParam(name="num", mode=WebParam.Mode.OUT, header=true)Holder<Long> num,
                                @WebParam(name="str")String str) {
        num.value = new Long(age);;
        return str+num.value;
    }


    // TCK test cases
    @WebMethod
    public String helloWorld() throws RemoteException{
        return "hello world";
    }

    @WebMethod
    public void oneWayOperation() throws RemoteException {
    }

    @WebMethod
    public String overloadedOperation(@WebParam(name="param")String param) throws RemoteException {
        return param;
    }

    @WebMethod(operationName="overloadedOperation2")
    public String overloadedOperation(@WebParam(name="param")String param,
                                      @WebParam(name="param2")String param2) throws RemoteException {
        return param + param2;
    }

    @WebMethod
    public String [] arrayOperation() throws RemoteException{
        return new String []{"one", "two", "three"};
    }

//    public SimpleBean getBean() throws RemoteException{
//	SimpleBean sb = new SimpleBean();
//	sb.setMyInt(5);
//	sb.setMyString("A String");
//	return sb;
//    }

    @WebMethod
    public String arrayOperationFromClient(@WebParam(name="array")String [] array) throws RemoteException{
        return "success";
    }

    @WebMethod
    public String holderOperation(@WebParam(name="holder1", mode=WebParam.Mode.INOUT)Holder<String> holder1,
                                  @WebParam(name="holder2", mode=WebParam.Mode.INOUT)Holder<String> holder2) throws RemoteException{
        holder1.value += "1";
        holder2.value += "2";
        return "success";
    }

}
