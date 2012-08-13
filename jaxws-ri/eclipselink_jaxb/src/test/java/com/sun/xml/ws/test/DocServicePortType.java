
package com.sun.xml.ws.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Action;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "DocServicePortType", targetNamespace = "http://performance.bea.com")
public interface DocServicePortType {

    /**
     * 
     * @param shortMessage
     * @param floatMessage
     */
    @WebMethod
    @RequestWrapper(localName = "echoBaseStruct", targetNamespace = "http://performance.bea.com", className = "com.bea.performance.BaseStruct")
    @ResponseWrapper(localName = "BaseStructOutput", targetNamespace = "http://weblogic/performance/benchmarks/jwswsee/doc", className = "com.bea.performance.BaseStruct")
    @Action(input = "http://performance.bea.com/DocServicePortType/echoBaseStructRequest", output = "http://performance.bea.com/DocServicePortType/echoBaseStructResponse")
    public void echoBaseStruct(
        @WebParam(name = "floatMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Float> floatMessage,
        @WebParam(name = "shortMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Short> shortMessage);

    /**
     * 
     * @param shortMessage
     * @param intMessage
     * @param anotherIntMessage
     * @param stringMessage
     * @param floatMessage
     */
    @WebMethod
    @RequestWrapper(localName = "echoExtendedStruct", targetNamespace = "http://performance.bea.com", className = "com.bea.performance.ExtendedStruct")
    @ResponseWrapper(localName = "ExtendedStructOutput", targetNamespace = "http://weblogic/performance/benchmarks/jwswsee/doc", className = "com.bea.performance.ExtendedStruct")
    @Action(input = "http://performance.bea.com/DocServicePortType/echoExtendedStructRequest", output = "http://performance.bea.com/DocServicePortType/echoExtendedStructResponse")
    public void echoExtendedStruct(
        @WebParam(name = "floatMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Float> floatMessage,
        @WebParam(name = "shortMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Short> shortMessage,
        @WebParam(name = "anotherIntMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Integer> anotherIntMessage,
        @WebParam(name = "intMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Integer> intMessage,
        @WebParam(name = "stringMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<String> stringMessage);

  
    /**
     * 
     * @param shortMessage
     * @param booleanMessage
     * @param intMessage
     * @param anotherIntMessage
     * @param stringMessage
     * @param floatMessage
     */
    @WebMethod
    @RequestWrapper(localName = "modifyMoreExtendedStruct", targetNamespace = "http://performance.bea.com", className = "com.bea.performance.MoreExtendedStruct")
    @ResponseWrapper(localName = "MoreExtendedStructOutput", targetNamespace = "http://weblogic/performance/benchmarks/jwswsee/doc", className = "com.bea.performance.MoreExtendedStruct")
    @Action(input = "http://performance.bea.com/DocServicePortType/modifyMoreExtendedStructRequest", output = "http://performance.bea.com/DocServicePortType/modifyMoreExtendedStructResponse")
    public void modifyMoreExtendedStruct(
        @WebParam(name = "floatMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Float> floatMessage,
        @WebParam(name = "shortMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Short> shortMessage,
        @WebParam(name = "anotherIntMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Integer> anotherIntMessage,
        @WebParam(name = "intMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Integer> intMessage,
        @WebParam(name = "stringMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<String> stringMessage,
        @WebParam(name = "booleanMessage", targetNamespace = "", mode = WebParam.Mode.INOUT)
        Holder<Boolean> booleanMessage);
}
